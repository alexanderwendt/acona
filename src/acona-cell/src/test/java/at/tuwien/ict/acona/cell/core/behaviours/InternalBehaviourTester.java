package at.tuwien.ict.acona.cell.core.behaviours;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.config.ActivatorConfigJadeBehaviour;
import at.tuwien.ict.acona.cell.config.BehaviourConfigJadeBehaviour;
import at.tuwien.ict.acona.cell.config.CellConfigJadeBehaviour;
import at.tuwien.ict.acona.cell.config.ConditionConfig;
import at.tuwien.ict.acona.cell.core.CellGatewayImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaServiceType;
import jade.wrapper.AgentController;

public class InternalBehaviourTester {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * In this test, one agent is created, which is the database. Another agent is created that shall read a value from the database with a blocking read function. The test is passed, if the read value is written to a subscribed datapoint
	 * 
	 */
	@Test
	public void SynchronizedReadTest() {
		try {
			String readAddress = "storageagent.data.value";
			String triggerAddress = "readeragent.data.command";
			String resultAddress = "data.result";
			int databaseValue = 12345;
			int expectedValue = 12345;
			
			//Create config JSON for reader agent
			CellConfigJadeBehaviour cellreader = CellConfigJadeBehaviour.newConfig("ReaderAgent", "at.tuwien.ict.acona.cell.core.CellImpl");
			//cellreader.setClass(CellImpl.class);
			cellreader.addCondition(ConditionConfig.newConfig("startreadcondition", "at.tuwien.ict.acona.cell.activator.conditions.ConditionIsNotEmpty"));
			cellreader.addBehaviour(BehaviourConfigJadeBehaviour.newConfig("readBehaviour", "at.tuwien.ict.acona.cell.core.helpers.TestReadAndWriteBehaviour")
					.setProperty("agentname", "StorageAgent")
					.setProperty("timeout", "2000")
					.setProperty("result", resultAddress)
					.setProperty("readaddress", readAddress));
			cellreader.addActivator(ActivatorConfigJadeBehaviour.newConfig("ReadActivator").setBehaviour("readBehaviour").setActivatorLogic("")
					.addMapping(triggerAddress, "startreadcondition"));
			
			//Create agent in the system
			//String[] args = {"1", "pong"};
			Object[] args = new Object[1];
			args[0] = cellreader.toJsonObject();
			
			AgentController readerAgent = null;
			try {
				readerAgent = this.util.createAgent(cellreader.getName(), cellreader.getClassToInvoke(), args, agentContainer);
			} catch (Exception e) {
				log.error("error", e);
			}
			
			
			
			log.debug("State={}", readerAgent.getState());
			
			//Create config JSON for storage agent
			CellConfigJadeBehaviour cellstorage = CellConfigJadeBehaviour.newConfig("StorageAgent", "at.tuwien.ict.acona.cell.core.InspectorCell");
			//cellstorage.setClass(InspectorCell.class);
			
			//Create cell inspector controller for the subscriber
			CellGatewayImpl externalController = new CellGatewayImpl();
			Object[] argsPublisher = new Object[2];
			argsPublisher[0] = cellstorage.toJsonObject();
			argsPublisher[1] = externalController;
			//Create agent in the system
			AgentController agentController = this.util.createAgent(cellstorage.getName(), Class.forName(cellstorage.getClassName()), argsPublisher, agentContainer);
			
			log.debug("State={}", agentController.getState());		
			
			//Write databasevalue directly into the storage
			externalController.getCell().getDataStorage().write(Datapoint.newDatapoint(readAddress).setValue(new JsonPrimitive(databaseValue)), null);
			
			//=== Start the system ===//
			this.comm.subscribeDatapoint("ReaderAgent", readAddress);
			
			//Send Write command
			Message writeMessage = Message.newMessage()
					.addReceiver("ReaderAgent")
					.setContent(Datapoint.newDatapoint(triggerAddress).setValue("START").toJsonObject())
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
