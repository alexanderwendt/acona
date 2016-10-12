package at.tuwien.ict.acona.cell.core.cellfunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.config.SystemConfig;
import at.tuwien.ict.acona.cell.core.CellGateway;
import at.tuwien.ict.acona.cell.core.CellGatewayImpl;
import at.tuwien.ict.acona.cell.core.cellfunction.helpers.CFIncrementService;
import at.tuwien.ict.acona.cell.core.cellfunction.helpers.LoopController;
import at.tuwien.ict.acona.cell.core.cellfunction.helpers.SequenceController;
import at.tuwien.ict.acona.cell.core.cellfunction.helpers.SimpleControllerService;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.framework.interfaces.ControllerCellGateway;
import at.tuwien.ict.acona.framework.interfaces.ControllerWrapper;
import at.tuwien.ict.acona.framework.modules.ServiceState;
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
									DatapointConfig.newConfig(COMMANDDATAPOINTNAME, COMMANDDATAPOINTNAME, SyncMode.push)));
			CellGatewayImpl controller = this.launcher.createAgent(controllerAgentConfig);

			controller.getCommunicator().write(Datapoint.newDatapoint("Test"), memoryAgentName);
			// controller.subscribeForeignDatapoint(processDatapoint,
			// memoryAgentName);

			// Create services
			CellConfig serviceAgent1 = CellConfig.newConfig(agentName1)
					.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
							.addSyncDatapoint(DatapointConfig.newConfig(INCREMENTATIONDATAPOINTNAME, processDatapoint,
									memoryAgentName, SyncMode.push)));
			CellGatewayImpl service1 = this.launcher.createAgent(serviceAgent1);

			CellConfig serviceAgent2 = CellConfig.newConfig(agentName2)
					.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
							.addSyncDatapoint(DatapointConfig.newConfig(INCREMENTATIONDATAPOINTNAME, processDatapoint,
									memoryAgentName, SyncMode.push)));
			CellGatewayImpl service2 = this.launcher.createAgent(serviceAgent2);

			CellConfig serviceAgent3 = CellConfig.newConfig(agentName3)
					.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
							.addSyncDatapoint(DatapointConfig.newConfig(INCREMENTATIONDATAPOINTNAME, processDatapoint,
									memoryAgentName, SyncMode.push)));
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
					controller.getCell().getLocalName(), 1000000);

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

			// Use a system config to init the whole system
			SystemConfig totalConfig = SystemConfig.newConfig()
					.addController(CellConfig.newConfig(controllerAgentName)
							.addCellfunction(CellFunctionConfig.newConfig("controllerservice", SequenceController.class)
									.setProperty("agent1", agentName1).setProperty("agent2", agentName2)
									.setProperty("agent3", agentName3).setProperty("servicename", ServiceName)
									.setProperty("delay", "1").addSyncDatapoint(
											DatapointConfig.newConfig(COMMANDDATAPOINTNAME, COMMANDDATAPOINTNAME, SyncMode.push))))
					.addMemory(CellConfig.newConfig(memoryAgentName))
					.addService(CellConfig.newConfig(agentName1)
							.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
									.addSyncDatapoint(INCREMENTATIONDATAPOINTNAME, processDatapoint, memoryAgentName, SyncMode.pull)))
					.addService(CellConfig.newConfig(agentName2)
							.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
									.addSyncDatapoint(INCREMENTATIONDATAPOINTNAME, processDatapoint, memoryAgentName, SyncMode.pull)))
					.addService(CellConfig.newConfig(agentName3)
							.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
									.addSyncDatapoint(INCREMENTATIONDATAPOINTNAME, processDatapoint, memoryAgentName, SyncMode.pull)))
					.setTopController(controllerAgentName);

			// this.launcher.createDebugUserInterface();

			this.launcher.init(totalConfig);

			// // Memory
			// CellGatewayImpl memoryAgent =
			// this.launcher.createAgent(CellConfig.newConfig(memoryAgentName));
			//
			// // Controller
			// CellConfig controllerAgentConfig =
			// CellConfig.newConfig(controllerAgentName)
			// .addCellfunction(CellFunctionConfig.newConfig(SequenceController.class)
			// .setProperty("agent1", agentName1).setProperty("agent2",
			// agentName2)
			// .setProperty("agent3", agentName3).setProperty("servicename",
			// ServiceName)
			// .setProperty("delay", "1").addSyncDatapoint(
			// DatapointConfig.newConfig(COMMANDDATAPOINTNAME,
			// COMMANDDATAPOINTNAME, SyncMode.push)));
			// CellGatewayImpl controller =
			// this.launcher.createAgent(controllerAgentConfig);
			//
			// controller.getCommunicator().write(Datapoint.newDatapoint("Test"),
			// memoryAgentName);
			// // controller.subscribeForeignDatapoint(processDatapoint,
			// // memoryAgentName);
			//
			// // Create services
			// CellConfig serviceAgent1 = CellConfig.newConfig(agentName1)
			// .addCellfunction(CellFunctionConfig.newConfig(ServiceName,
			// CFIncrementService.class)
			// .addSyncDatapoint(INCREMENTATIONDATAPOINTNAME, processDatapoint,
			// memoryAgentName, SyncMode.pull));
			// CellGatewayImpl service1 =
			// this.launcher.createAgent(serviceAgent1);
			//
			// CellConfig serviceAgent2 = CellConfig.newConfig(agentName2)
			// .addCellfunction(CellFunctionConfig.newConfig(ServiceName,
			// CFIncrementService.class)
			// .addSyncDatapoint(DatapointConfig.newConfig(INCREMENTATIONDATAPOINTNAME,
			// processDatapoint,
			// memoryAgentName, SyncMode.pull)));
			// CellGatewayImpl service2 =
			// this.launcher.createAgent(serviceAgent2);
			//
			// CellConfig serviceAgent3 = CellConfig.newConfig(agentName3)
			// .addCellfunction(CellFunctionConfig.newConfig(ServiceName,
			// CFIncrementService.class)
			// .addSyncDatapoint(DatapointConfig.newConfig(INCREMENTATIONDATAPOINTNAME,
			// processDatapoint,
			// memoryAgentName, SyncMode.pull)));
			// CellGatewayImpl service3 =
			// this.launcher.createAgent(serviceAgent3);

			// synchronized (this) {
			// try {
			// this.wait(1000);
			// } catch (InterruptedException e) {
			//
			// }
			// }
			log.info("=== All agents initialized ===");

			launcher.getAgent(memoryAgentName).writeLocalDatapoint(Datapoint.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			log.info("Datapoints on the way");
			// memoryAgent.writeLocalDatapoint(Datapoint.newDatapoint(processDatapoint).setValue(new
			// JsonPrimitive(startValue)));
			// Start the system by setting start

			CellGateway controller = launcher.getTopController();

			// Test the wrapper for controllers too
			ControllerCellGateway controllerCellGateway = new ControllerWrapper(controller);

			Datapoint state = controller.getCommunicator().query(
					Datapoint.newDatapoint(COMMANDDATAPOINTNAME).setValue(ControlCommand.START.toString()), Datapoint.newDatapoint("state"), 100000);

			// controllerCellGateway.executeService("", "controllerservice", new
			// JsonObject(), 10000);

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

			double result = launcher.getAgent(memoryAgentName).getCommunicator().read(processDatapoint).getValue().getAsDouble();

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
	public void AconaServiceStartsAconaService() {
		try {
			log.info("=== Test AconaServiceStartsAconaService ===");

			final String INCREMENTATIONDATAPOINTNAME = "increment";

			// define all datapoints that shall be used
			String processDatapoint = "memory.value"; // put into memory mock
														// agent

			// === Agent names ===//
			String serviceAgentName = "IncrementServiceAgent";
			String controllerAgentName = "ControllerAgent";
			String memoryAgentName = "MemoryAgent";

			// === Function names ===//
			String serviceName = "IncrementService"; // The same name for all
														// services
			String controllerServiceName = "controllerservice";

			// === Values ===//
			double startValue = 0;
			int expectedResult = 1;

			// === Config ===//
			SystemConfig totalConfig = SystemConfig.newConfig();
			totalConfig.addController(CellConfig.newConfig(controllerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(controllerServiceName, SimpleControllerService.class)
							.setProperty("agentname", serviceAgentName)
							.setProperty("servicename", serviceName)
							.setProperty("delay", "10")));

			totalConfig.addMemory(CellConfig.newConfig(memoryAgentName));
			totalConfig.setTopController(controllerAgentName);

			totalConfig.addService(CellConfig.newConfig(serviceAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(serviceName, CFIncrementService.class)
							.addSyncDatapoint(INCREMENTATIONDATAPOINTNAME, processDatapoint, memoryAgentName, SyncMode.pull)));

			// === System initialization ===//

			// this.launcher.createDebugUserInterface();

			this.launcher.init(totalConfig);
			CellGateway topController = launcher.getTopController();
			topController.getCommunicator().setDefaultTimeout(100000);
			// Set start values
			launcher.getAgent(memoryAgentName).writeLocalDatapoint(Datapoint.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));

			// }
			// log.info("=== All agents initialized ===");

			log.info("=== System initialized ===");
			// === System operation ===//

			Datapoint resultState = topController.getCommunicator().query(Datapoint.newDatapoint(controllerServiceName + ".command").setValue(ControlCommand.START.toString()),
					Datapoint.newDatapoint(controllerServiceName + ".state"), 100000);

			log.info("=== System operation finished. Extract results ===");
			// === Extract results ===//
			log.debug("Received state={}", resultState);

			// Read from memory
			Datapoint memoryDatapoint = launcher.getAgent(memoryAgentName).getCommunicator().read(processDatapoint);
			double result = memoryDatapoint.getValue().getAsDouble();

			log.info("correct value={}, actual value={}", expectedResult, result);

			assertEquals(result, expectedResult, 0.0);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

	/**
	 * In this test, one controller will start 100 increment services in a
	 * sequence. The incrementservices increases the number in the memory with
	 * +1. At the end the number in the memory shall be the same as the number
	 * of services in the system.
	 * 
	 */
	@Test
	public void aconaServiceIncrementorCountTo100() {
		try {
			log.info("=== Test AconaServiceStartsAconaService ===");

			// === Agent names ===//
			String serviceAgentName = "IncrementServiceAgent";
			String controllerAgentName = "ControllerAgent";
			String memoryAgentName = "MemoryAgent";

			// === Function names ===//
			String controllerServiceName = "controllerservice";

			String serviceName = "IncrementService"; // The same name for all services
			final String IncrementFunctionDatapointID = "increment";

			// === Datappointnames ===//
			String processDatapoint = "memory.value"; // put into memory mock agent

			// === Values ===//
			int numberOfAgents = 100;

			// values
			double startValue = 0;
			int expectedResult = numberOfAgents;

			// === Config ===//
			//Create total config
			SystemConfig totalConfig = SystemConfig.newConfig();

			//Add controller
			totalConfig.addController(CellConfig.newConfig(controllerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(controllerServiceName, LoopController.class)
							.setProperty("agentnameprefix", serviceAgentName)
							.setProperty("servicename", serviceName)
							.setProperty("numberofagents", String.valueOf(numberOfAgents))
							.setProperty("delay", "1")));

			//Add memory
			totalConfig.addMemory(CellConfig.newConfig(memoryAgentName));
			totalConfig.setTopController(controllerAgentName);

			//Add services
			for (int i = 1; i <= numberOfAgents; i++) {
				totalConfig.addService(CellConfig.newConfig(serviceAgentName + i)
						.addCellfunction(CellFunctionConfig.newConfig(serviceName, CFIncrementService.class)
								.addSyncDatapoint(IncrementFunctionDatapointID, processDatapoint, memoryAgentName, SyncMode.pull)));
			}

			// this.launcher.createDebugUserInterface();

			this.launcher.init(totalConfig);

			// }
			// log.info("=== All agents initialized ===");

			launcher.getAgent(memoryAgentName).writeLocalDatapoint(Datapoint.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			log.info("Datapoints on the way. Start system");
			// memoryAgent.writeLocalDatapoint(Datapoint.newDatapoint(processDatapoint).setValue(new
			// JsonPrimitive(startValue)));
			// Start the system by setting start

			// this.launcher.getAgent("AgentIncrementService1").getCommunicator().write(Datapoint.newDatapoint("Increment.command").setValue(ControlCommand.START.toString()));

			CellGateway controller = launcher.getTopController();

			// controller.getCommunicator().query(Datapoint.newDatapoint("Increment.command").setValue(ControlCommand.START.toString()),
			// agentName + 1, Datapoint.newDatapoint("Increment.state"),
			// agentName + 1, 10000);

			//			controller.getCommunicator().query(Datapoint.newDatapoint(controllerServiceName + ".command").setValue(ControlCommand.START.toString()),
			//					Datapoint.newDatapoint(controllerServiceName + ".state"), 10000);

			// Test the wrapper for controllers too
			ControllerCellGateway controllerCellGateway = new ControllerWrapper(controller);
			ServiceState state = controllerCellGateway.executeService(controllerServiceName, new JsonObject(), 1000000);

			log.debug("Received state={}", state);

			double result = launcher.getAgent(memoryAgentName).getCommunicator().read(processDatapoint).getValue().getAsDouble();

			log.debug("correct value={}, actual value={}", expectedResult, result);

			assertEquals(result, expectedResult, 0.0);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

}
