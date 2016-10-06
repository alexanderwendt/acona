package at.tuwien.ict.acona.cell.core.cellfunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.core.CellGatewayImpl;
import at.tuwien.ict.acona.cell.core.cellfunction.helpers.CFIncrementService;
import at.tuwien.ict.acona.cell.core.cellfunction.helpers.SequenceController;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.jadelauncher.util.KoreExternalControllerImpl;
import jade.core.Runtime;

public class AconaServiceTester {
	private static Logger log = LoggerFactory.getLogger(AconaServiceTester.class);
	private KoreExternalControllerImpl launcher = KoreExternalControllerImpl.getLauncher();

	@Before
	public void setUp() throws Exception {
		try {
			// Create container
			log.debug("Create or get main container");
			this.launcher.createMainContainer("localhost", 1099, "MainContainer");

			log.debug("Create subcontainer");
			this.launcher.createSubContainer("localhost", 1099, "Subcontainer");

			// log.debug("Create gui");
			// this.commUtil.createDebugUserInterface();

			// Create gateway
			// commUtil.initJadeGateway();

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
	}

	/**
	 * Idea: Create an agent with the following behaviours (not jade): A
	 * controller runs every 5s. It starts a getDataFunction. When the data has
	 * been received, the publish data function is executed. Data is read from
	 * another dummy agent, which acts as a memory In the "Drivetrack-Agent", 2
	 * values are read from a memory agent, added and published within the
	 * agent. The result is subscribed by an output agent The Outbuffer is only
	 * an empty mock, which is used as a gateway
	 * 
	 */
	@Test
	public void externalControllerWithDatabaseCellsAndAdditionCellTest() {
		try {
			String COMMANDDATAPOINTNAME = "command";
			String STATUSDATAPOINTNAME = "status";
			String INCREMENTATIONDATAPOINTNAME = "increment";

			String controllerFunctionName = "controller";

			// define all datapoints that shall be used
			String processDatapoint = "memory.value"; // put into memory mock
														// agent

			String agentName1 = "AgentIncrementService1";
			String agentName2 = "AgentIncrementService2";
			String agentName3 = "AgentIncrementService3";

			String ServiceName = "Increment"; // The same name for all services

			String controllerAgentName = "IncrementController";
			String memoryAgentName = "MemoryAgent";

			// values
			double startValue = 0;
			int expectedResult = 3;

			// Memory
			CellGatewayImpl memoryAgent = this.launcher.createAgent(CellConfig.newConfig(memoryAgentName));

			// Controller
			CellConfig controllerAgentConfig = CellConfig.newConfig(controllerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(SequenceController.class)
							.setProperty("agent1", agentName1).setProperty("agent2", agentName2)
							.setProperty("agent3", agentName3).setProperty("servicename", ServiceName)
							.setProperty("delay", "1000").addSyncDatapoint(
									DatapointConfig.newConfig(COMMANDDATAPOINTNAME, COMMANDDATAPOINTNAME, "", "push")));
			CellGatewayImpl controller = this.launcher.createAgent(controllerAgentConfig);

			controller.getCommunicator().write(Datapoint.newDatapoint("Test"), memoryAgentName);
			// controller.subscribeForeignDatapoint(processDatapoint,
			// memoryAgentName);

			// Create services
			CellConfig serviceAgent1 = CellConfig.newConfig(agentName1)
					.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
							.addSyncDatapoint(DatapointConfig.newConfig(INCREMENTATIONDATAPOINTNAME, processDatapoint,
									memoryAgentName, "push")));
			CellGatewayImpl service1 = this.launcher.createAgent(serviceAgent1);

			CellConfig serviceAgent2 = CellConfig.newConfig(agentName2)
					.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
							.addSyncDatapoint(DatapointConfig.newConfig(INCREMENTATIONDATAPOINTNAME, processDatapoint,
									memoryAgentName, "push")));
			CellGatewayImpl service2 = this.launcher.createAgent(serviceAgent2);

			CellConfig serviceAgent3 = CellConfig.newConfig(agentName3)
					.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
							.addSyncDatapoint(DatapointConfig.newConfig(INCREMENTATIONDATAPOINTNAME, processDatapoint,
									memoryAgentName, "push")));
			CellGatewayImpl service3 = this.launcher.createAgent(serviceAgent3);

			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}
			log.info("=== All agents initialized ===");

			memoryAgent.writeLocalDatapoint(
					Datapoint.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			log.info("Datapoints on the way");
			memoryAgent.writeLocalDatapoint(
					Datapoint.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			// Start the system by setting start
			Datapoint state = controller.getCommunicator().query(
					Datapoint.newDatapoint(COMMANDDATAPOINTNAME).setValue(ControlCommand.START.toString()),
					controller.getCell().getLocalName(), Datapoint.newDatapoint("state"),
					controller.getCell().getLocalName(), 10000);

			// Write the numbers in the database agents
			// client1.writeLocalDatapoint(Datapoint.newDatapoint(memorydatapoint1).setValue(String.valueOf(value1)));
			// client2.writeLocalDatapoint(Datapoint.newDatapoint(memorydatapoint2).setValue(String.valueOf(value2)));
			//
			// //Query the service with start and then get the status
			// //Set default timeout to a high number to be able to debug
			// controlAgent.getCommunicator().setDefaultTimeout(100000);
			// log.debug("Execute query");
			// Datapoint resultState =
			// controlAgent.getCommunicator().query(Datapoint.newDatapoint(commandDatapoint).setValue(new
			// JsonPrimitive(ControlCommand.START.toString())),
			// additionAgentName, Datapoint.newDatapoint(STATUSDATAPOINTNAME),
			// additionAgentName, 100000);
			// log.debug("Query executed with result={}", resultState);
			//
			// double sum = controlAgent.getCommunicator().read(resultdatapoint,
			// outputmemoryAgentName).getValue().getAsJsonPrimitive().getAsDouble();
			// client1.getCell().getCommunicator().write(Datapoint.newDatapoint(commandDatapoint).setValue(new
			// JsonPrimitive("START")), drivetrackAgentName);
			// this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver(drivetrackAgentName).setContent(Datapoint.newDatapoint(commandDatapoint).setValue(new
			// JsonPrimitive("START"))).setService(AconaServiceType.WRITE));

			// synchronized (this) {
			// try {
			// this.wait(6000);
			// } catch (InterruptedException e) {
			//
			// }
			// }

			// client1.getDataStorage().write(Datapoint.newDatapoint(memorydatapoint1).setValue(String.valueOf(value1+1)),
			// "nothing");
			// client1.getDataStorage().write(Datapoint.newDatapoint(memorydatapoint2).setValue(String.valueOf(value2+2)),
			// "nothing");

			// client1.getCell().getCommunicator().write(Datapoint.newDatapoint(commandDatapoint).setValue(new
			// JsonPrimitive("START")), drivetrackAgentName);
			// this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver(drivetrackAgentName).setContent(Datapoint.newDatapoint(commandDatapoint).setValue(new
			// JsonPrimitive("START"))).setService(AconaServiceType.WRITE));

			// Get the result from the result receiver agent
			// String result =
			// client2.getCommunicator().read(resultdatapoint).getValueAsString();
			double result = memoryAgent.getCommunicator().read(processDatapoint).getValue().getAsDouble();

			log.debug("correct value={}, actual value={}", expectedResult, result);

			assertEquals(result, expectedResult, 0.0);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

	/**
	 * Idea: Create an agent with the following behaviours (not jade): A
	 * controller runs every 5s. It starts a getDataFunction. When the data has
	 * been received, the publish data function is executed. Data is read from
	 * another dummy agent, which acts as a memory In the "Drivetrack-Agent", 2
	 * values are read from a memory agent, added and published within the
	 * agent. The result is subscribed by an output agent The Outbuffer is only
	 * an empty mock, which is used as a gateway
	 * 
	 */
	@Test
	public void aconaServiceWithFullControlReadDatapointsTest() {
		try {
			String COMMANDDATAPOINTNAME = "command";
			String STATUSDATAPOINTNAME = "status";
			String INCREMENTATIONDATAPOINTNAME = "increment";

			String controllerFunctionName = "controller";

			// define all datapoints that shall be used
			String processDatapoint = "memory.value"; // put into memory mock
														// agent

			String agentName1 = "AgentIncrementService1";
			String agentName2 = "AgentIncrementService2";
			String agentName3 = "AgentIncrementService3";

			String ServiceName = "Increment"; // The same name for all services

			String controllerAgentName = "IncrementController";
			String memoryAgentName = "MemoryAgent";

			// values
			double startValue = 0;
			int expectedResult = 3;

			// Memory
			CellGatewayImpl memoryAgent = this.launcher.createAgent(CellConfig.newConfig(memoryAgentName));

			// Controller
			CellConfig controllerAgentConfig = CellConfig.newConfig(controllerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(SequenceController.class)
							.setProperty("agent1", agentName1).setProperty("agent2", agentName2)
							.setProperty("agent3", agentName3).setProperty("servicename", ServiceName)
							.setProperty("delay", "1").addSyncDatapoint(
									DatapointConfig.newConfig(COMMANDDATAPOINTNAME, COMMANDDATAPOINTNAME, "push")));
			CellGatewayImpl controller = this.launcher.createAgent(controllerAgentConfig);

			controller.getCommunicator().write(Datapoint.newDatapoint("Test"), memoryAgentName);
			// controller.subscribeForeignDatapoint(processDatapoint,
			// memoryAgentName);

			// Create services
			CellConfig serviceAgent1 = CellConfig.newConfig(agentName1)
					.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
							.addSyncDatapoint(INCREMENTATIONDATAPOINTNAME, processDatapoint, memoryAgentName, "pull"));
			CellGatewayImpl service1 = this.launcher.createAgent(serviceAgent1);

			CellConfig serviceAgent2 = CellConfig.newConfig(agentName2)
					.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
							.addSyncDatapoint(DatapointConfig.newConfig(INCREMENTATIONDATAPOINTNAME, processDatapoint,
									memoryAgentName, "pull")));
			CellGatewayImpl service2 = this.launcher.createAgent(serviceAgent2);

			CellConfig serviceAgent3 = CellConfig.newConfig(agentName3)
					.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
							.addSyncDatapoint(DatapointConfig.newConfig(INCREMENTATIONDATAPOINTNAME, processDatapoint,
									memoryAgentName, "pull")));
			CellGatewayImpl service3 = this.launcher.createAgent(serviceAgent3);

			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}
			log.info("=== All agents initialized ===");

			memoryAgent.writeLocalDatapoint(
					Datapoint.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			log.info("Datapoints on the way");
			// memoryAgent.writeLocalDatapoint(Datapoint.newDatapoint(processDatapoint).setValue(new
			// JsonPrimitive(startValue)));
			// Start the system by setting start
			Datapoint state = controller.getCommunicator().query(
					Datapoint.newDatapoint(COMMANDDATAPOINTNAME).setValue(ControlCommand.START.toString()),
					controller.getCell().getLocalName(), Datapoint.newDatapoint("state"),
					controller.getCell().getLocalName(), 100000);
			log.debug("Received state={}", state);

			// Write the numbers in the database agents
			// client1.writeLocalDatapoint(Datapoint.newDatapoint(memorydatapoint1).setValue(String.valueOf(value1)));
			// client2.writeLocalDatapoint(Datapoint.newDatapoint(memorydatapoint2).setValue(String.valueOf(value2)));
			//
			// //Query the service with start and then get the status
			// //Set default timeout to a high number to be able to debug
			// controlAgent.getCommunicator().setDefaultTimeout(100000);
			// log.debug("Execute query");
			// Datapoint resultState =
			// controlAgent.getCommunicator().query(Datapoint.newDatapoint(commandDatapoint).setValue(new
			// JsonPrimitive(ControlCommand.START.toString())),
			// additionAgentName, Datapoint.newDatapoint(STATUSDATAPOINTNAME),
			// additionAgentName, 100000);
			// log.debug("Query executed with result={}", resultState);
			//
			// double sum = controlAgent.getCommunicator().read(resultdatapoint,
			// outputmemoryAgentName).getValue().getAsJsonPrimitive().getAsDouble();
			// client1.getCell().getCommunicator().write(Datapoint.newDatapoint(commandDatapoint).setValue(new
			// JsonPrimitive("START")), drivetrackAgentName);
			// this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver(drivetrackAgentName).setContent(Datapoint.newDatapoint(commandDatapoint).setValue(new
			// JsonPrimitive("START"))).setService(AconaServiceType.WRITE));

			// synchronized (this) {
			// try {
			// this.wait(6000);
			// } catch (InterruptedException e) {
			//
			// }
			// }

			double result = memoryAgent.getCommunicator().read(processDatapoint).getValue().getAsDouble();

			log.debug("correct value={}, actual value={}", expectedResult, result);

			assertEquals(result, expectedResult, 0.0);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

}
