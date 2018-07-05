package at.tuwien.ict.acona.mq.cell.communication;

import java.util.HashMap;
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
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.datastructures.util.JsonUtils;
import at.tuwien.ict.acona.mq.datastructures.Datapoint;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.RequestError;
import at.tuwien.ict.acona.mq.datastructures.Response;

/**
 * A Mqtt basic requestor
 *
 */
public class MqttCommunicatorImpl implements MqttCommunicator {

	private static final Logger log = LoggerFactory.getLogger(MqttCommunicatorImpl.class);

	// A unique Reply-To Topic for the client is obtained from Solace
	// private String replyToPayload = "";

	private Gson gson = new Gson();

	private Map<String, Response> incomingRequestMessages = new ConcurrentHashMap<>();
	private Map<String, Datapoint> incomingReadMessages = new HashMap<>();

	// Service Classes
	final Map<String, Function<Request, Response>> handlerMap = new HashMap<>();

	// Semaphore used for synchronizing b/w threads
	private final Semaphore latch = new Semaphore(0);
	// private final SynchronousQueue<Datapoint> queue = new SynchronousQueue<>();
	private MqttClient mqttClient;

	// === Parameter variables ===//
	private String host = "tcp://127.0.0.1:1883";
	private String username = "acona";
	private String password = "acona";
	private String functionName = "FunctionRequester";
	private String agentName = "agent1";
	// private final String functionReplyTopic = agentName + "/" + functionName + "/" + "replyto";

	private int defaultTimeout = 10000;

	final String requestTopic = "T/GettingStarted/request";

	// Subscribed mqtt addresses
	private String rootAddress = "";
	private String subscribedReplyAddress = "";
	private String subscribedServiceAddressPrefix = "";
	private String subscribedCommandAddress = "";

	// Published mqtt addresses
	private String publishedStateAddress = "";

	public MqttCommunicatorImpl() {

	}

	@Override
	public void init(String host, String userName, String password, String agentName, String functionName) throws Exception {
		log.debug("BasicRequestor Tester initializing...");

		try {
			// Set the input parameters
			this.host = host;
			this.username = userName;
			this.password = password;
			this.functionName = functionName;
			this.agentName = agentName;

			this.rootAddress = agentName + "/" + functionName;
			this.subscribedReplyAddress = this.rootAddress + "/replyto";
			this.subscribedServiceAddressPrefix = this.rootAddress;
			this.subscribedCommandAddress = this.rootAddress + "/command";
			this.publishedStateAddress = this.rootAddress + "/state";

			// Create an Mqtt client
			mqttClient = new MqttClient(this.host, this.functionName);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			connOpts.setUserName(this.username);
			connOpts.setPassword(this.password.toCharArray());

			// Connect the client
			log.debug("Connecting to MQTT messaging at " + this.host);
			mqttClient.connect(connOpts);
			log.debug("Connected");

			// Callback - Anonymous inner-class for receiving the Reply-To topic from the Solace broker
			mqttClient.setCallback(new MqttCallback() {

				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					// Propoerties of incoming messages
					// JsonElement message

					// If the topic is recived at the reply-to address, then there is a blocking function waiting for it.
					// Check if the message is a JsonObject
					String payloadString = new String(message.getPayload());
					log.debug("Recieved message={} from topic={}", payloadString, topic);
					JsonUtils util = new JsonUtils();

					JsonElement jsonMessage;
					if (util.isJsonObject(payloadString)) {
						jsonMessage = gson.fromJson(payloadString, JsonObject.class);
					} else {
						jsonMessage = gson.toJsonTree(payloadString);
					}

					// Make a check if String is Json

					// If this is a reply of a request done by this function, then put it in the table
					if (topic != null && incomingReadMessages.containsKey(topic)) {
						// This message origins from the read method. The message shall be
						Datapoint dp = (new Datapoint(topic)).setValue(jsonMessage);
						incomingReadMessages.put(topic, dp);
						log.debug("Received read message={}", dp);

						latch.release(); // unblock main thread

					} else if (topic != null && topic.equals(subscribedReplyAddress) && jsonMessage instanceof JsonObject && Response.isResponse((JsonObject) jsonMessage)) {
						// JsonObject jsonResponse = gson.fromJson(payloadString, JsonObject.class);
						Response response = Response.newResponse(payloadString);
						log.debug("Received Reply-to topic for the MQTT client:" + "Reply-To: " + jsonMessage);

						// JsonObject response = gson.fromJson(jsonMessage, JsonObject.class);
						// Get correlationid
						// String correlationid = response.getCorrenationid();
						// JsonElement responseMessage = response.getResult(); // The message can be any json structure

						// JsonElement error = response.getError();

						incomingRequestMessages.put(response.getCorrenationid(), response);
						latch.release(); // unblock main thread

						// If this is a received RPC call
					} else if (topic != null && topic.startsWith(subscribedServiceAddressPrefix) && jsonMessage instanceof JsonObject && Request.isRequest((JsonObject) jsonMessage)) {
						// Run service
						Request req = Request.newRequest(payloadString);
						// JsonElement responseMessage = req.getjsonMessage.get("message"); // The message can be any json structure
						Response result = handlerMap.get(topic).apply(req);

						// this.performaction
						// TODO: Add method(method name)

						// send back
						Response response = result;

						String responseString = response.toJson().toString();

						// Create a request message and set the request payload
						MqttMessage responseMessage = new MqttMessage(responseString.getBytes());
						responseMessage.setQos(0);

						MqttTopic mqttTopic = mqttClient.getTopic(response.getReplyTo());
						mqttTopic.publish(responseMessage);
						// mqttClient.publish(response.getReplyTo(), responseMessage);
						log.debug("Returning response {}", response);
					} else if (topic != null && topic.equals(subscribedCommandAddress)) {
						// this.setcommand(command)
						// TODO: Add method;
						log.warn("Commands have not been implemented yet");

					} else {
						try {
							// execute the updatefunction, updateValueByID
							// TODO: Addmethod

							// Parse the response payload and convert to a JSONObject
							JsonElement obj = gson.toJsonTree(new String(message.getPayload()));
							JsonObject jsonPayload = obj.getAsJsonObject();

							log.debug("\nReceived a response!" +
									"\n\tCorrel. Id: " + jsonPayload.get("correlationId") +
									"\n\tMessage:    " + jsonPayload.get("message") + "\n");
						} catch (Exception ex) {
							log.debug("Exception parsing response message!");
							ex.printStackTrace();
						}
					}

				}

				@Override
				public void connectionLost(Throwable cause) {
					log.error("Connection to MQTT messaging lost!", cause);
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

			// Wait for till we have received the reply to Topic
//			try {
//				latch.acquire();
//			} catch (InterruptedException e) {
//				log.debug("I was awoken while waiting");
//			}

			// Check if we have a Reply-To topic
//			if (replyToTopic == null || replyToTopic.isEmpty()) {
//				log.debug("Unable to request Reply-To from Solace. Exiting");
//				System.exit(0);
//			}
//
//			// Subscribe client to the Solace provide Reply-To topic with a QoS level of 0
//			log.debug("Subscribing client to Solace provide Reply-To topic");
//			mqttClient.subscribe(replyToTopic, 0);

			// Add function to function table
			// this.handlerMap.put("test", (Request input) -> serviceDoAnything(input));
			// this.handlerMap.get("address").accept();
//			this.handlerMap.keySet().forEach(k -> {
//				try {
//					mqttClient.subscribe(k, 0);
//				} catch (MqttException e) {
//					log.error("Cannot subscribe {}", k, e);
//				}
//			});

			log.info("{}>initialized", this.functionName);

//			try {
//				latch.await(); // block here until message received, and latch will flip
//			} catch (InterruptedException e) {
//				log.debug("I was awoken while waiting");
//			}

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
	public void addRequestHandlerFunction(String topicSuffix, Function<Request, Response> function) throws Exception {
		this.handlerMap.put(this.rootAddress + "/" + topicSuffix, function);
		try {
			mqttClient.subscribe(this.rootAddress + "/" + topicSuffix, 0);
		} catch (MqttException e) {
			log.error("Cannot subscribe input to service function {}", this.rootAddress + "/" + topicSuffix);
			throw new Exception(e.getMessage());
		}
		log.info("Added function to {}", this.rootAddress + "/" + topicSuffix);

	}

	@Override
	public void removeRequestHandlerFunction(String topicSuffix) throws Exception {
		mqttClient.unsubscribe(topicSuffix);
		this.handlerMap.remove(topicSuffix);

		log.debug("Unsubscribed topic={} and removed its function.", topicSuffix);
	}

//	// Service function
//	private JsonElement serviceDoAnything(Request request) {
//		// Parameter List<TopicNames>
//
//		try {
//
//			request.getParameter("test", Datapoint.class);
//
//		} catch (Exception e) {
//
//		}
//
//		return new JsonPrimitive("test");
//
//	}

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
			reqMessage.setQos(0);

			log.debug("Sending request to: " + topic);

			// Publish the request message
			mqttClient.publish(topic, reqMessage);

			if (isSychronousCall == true) {
				// Wait for till we have received a response
				try {
					log.debug("Message sent, wait for answer for {}ms", timeout);
					latch.tryAcquire(timeout, TimeUnit.MILLISECONDS); // block here until message received
				} catch (InterruptedException e) {
					log.error("Timeout or interruption error for request " + request, e);
					// latch.release();
				}

				// Get the message answer from the map
				result = this.incomingRequestMessages.getOrDefault(correlationID, new Response(request, new RequestError("Timeout error")));
				if (result.hasError() == true) {
					log.error("Timeout after {}ms. No response from request on topic {}", timeout, topic);
					throw new Exception("Timeout after " + timeout + "ms. No response from request on topic " + topic);
				}
				this.incomingRequestMessages.remove(correlationID);
				log.debug("Got message={}", result);
			}

		} catch (Exception e) {
			// log.error("Cannot send request", e);
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
	public void shutDown() {
		// Disconnect the client
		try {
			mqttClient.disconnect();
		} catch (MqttException e) {
			log.error("Cannot disconnect from client={}", mqttClient.getServerURI(), e);
		}

		log.debug("Exiting function={}", this.agentName + "/" + this.functionName);
	}

	@Override
	public Datapoint read(String topic) throws Exception {
		Datapoint result = null;

		// Read from topic
		try {
			// this.mqttClient.getTopic(topic).
			// Add topic that shall be read
			this.incomingReadMessages.put(topic, null);
			// Subscribe the topic
			this.mqttClient.subscribe(topic);
			// Wait for subscription to arrive
			boolean timeout = this.latch.tryAcquire(defaultTimeout, TimeUnit.MILLISECONDS);
			if (timeout == false) {
				log.error("Timeout");
			}
			// Unsubscribe the topic
			this.mqttClient.unsubscribe(topic);
			// Get the content of the topic
			result = this.incomingReadMessages.get(topic);
			// Remove the message from the temp map
			this.incomingReadMessages.remove(topic);

			log.debug("Read datapoint={}", result);
		} catch (MqttException e) {
			log.error("MQTT error", e);
			throw new Exception(e.getMessage());
		} catch (InterruptedException e) {
			log.error("MQTT error", e);
			log.error("Timeout for the read method after waiting {}ms", defaultTimeout);
			throw new Exception(e.getMessage());
		}

		return result;
	}

	@Override
	public void write(Datapoint datapoint) throws Exception {
		// Write persistent
		String address = datapoint.getCompleteAddress();
		this.mqttClient.getTopic(address).publish(datapoint.getValue().toString().getBytes(), 0, true);
		log.debug("Written datapoint={}", datapoint);
	}

	@Override
	public Datapoint subscribeDatapoint(String topicFilter) throws Exception {
		// Subscribe a topic
		this.mqttClient.subscribe(topicFilter);

		// Read the value from that topic to get the initial value
		return null;
	}

	@Override
	public void unsubscribeDatapoint(String topicFilter) throws Exception {
		this.mqttClient.unsubscribe(topicFilter);
	}
}
