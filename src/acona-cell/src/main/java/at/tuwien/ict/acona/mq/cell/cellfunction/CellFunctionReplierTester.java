package at.tuwien.ict.acona.mq.cell.cellfunction;

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

import java.util.concurrent.CountDownLatch;

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

/**
 * A Mqtt basic replier
 *
 */
public class CellFunctionReplierTester {
	private static final Logger log = LoggerFactory.getLogger(CellFunctionReplierTester.class);

	private Gson parser = new Gson();

	public void run(String... args) {
		log.debug("BasicReplier initializing...");

		String host = args[0];
		String username = args[1];
		String password = args[2];

		if (!host.startsWith("tcp://")) {
			host = "tcp://" + host;
		}

		try {
			// Create an Mqtt client
			final MqttClient mqttClient = new MqttClient(host, "HelloWorldBasicReplier");
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			connOpts.setUserName(username);
			connOpts.setPassword(password.toCharArray());

			// Connect the client
			log.debug("Connecting to Solace messaging at " + host);
			mqttClient.connect(connOpts);
			log.debug("Connected");

			// Latch used for synchronizing b/w threads
			final CountDownLatch latch = new CountDownLatch(1);

			// Topic filter the client will subscribe to receive requests
			final String requestTopic = "T/GettingStarted/request";

			// Callback - Anonymous inner-class for receiving request messages
			mqttClient.setCallback(new MqttCallback() {

				@Override
				public void messageArrived(String topic, MqttMessage message) throws Exception {
					try {
						// Parse the received request message and convert payload to a JSONObject
						JsonElement payloadObj = parser.toJsonTree(new String(message.getPayload()));
						JsonObject jsonPayload = payloadObj.getAsJsonObject();

						// Get the correlationId and replyTo fields from the payload
						String correlationId = jsonPayload.get("correlationId").getAsString();
						String replyTo = jsonPayload.get("replyTo").getAsString();
						String messageContent = jsonPayload.get("message").getAsString();

						log.debug("\nReceived a request message!" +
								"\n\tCorrel. Id: " + correlationId +
								"\n\tReply To:   " + replyTo +
								"\n\tMessage:    " + messageContent + "\n");

						// Create the response payload in JSON format and set correlationId
						// to the id received in the request message above. Requestor will
						// use this to correlate the response with its request message.
						JsonObject obj = new JsonObject();
						obj.addProperty("correlationId", correlationId);
						obj.addProperty("message", "Sample Response");
						String respPayload = obj.toString();

						// Create a response message and set the response payload
						MqttMessage respMessage = new MqttMessage(respPayload.getBytes());
						respMessage.setQos(0);

						log.debug("Sending response to: " + replyTo);

						// Publish the response message to the replyTo topic retrieved
						// from the request message above
						MqttTopic mqttTopic = mqttClient.getTopic(replyTo);
						mqttTopic.publish(respMessage);

						latch.countDown(); // unblock main thread
					} catch (Exception ex) {
						log.debug("Exception parsing request message!");
						ex.printStackTrace();
					}
				}

				@Override
				public void connectionLost(Throwable cause) {
					System.out.println("Connection to Solace messaging lost!" + cause.getMessage());
					latch.countDown();
				}

				@Override
				public void deliveryComplete(IMqttDeliveryToken token) {
				}

			});

			// Subscribe client to the topic filter with a QoS level of 0
			log.debug("Subscribing client to request topic: " + requestTopic);
			mqttClient.subscribe(requestTopic, 0);

			log.debug("Waiting for request message...");
			// Wait for till we have received a request and sent a response
			try {
				latch.await(); // block here until message received, and latch will flip
			} catch (InterruptedException e) {
				log.debug("I was awoken while waiting");
			}

			// Disconnect the client
			mqttClient.disconnect();
			System.out.println("Exiting");

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
//		if (args.length != 3) {
//			log.debug("Usage: basicReplier <host:port> <client-username> <client-password>");
//			// System.out.println();
//			System.exit(-1);
//		}

		String[] param = new String[3];
		param[0] = "tcp://127.0.0.1:1883";
		param[1] = "test";
		param[2] = "test";

		new CellFunctionReplierTester().run(param);
	}
}