package at.tuwien.ict.kore.communicator.core;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import at.tuwien.ict.kore.communicator.util.JadeContainerUtil;
import jade.core.Runtime;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ContainerController;

public class MessageTester {
	
	private static Logger log = LoggerFactory.getLogger(MessageTester.class);
	

	@Test
	public void testCreateMessage() {
		try {
			String expectedreceiver = "receiver";
			String expectedMessage = "testmessage";
			String expectedType = "type";
			JsonObject testObject = JsonMessage.createMessage(JsonMessage.toContentString(expectedMessage), expectedreceiver, expectedType);
			
			String actualreceiver = testObject.get(JsonMessage.RECEIVER).getAsString();
			String actualMessage = testObject.get(JsonMessage.BODY).getAsJsonObject().get(JsonMessage.CONTENT).getAsString();
			String actualType = testObject.get(JsonMessage.TYPE).getAsString();
			
			log.debug("Got receiver={}, type={}, message={}", actualreceiver, actualType, actualMessage);
			
			assertEquals(expectedreceiver, actualreceiver);
			assertEquals(expectedMessage, actualMessage);
			assertEquals(expectedType, actualType);
			
	
		} catch (Exception e) {
			log.error("Cannot init system", e);
			fail("Error");
		}
	}
	
//	@Test
//	public void testConvertMessage() {
//		try {
//			String expectedreceiver = "receiver";
//			String expectedMessage = "testmessage";
//			String expectedType = "type";
//			JsonObject testObject = Message.createMessage("testmessage", "receiver", "type");
//			
//			//Convert to ACL and back again
//			ACLMessage message = Message.convertToACL(testObject);
//			JsonObject result = Message.convertToJson(message);
//			
//			String actualreceiver = result.get(Message.RECEIVER).getAsString();
//			String actualMessage = result.get(Message.BODY).getAsJsonObject().get(Message.CONTENT).getAsString();
//			String actualType = result.get(Message.TYPE).getAsString();
//			
//			log.debug("Got receiver={}, type={}, message={}", actualreceiver, actualType, actualMessage);
//			
//			assertEquals(expectedreceiver, actualreceiver);
//			assertEquals(expectedMessage, actualMessage);
//			assertEquals(expectedType, actualType);
//			
//	
//		} catch (Exception e) {
//			log.error("Cannot init system", e);
//			fail("Error");
//		}
//	}
	
	

}
