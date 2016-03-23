package at.tuwien.ict.acona.cell.datastructures;

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.datastructures.types.AconaService;

public class MessageTester {
	
	private static Logger log = LoggerFactory.getLogger(MessageTester.class);
	

	@Test
	public void testCreateMessage() {
		try {
			String expectedreceiver = "receiver";
			String expectedMessage = "testmessage";
			String expectedType = AconaService.READ;
			Message testObject = Message.newMessage().setReceiver(expectedreceiver).setService(expectedType).setContent(expectedMessage);
			
			String actualreceiver = testObject.getReceivers()[0];
			String actualMessage = testObject.getContent().getAsString();
			String actualType = testObject.getService();
			
			log.debug("Got receiver={}, type={}, message={}", actualreceiver, actualType, actualMessage);
			
			assertEquals(expectedreceiver, actualreceiver);
			assertEquals(expectedMessage, actualMessage);
			assertEquals(expectedType, actualType);
			
	
		} catch (Exception e) {
			log.error("Cannot init system", e);
			fail("Error");
		}
	}
}
