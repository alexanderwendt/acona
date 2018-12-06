package at.tuwien.ict.acona.mq.cell.cellfunction;

import java.util.UUID;

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

/**
 * A Mqtt basic requestor
 *
 */
public class CellFunctionRequester {

	private static final Logger log = LoggerFactory.getLogger(CellFunctionRequester.class);

	// A unique Reply-To Topic for the client is obtained from Solace
	private String replyToTopic = "$SYS/client/reply-to";

	private Gson parser = new Gson();

	public void run(String... args) {
		log.debug("BasicRequestor initializing...");

		String host = args[0];
		String username = args[1];
		String password = args[2];

		if (!host.startsWith("tcp://")) {
			host = "tcp://" + host;
		}

		try {
			// Create an Mqtt client
			final MqttClient mqttClient = new MqttClient(host, "HelloWorldBasicRequestor");
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			connOpts.setUserName(username);
			connOpts.setPassword(password.toCharArray());

			// Connect the client
			System.out.println("Connecting to Solace messaging at " + host);
			mqttClient.connect(connOpts);
			System.out.println("Connected");

			// Semaphore used for synchronizing b/w threads
			final Semaphore latch = new Semaphore(0);

			// Topic the client will use to send request messages
			final String requestTopic = "T/GettingStarted/request";

			// Callback - Anonymous inner-class for receiving the Reply-To topic from the Solace broker
			mqttClient.setCallback(new MqttCallback() {
				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					// If the topic is "$SYS/client/reply-to" then set our replyToTopic
					// to with the contents of the message payload received
					if (topic != null && topic.equals("$SYS/client/reply-to")) {
						replyToTopic = new String(message.getPayload());
						log.debug("\nReceived Reply-to topic from Solace for the MQTT client:" +
								"\n\tReply-To: " + replyToTopic + "\n");
					} else {
						// Received a response to our request
						try {
							// Parse the response payload and convert to a JSONObject
							JsonElement obj = parser.toJsonTree(new String(message.getPayload()));
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
			// log.debug("Requesting Reply-To topic from Solace...");
			// mqttClient.subscribe("$SYS/client/reply-to", 0);

			// Wait for till we have received the reply to Topic
			// try {
			// latch.acquire();
			// } catch (InterruptedException e) {
			// log.debug("I was awoken while waiting");
			// }

			// Check if we have a Reply-To topic
			// if (replyToTopic == null || replyToTopic.isEmpty()) {
			// log.debug("Unable to request Reply-To from Solace. Exiting");
			// System.exit(0);
			// }

			// Subscribe client to the Solace provide Reply-To topic with a QoS level of 0
			log.debug("Subscribing client to Solace provide Reply-To topic");
			mqttClient.subscribe(replyToTopic, 0);

			// Create the request payload in JSON format
			JsonObject obj = new JsonObject();
			obj.addProperty("correlationId", UUID.randomUUID().toString());
			obj.addProperty("replyTo", replyToTopic);
			obj.addProperty("message", "Sample Request");
			String reqPayload = obj.toString();

			// Create a request message and set the request payload
			MqttMessage reqMessage = new MqttMessage(reqPayload.getBytes());
			reqMessage.setQos(0);

			log.debug("Sending request to: " + requestTopic);

			// Publish the request message
			mqttClient.publish(requestTopic, reqMessage);

			// Wait for till we have received a response
			try {
				latch.tryAcquire(1000, TimeUnit.MILLISECONDS); // block here until message received
			} catch (InterruptedException e) {
				log.debug("I was awoken while waiting");
			}

			// Disconnect the client
			mqttClient.disconnect();
			log.debug("Exiting");

			System.exit(0);
		} catch (MqttException me) {
			log.debug("reason " + me.getReasonCode());
			log.debug("msg " + me.getMessage());
			log.debug("loc " + me.getLocalizedMessage());
			log.debug("cause " + me.getCause());
			log.debug("excep " + me);
			me.printStackTrace();
		}
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

		new CellFunctionRequester().run(param);
	}
}
