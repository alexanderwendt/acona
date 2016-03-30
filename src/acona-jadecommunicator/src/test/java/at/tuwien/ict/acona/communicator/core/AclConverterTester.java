package at.tuwien.ict.acona.communicator.core;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaService;
import at.tuwien.ict.acona.communicator.util.ACLUtils;
import at.tuwien.ict.acona.communicator.util.JadeContainerUtil;
import jade.core.Runtime;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ContainerController;

public class AclConverterTester {
	
	private static Logger log = LoggerFactory.getLogger(AclConverterTester.class);
	
	private ContainerController agentContainer;
	ContainerController mainContainerController;
	
	private final JadeContainerUtil util = new JadeContainerUtil();
	private Communicator comm;
	
	@Before
	public void setup() {
		//Message syntax:
		//receiver, type, message as JSonObject that can be transformed into a Cell data structure
		
		try {
			//Create container
			log.debug("Create or get main container");
			mainContainerController = this.util.createMainJADEContainer("localhost", 1099, "MainContainer");
					
			log.debug("Create subcontainer");
			agentContainer = this.util.createAgentContainer("localhost", 1099, "Subcontainer"); 
			
			//log.debug("Create gui");
			//this.util.createRMAInContainer(agentContainer);
			
			//Create gateway
			log.debug("Create gateway");
			comm = new CommunicatorImpl();
			comm.init();
			
		} catch (Exception e) {
			log.error("Cannot initialize test environment", e);
		}
	}
	
	@After
	public void takeDown() {
		synchronized (this) {
			try {
				this.wait(200);
			} catch (InterruptedException e) {
				
			}
		}
		
		Runtime runtime = Runtime.instance();
		runtime.shutDown();
		synchronized (this) {
			try {
				this.wait(200);
			} catch (InterruptedException e) {
				
			}
		}
		this.comm.shutDown();
	}
	

	@Test
	public void testConvertMessage() {
		try {
			String expectedreceiver = "receiver";
			String expectedMessage = "testmessage";
			AconaService expectedType = AconaService.READ;
			Message testObject = Message.newMessage().setReceiver(expectedreceiver).setService(expectedType).setContent(expectedMessage);
			
			//Convert to ACL and back again
			ACLMessage message = ACLUtils.convertToACL(testObject);
			Message result = ACLUtils.convertToMessage(message);
			
			String actualreceiver = result.getReceivers()[0];
			String actualMessage = result.getContent().getAsJsonPrimitive().getAsString();
			AconaService actualType = result.getService();
			
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
