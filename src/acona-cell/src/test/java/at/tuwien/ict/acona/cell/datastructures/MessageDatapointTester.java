package at.tuwien.ict.acona.cell.datastructures;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.core.CellServiceTester;
import at.tuwien.ict.acona.cell.datastructures.types.AconaService;
import at.tuwien.ict.acona.communicator.core.Communicator;
import at.tuwien.ict.acona.communicator.core.CommunicatorImpl;
import at.tuwien.ict.acona.communicator.util.ACLUtils;
import at.tuwien.ict.acona.communicator.util.JadeContainerUtil;
import jade.core.Runtime;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class MessageDatapointTester {

	private static Logger log = LoggerFactory.getLogger(CellServiceTester.class);
	private final JadeContainerUtil util = new JadeContainerUtil();
	private Communicator comm;
	
	private ContainerController agentContainer;
	ContainerController mainContainerController;

	@Before
	public void setUp() throws Exception {
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
	public void tearDown() throws Exception {
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
	public void MessageConversionTestDatapoints() {
		try {
			String expectedreceiver = "receiver";
			AconaService expectedType = AconaService.READ;
			String input = "{\"ADDRESS\":\"subscribe.test.address\",\"TYPE\":\"\",\"VALUE\":\"MuHaahAhaAaahAAHA\"}";
			Datapoint dp = Datapoint.toDatapoint(input);
			Message testObject = Message.newMessage().setReceiver(expectedreceiver).setService(expectedType).setContent(dp);
			//To ACLMessage
			ACLMessage aclin = ACLUtils.convertToACL(testObject);
			
			//Back to message
			Message messageout = ACLUtils.convertToMessage(aclin);
			
			String actualreceiver = testObject.getReceivers()[0];
			//String actualMessage = testObject.getContent().getAsString();
			AconaService actualType = testObject.getService();
			
			//Now, the message would be sent and received. Convert back to datapoint
			Datapoint dpback = Datapoint.toDatapoint(messageout.getContent().toString());
			
			log.debug("Got receiver={}, type={}, message={}", actualreceiver, actualType, dpback);
			
			assertEquals("MuHaahAhaAaahAAHA", dpback.getValue().getAsString());
			
	
		} catch (Exception e) {
			log.error("Cannot execute test", e);
			fail("Error");
		}
	}
	

	@Test
	public void MessageConversionTestPlainStrings() {
		try {
			String expectedreceiver = "receiver";
			AconaService expectedType = AconaService.READ;
			String input = "Teststring";
			//Datapoint dp = Datapoint.toDatapoint(input);
			Message testObject = Message.newMessage().setReceiver(expectedreceiver).setService(expectedType).setContent(input);
			//To ACLMessage
			ACLMessage aclin = ACLUtils.convertToACL(testObject);
			
			//Back to message
			Message messageout = ACLUtils.convertToMessage(aclin);
			
			String actualreceiver = testObject.getReceivers()[0];
			//String actualMessage = testObject.getContent().getAsString();
			AconaService actualType = testObject.getService();
			
			//Now, the message would be sent and received. Convert back to datapoint
			String dpback = messageout.getStringContent();
			
			log.debug("Got receiver={}, type={}, message={}", actualreceiver, actualType, dpback);
			
			assertEquals("Teststring", dpback);
			
	
		} catch (Exception e) {
			log.error("Cannot execute test", e);
			fail("Error");
		}
	}
}
