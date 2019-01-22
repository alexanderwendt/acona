package at.tuwien.ict.acona.mq.cell.communication;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding copyright
 * ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions and limitations under the License.
 */

import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import at.tuwien.ict.acona.mq.cell.cellfunction.CellFunction;
import at.tuwien.ict.acona.mq.cell.storage.DataStorage;
import at.tuwien.ict.acona.mq.datastructures.DPBuilder;
import at.tuwien.ict.acona.mq.datastructures.Datapoint;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.RequestError;
import at.tuwien.ict.acona.mq.datastructures.Response;
import at.tuwien.ict.acona.mq.utils.JsonUtils;

/**
 * A Mqtt basic requestor
 *
 */
public class MqttCommunicatorImpl implements MqttCommunicator {

	private static final Logger log = LoggerFactory.getLogger(MqttCommunicatorImpl.class);

	private Gson gson = new Gson();
	private final DPBuilder dpBuilder = new DPBuilder();
	private final JsonUtils util = new JsonUtils();
	private int qos=1;

	private Map<String, Response> incomingRequestMessages = new ConcurrentHashMap<>();
	//private Map<String, JsonElement> incomingTrackedMessages = new ConcurrentHashMap<>();
	//As only one request can be made at a time, only one message must be stored
	//private Response incomingRequestMessage = null;
	//private Map<String, SynchronousQueue<JsonElement>> incomingTrackedMessagesQueue = new ConcurrentHashMap<>();

	// private Map<String, Datapoint> incomingReadMessages = new HashMap<>();

	// Service Classes
	// private final Map<String, Function<Request, Response>> handlerMap = new HashMap<>();

	// Random Function Methods to process the incoming input
	// final Map<String, Consumer<JsonElement>> customHandlerMap = new HashMap<>();

	// Semaphore used for synchronizing b/w threads. Each cell function runs in a thread. Only one block per thread is allowed
	private final Semaphore latch = new Semaphore(0);
	private MqttClient mqttClient;

	private final DataStorage storage;
	private CellFunction cellfunction;
	private String cellName;

	// === Parameter variables ===//
	private String host = "tcp://127.0.0.1:1883";
	private String username = "acona";
	private String password = "acona";
	// private String functionName = "FunctionRequester";
	// private String agentName = "agent1";
	// private final String functionReplyTopic = agentName + "/" + functionName + "/" + "replyto";

	private int defaultTimeout = 10000;

	// final String requestTopic = "T/GettingStarted/request";

	// Subscribed mqtt addresses
	private String rootAddress = ""; // Root address <[agentname]>/functionname
	private String subscribedReplyAddress = "";
	private String subscribedServiceAddressPrefix = "";
	private String subscribedCommandAddress = "";

	// Published mqtt addresses
	private String publishedStateAddress = "";

	public MqttCommunicatorImpl(DataStorage storage) {
		this.storage = storage;
	}

	@Override
	public void init(String host, String userName, String password, CellFunction cellFunction) throws Exception {
		log.debug("BasicRequestor Tester initializing...");

		// , Map<String, Function<Request, Response>> functions

		try {
			// Set the input parameters
			this.host = host;
			this.username = userName;
			this.password = password;
			this.cellfunction = cellFunction;
			this.cellName = this.cellfunction.getCellName();

			this.rootAddress = this.dpBuilder.generateCellTopic(cellName);
			if (this.cellfunction.getFunctionName().isEmpty() == false) {
				this.rootAddress += "/" + cellfunction.getFunctionName();
			}

			this.subscribedReplyAddress = this.rootAddress + "/replyto";
			this.subscribedServiceAddressPrefix = this.rootAddress;
			this.subscribedCommandAddress = this.rootAddress + "/command";
			this.publishedStateAddress = this.rootAddress + "/state";

			// Create an Mqtt client
			mqttClient = new MqttClient(this.host, this.cellName + "_" + this.cellfunction.getFunctionName(), new MemoryPersistence());	//Memory persistance to keep all messages in the memory and not HDD
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			connOpts.setUserName(this.username);
			connOpts.setPassword(this.password.toCharArray());
			connOpts.setMaxInflight(10000);   //Set max handled messages at the same time. Set 2000.

			// Connect the client
			log.debug("Connecting to MQTT messaging at " + this.host);
			mqttClient.connect(connOpts);
			log.debug("Connected");

			// Callback - Anonymous inner-class for receiving the Reply-To topic from the Solace broker
			mqttClient.setCallback(new MqttCallback() {

				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					//Check if error in the topic
					// JsonElement message

					// If the topic is recived at the reply-to address, then there is a blocking function waiting for it.
					// Check if the message is a JsonObject
					String payloadString = new String(message.getPayload());
					log.debug("{}> Recieved message={} from topic={}", cellName, payloadString, topic);
					
					if (topic==null) {
						throw new Exception("Received a message without any topic. Topic=NULL. Message= " + payloadString);
					}
					

					// log.debug("1");
					JsonElement jsonMessage;
					if (util.isJsonObject(payloadString)) {
						// log.debug("1");
						jsonMessage = gson.fromJson(payloadString, JsonObject.class);
						// log.debug("2");
					} else {
						// log.debug("3");
						jsonMessage = gson.toJsonTree(payloadString);
						// log.debug("4");
					}

					log.debug("Got Json Message={}", jsonMessage);

					// Make a check if String is Json

					// If this is a reply to a request done by this function, then put it in the table
//					if (topic != null && incomingTrackedMessages.containsKey(topic)) {
//						// This message origins from the read method. The message shall be
//						incomingTrackedMessages.put(topic, jsonMessage);
//						log.debug("{}>Received tracked message on topic {}, message={}", cellName, topic, jsonMessage);
//
//						// latch.release(); // unblock main thread
//
//						
//					} else 
					// Message is an incoming response to an executed request. Provide answer to the calling function
					if (topic.equals(subscribedReplyAddress) && jsonMessage instanceof JsonObject && Response.isResponse((JsonObject) jsonMessage)) {
						// log.debug("6");
						Response response = Response.newResponse(payloadString);
						// log.debug("7");
						log.debug("Received Reply-to topic for the MQTT client:" + "Reply-To: " + jsonMessage);

						incomingRequestMessages.put(response.getCorrelationid(), response);
						// log.debug("8");
						// log.debug("Sync1");
						latch.release(); // unblock main thread
						// log.debug("Sync2");

						// log.debug("9");
					// If this is a received RPC call request. Start service for the service
					} else if (topic.startsWith(subscribedServiceAddressPrefix) && jsonMessage instanceof JsonObject && Request.isRequest((JsonObject) jsonMessage)) {
						// Run service
						Request req = Request.newRequest(payloadString);
						// JsonElement responseMessage = req.getjsonMessage.get("message"); // The message can be any json structure
						// Response response = handlerMap.get(topic).apply(req);

						// this.performaction
						// FIXME
						Response response = cellFunction.performOperation(topic, req);

						//If not null, send result to caller
						if (response!=null) {
							sendResponseToOpenRequest(response);
						} else {
							log.debug("Do not return anything. Request {} was set to openRequest", req);
						}


					// Message is a command for the function
					} else if (topic.equals(subscribedCommandAddress)) {
						// this.setcommand(command)
						// TODO: Add method;
						log.info("Commands shall not get here {}, {}", topic, jsonMessage);

					// Else, any other message that is subscribed
					} else {
						log.debug("Update subscribed data: {}", topic);
						if (topic.equals(subscribedReplyAddress)==true) {
							log.error("Topic: {}. Erroneous response from method {}", topic, jsonMessage);
						}
						cellFunction.updateSubscribedData(topic, jsonMessage);
					}
				}

				@Override
				public void connectionLost(Throwable cause) {
					log.info("Connection to MQTT messaging lost!", cause);
					// latch.release();
				}

				@Override
				public void deliveryComplete(IMqttDeliveryToken token) {

				}
			});

			// Subscribe client to the special Solace topic for requesting a unique
			// Reply-to destination for the MQTT client
			log.debug("Requesting Reply-To topic for this function. Address={}.", subscribedReplyAddress);
			// Subscribe the reply-to address
			mqttClient.subscribe(this.subscribedReplyAddress, 0);
			// Subscribe the command address
			mqttClient.subscribe(this.subscribedCommandAddress, 0);

			log.info("{}>initialized", this.cellfunction.getFunctionName());

		} catch (MqttException me) {
			log.debug("reason " + me.getReasonCode());
			log.debug("msg " + me.getMessage());
			log.debug("loc " + me.getLocalizedMessage());
			log.debug("cause " + me.getCause());
			log.debug("excep " + me);
			throw new Exception(me.getMessage());
		}

	}

	@Override
	public void setDefaultTimeout(int timeout) {
		this.defaultTimeout = timeout;

	}

	@Override
	public int getDefaultTimeout() {
		return this.defaultTimeout;
	}

	private Response executeRequest(String topic, Request request, boolean isSychronousCall, int timeout) throws Exception {
		// Topic the client will use to send request messages
		Response result = null;

		try {
			request.setReplyTo(this.subscribedReplyAddress);
			// request.setParameter("message", message);

			String correlationID = request.getCorrelationId(); // UUID.randomUUID().toString();
			String reqPayload = request.toJson().toString();

			// Create a request message and set the request payload
			MqttMessage reqMessage = new MqttMessage(reqPayload.getBytes());
			reqMessage.setQos(qos);
			
			String dpPublish = (new DPBuilder()).newDatapoint(topic).getCompleteAddressAsTopic(this.cellName);

			log.debug("{}>Sending request to: " + dpPublish, this.cellName);

			// Publish the request message
			mqttClient.publish(dpPublish, reqMessage);

			if (isSychronousCall == true) {
				// Wait for till we have received a response
				try {
					log.debug("{}> Message sent to {}, wait for answer for {}ms, correlationoid={}", cellName, dpPublish, timeout, correlationID);
					latch.tryAcquire(timeout, TimeUnit.MILLISECONDS); // block here until message received
				} catch (InterruptedException e) {
					log.error("Interruption error for request " + request, e);
					// latch.release();
				}

				// Get the message answer from the map
				result = this.incomingRequestMessages.getOrDefault(correlationID, new Response(request, new RequestError("Timeout error")));
				if (result.hasError() == true) {
					log.error("Timeout after {}ms. No response from request on correlationID {}, topic {}, request={}", timeout, correlationID, dpPublish, request);
					throw new Exception("Timeout after " + timeout + "ms. No response from request on topic " + dpPublish);
				}
				this.incomingRequestMessages.remove(correlationID);
				log.debug("Got message={}", result);
			}

		} catch (Exception e) {
			log.error("Cannot send request", e);
			throw new Exception(e.getMessage());
		}

		return result;
	}

	@Override
	public Response execute(String agentNameAndService, Request methodParameters, int timeout) throws Exception {
		return this.executeRequest(agentNameAndService, methodParameters, true, timeout);
	}

	@Override
	public Response execute(String agentNameAndService, Request methodParameters) throws Exception {
		return this.executeRequest(agentNameAndService, methodParameters, true, this.defaultTimeout);
	}

	@Override
	public void executeAsynchronous(String agentAndServiceName, Request methodParameters) throws Exception {
		this.executeRequest(agentAndServiceName, methodParameters, false, this.getDefaultTimeout());

	}
	
	@Override
	public void sendResponseToOpenRequest(Response response) throws Exception {
		try {
			// send back
			String responseString = response.toJson().toString();

			// Create a request message and set the request payload
			MqttMessage responseMessage = new MqttMessage(responseString.getBytes());
			responseMessage.setQos(qos);

			MqttTopic mqttTopic = mqttClient.getTopic(response.getReplyTo());
			try {
				mqttTopic.publish(responseMessage);
			} catch (MqttPersistenceException e) {
				log.error("MQTT Persistence error. Message cannot be published: {}", response, e);
				throw new Exception(e.getMessage());
			} catch (MqttException e) {
				log.error("MQTT other error. Message cannot be published {}", response, e);
				throw new Exception(e.getMessage());
			}
			
			log.debug("Returning response {}", response);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

//	@Override
//	public Datapoint executeRequestBlockForResult(String serviceAddress, Request req, String resultAddress, JsonElement expectedResult) throws Exception {
//		Datapoint result = this.dpBuilder.newNullDatapoint();
//
//		try {
//			// Subscribe the result datapoint
//			this.subscribeDatapoint(resultAddress);
//
//			// Add incoming value
//			SynchronousQueue<JsonElement> block = new SynchronousQueue<JsonElement>();
//
//			this.incomingTrackedMessagesQueue.put(resultAddress, block);
//
//			// Execute the request without waiting for an answer
//			this.executeAsynchronous(serviceAddress, req);
//
//			// Wait for till we have received a response
//			boolean repsponseIntime = false;
//
//			JsonElement resultJson = null;
//
//			Datapoint tempResult = null;
//			// Execute at least once
//			do {
//				try {
//					log.debug("Message sent, wait for answer for {}ms, correlationoid={}", this.getDefaultTimeout(), resultAddress);
//					// repsponseIntime = latch.tryAcquire(this.getDefaultTimeout(), TimeUnit.MILLISECONDS); // block here until message received
//					resultJson = block.poll(getDefaultTimeout(), TimeUnit.MILLISECONDS);
//				} catch (InterruptedException e) {
//					log.error("Interruption error for tracked value at " + resultAddress, e);
//					// latch.release();
//				}
//
//				// Get the message answer from the map
//				// resultJson = this.incomingTrackedMessages.get(resultAddress);
//				log.info("Result={}", resultJson);
//				tempResult = this.dpBuilder.toDatapoint(resultJson.getAsJsonObject());
//
//			} while (repsponseIntime == true && expectedResult != null && expectedResult.equals(new JsonPrimitive("")) == false && tempResult.getValue().toString().equals(expectedResult.toString()) == false);
//
//			if (expectedResult != null && expectedResult.equals(new JsonPrimitive("")) == false) {
//				result = this.dpBuilder.toDatapoint(resultJson.getAsJsonObject());
//			}
//
//			// If responsetime is true, there was no timeout
//			if (repsponseIntime == false) {
//				log.error("Timeout after {}ms. No response from tracked value on topic {}, request={}", this.getDefaultTimeout(), resultAddress);
//				throw new Exception("Timeout after " + this.getDefaultTimeout() + "ms. No response from request on topic " + resultAddress);
//			}
//			log.debug("Got message={}", result);
//
//		} catch (Exception e) {
//			throw e;
//		} finally {
//			this.incomingRequestMessages.remove(resultAddress);
//		}
//
//		return result;
//	}

	@Override
	public void shutDown() {
		// Disconnect the client
		try {
			//Release locks
			this.latch.release();
			
			// disconnect
			mqttClient.disconnect();
			//mqttClient.disconnect();

			// Close the client
			mqttClient.close();
		} catch (MqttException e) {
			log.error("Cannot disconnect from client={}", mqttClient.getServerURI(), e);
		}

		log.debug("Exiting function={}", this.cellName + "/" + this.cellfunction);
	}

	@Override
	public List<Datapoint> readWildcard(String address) throws Exception {
		final String service = "/dataaccess/read";

		List<Datapoint> result = new ArrayList<>();

		try {
			// Check if the datapoint is uses the local agent, else call a service in another agent
			Datapoint dp = this.dpBuilder.newDatapoint(address);
			if (dp.getAgent(this.cellName).equals(this.cellName) == true) {
				// If the agent name in the address is this agent, then use the local access
				result = storage.read(dp.getAddress());
				log.debug("Read datapoint={} locally", result);
			} else {
				// If the agent name differs from this agent, then use service execution
				// Get the read service address in the other agent
				// <agent>/dataaccess/read
				String remoteAgentName = dp.getAgent(this.cellName);
				String serviceAddress = "<" + remoteAgentName + ">" + service;

				// Create the request
				Request req = new Request();
				req.setParameter("param", address);

				Response resp = this.execute(serviceAddress, req);
				result = resp.getResult(new TypeToken<List<Datapoint>>() {});

				log.debug("Read datapoint={} remote", result);
			}

			if (result == null || result.isEmpty()) {
				result.add(this.dpBuilder.newDatapoint(address).setAgentIfAbsent(cellName));
			}

		} catch (Exception e) {
			log.error("Cannot read from storage", e);
			throw new Exception(e.getMessage());
		}

//		// Read from topic
//		try {
//			
//			storage.read(topic);
//			// Add topic that shall be read
//			this.incomingReadMessages.put(topic, null);
//			// Subscribe the topic
//			IMqttToken token = this.mqttClient.subscribeWithResponse(topic);
//			List<String> topics = Arrays.asList(token.getTopics());
//			if (topics.isEmpty() == false) {
//				log.debug("Subscribed topics={}", topics.size());
//
//				// Wait for subscription to arrive
//				boolean timeout = this.latch.tryAcquire(defaultReadTimeout, TimeUnit.MILLISECONDS);
//				// Get value per read listener
//				if (timeout == false) {
//					log.warn("Timeout, i.e. no persistent value exists on topic {}", topic);
//				}
//
//			} else {
//				log.warn("There exist no topics to subscribe for topic={}", topic);
//			}
//
//			// Unsubscribe the topic
//			this.mqttClient.unsubscribe(topic);
//			// Get the content of the topic
//			result.add(this.incomingReadMessages.get(topic));
//
//			// Remove the message from the temp map
//			this.incomingReadMessages.remove(topic);
//
//			if (result.isEmpty()) {
//				result.add(dp.newDatapoint(topic));
//			}
//
//			log.debug("Read datapoint={}", result);
//		} catch (MqttException e) {
//			log.error("MQTT error", e);
//			throw new Exception(e.getMessage());
//		} catch (InterruptedException e) {
//			log.error("MQTT error", e);
//			log.error("Timeout for the read method after waiting {}ms", defaultTimeout);
//			throw new Exception(e.getMessage());
//		}

		return result;
	}

	@Override
	public Datapoint read(String topic) throws Exception {
		return this.readWildcard(topic).get(0);
	}

	@Override
	public void write(Datapoint datapoint) throws Exception {
		final String service = "/dataaccess/write";

		if (datapoint.getAgent(this.cellName).equals(this.cellName) == true) {
			// If the agent name in the address is this agent, then use the local access
			storage.write(datapoint);
			log.debug("{}>Written datapoint={} locally", this.cellName, datapoint);
		} else {
			// If the agent name differs from this agent, then use service execution
			// Get the read service address in the other agent
			// <agent>/dataaccess/read
			String remoteAgentName = datapoint.getAgent(this.cellName);
			String serviceAddress = "<" + remoteAgentName + ">" + service;

			// Create the request
			Request req = new Request();
			req.setParameter("param", datapoint.toJsonObject());

			Response resp = this.execute(serviceAddress, req);
			// resp.getResult(new TypeToken<List<Datapoint>>() {});

			log.debug("Written datapoint={} remote. Result={}", datapoint, resp.getResult());
		}
	}
	
	@Override
	public void write(String address, JsonElement value) throws Exception {
		this.write(this.dpBuilder.newDatapoint(address).setValue(value));
		
	}

	@Override
	public void write(List<Datapoint> datapoints) throws Exception {
		for (Datapoint dp : datapoints) {
			this.write(dp);
		}

	}

	@Override
	public Datapoint subscribeDatapoint(String address) throws Exception {
		// final String service = "/dataaccess/subscribe";

		Datapoint dp = this.dpBuilder.newDatapoint(address);
		// Subscribe in the database
//		if (dp.getAgent(this.agentName).equals(this.agentName) == true) {
//			// If the agent name in the address is this agent, then use the local access
//			storage.subscribeDatapoint(dp.getAddress(), this.cellfunction.getFunctionName());
//			dp = storage.readFirst(dp.getAddress());
//		} else {
//			// If the agent name differs from this agent, then use service execution
//			// Get the read service address in the other agent
//			// <agent>/dataaccess/read
//			String remoteAgentName = dp.getAgent(this.agentName);
//			String serviceAddress = dpBuilder.generateCellTopic(remoteAgentName) + service;
//
//			// Create the request
//			Request req = new Request();
//			req.setParameter("param", dp.getAddress());
//
//			Response resp = this.execute(serviceAddress, req);
//
//			// Read the value of the datapoint.
//			dp = this.read(address);
//		}

		// Subscribe a topic in MQTT
		this.mqttClient.subscribe(dp.getCompleteAddressAsTopic(this.cellName));

		log.debug("Subscribed address={}, topic={}", address, dp.getCompleteAddressAsTopic(this.cellName));

		return dp;
	}

	@Override
	public void unsubscribeDatapoint(String address) throws Exception {
		// final String service = "/dataaccess/unsubscribe";

		Datapoint dp = this.dpBuilder.newDatapoint(address);
		// Subscribe in the database
//		if (dp.getAgent(this.agentName).equals(this.agentName) == true) {
//			// If the agent name in the address is this agent, then use the local access
//			storage.unsubscribeDatapoint(address, this.cellfunction.getFunctionName());
//		} else {
//			// If the agent name differs from this agent, then use service execution
//			// Get the read service address in the other agent
//			// <agent>/dataaccess/read
//			String remoteAgentName = dp.getAgent(this.agentName);
//			String serviceAddress = "<" + remoteAgentName + ">" + service;
//
//			// Create the request
//			Request req = new Request();
//			req.setParameter("param", address);
//
//			Response resp = this.execute(serviceAddress, req);
//		}

		// Unsubscribe from MQTT
		this.mqttClient.unsubscribe(dp.getCompleteAddressAsTopic(this.cellName));

		log.debug("Unsubscribed address={}, topic={}", address, dp.getCompleteAddressAsTopic(this.cellName));
	}

	@Override
	public void publishDatapoint(Datapoint dp) throws Exception {
		String topic = "";

		try {
			// Create the agent address from the data storage
			topic = dp.getCompleteAddressAsTopic(this.cellName);
			dp.setAgent(this.cellName);
			MqttTopic top = this.mqttClient.getTopic(topic);
			MqttMessage mqttMessage = new MqttMessage(dp.toJsonObject().toString().getBytes());
			mqttMessage.setQos(qos);
			top.publish(mqttMessage);
			log.debug("{}>Published {} to {}", this.cellName, dp, topic);

		} catch (Exception e) {
			log.error("Cannot publish datapoint {} to topic={}", dp, topic);
		}

	}

	@Override
	public void publishTopic(String topic, JsonElement message) throws Exception {
		this.publishTopic(topic, message, false);

	}

	@Override
	public void publishTopic(String topic, JsonElement message, boolean isPersistent) throws Exception {
		// Publish only the topic, use the local address of the
		MqttMessage mqttMessage = new MqttMessage(message.toString().getBytes());
		mqttMessage.setQos(qos);
		mqttMessage.setRetained(isPersistent);
		try {
			MqttTopic top = this.mqttClient.getTopic(topic);
			top.publish(mqttMessage);

			log.debug("Published message={} to topic= {}", message, topic);
		} catch (MqttPersistenceException e) {
			log.error("Persistent error to publish datapoint={}", dpBuilder);
			throw new Exception(e.getMessage());
		} catch (MqttException e) {
			log.error("MQTT error for datapoint={}", dpBuilder);
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public void subscribeTopic(String topicfilter) throws Exception {
		// Subscribe a topic
		this.mqttClient.subscribe(topicfilter);
		log.debug("Subscribed topic={}", topicfilter);
	}

	@Override
	public void unsubscribeTopic(String topicfilter) throws Exception {
		// Subscribe a topic
		this.mqttClient.unsubscribe(topicfilter);
		log.debug("Unsubscribed topic={}", topicfilter);

	}

	@Override
	public void remove(String address) throws Exception {
		this.storage.remove(address);
	}

}
