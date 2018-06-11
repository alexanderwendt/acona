package at.tuwien.ict.acona.mq.cell.cellfunction;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * A Mqtt basic requestor
 *
 */
public class MqttCommunicatorImpl {

	private static final Logger log = LoggerFactory.getLogger(MqttCommunicatorImpl.class);

	// A unique Reply-To Topic for the client is obtained from Solace
	// private String replyToPayload = "";

	private Gson gson = new Gson();

	private Map<String, JsonElement> incomingMessages = new ConcurrentHashMap<>();

	// Service Classes
	final Map<String, Function<JsonElement, JsonElement>> handlerMap = new HashMap<>();

	// Semaphore used for synchronizing b/w threads
	private final Semaphore latch = new Semaphore(0);
	private MqttClient mqttClient;

	// === Parameter variables ===//
	String host = "tcp://127.0.0.1:1883";
	String username = "acona";
	String password = "acona";
	String functionName = "FunctionRequester";
	String agentName = "agent1";

	private int timeout = 100000;

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

	public void init() throws Exception {
		log.debug("BasicRequestor Tester initializing...");

		try {
			this.rootAddress = agentName + "/" + functionName;
			this.subscribedReplyAddress = this.rootAddress + "/reply-to";
			this.subscribedServiceAddressPrefix = this.rootAddress + "/service";
			this.subscribedCommandAddress = this.rootAddress + "/command";
			this.publishedStateAddress = this.rootAddress + "/state";

			// Create an Mqtt client
			mqttClient = new MqttClient(host, functionName);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			connOpts.setUserName(username);
			connOpts.setPassword(password.toCharArray());

			// Connect the client
			log.debug("Connecting to Solace messaging at " + host);
			mqttClient.connect(connOpts);
			log.debug("Connected");

			// Callback - Anonymous inner-class for receiving the Reply-To topic from the Solace broker
			mqttClient.setCallback(new MqttCallback() {

				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					// Propoerties of incoming messages
					// JsonElement message
					// C

					// If the topic is recived at the reply-to address, then there is a blocking function waiting for it.
					// Check if the message is a JsonObject
					String payloadString = new String(message.getPayload());
					JsonObject jsonMessage = gson.fromJson(payloadString, JsonObject.class);
					// Make a check if String is Json

					// If this is a reply of a request
					if (topic != null && topic.equals(subscribedReplyAddress)) {
						// String replyToPayload = new String(message.getPayload());
						log.debug("\nReceived Reply-to topic from Solace for the MQTT client:" +
								"\n\tReply-To: " + jsonMessage + "\n");

						// JsonObject response = gson.fromJson(jsonMessage, JsonObject.class);
						// Get correlationid
						String correlationid = jsonMessage.get("correlationid").getAsString();
						JsonElement responseMessage = jsonMessage.get("message"); // The message can be any json structure

						incomingMessages.put(correlationid, responseMessage);

					} else if (topic != null && topic.startsWith(subscribedServiceAddressPrefix)) {
						// Run service
						JsonElement responseMessage = jsonMessage.get("message"); // The message can be any json structure
						JsonElement result = handlerMap.get(topic).apply(responseMessage);

						// this.performaction
						// TODO: Add method(method name)

					} else if (topic != null && topic.equals(subscribedCommandAddress)) {
						// this.setcommand(command)
						// TODO: Add method;

					} else { // This is a perform operation
						// Received a response to our request
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

					latch.release(); // unblock main thread
				}

				@Override
				public void connectionLost(Throwable cause) {
					log.debug("Connection to Solace messaging lost!" + cause.getMessage());
					latch.release();
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
			this.handlerMap.put("test", (JsonElement input) -> serviceRead(input));
			// this.handlerMap.get("address").accept();

		} catch (MqttException me) {
			log.debug("reason " + me.getReasonCode());
			log.debug("msg " + me.getMessage());
			log.debug("loc " + me.getLocalizedMessage());
			log.debug("cause " + me.getCause());
			log.debug("excep " + me);
			throw new Exception(me.getMessage());
		}

	}

	// Service function
	private JsonElement serviceRead(JsonElement message) {
		return new JsonPrimitive("test");

	}

	public JsonElement execute(String topic, JsonElement message) throws Exception {
		return this.executeRequest(topic, message, true);
	}

	public void executeAsync(String topic, JsonElement message) throws Exception {
		this.executeRequest(topic, message, false);
	}

	private JsonElement executeRequest(String topic, JsonElement message, boolean isSychronousCall) throws Exception {
		// Topic the client will use to send request messages
		JsonElement result = new JsonPrimitive("");

		try {
			// Create the request payload in JSON format
			// Request attribute
			// String caller [agent:function]
			// String correlationid [unique string]
			// String type []
			// JsonElement message

			JsonObject payload = new JsonObject();
			String correlationID = "1"; // UUID.randomUUID().toString();
			payload.addProperty("correlationid", correlationID);
			payload.addProperty("caller", agentName + "/" + functionName);
			payload.add("message", message);
			String reqPayload = payload.toString();

			// Create a request message and set the request payload
			MqttMessage reqMessage = new MqttMessage(reqPayload.getBytes());
			reqMessage.setQos(0);

			log.debug("Sending request to: " + topic);

			// Publish the request message
			mqttClient.publish(topic, reqMessage);

			if (isSychronousCall == true) {
				// Wait for till we have received a response
				try {
					latch.tryAcquire(timeout, TimeUnit.MILLISECONDS); // block here until message received
				} catch (InterruptedException e) {
					log.error("Timeout or interruption error!", e);
				}

				// Get the message answer from the map
				result = this.incomingMessages.getOrDefault(correlationID, new JsonPrimitive(""));
				this.incomingMessages.remove(correlationID);
			}

		} catch (Exception e) {
			log.error("Cannot send request", e);
			throw new Exception(e.getMessage());
		}

		return result;
	}

	public void shutDownClient() {
		// Disconnect the client
		try {
			mqttClient.disconnect();
		} catch (MqttException e) {
			log.error("Cannot disconnect from client={}", mqttClient.getServerURI(), e);
		}

		log.debug("Exiting function={}", this.agentName + "/" + this.functionName);
	}

	public void run() throws Exception {
		JsonElement result = this.execute(requestTopic, new JsonPrimitive("Message for test"));
		log.info("Read data={}", result);

	}

	public static void main(String[] args) {
		// Check command line arguments
		// if (args.length != 3) {
		// log.debug("Usage: basicRequestor <host:port> <client-username> <client-password>");
		// System.exit(-1);
		// }

		String[] param = new String[3];
		param[0] = "tcp://127.0.0.1:1883";
		param[1] = "test";
		param[2] = "test";

		MqttCommunicatorImpl requester = new MqttCommunicatorImpl();
		try {
			requester.init();
			for (int i = 0; i < 10; i++) {
				requester.run();

			}

			requester.shutDownClient();
			System.exit(0);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
