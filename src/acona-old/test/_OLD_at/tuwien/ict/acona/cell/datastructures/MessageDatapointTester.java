package _OLD_at.tuwien.ict.acona.cell.datastructures;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.core.CellBasicServiceTester;
import at.tuwien.ict.acona.cell.datastructures.types.AconaServiceType;
import at.tuwien.ict.acona.jadelauncher.util.ACLUtils;
import at.tuwien.ict.acona.jadelauncher.util.JadeContainerUtil;
import at.tuwien.ict.acona.jadelauncher.util.KoreExternalControllerImpl;
import jade.core.Runtime;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

@Deprecated
public class MessageDatapointTester {

	private static Logger log = LoggerFactory.getLogger(CellBasicServiceTester.class);
	private KoreExternalControllerImpl launcher = KoreExternalControllerImpl.getLauncher();
	
	private ContainerController agentContainer;
	ContainerController mainContainerController;

	@Before
	public void setUp() throws Exception {
		try {
			//Create container
			log.debug("Create or get main container");
			this.launcher.createMainContainer("localhost", 1099, "MainContainer");
			
			log.debug("Create subcontainer");
			this.launcher.createSubContainer("localhost", 1099, "Subcontainer");
			
			//log.debug("Create gui");
			//this.commUtil.createDebugUserInterface();
			
			//Create gateway
			//commUtil.initJadeGateway();
			
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
		//this.commUtil.shutDownJadeGateway();
	}

	//@Test
	public void MessageConversionTestDatapoints() {
		try {
			String expectedreceiver = "receiver";
			AconaServiceType expectedType = AconaServiceType.READ;
			String input = "{\"ADDRESS\":\"subscribe.test.address\",\"TYPE\":\"\",\"VALUE\":\"MuHaahAhaAaahAAHA\"}";
			Datapoint dp = Datapoint.toDatapoint(input);
			Message testObject = Message.newMessage().setReceiver(expectedreceiver).setService(expectedType).setContent(dp);
			//To ACLMessage
			ACLMessage aclin = ACLUtils.convertToACL(testObject);
			
			//Back to message
			Message messageout = ACLUtils.convertToMessage(aclin);
			
			String actualreceiver = testObject.getReceivers()[0];
			//String actualMessage = testObject.getContent().getAsString();
			AconaServiceType actualType = testObject.getService();
			
			//Now, the message would be sent and received. Convert back to datapoint
			Datapoint dpback = Datapoint.toDatapoint(messageout.getContent().toString());
			
			log.debug("Got receiver={}, type={}, message={}", actualreceiver, actualType, dpback);
			
			assertEquals("MuHaahAhaAaahAAHA", dpback.getValue().getAsString());
			
	
		} catch (Exception e) {
			log.error("Cannot execute test", e);
			fail("Error");
		}
	}
	

	//@Test
	public void MessageConversionTestPlainStrings() {
		try {
			String expectedreceiver = "receiver";
			AconaServiceType expectedType = AconaServiceType.READ;
			String input = "Teststring";
			//Datapoint dp = Datapoint.toDatapoint(input);
			Message testObject = Message.newMessage().setReceiver(expectedreceiver).setService(expectedType).setContent(input);
			//To ACLMessage
			ACLMessage aclin = ACLUtils.convertToACL(testObject);
			
			//Back to message
			Message messageout = ACLUtils.convertToMessage(aclin);
			
			String actualreceiver = testObject.getReceivers()[0];
			//String actualMessage = testObject.getContent().getAsString();
			AconaServiceType actualType = testObject.getService();
			
			//Now, the message would be sent and received. Convert back to datapoint
			String dpback = messageout.getContentAsString();
			
			log.debug("Got receiver={}, type={}, message={}", actualreceiver, actualType, dpback);
			
			assertEquals("Teststring", dpback);
			
	
		} catch (Exception e) {
			log.error("Cannot execute test", e);
			fail("Error");
		}
	}
}
