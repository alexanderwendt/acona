package at.tuwien.ict.acona.cell.core;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import _OLD.at.tuwien.ict.acona.cell.config.ActivatorConfigJadeBehaviour;
import _OLD.at.tuwien.ict.acona.cell.config.BehaviourConfigJadeBehaviour;
import _OLD.at.tuwien.ict.acona.cell.config.CellConfigJadeBehaviour;
import _OLD.at.tuwien.ict.acona.cell.config.ConditionConfig;
import _OLD.at.tuwien.ict.acona.cell.custombehaviours.SendAsynchronousBehaviour;
import at.tuwien.ict.acona.cell.cellfunction.special.conditions.ConditionHasValue;
import at.tuwien.ict.acona.cell.cellfunction.special.conditions.ConditionIsNotEmpty;
import at.tuwien.ict.acona.cell.core.helpers.ReadOperandBehaviour;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaServiceType;
import at.tuwien.ict.acona.jadelauncher.core.Gateway;
import at.tuwien.ict.acona.jadelauncher.util.KoreExternalControllerImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;

public class CellSendTester {

	private static Logger log = LoggerFactory.getLogger(CellSendTester.class);
	//private final JadeContainerUtil util = new JadeContainerUtil();
	private KoreExternalControllerImpl commUtil = KoreExternalControllerImpl.getLauncher();
	private Gateway comm = commUtil.getJadeGateway();

	@Before
	public void setUp() throws Exception {
		try {
			//Create container
			log.debug("Create or get main container");
			this.commUtil.createMainContainer("localhost", 1099, "MainContainer");
			
			log.debug("Create subcontainer");
			this.commUtil.createSubContainer("localhost", 1099, "Subcontainer");
			
			//log.debug("Create gui");
			//this.commUtil.createDebugUserInterface();
			
			//Create gateway
			commUtil.initJadeGateway();
			
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
		this.commUtil.shutDownJadeGateway();
	}
	
	/**
	 * Idea: Create a reader behaviour that reads 2 values from 2 different agents. It has the "state=1". 2 agents provide 2 datapoints with double values. The reader behaviour sends read requests to all these agents and 
	 * sets a status datapoint to 2 . The 2 agents respond. If all 3 messages arrive, a process behaviour is triggered "state=2". It sums the operands and sends the result to a 3rd agent. If the sum is correct, the test is passed.
	 * 
	 * 
	 */
	//@Test
	public void singleReadtest() {
		try {
			String stateaddress = "agent.state";
			String triggeraddress = "agent.trigger";
			String operand1address = "agent.operand1";
			String operand2address = "agent.operand2";
			String resultAddress = "agent.result";
			
			//Create Database agents 1-2
			CellConfigJadeBehaviour dbAgent1 = CellConfigJadeBehaviour.newConfig("dbagent1", CellImpl.class.getName());
			this.commUtil.createAgent(dbAgent1);
			
			//Create the calculator agent
			//Create the basic information for any agent
			CellConfigJadeBehaviour additionAgent = CellConfigJadeBehaviour.newConfig("AdditionAgent", "at.tuwien.ict.acona.cell.core.CellImpl");
			
			//Create conditions that can be used in the agents, only the name of the condition and their classes
			//Readerconditions
			additionAgent.addCondition(ConditionConfig.newConfig("starttrigger", ConditionHasValue.class.getName())
					.setProperty("comparestring", "START"));
			
			//Create behaviours that will be used by the agents
			//Add the reader
			additionAgent.addBehaviour(BehaviourConfigJadeBehaviour.newConfig("S1", ReadOperandBehaviour.class.getName())
					.setProperty("op1agent", "dbagent1")
					.setProperty("op1address", operand1address)
					.setProperty("op2agent", "dbagent2")
					.setProperty("op2address", operand2address)
					.setProperty("successstateid", "OK")
					.setProperty("stateaddress", stateaddress));
			
			//Add activators
			//Add reader activator
			additionAgent.addActivator(ActivatorConfigJadeBehaviour.newConfig("T0").setBehaviour("S1").setActivatorLogic("")
					.addMapping(triggeraddress, "starttrigger"));
			
			this.commUtil.createAgent(additionAgent);
			
			
			//Create result receiver agent
			CellConfigJadeBehaviour receiverAgent = CellConfigJadeBehaviour.newConfig("receiveragent", "at.tuwien.ict.acona.cell.core.CellImpl");
			this.commUtil.createAgent(receiverAgent);
			
			//subscribe the result without timeout
			this.comm.subscribeDatapoint("receiveragent", resultAddress);
			
			//Write the numbers in the database agents
			this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver("dbagent1").setContent(Datapoint.newDatapoint(operand1address).setValue(new JsonPrimitive(11.))).setService(AconaServiceType.WRITE));
			
			//Trigger the calculator agent
			this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver("AdditionAgent").setContent(Datapoint.newDatapoint(triggeraddress).setValue(new JsonPrimitive("START"))).setService(AconaServiceType.WRITE));
			
			
			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {
					
				}
			}
			
			//Get the result from the result receiver agent
			double actualResult = this.comm.getDatapointFromAgent(100000, true).getValue().getAsDouble();
			
			log.debug("correct value={}, actual value={}", 33, actualResult);
			
			assertEquals(33, actualResult, 0.0);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}
		
	}

	
	/**
	 * Idea: Create a reader behaviour that reads 2 values from 2 different agents. It has the "state=1". 2 agents provide 2 datapoints with double values. The reader behaviour sends read requests to all these agents and 
	 * sets a status datapoint to 2 . The 2 agents respond. If all 3 messages arrive, a process behaviour is triggered "state=2". It sums the operands and sends the result to a 3rd agent. If the sum is correct, the test is passed.
	 * 
	 * 
	 */
	@Test
	public void asynchronousReadtest() {
		try {
			String stateaddress = "agent.state";
			String triggeraddress = "agent.trigger";
			String operand1address = "agent.operand1";
			String operand2address = "agent.operand2";
			String resultAddress = "agent.result";
			
			//Create Database agents 1-2
			CellConfigJadeBehaviour dbAgent1 = CellConfigJadeBehaviour.newConfig("dbagent1", CellImpl.class.getName());
			this.commUtil.createAgent(dbAgent1);
			CellConfigJadeBehaviour dbAgent2 = CellConfigJadeBehaviour.newConfig("dbagent2", "at.tuwien.ict.acona.cell.core.CellImpl");
			this.commUtil.createAgent(dbAgent2);
			
			
			
			//Create the calculator agent
			//Create the basic information for any agent
			CellConfigJadeBehaviour additionAgent = CellConfigJadeBehaviour.newConfig("AdditionAgent", "at.tuwien.ict.acona.cell.core.CellImpl");
			
			//Create conditions that can be used in the agents, only the name of the condition and their classes
			//Readerconditions
			additionAgent.addCondition(ConditionConfig.newConfig("starttrigger", ConditionHasValue.class.getName())
					.setProperty("comparestring", "START"));
			
			//Addition agent conditions
			additionAgent.addCondition(ConditionConfig.newConfig("operand1", ConditionIsNotEmpty.class.getName()));	//Names can be added directly by the classes too
			additionAgent.addCondition(ConditionConfig.newConfig("operand2", "at.tuwien.ict.acona.cell.activator.conditions.ConditionIsNotEmpty"));
			
			//Resultsender conditions
			additionAgent.addCondition(ConditionConfig.newConfig("receivername", ConditionIsNotEmpty.class.getName()));
			additionAgent.addCondition(ConditionConfig.newConfig("datapointsource", ConditionIsNotEmpty.class.getName()));
			
			//Create behaviours that will be used by the agents
			//Add the reader
			additionAgent.addBehaviour(BehaviourConfigJadeBehaviour.newConfig("S1", ReadOperandBehaviour.class.getName())
					.setProperty("op1agent", "dbagent1")
					.setProperty("op1address", operand1address)
					.setProperty("op2agent", "dbagent2")
					.setProperty("op2address", operand2address)
					.setProperty("successstateid", "OK")
					.setProperty("stateaddress", stateaddress));
			
			//Add the addition itself
			additionAgent.addBehaviour(BehaviourConfigJadeBehaviour.newConfig("S2", "at.tuwien.ict.acona.cell.core.helpers.AdditionBehaviour")
					.setProperty("operand1", operand1address)
					.setProperty("operand2", operand2address)
					.setProperty("result", resultAddress));
			
			//Add the sender
			additionAgent.addBehaviour(BehaviourConfigJadeBehaviour.newConfig("S3", SendAsynchronousBehaviour.class.getName())
					.setProperty("receivernameaddress", "s3.receiveragent")
					.setProperty("datapointsourceaddress", resultAddress)
					.setProperty("datapointtargetaddress", resultAddress)
					.setProperty("aconaserviceaddress", "s3.service")
					.setProperty("defaultservice", "WRITE"));
			
			//Add activators
			//Add reader activator
			additionAgent.addActivator(ActivatorConfigJadeBehaviour.newConfig("T0").setBehaviour("S1").setActivatorLogic("")
					.addMapping(triggeraddress, "starttrigger"));
			
			//Add addition activator
			additionAgent.addActivator(ActivatorConfigJadeBehaviour.newConfig("T1").setBehaviour("S2").setActivatorLogic("")
					.addMapping(operand1address, "operand1")
					.addMapping(operand2address, "operand2"));
			
			//Add sender activator
			additionAgent.addActivator(ActivatorConfigJadeBehaviour.newConfig("T2").setBehaviour("S3")
					.addMapping("s3.receiveragent", "receivername")
					.addMapping(resultAddress, "datapointsource"));
			
			this.commUtil.createAgent(additionAgent);
			
			
			//Create result receiver agent
			CellConfigJadeBehaviour receiverAgent = CellConfigJadeBehaviour.newConfig("receiveragent", "at.tuwien.ict.acona.cell.core.CellImpl");
			this.commUtil.createAgent(receiverAgent);
			
			//subscribe the result without timeout
			this.comm.subscribeDatapoint("receiveragent", resultAddress);
			
			//Write the numbers in the database agents
			this.comm.sendSynchronousMessageToAgent(Message.newMessage().addReceiver("dbagent1").setContent(Datapoint.newDatapoint(operand1address).setValue(new JsonPrimitive(11.))).setService(AconaServiceType.WRITE));
			this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver("dbagent2").setContent(Datapoint.newDatapoint(operand2address).setValue(new JsonPrimitive(22.))).setService(AconaServiceType.WRITE));
			this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver("AdditionAgent").setContent(Datapoint.newDatapoint("s3.receiveragent").setValue(new JsonPrimitive("receiveragent"))).setService(AconaServiceType.WRITE));
			
			//Trigger the calculator agent
			this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver("AdditionAgent").setContent(Datapoint.newDatapoint(triggeraddress).setValue(new JsonPrimitive("START"))).setService(AconaServiceType.WRITE));
			
			
//			synchronized (this) {
//				try {
//					this.wait(2000);
//				} catch (InterruptedException e) {
//					
//				}
//			}
			
			//Get the result from the result receiver agent
			double actualResult = this.comm.getDatapointFromAgent(100000, true).getValue().getAsDouble();
			
			log.debug("correct value={}, actual value={}", 33, actualResult);
			
			assertEquals(33, actualResult, 0.0);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}
		
	}


//	@Test
//	public void sendMessageOrderingtest() {
//		try {
//			String readAddress = "storageagent.data.value";
//			String triggerAddress = "readeragent.data.command";
//			String resultAddress = "data.result";
//			int databaseValue = 12345;
//			int expectedValue = 12345;
//			
//			//Create config JSON for reader agent
//			CellConfig cellreader = CellConfig.newConfig("SenderAgent", CustomTestCell.class.getName());
//			cellreader.setClass(CustomTestCell.class);
//			cellreader.addProperty("targetcell", "TesterAgent");
//			
//			//Create agent in the system
//			Object[] args = new Object[1];
//			args[0] = cellreader.toJsonObject();
//			AgentController senderAgent = this.util.createAgent(cellreader.getName(), cellreader.getClassToInvoke(), args, agentContainer);
//			
//			log.debug("State={}", senderAgent.getState());
//			
//						
//			//Create config JSON for storage agent
//			CellConfig cellstorage = CellConfig.newConfig("TesterAgent", CustomTestCell.class.toString());
//			cellstorage.setClass(InspectorCell.class);
//			
//			//Create cell inspector controller for the subscriber
//			InspectorCellClient externalController = new InspectorCellClient();
//			Object[] argsPublisher = new Object[2];
//			argsPublisher[0] = cellstorage.toJsonObject();
//			argsPublisher[1] = externalController;
//			//Create agent in the system
//			AgentController agentController = this.util.createAgent(cellstorage.getName(), cellstorage.getClassToInvoke(), argsPublisher, agentContainer);
//			
//			log.debug("State={}", agentController.getState());		
//			
//			//Write databasevalue directly into the storage
//			externalController.getCell().getDataStorage().write(Datapoint.newDatapoint(readAddress).setValue(new JsonPrimitive(databaseValue)), null);
//			
//			//=== Start the system ===//
//			this.comm.subscribeDatapoint("ReaderAgent", readAddress);
//			
//			//Send Write command
//			Message writeMessage = Message.newMessage()
//					.addReceiver("ReaderAgent")
//					.setContent(Datapoint.newDatapoint(triggerAddress).setValue("START").toJsonObject())
//					.setService(AconaService.WRITE);
//			
//			Message ack = this.comm.sendSynchronousMessageToAgent(writeMessage, 10000);
//			log.debug("Tester: Acknowledge of cell writing recieved={}", ack);
//			
//			//Subscribe the result
//			double actualResult = this.comm.getDatapointFromAgent(20000, true).getValue().getAsInt();
//			//actualResult = this.comm.getDatapointFromAgent(20000, false).getValue().getAsInt();
//			
//			//this.myAgent.send(ACLUtils.convertToACL(Message.newMessage().addReceiver(msg.getSender().getLocalName()).setService(AconaService.READ).setContent(Datapoint.newDatapoint("test"))));
//			
//			log.debug("correct value={}, actual value={}", expectedValue, actualResult);
//			
//			assertEquals(true, true);
//			log.info("Test passed");
//		} catch (Exception e) {
//			log.error("Error testing system", e);
//			fail("Error");
//		}
//	}
	
	


}
