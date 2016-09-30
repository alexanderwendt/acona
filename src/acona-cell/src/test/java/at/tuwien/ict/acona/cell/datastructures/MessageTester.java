package at.tuwien.ict.acona.cell.datastructures;

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.datastructures.types.AconaServiceType;

@Deprecated
public class MessageTester {
	
	private static Logger log = LoggerFactory.getLogger(MessageTester.class);
	

	@Test
	public void testCreateMessage() {
		try {
			String expectedreceiver = "receiver";
			String expectedMessage = "testmessage";
			AconaServiceType expectedType = AconaServiceType.READ;
			Message testObject = Message.newMessage().setReceiver(expectedreceiver).setService(expectedType).setContent(expectedMessage);
			
			String actualreceiver = testObject.getReceivers()[0];
			String actualMessage = testObject.getContent().getAsString();
			AconaServiceType actualType = testObject.getService();
			
			log.debug("Got receiver={}, type={}, message={}", actualreceiver, actualType, actualMessage);
			
			assertEquals(expectedreceiver, actualreceiver);
			assertEquals(expectedMessage, actualMessage);
			assertEquals(expectedType, actualType);
			
	
		} catch (Exception e) {
			log.error("Cannot init system", e);
			fail("Error");
		}
	}
	
	@Test
	public void testDatapointToMessageTester() {
		try {
			String expectedreceiver = "receiver";
			String expectedMessage = "testmessage";
			AconaServiceType expectedType = AconaServiceType.READ;
			String input = "{\"ADDRESS\":\"subscribe.test.address\",\"TYPE\":\"\",\"VALUE\":\"MuHaahAhaAaahAAHA\"}";
			Datapoint dp = Datapoint.toDatapoint(input);
			
			Message testObject = Message.newMessage().setReceiver(expectedreceiver).setService(expectedType).setContent(expectedMessage);
			
			String actualreceiver = testObject.getReceivers()[0];
			//String actualMessage = testObject.getContent().getAsString();
			AconaServiceType actualType = testObject.getService();
			testObject.setContent(dp);
			
			//Now, the message would be sent and received. Convert back to datapoint
			Datapoint dpback = Datapoint.toDatapoint(testObject.getContent().getAsJsonObject());
			
			log.debug("Got receiver={}, type={}, message={}", actualreceiver, actualType, dpback);
			
			assertEquals("MuHaahAhaAaahAAHA", dpback.getValue().getAsString());
			
	
		} catch (Exception e) {
			log.error("Cannot init system", e);
			fail("Error");
		}
	}
}
