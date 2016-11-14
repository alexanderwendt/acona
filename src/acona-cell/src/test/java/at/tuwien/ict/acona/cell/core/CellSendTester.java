package at.tuwien.ict.acona.cell.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.cellfunction.specialfunctions.CFDataStorageUpdate;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.core.cellfunction.helpers.CFDurationThreadTester;
import at.tuwien.ict.acona.jadelauncher.util.KoreExternalControllerImpl;
import jade.core.Runtime;

public class CellSendTester {

	private static Logger log = LoggerFactory.getLogger(CellSendTester.class);
	// private final JadeContainerUtil util = new JadeContainerUtil();
	private KoreExternalControllerImpl launchUtil = KoreExternalControllerImpl.getLauncher();

	/**
	 * Setup the JADE communication. No Jade Gateway necessary
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		try {

			// Create container
			log.debug("Create or get main container");
			this.launchUtil.createMainContainer("localhost", 1099, "MainContainer");
			// mainContainerController =
			// this.util.createMainJADEContainer("localhost", 1099,
			// "MainContainer");

			log.debug("Create subcontainer");
			this.launchUtil.createSubContainer("localhost", 1099, "Subcontainer");

		} catch (Exception e) {
			log.error("Cannot initialize test environment", e);
		}
	}

	/**
	 * Tear down the JADE container
	 * 
	 * @throws Exception
	 */
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
	}

	/**
	 * Test the high level method query. Query works like a combination of write
	 * and subscribe. Start a controller agent and a service agent. Send a
	 * command or query to the controller agent. It operates for 2s, then there
	 * is a result, which is written to a certain datapoint. This datapoint is
	 * subscribed by the query and if any calue is written to the subscribed
	 * datapoint within a timeout, the query is executed. The value read is the
	 * testvalue
	 * 
	 * 
	 */
	@Test
	public void queryControllerTest() {
		try {
			String commandDatapointAddress = "datapoint.command";
			String queryDatapointAddress = "datapoint.query";
			String executeonceDatapointAddress = "datapoint.executeonce";
			String resultDatapointAddress = "datapoint.result";
			String controllerAgentName = "controllerAgent";
			String serviceAgentName = "serviceAgent";

			String expectedResult = "FINISHED";

			// Create service agent
			CellConfig testagent = CellConfig.newConfig(serviceAgentName, CellImpl.class)
					.addCellfunction(CellFunctionConfig.newConfig("testExecutor", CFDurationThreadTester.class)
							.addSyncDatapoint(DatapointConfig.newConfig(CFDurationThreadTester.commandDatapointID, commandDatapointAddress, SyncMode.push))
							.addSyncDatapoint(DatapointConfig.newConfig(CFDurationThreadTester.queryDatapointID, queryDatapointAddress, SyncMode.push))
							.addSyncDatapoint(DatapointConfig.newConfig(CFDurationThreadTester.executeonceDatapointID, executeonceDatapointAddress, SyncMode.push))
							.addWriteDatapoint(DatapointConfig.newConfig(CFDurationThreadTester.resultDatapointID, resultDatapointAddress, SyncMode.pull)));
			CellGatewayImpl cellService = this.launchUtil.createAgent(testagent);
			cellService.getCommunicator().setDefaultTimeout(1000000);

			// Create inspector or the new gateway
			CellGatewayImpl cellControlSubscriber = this.launchUtil.createAgent(CellConfig.newConfig(controllerAgentName, CellImpl.class));

			String result = cellControlSubscriber.getCommunicator().queryDatapoints(queryDatapointAddress, new JsonPrimitive("SELECT * FILESERVER"), serviceAgentName, resultDatapointAddress, serviceAgentName, 1000000).getValueAsString();
			log.debug("Received back from query={}", result);

			// String result =
			// cellControlSubscriber.readLocalDatapoint(resultDatapoint).getValueAsString();

			log.debug("correct value={}, actual value={}", expectedResult, result);

			assertEquals(result, expectedResult);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}
	}

	/**
	 * Test the high level method query. Query works like a combination of write
	 * and subscribe. Start a controller agent and a service agent. Send a
	 * command or query to the controller agent. It operates for 2s, then there
	 * is a result, which is written to a certain datapoint. This datapoint is
	 * subscribed by the query and if any calue is written to the subscribed
	 * datapoint within a timeout, the query is executed. The value read is the
	 * testvalue
	 * 
	 * 
	 */
	@Test
	public void queryControllerWithForeignDatapointTest() {
		try {
			String commandDatapointAddress = "datapoint.command";
			String queryDatapointAddress = "datapoint.query";
			String executeonceDatapointAddress = "datapoint.executeonce";
			String resultDatapointAddress = "datapoint.result";
			String controllerAgentName = "controllerAgent";
			String serviceAgentName = "serviceAgent";

			String expectedResult = "FINISHED";

			// SubscriptionAgent
			CellConfig subscriptionagent = CellConfig.newConfig("subscriptionagent");
			this.launchUtil.createAgent(subscriptionagent);

			// Create service agent
			CellConfig testagent = CellConfig.newConfig(serviceAgentName, CellImpl.class)
					.addCellfunction(CellFunctionConfig.newConfig("testExecutor", CFDurationThreadTester.class)
							.addSyncDatapoint(DatapointConfig.newConfig(CFDurationThreadTester.commandDatapointID, commandDatapointAddress, serviceAgentName, SyncMode.push))
							.addSyncDatapoint(DatapointConfig.newConfig(CFDurationThreadTester.queryDatapointID, queryDatapointAddress, serviceAgentName, SyncMode.push))
							.addSyncDatapoint(DatapointConfig.newConfig("executeonce", executeonceDatapointAddress, serviceAgentName, SyncMode.push))
							.addWriteDatapoint(DatapointConfig.newConfig(CFDurationThreadTester.resultDatapointID, resultDatapointAddress, SyncMode.pull)));
			this.launchUtil.createAgent(testagent);

			// Create inspector or the new gateway
			CellGatewayImpl cellControlSubscriber = this.launchUtil.createAgent(CellConfig.newConfig(controllerAgentName, CellImpl.class)
					.addCellfunction(CellFunctionConfig.newConfig("updater", CFDataStorageUpdate.class)
							.addSyncDatapoint(resultDatapointAddress, resultDatapointAddress, serviceAgentName, SyncMode.push)));

			String result = cellControlSubscriber.getCommunicator().queryDatapoints(queryDatapointAddress, new JsonPrimitive("SELECT * FILESERVER"), serviceAgentName, resultDatapointAddress, serviceAgentName, 10000).getValueAsString();
			log.debug("Received back from query={}", result);

			// String result =
			// cellControlSubscriber.readLocalDatapoint(resultDatapoint).getValueAsString();

			log.debug("correct value={}, actual value={}", "FINISHED", result);

			assertEquals(result, expectedResult);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}
	}

	// /**
	// * Idea: Create a reader behaviour that reads 2 values from 2 different
	// agents. It has the "state=1". 2 agents provide 2 datapoints with double
	// values. The reader behaviour sends read requests to all these agents and
	// * sets a status datapoint to 2 . The 2 agents respond. If all 3 messages
	// arrive, a process behaviour is triggered "state=2". It sums the operands
	// and sends the result to a 3rd agent. If the sum is correct, the test is
	// passed.
	// *
	// *
	// */
	// //@Test
	// public void singleReadtest() {
	// try {
	// String stateaddress = "agent.state";
	// String triggeraddress = "agent.trigger";
	// String operand1address = "agent.operand1";
	// String operand2address = "agent.operand2";
	// String resultAddress = "agent.result";
	//
	// //Create Database agents 1-2
	// CellConfig dbAgent1 = CellConfig.newConfig("dbagent1");
	// this.launchUtil.createAgent(dbAgent1);
	// CellConfig dbAgent2 = CellConfig.newConfig("dbagent2");
	// this.launchUtil.createAgent(dbAgent2);
	// CellConfig resultAgent = CellConfig.newConfig("resultagent");
	// this.launchUtil.createAgent(resultAgent);
	//
	// //Create the calculator agent
	// //Create the basic information for any agent
	// CellConfig additionAgent = CellConfig.newConfig("AdditionAgent");
	//
	//
	//
	//
	//
	// //Create conditions that can be used in the agents, only the name of the
	// condition and their classes
	// //Readerconditions
	// additionAgent.addCondition(ConditionConfig.newConfig("starttrigger",
	// ConditionHasValue.class.getName())
	// .setProperty("comparestring", "START"));
	//
	// //Create behaviours that will be used by the agents
	// //Add the reader
	// additionAgent.addBehaviour(BehaviourConfigJadeBehaviour.newConfig("S1",
	// AdditionFunction.class.getName())
	// .setProperty("op1agent", "dbagent1")
	// .setProperty("op1address", operand1address)
	// .setProperty("op2agent", "dbagent2")
	// .setProperty("op2address", operand2address)
	// .setProperty("successstateid", "OK")
	// .setProperty("stateaddress", stateaddress));
	//
	// //Add activators
	// //Add reader activator
	// additionAgent.addActivator(ActivatorConfigJadeBehaviour.newConfig("T0").setBehaviour("S1").setActivatorLogic("")
	// .addMapping(triggeraddress, "starttrigger"));
	//
	// this.commUtil.createAgent(additionAgent);
	//
	//
	// //Create result receiver agent
	// CellConfigJadeBehaviour receiverAgent =
	// CellConfigJadeBehaviour.newConfig("receiveragent",
	// "at.tuwien.ict.acona.cell.core.CellImpl");
	// this.commUtil.createAgent(receiverAgent);
	//
	// //subscribe the result without timeout
	// this.comm.subscribeDatapoint("receiveragent", resultAddress);
	//
	// //Write the numbers in the database agents
	// this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver("dbagent1").setContent(Datapoint.newDatapoint(operand1address).setValue(new
	// JsonPrimitive(11.))).setService(AconaServiceType.WRITE));
	//
	// //Trigger the calculator agent
	// this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver("AdditionAgent").setContent(Datapoint.newDatapoint(triggeraddress).setValue(new
	// JsonPrimitive("START"))).setService(AconaServiceType.WRITE));
	//
	//
	// synchronized (this) {
	// try {
	// this.wait(2000);
	// } catch (InterruptedException e) {
	//
	// }
	// }
	//
	// //Get the result from the result receiver agent
	// double actualResult = this.comm.getDatapointFromAgent(100000,
	// true).getValue().getAsDouble();
	//
	// log.debug("correct value={}, actual value={}", 33, actualResult);
	//
	// assertEquals(33, actualResult, 0.0);
	// log.info("Test passed");
	// } catch (Exception e) {
	// log.error("Error testing system", e);
	// fail("Error");
	// }
	//
	// }

	// /**
	// * Idea: Create a reader behaviour that reads 2 values from 2 different
	// agents. It has the "state=1". 2 agents provide 2 datapoints with double
	// values. The reader behaviour sends read requests to all these agents and
	// * sets a status datapoint to 2 . The 2 agents respond. If all 3 messages
	// arrive, a process behaviour is triggered "state=2". It sums the operands
	// and sends the result to a 3rd agent. If the sum is correct, the test is
	// passed.
	// *
	// *
	// */
	// @Test
	// public void asynchronousReadtest() {
	// try {
	// String stateaddress = "agent.state";
	// String triggeraddress = "agent.trigger";
	// String operand1address = "agent.operand1";
	// String operand2address = "agent.operand2";
	// String resultAddress = "agent.result";
	//
	// //Create Database agents 1-2
	// CellConfigJadeBehaviour dbAgent1 =
	// CellConfigJadeBehaviour.newConfig("dbagent1", CellImpl.class.getName());
	// this.commUtil.createAgent(dbAgent1);
	// CellConfigJadeBehaviour dbAgent2 =
	// CellConfigJadeBehaviour.newConfig("dbagent2",
	// "at.tuwien.ict.acona.cell.core.CellImpl");
	// this.commUtil.createAgent(dbAgent2);
	//
	//
	//
	// //Create the calculator agent
	// //Create the basic information for any agent
	// CellConfigJadeBehaviour additionAgent =
	// CellConfigJadeBehaviour.newConfig("AdditionAgent",
	// "at.tuwien.ict.acona.cell.core.CellImpl");
	//
	// //Create conditions that can be used in the agents, only the name of the
	// condition and their classes
	// //Readerconditions
	// additionAgent.addCondition(ConditionConfig.newConfig("starttrigger",
	// ConditionHasValue.class.getName())
	// .setProperty("comparestring", "START"));
	//
	// //Addition agent conditions
	// additionAgent.addCondition(ConditionConfig.newConfig("operand1",
	// ConditionIsNotEmpty.class.getName())); //Names can be added directly by
	// the classes too
	// additionAgent.addCondition(ConditionConfig.newConfig("operand2",
	// "at.tuwien.ict.acona.cell.activator.conditions.ConditionIsNotEmpty"));
	//
	// //Resultsender conditions
	// additionAgent.addCondition(ConditionConfig.newConfig("receivername",
	// ConditionIsNotEmpty.class.getName()));
	// additionAgent.addCondition(ConditionConfig.newConfig("datapointsource",
	// ConditionIsNotEmpty.class.getName()));
	//
	// //Create behaviours that will be used by the agents
	// //Add the reader
	// additionAgent.addBehaviour(BehaviourConfigJadeBehaviour.newConfig("S1",
	// AdditionFunction.class.getName())
	// .setProperty("op1agent", "dbagent1")
	// .setProperty("op1address", operand1address)
	// .setProperty("op2agent", "dbagent2")
	// .setProperty("op2address", operand2address)
	// .setProperty("successstateid", "OK")
	// .setProperty("stateaddress", stateaddress));
	//
	// //Add the addition itself
	// additionAgent.addBehaviour(BehaviourConfigJadeBehaviour.newConfig("S2",
	// "at.tuwien.ict.acona.cell.core.helpers.AdditionBehaviour")
	// .setProperty("operand1", operand1address)
	// .setProperty("operand2", operand2address)
	// .setProperty("result", resultAddress));
	//
	// //Add the sender
	// additionAgent.addBehaviour(BehaviourConfigJadeBehaviour.newConfig("S3",
	// SendAsynchronousBehaviour.class.getName())
	// .setProperty("receivernameaddress", "s3.receiveragent")
	// .setProperty("datapointsourceaddress", resultAddress)
	// .setProperty("datapointtargetaddress", resultAddress)
	// .setProperty("aconaserviceaddress", "s3.service")
	// .setProperty("defaultservice", "WRITE"));
	//
	// //Add activators
	// //Add reader activator
	// additionAgent.addActivator(ActivatorConfigJadeBehaviour.newConfig("T0").setBehaviour("S1").setActivatorLogic("")
	// .addMapping(triggeraddress, "starttrigger"));
	//
	// //Add addition activator
	// additionAgent.addActivator(ActivatorConfigJadeBehaviour.newConfig("T1").setBehaviour("S2").setActivatorLogic("")
	// .addMapping(operand1address, "operand1")
	// .addMapping(operand2address, "operand2"));
	//
	// //Add sender activator
	// additionAgent.addActivator(ActivatorConfigJadeBehaviour.newConfig("T2").setBehaviour("S3")
	// .addMapping("s3.receiveragent", "receivername")
	// .addMapping(resultAddress, "datapointsource"));
	//
	// this.commUtil.createAgent(additionAgent);
	//
	//
	// //Create result receiver agent
	// CellConfigJadeBehaviour receiverAgent =
	// CellConfigJadeBehaviour.newConfig("receiveragent",
	// "at.tuwien.ict.acona.cell.core.CellImpl");
	// this.commUtil.createAgent(receiverAgent);
	//
	// //subscribe the result without timeout
	// this.comm.subscribeDatapoint("receiveragent", resultAddress);
	//
	// //Write the numbers in the database agents
	// this.comm.sendSynchronousMessageToAgent(Message.newMessage().addReceiver("dbagent1").setContent(Datapoint.newDatapoint(operand1address).setValue(new
	// JsonPrimitive(11.))).setService(AconaServiceType.WRITE));
	// this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver("dbagent2").setContent(Datapoint.newDatapoint(operand2address).setValue(new
	// JsonPrimitive(22.))).setService(AconaServiceType.WRITE));
	// this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver("AdditionAgent").setContent(Datapoint.newDatapoint("s3.receiveragent").setValue(new
	// JsonPrimitive("receiveragent"))).setService(AconaServiceType.WRITE));
	//
	// //Trigger the calculator agent
	// this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver("AdditionAgent").setContent(Datapoint.newDatapoint(triggeraddress).setValue(new
	// JsonPrimitive("START"))).setService(AconaServiceType.WRITE));
	//
	//
	//// synchronized (this) {
	//// try {
	//// this.wait(2000);
	//// } catch (InterruptedException e) {
	////
	//// }
	//// }
	//
	// //Get the result from the result receiver agent
	// double actualResult = this.comm.getDatapointFromAgent(100000,
	// true).getValue().getAsDouble();
	//
	// log.debug("correct value={}, actual value={}", 33, actualResult);
	//
	// assertEquals(33, actualResult, 0.0);
	// log.info("Test passed");
	// } catch (Exception e) {
	// log.error("Error testing system", e);
	// fail("Error");
	// }
	//
	// }

	// @Test
	// public void sendMessageOrderingtest() {
	// try {
	// String readAddress = "storageagent.data.value";
	// String triggerAddress = "readeragent.data.command";
	// String resultAddress = "data.result";
	// int databaseValue = 12345;
	// int expectedValue = 12345;
	//
	// //Create config JSON for reader agent
	// CellConfig cellreader = CellConfig.newConfig("SenderAgent",
	// CustomTestCell.class.getName());
	// cellreader.setClass(CustomTestCell.class);
	// cellreader.addProperty("targetcell", "TesterAgent");
	//
	// //Create agent in the system
	// Object[] args = new Object[1];
	// args[0] = cellreader.toJsonObject();
	// AgentController senderAgent = this.util.createAgent(cellreader.getName(),
	// cellreader.getClassToInvoke(), args, agentContainer);
	//
	// log.debug("State={}", senderAgent.getState());
	//
	//
	// //Create config JSON for storage agent
	// CellConfig cellstorage = CellConfig.newConfig("TesterAgent",
	// CustomTestCell.class.toString());
	// cellstorage.setClass(InspectorCell.class);
	//
	// //Create cell inspector controller for the subscriber
	// InspectorCellClient externalController = new InspectorCellClient();
	// Object[] argsPublisher = new Object[2];
	// argsPublisher[0] = cellstorage.toJsonObject();
	// argsPublisher[1] = externalController;
	// //Create agent in the system
	// AgentController agentController =
	// this.util.createAgent(cellstorage.getName(),
	// cellstorage.getClassToInvoke(), argsPublisher, agentContainer);
	//
	// log.debug("State={}", agentController.getState());
	//
	// //Write databasevalue directly into the storage
	// externalController.getCell().getDataStorage().write(Datapoint.newDatapoint(readAddress).setValue(new
	// JsonPrimitive(databaseValue)), null);
	//
	// //=== Start the system ===//
	// this.comm.subscribeDatapoint("ReaderAgent", readAddress);
	//
	// //Send Write command
	// Message writeMessage = Message.newMessage()
	// .addReceiver("ReaderAgent")
	// .setContent(Datapoint.newDatapoint(triggerAddress).setValue("START").toJsonObject())
	// .setService(AconaService.WRITE);
	//
	// Message ack = this.comm.sendSynchronousMessageToAgent(writeMessage,
	// 10000);
	// log.debug("Tester: Acknowledge of cell writing recieved={}", ack);
	//
	// //Subscribe the result
	// double actualResult = this.comm.getDatapointFromAgent(20000,
	// true).getValue().getAsInt();
	// //actualResult = this.comm.getDatapointFromAgent(20000,
	// false).getValue().getAsInt();
	//
	// //this.myAgent.send(ACLUtils.convertToACL(Message.newMessage().addReceiver(msg.getSender().getLocalName()).setService(AconaService.READ).setContent(Datapoint.newDatapoint("test"))));
	//
	// log.debug("correct value={}, actual value={}", expectedValue,
	// actualResult);
	//
	// assertEquals(true, true);
	// log.info("Test passed");
	// } catch (Exception e) {
	// log.error("Error testing system", e);
	// fail("Error");
	// }
	// }

}
