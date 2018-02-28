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
import at.tuwien.ict.acona.cell.cellfunction.ServiceState;
import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.config.SystemConfig;
import at.tuwien.ict.acona.cell.core.CellGateway;
import at.tuwien.ict.acona.cell.core.CellGatewayImpl;
import at.tuwien.ict.acona.cell.core.cellfunction.helpers.AdditionFunctionWithDuration;
import at.tuwien.ict.acona.cell.core.cellfunction.helpers.CFIncrementService;
import at.tuwien.ict.acona.cell.core.cellfunction.helpers.LoopController;
import at.tuwien.ict.acona.cell.core.cellfunction.helpers.SequenceController;
import at.tuwien.ict.acona.cell.core.cellfunction.helpers.SimpleControllerService;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.DatapointBuilder;
import at.tuwien.ict.acona.launcher.SystemControllerImpl;
import jade.core.Runtime;

public class AconaServiceTester {
	private static Logger log = LoggerFactory.getLogger(AconaServiceTester.class);
	private SystemControllerImpl launcher = SystemControllerImpl.getLauncher();

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
	 * Idea: Create an agent with the following behaviours (not jade): A controller
	 * runs every 5s. It starts a getDataFunction. When the data has been received,
	 * the publish data function is executed. Data is read from another dummy agent,
	 * which acts as a memory In the "Drivetrack-Agent", 2 values are read from a
	 * memory agent, added and published within the agent. The result is subscribed
	 * by an output agent The Outbuffer is only an empty mock, which is used as a
	 * gateway
	 * 
	 */
	@Test
	public void externalControllerWithDatabaseCellsAndAdditionCellTest() {
		try {
			String COMMANDDATAPOINTNAME = "command";
			// String STATUSDATAPOINTNAME = "status";
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
					.addCellfunction(CellFunctionConfig.newConfig(controllerFunctionName, SequenceController.class)
							.setProperty("agent1", agentName1).setProperty("agent2", agentName2)
							.setProperty("agent3", agentName3).setProperty("servicename", ServiceName)
							.setProperty("delay", "1000").addManagedDatapoint(DatapointConfig
									.newConfig(COMMANDDATAPOINTNAME, COMMANDDATAPOINTNAME, SyncMode.SUBSCRIBEONLY)));
			CellGatewayImpl controller = this.launcher.createAgent(controllerAgentConfig);

			controller.getCommunicator().write(memoryAgentName, DatapointBuilder.newDatapoint("Test"));

			// Create services
			CellConfig serviceAgent1 = CellConfig.newConfig(agentName1)
					.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
							.addManagedDatapoint(DatapointConfig.newConfig(INCREMENTATIONDATAPOINTNAME,
									processDatapoint, memoryAgentName, SyncMode.SUBSCRIBEWRITEBACK)));
			CellGatewayImpl service1 = this.launcher.createAgent(serviceAgent1);

			CellConfig serviceAgent2 = CellConfig.newConfig(agentName2)
					.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
							.addManagedDatapoint(DatapointConfig.newConfig(INCREMENTATIONDATAPOINTNAME,
									processDatapoint, memoryAgentName, SyncMode.SUBSCRIBEWRITEBACK)));
			CellGatewayImpl service2 = this.launcher.createAgent(serviceAgent2);

			CellConfig serviceAgent3 = CellConfig.newConfig(agentName3)
					.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
							.addManagedDatapoint(DatapointConfig.newConfig(INCREMENTATIONDATAPOINTNAME,
									processDatapoint, memoryAgentName, SyncMode.SUBSCRIBEWRITEBACK)));
			CellGatewayImpl service3 = this.launcher.createAgent(serviceAgent3);

			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}
			log.info("=== All agents initialized ===");

			// memoryAgent.getCommunicator().write(Datapoint.newDatapoint(processDatapoint).setValue(new
			// JsonPrimitive(startValue)));
			log.info("Datapoints on the way");
			memoryAgent.getCommunicator()
					.write(DatapointBuilder.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			// Start the system by setting start
			Datapoint state = controller.getCommunicator().queryDatapoints(controller.getCell().getLocalName(),
					COMMANDDATAPOINTNAME, new JsonPrimitive(ControlCommand.START.toString()),
					controller.getCell().getLocalName(), controllerFunctionName + ".state",
					new JsonPrimitive(ServiceState.FINISHED.toString()), 10000);

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
	 * Idea: Create an agent with the following behaviours (not jade): A controller
	 * runs every 5s. It starts a getDataFunction. When the data has been received,
	 * the publish data function is executed. Data is read from another dummy agent,
	 * which acts as a memory In the "Drivetrack-Agent", 2 values are read from a
	 * memory agent, added and published within the agent. The result is subscribed
	 * by an output agent The Outbuffer is only an empty mock, which is used as a
	 * gateway
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
									.setProperty("delay", "1")
									.addManagedDatapoint(DatapointConfig.newConfig(COMMANDDATAPOINTNAME,
											COMMANDDATAPOINTNAME, SyncMode.SUBSCRIBEONLY))))
					.addMemory(CellConfig.newConfig(memoryAgentName))
					.addService(CellConfig.newConfig(agentName1)
							.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
									.addManagedDatapoint(INCREMENTATIONDATAPOINTNAME, processDatapoint, memoryAgentName,
											SyncMode.READWRITEBACK)))
					.addService(CellConfig.newConfig(agentName2)
							.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
									.addManagedDatapoint(INCREMENTATIONDATAPOINTNAME, processDatapoint, memoryAgentName,
											SyncMode.READWRITEBACK)))
					.addService(CellConfig.newConfig(agentName3)
							.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
									.addManagedDatapoint(INCREMENTATIONDATAPOINTNAME, processDatapoint, memoryAgentName,
											SyncMode.READWRITEBACK)))
					.setTopController(controllerAgentName);

			// this.launcher.createDebugUserInterface();

			this.launcher.init(totalConfig);
			log.info("=== All agents initialized ===");

			launcher.getAgent(memoryAgentName).getCommunicator()
					.write(DatapointBuilder.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			log.info("Datapoints on the way");
			// memoryAgent.getCommunicator().write(Datapoint.newDatapoint(processDatapoint).setValue(new
			// JsonPrimitive(startValue)));
			// Start the system by setting start

			CellGateway controller = launcher.getTopController();

			// Test the wrapper for controllers too
			// ControllerCellGateway controllerCellGateway = new
			// ControllerWrapper(controller);

			Datapoint state = controller.getCommunicator().queryDatapoints(COMMANDDATAPOINTNAME,
					ControlCommand.START.toString(), "controllerservice.state",
					new JsonPrimitive(ServiceState.FINISHED.toString()).getAsString(), 100000);

			// controllerCellGateway.executeService("", "controllerservice", new
			// JsonObject(), 10000);

			log.debug("Received state={}", state);

			// Write the numbers in the database agents
			// client1.getCommunicator().write(Datapoint.newDatapoint(memorydatapoint1).setValue(String.valueOf(value1)));
			// client2.getCommunicator().write(Datapoint.newDatapoint(memorydatapoint2).setValue(String.valueOf(value2)));
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

			double result = launcher.getAgent(memoryAgentName).getCommunicator().read(processDatapoint).getValue()
					.getAsDouble();

			log.debug("correct value={}, actual value={}", expectedResult, result);

			assertEquals(result, expectedResult, 0.0);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

	/**
	 * Idea: Create an agent with the following behaviours (not jade): A controller
	 * runs every 5s. It starts a getDataFunction. When the data has been received,
	 * the publish data function is executed. Data is read from another dummy agent,
	 * which acts as a memory In the "Drivetrack-Agent", 2 values are read from a
	 * memory agent, added and published within the agent. The result is subscribed
	 * by an output agent The Outbuffer is only an empty mock, which is used as a
	 * gateway
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
							.setProperty("agentname", serviceAgentName).setProperty("servicename", serviceName)
							.setProperty("delay", "10")));

			totalConfig.addMemory(CellConfig.newConfig(memoryAgentName));
			totalConfig.setTopController(controllerAgentName);

			totalConfig.addService(CellConfig.newConfig(serviceAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(serviceName, CFIncrementService.class)
							.addManagedDatapoint(INCREMENTATIONDATAPOINTNAME, processDatapoint, memoryAgentName,
									SyncMode.READWRITEBACK)));

			// === System initialization ===//

			// this.launcher.createDebugUserInterface();

			this.launcher.init(totalConfig);
			CellGateway topController = launcher.getTopController();
			topController.getCommunicator().setDefaultTimeout(100000);
			// Set start values
			launcher.getAgent(memoryAgentName).getCommunicator()
					.write(DatapointBuilder.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));

			// }
			// log.info("=== All agents initialized ===");

			log.info("=== System initialized ===");
			// === System operation ===//

			Datapoint resultState = topController.getCommunicator().queryDatapoints(controllerServiceName + ".command",
					ControlCommand.START.toString(), controllerServiceName + ".state",
					new JsonPrimitive(ServiceState.FINISHED.toString()).getAsString(), 100000);

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
	 * In this test, one controller will start 100 increment services in a sequence.
	 * The incrementservices increases the number in the memory with +1. At the end
	 * the number in the memory shall be the same as the number of services in the
	 * system.
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
			// Create total config
			SystemConfig totalConfig = SystemConfig.newConfig();

			// Add controller
			totalConfig.addController(CellConfig.newConfig(controllerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(controllerServiceName, LoopController.class)
							.setProperty("agentnameprefix", serviceAgentName).setProperty("servicename", serviceName)
							.setProperty("numberofagents", String.valueOf(numberOfAgents)).setProperty("delay", "1")));

			// Add memory
			totalConfig.addMemory(CellConfig.newConfig(memoryAgentName));
			totalConfig.setTopController(controllerAgentName);

			// Add services
			for (int i = 1; i <= numberOfAgents; i++) {
				totalConfig.addService(CellConfig.newConfig(serviceAgentName + i)
						.addCellfunction(CellFunctionConfig.newConfig(serviceName, CFIncrementService.class)
								.addManagedDatapoint(IncrementFunctionDatapointID, processDatapoint, memoryAgentName,
										SyncMode.READWRITEBACK)));
			}

			// this.launcher.createDebugUserInterface();

			this.launcher.init(totalConfig);

			// }
			// log.info("=== All agents initialized ===");

			launcher.getAgent(memoryAgentName).getCommunicator()
					.write(DatapointBuilder.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			log.info("Datapoints on the way. Start system");
			// memoryAgent.getCommunicator().write(Datapoint.newDatapoint(processDatapoint).setValue(new
			// JsonPrimitive(startValue)));
			// Start the system by setting start

			// this.launcher.getAgent("AgentIncrementService1").getCommunicator().write(Datapoint.newDatapoint("Increment.command").setValue(ControlCommand.START.toString()));

			CellGateway controller = launcher.getTopController();

			// controller.getCommunicator().query(Datapoint.newDatapoint("Increment.command").setValue(ControlCommand.START.toString()),
			// agentName + 1, Datapoint.newDatapoint("Increment.state"),
			// agentName + 1, 10000);

			// controller.getCommunicator().query(Datapoint.newDatapoint(controllerServiceName
			// + ".command").setValue(ControlCommand.START.toString()),
			// Datapoint.newDatapoint(controllerServiceName + ".state"), 10000);

			// Test the wrapper for controllers too
			// ControllerCellGateway controllerCellGateway = new
			// ControllerWrapper(controller);
			ServiceState state = controller.getCommunicator().executeServiceBlocking(controllerServiceName);

			log.debug("Received state={}", state);

			double result = launcher.getAgent(memoryAgentName).getCommunicator().read(processDatapoint).getValue()
					.getAsDouble();

			log.debug("correct value={}, actual value={}", expectedResult, result);

			assertEquals(result, expectedResult, 0.0);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

	/**
	 * Idea: Start a function that calculates something with a delay. While it is
	 * calculating, new subscribed values arrives. The system shall automatically
	 * restart and process the newly arrived values. The test tests the feature that
	 * new data retriggers the function. If multiple subscribed data arrives, the
	 * system shall still only start once. In order to trigger this type of function
	 * the setStart shall be executed.
	 * 
	 * There is a calculator function that calculates a result of operand A and B
	 * within 5s. During the calculation, operand A changes.
	 * 
	 * 
	 */
	@Test
	public void echoExecutionOfFunctionOnValueUpdate() {
		try {
			String additionServiceName = "AdditionService";
			String agentName = "AdditionAgent";

			String operand1Address = "operand1";
			String operand2Address = "operand2";
			String resultAddress = "result";

			// values
			double operand1 = 2;
			double operand2 = 3;

			double operand2new = 4;

			double expectedResult = operand1 + operand2new;

			// Use a system config to init the whole system
			CellGatewayImpl cellAddition = this.launcher.createAgent(CellConfig.newConfig(agentName)
					.addCellfunction(CellFunctionConfig
							.newConfig(additionServiceName, AdditionFunctionWithDuration.class)
							.addManagedDatapoint(AdditionFunctionWithDuration.OPERANDID1, operand1Address,
									SyncMode.SUBSCRIBEONLY)
							.addManagedDatapoint(AdditionFunctionWithDuration.OPERANDID2, operand2Address,
									SyncMode.SUBSCRIBEONLY)
							.addManagedDatapoint(AdditionFunctionWithDuration.RESULT, resultAddress,
									SyncMode.WRITEONLY)));

			synchronized (this) {
				try {
					this.wait(500);
				} catch (InterruptedException e) {

				}
			}

			log.info("=== All agents initialized ===");

			// Write values
			cellAddition.getCommunicator()
					.write(DatapointBuilder.newDatapoint(operand1Address).setValue(new JsonPrimitive(operand1)));
			cellAddition.getCommunicator()
					.write(DatapointBuilder.newDatapoint(operand2Address).setValue(new JsonPrimitive(operand2)));

			// The result should be there in 5s. Therefore wait 500ms
			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {

				}
			}

			cellAddition.getCommunicator()
					.write(DatapointBuilder.newDatapoint(operand2Address).setValue(new JsonPrimitive(operand2new)));
			cellAddition.getCommunicator()
					.write(DatapointBuilder.newDatapoint(operand2Address).setValue(new JsonPrimitive(operand2new)));
			cellAddition.getCommunicator()
					.write(DatapointBuilder.newDatapoint(operand2Address).setValue(new JsonPrimitive(operand2new)));

			// The result should be there in 5s. Therefore wait 500ms
			synchronized (this) {
				try {
					this.wait(7000);
				} catch (InterruptedException e) {

				}
			}

			double result = cellAddition.getCommunicator().read(resultAddress).getValue().getAsDouble();

			log.debug("correct value={}, actual value={}", expectedResult, result);

			assertEquals(result, expectedResult, 0.0);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

}
