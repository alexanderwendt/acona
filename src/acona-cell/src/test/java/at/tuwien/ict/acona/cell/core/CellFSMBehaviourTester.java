package at.tuwien.ict.acona.cell.core;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.core.CellGatewayImpl;
import at.tuwien.ict.acona.cell.core.helpers.CellWithFSMBehaviour;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaServiceType;
import at.tuwien.ict.acona.jadelauncher.core.Gateway;
import at.tuwien.ict.acona.jadelauncher.core.GatewayImpl;
import at.tuwien.ict.acona.jadelauncher.util.JadeContainerUtil;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class CellFSMBehaviourTester {
	
	private static Logger log = LoggerFactory.getLogger(CellFSMBehaviourTester.class);
	private final JadeContainerUtil util = new JadeContainerUtil();
	private Gateway comm;
	
	private ContainerController agentContainer;
	private ContainerController mainContainerController;

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
			comm = new GatewayImpl();
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

	
	/**
	 * In this test, one agent is created, which is the database. Another agent is created that shall read a value from the database with a blocking read function. The test is passed, if the read value is written to a subscribed datapoint
	 * 
	 */
	//@Test
	public void fsmBehaviourTest() {
		try {
			//Create agent in the system
			//String[] args = {"1", "pong"};
			CellGatewayImpl externalController = new CellGatewayImpl();
			Object[] args = new Object[2];
			args[0] = new JsonObject();
			args[1] = externalController;
			AgentController readerAgent = this.util.createAgent("FSMAgent", CellWithFSMBehaviour.class, args, agentContainer);
			double expectedValue = 1.2;
			
			log.debug("State={}", readerAgent.getState());
				
			
			//Write databasevalue directly into the storage
			externalController.getCell().getDataStorage().write(Datapoint.newDatapoint("Test").setValue(new JsonPrimitive(1.2)), null);
			
			//=== Start the system ===//
			this.comm.subscribeDatapoint("ReaderAgent", "none");
			
			//Send Write command
			Message writeMessage = Message.newMessage()
					.addReceiver("ReaderAgent")
					.setContent(Datapoint.newDatapoint("anything").setValue("START").toJsonObject())
					.setService(AconaServiceType.WRITE);
			
			Message ack = this.comm.sendSynchronousMessageToAgent(writeMessage, 10000);
			log.debug("Tester: Acknowledge of cell writing recieved={}", ack);
			
			//Subscribe the result
			double actualResult = this.comm.getDatapointFromAgent(20000, true).getValue().getAsInt();
			
			//this.myAgent.send(ACLUtils.convertToACL(Message.newMessage().addReceiver(msg.getSender().getLocalName()).setService(AconaService.READ).setContent(Datapoint.newDatapoint("test"))));
			
			log.debug("correct value={}, actual value={}", expectedValue, actualResult);
			
			assertEquals(expectedValue, actualResult, 0.0);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}
	}

}
