package at.tuwien.ict.acona.mq.core.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.mq.core.agentfunction.ControlCommand;
import at.tuwien.ict.acona.mq.core.agentfunction.SyncMode;
import at.tuwien.ict.acona.mq.core.agentfunction.helper.IncrementServiceThread;
import at.tuwien.ict.acona.mq.core.agentfunction.helper.LoopController;
import at.tuwien.ict.acona.mq.core.agentfunction.helper.SequenceController;
import at.tuwien.ict.acona.mq.core.agentfunction.helper.SimpleController;
import at.tuwien.ict.acona.mq.core.agentfunction.helper.TimeRegisterFunction;
import at.tuwien.ict.acona.mq.core.agentfunction.specialfunctions.DatapointMirroring;
import at.tuwien.ict.acona.mq.core.agentfunction.specialfunctions.DatapointTransfer;
import at.tuwien.ict.acona.mq.core.agentfunction.specialfunctions.SimpleReproduction;
import at.tuwien.ict.acona.mq.core.config.AgentConfig;
import at.tuwien.ict.acona.mq.core.config.FunctionConfig;
import at.tuwien.ict.acona.mq.core.config.DatapointConfig;
import at.tuwien.ict.acona.mq.core.core.Cell;
import at.tuwien.ict.acona.mq.datastructures.DPBuilder;
import at.tuwien.ict.acona.mq.datastructures.Datapoint;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.launcher.SystemControllerImpl;

public class MqCellCoreFunctionTester {
	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final DPBuilder dpb = new DPBuilder();
	private SystemControllerImpl launcher = SystemControllerImpl.getLauncher();

	@Before
	public void setUp() throws Exception {
		try {

		} catch (Exception e) {
			log.error("Cannot initialize test environment", e);
		}
	}

	@After
	public void tearDown() throws Exception {
		// Clear all cells
		synchronized (this) {
			try {
				this.wait(10);
			} catch (InterruptedException e) {

			}
		}
		this.launcher.stopSystem();

		synchronized (this) {
			try {
				this.wait(10);
			} catch (InterruptedException e) {

			}
		}
	}

	/**
	 * Create 1 agent. Write a value to the storage. Read the same value again.
	 * 
	 */
	@Test
	public void testCellInternalWriteAndRead() {
		log.info("Start test to read a value from the same cell");
		try {
//			String host = "tcp://127.0.0.1:1883";
//			String username = "acona";
//			String password = "acona";

			String agentNameServer = "ServerCell";

			String datapointAddress = "test.value";
			String value = "Hello Cell";

			// Create the server agent
			AgentConfig serverConfig = AgentConfig.newConfig(agentNameServer);
			Cell server = launcher.createAgent(serverConfig);

			log.info("=== System initialized ===");

			// Write a value from the client to the server
			server.getCommunicator().write(dpb.newDatapoint(datapointAddress).setValue(value));

			log.debug("Written value");
			// Read that value from the server
			String result = server.getCommunicator().read(datapointAddress).getValueAsString();

			log.debug("correct value={}, actual value={}", value, result);
			assertEquals(value, result);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}
	}

	/**
	 * Create 2 cells. Write a value from the client to the server. Read the same value again. 2 agents are used here.
	 * 
	 */
	@Test
	public void testCellToCellWriteAndRead() {
		log.info("Start test to read a value from a foreign cell");
		try {
//			String host = "tcp://127.0.0.1:1883";
//			String username = "acona";
//			String password = "acona";

			String agentNameServer = "ServerCell";
			String agentNameClient = "ClientCell";

			String datapointAddress = "test.value";
			String value = "Hello Cell";

			// Create the server agent
			AgentConfig serverConfig = AgentConfig.newConfig(agentNameServer);
			Cell server = launcher.createAgent(serverConfig);

			AgentConfig clientConfig = AgentConfig.newConfig(agentNameClient);
			Cell client = launcher.createAgent(clientConfig);

			log.info("=== System initialized ===");

			// Write a value from the client to the server
			client.getCommunicator().write(dpb.newDatapoint(agentNameServer + ":" + datapointAddress).setValue(value));

			log.debug("Written value");
			// Read that value from the server
			String result = client.getCommunicator().read(agentNameServer + ":" + datapointAddress).getValueAsString();

			log.debug("correct value={}, actual value={}", value, result);
			assertEquals(value, result);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}
	}

	/**
	 * Create 1 cell. Subscribe a certain value from the storage. Write a value to the storage. The subscribing function shall be notified and put the value on a certain location in the storage.
	 * 
	 */
	@Test
	public void testCellInternalSubscribeAndNotify() {
		log.info("Start test to read a value from the same cell");
		try {
//			String host = "tcp://127.0.0.1:1883";
//			String username = "acona";
//			String password = "acona";

			String agentNameServer = "ServerCell";

			String datapointSourceAddress = "test/value";
			String datapointDestinationAddress = "test2/value";
			String value = "Hello Cell";

			// Create the server agent
			AgentConfig serverConfig = AgentConfig.newConfig(agentNameServer)
					.addFunction(FunctionConfig.newConfig(DatapointTransfer.class)
							.setProperty(DatapointTransfer.PARAMSOURCEADDRESS, "<" + agentNameServer + ">/" + datapointSourceAddress)
							.setProperty(DatapointTransfer.PARAMDESTINATIONADDRESS, "<" + agentNameServer + ">/" + datapointDestinationAddress));
			Cell server = launcher.createAgent(serverConfig);

			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}

			log.info("=== System initialized ===");

			// Write a value from the client to the server
			server.getCommunicator().write(dpb.newDatapoint(datapointSourceAddress).setValue(value));
			log.debug("Written value");

			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}

			// Read that value from the server
			String result = server.getCommunicator().read(datapointDestinationAddress).getValueAsString();

			log.debug("correct value={}, actual value={}", value, result);
			assertEquals(value, result);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}
	}

	/**
	 * Create 2 agents. Test if one agent can subscribe a value from another agent.
	 * 
	 */
	@Test
	public void testCellToCellSubscribeAndNotify() {
		log.info("Start test to read a value from a foreign cell");
		try {
			String agentNameServer = "ServerCell";
			String agentNameClient = "ClientCell";

			String datapointSourceAddress = agentNameServer + ":test/value";
			String datapointDestinationAddress = agentNameClient + ":test/value";
			String value = "Hello Cell";

			// Create the server agent
			AgentConfig serverConfig = AgentConfig.newConfig(agentNameServer);
			Cell server = launcher.createAgent(serverConfig);

			// Create the server agent
			AgentConfig clientConfig = AgentConfig.newConfig(agentNameClient)
					.addFunction(FunctionConfig.newConfig(DatapointTransfer.class)
							.setProperty(DatapointTransfer.PARAMSOURCEADDRESS, datapointSourceAddress)
							.setProperty(DatapointTransfer.PARAMDESTINATIONADDRESS, datapointDestinationAddress));
			Cell client = launcher.createAgent(clientConfig);

			client.getCommunicator().setDefaultTimeout(100000);

			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}

			log.info("=== System initialized ===");

			// Write a value from the client to the server
			client.getCommunicator().write(dpb.newDatapoint(datapointSourceAddress).setValue(value));

			log.debug("Written value");

			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}
			// Read that value from the server
			String result = client.getCommunicator().read(datapointDestinationAddress).getValueAsString();

			log.debug("correct value={}, actual value={}", value, result);
			assertEquals(value, result);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}
	}

	/**
	 * Instantiate 3 or more agents. Agent n subscribes a datapoint from agent n-1. In agent 0, a value is set and propagated through the system to agent n. This value is measured and checked if it is the
	 * input value. The test is passed if input value of the system is equal to the value of the last agent.
	 */
	@Test
	public void chainOfSubscribersTest() {
		// final int minWaitTime = 5;
		final int numberOfAgents = 100; // If there are errors with nullpointers.
										// Set the timeouts of the queues in the
										// communication!!
		// create message for subscription. Fields: Address
		String agentNameTemplate = "agent";

		String datapointaddress = "subscribe/test/address";
		String value2 = "MuHaahAhaAaahAAHA";

		// Create 2 agents. One shall subscribe the other. One shall be written
		// to. The subscribing agent shall be notified.

		try {
			long setupTimeStart = System.currentTimeMillis();

			// Create X=5 agents
			List<Cell> inspectors = new ArrayList<>();
			Cell firstCell = this.launcher.createAgent(AgentConfig.newConfig(agentNameTemplate + 0));
			inspectors.add(firstCell);
			for (int i = 1; i < numberOfAgents; i++) {
				Cell cell = (this.launcher.createAgent(AgentConfig.newConfig(agentNameTemplate + i)
						.addFunction(FunctionConfig.newConfig("updater", DatapointMirroring.class)
								.addManagedDatapoint(datapointaddress, inspectors.get(i - 1).getName() + ":" + datapointaddress, SyncMode.SUBSCRIBEONLY))));
				inspectors.add(cell);
				cell.getFunctionHandler().getCellFunction("<" + agentNameTemplate + i + ">/" + "updater").getCommunicator().setDefaultTimeout(60000);
			}

			// Add special time function
			Cell timeRegister = this.launcher.createAgent(AgentConfig.newConfig("TimeRegister")
					.addFunction(FunctionConfig.newConfig("TimeRegisterFunction", TimeRegisterFunction.class)
							.addManagedDatapoint("STOPTIME", agentNameTemplate + (numberOfAgents - 1) + ":" + datapointaddress, SyncMode.SUBSCRIBEONLY)));

			long setupStopTime = System.currentTimeMillis();

			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {

				}
			}

			// Start tic
			long starttime = System.currentTimeMillis();
			log.info("=================Start time measurement: {}=====================", starttime);
			inspectors.get(0).getCommunicator().write(this.dpb.newDatapoint(datapointaddress).setValue(value2));

			log.info("Datastorage of the last agent={}", inspectors.get(numberOfAgents - 1).getDataStorage());

			log.debug("Get database of publisher={}", inspectors.get(0).getDataStorage());

			synchronized (this) {
				try {
					this.wait(10000);
				} catch (InterruptedException e) {

				}
			}

			// Get the value from the last agent
			log.info("Datastorage of the last agent={}", inspectors.get(numberOfAgents - 1).getDataStorage());

			log.info("=================End time measurement: {}=====================", System.currentTimeMillis() - starttime);
			String answer = inspectors.get(numberOfAgents - 1).getCommunicator().read(datapointaddress).getValue().getAsString();// JsonMessage.getBody(result).get(datapointaddress).getAsString();

			long endTime = Long.valueOf(timeRegister.getCommunicator().read("<TimeRegister>/TimeRegisterFunction/result").getValueAsString());

			String setupStart = new Date(setupTimeStart).toString();
			String setupStop = new Date(setupStopTime).toString();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			String startTime = sdf.format(new Date(starttime)).toString();

			log.info("Duration setup={}, duration experiment={}", setupStopTime - setupTimeStart, endTime - starttime);

			assertEquals(value2, answer);

			log.info("Test passed.");

		} catch (Exception e) {
			log.error("Cannot test system", e);
			fail("Error");
		}
	}

	/**
	 * Test how to make a request, which is blocking within another blocking request. In the blocking answer request, a request is sent to a service. The result or state datapoint is subscribed. However,
	 * as the subscription also sends a request, a request is sent within a request. the challege is to map the answer to the right blocker and in the right order.
	 * 
	 * Create an agent with a service "controller". The controller service starts blocking the service Delayservice. The first service subscribes the state of the Delayservice. As the delayservice
	 * finishes it sets its state to finished. That releases the controller service.
	 * 
	 */
	@Test
	public void requestInReqiestTest() {
		try {
			String controllerAgentName = "ServiceAgent";
			String controllerFunctionName = "controllerService";
			String ServiceName = "IncrementService"; // The same name for all services

			// define all datapoints that shall be used
			String processDatapoint = "memory.value"; // put into memory mock
			String INCREMENTATIONDATAPOINTNAME = "increment";

			// values
			double startValue = 2;
			int expectedResult = 4;

			// Memory
			// Cell memoryAgent = this.launcher.createAgent(CellConfig.newConfig(memoryAgentName));

			// Controller
			AgentConfig controllerAgentConfig = AgentConfig.newConfig(controllerAgentName)
					.addFunction(FunctionConfig.newConfig(controllerFunctionName, SimpleController.class)
							.setProperty("agentname", controllerAgentName)
							.setProperty("servicename", ServiceName)
							.setProperty("delay", "1000"))
					.addFunction(FunctionConfig.newConfig(ServiceName, IncrementServiceThread.class)
							.addManagedDatapoint(DatapointConfig.newConfig(INCREMENTATIONDATAPOINTNAME, controllerAgentName + ":" + processDatapoint, SyncMode.SUBSCRIBEWRITEBACK)));
			Cell controller = this.launcher.createAgent(controllerAgentConfig);

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
			controller.getCommunicator().write(this.dpb.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));

			log.debug("Start value set. Start the service");
			// Start the system by setting start. Blocking system by setting blocking true
			controller.getCommunicator().execute(controller.getName() + ":" + controllerFunctionName + "/command", 
				(new Request())
				.setParameter("command", ControlCommand.START)
				.setParameter("blocking", true), 100000);
			
			controller.getCommunicator().execute(controller.getName() + ":" + controllerFunctionName + "/command", 
					(new Request())
					.setParameter("command", ControlCommand.START)
					.setParameter("blocking", true), 100000);

			double result = controller.getCommunicator().read(processDatapoint).getValue().getAsDouble();

			log.debug("correct value={}, actual value={}", expectedResult, result);

			assertEquals(result, expectedResult, 0.0);
			log.info("Test passed");
			
			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}
			
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

	/**
	 * Idea: Create an agent with the following behaviours: A controller runs every 5s. It starts a getDataFunction. When the data has been received, the publish data function is executed. Data is read
	 * from another dummy agent, which acts as a memory In the "Drivetrack-Agent", 2 values are read from a memory agent, added and published within the agent. The result is subscribed by an output agent
	 * The Outbuffer is only an empty mock, which is used as a gateway
	 * 
	 */
	@Test
	public void externalControllerWithDatabaseCellsAndAdditionCellTest() {
		try {
			// String COMMANDDATAPOINTNAME = "command";
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
			double startValue = 2;
			int expectedResult = 5;

			// Memory
			Cell memoryAgent = this.launcher.createAgent(AgentConfig.newConfig(memoryAgentName));

			// Controller
			AgentConfig controllerAgentConfig = AgentConfig.newConfig(controllerAgentName)
					.addFunction(FunctionConfig.newConfig(controllerFunctionName, SequenceController.class)
							.setProperty("agent1", agentName1)
							.setProperty("agent2", agentName2)
							.setProperty("agent3", agentName3)
							.setProperty("servicename", ServiceName)
							.setProperty("delay", "1000"));
			Cell controller = this.launcher.createAgent(controllerAgentConfig);

			controller.getCommunicator().write(this.dpb.newDatapoint(memoryAgentName + ":Test"));

			// Create services
			AgentConfig serviceAgent1 = AgentConfig.newConfig(agentName1)
					.addFunction(FunctionConfig.newConfig(ServiceName, IncrementServiceThread.class)
							.addManagedDatapoint(DatapointConfig.newConfig(INCREMENTATIONDATAPOINTNAME,
									memoryAgentName + ":" + processDatapoint, SyncMode.SUBSCRIBEWRITEBACK)));
			Cell service1 = this.launcher.createAgent(serviceAgent1);

			AgentConfig serviceAgent2 = AgentConfig.newConfig(agentName2)
					.addFunction(FunctionConfig.newConfig(ServiceName, IncrementServiceThread.class)
							.addManagedDatapoint(DatapointConfig.newConfig(INCREMENTATIONDATAPOINTNAME,
									memoryAgentName + ":" + processDatapoint, SyncMode.SUBSCRIBEWRITEBACK)));
			Cell service2 = this.launcher.createAgent(serviceAgent2);

			AgentConfig serviceAgent3 = AgentConfig.newConfig(agentName3)
					.addFunction(FunctionConfig.newConfig(ServiceName, IncrementServiceThread.class)
							.addManagedDatapoint(DatapointConfig.newConfig(INCREMENTATIONDATAPOINTNAME,
									memoryAgentName + ":" + processDatapoint, SyncMode.SUBSCRIBEWRITEBACK)));
			Cell service3 = this.launcher.createAgent(serviceAgent3);

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
			memoryAgent.getCommunicator().write(this.dpb.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			// Start the system by setting start
			controller.getCommunicator().setDefaultTimeout(100000);
			controller.getCommunicator().execute(controller.getName() + ":" + controllerFunctionName + "/command", 
					(new Request())
					.setParameter("command", ControlCommand.START)
					.setParameter("blocking", true), 100000);
			
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
	 * Idea: Create an agent with the following behaviours (not jade): A controller runs every 5s. It starts a getDataFunction. When the data has been received, the publish data function is executed. Data
	 * is read from another dummy agent, which acts as a memory In the "Drivetrack-Agent", 2 values are read from a memory agent, added and published within the agent. The result is subscribed by an
	 * output agent The Outbuffer is only an empty mock, which is used as a gateway
	 * 
	 */
	@Test
	public void aconaServiceWithFullControlReadDatapointsTest() {
		try {
			//String COMMANDDATAPOINTNAME = "command";
			//String STATUSDATAPOINTNAME = "status";
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
			double startValue = 3;
			int expectedResult = 6;

			// Use a system config to init the whole system
			Cell controller = this.launcher.createAgent(AgentConfig.newConfig(controllerAgentName)
					.addFunction(FunctionConfig.newConfig(controllerFunctionName, SequenceController.class)
							.setProperty("agent1", agentName1).setProperty("agent2", agentName2)
							.setProperty("agent3", agentName3).setProperty("servicename", ServiceName)
							.setProperty("delay", "1")));
			this.launcher.createAgent(AgentConfig.newConfig(memoryAgentName));
			this.launcher.createAgent(AgentConfig.newConfig(agentName1)
					.addFunction(FunctionConfig.newConfig(ServiceName, IncrementServiceThread.class)
							.addManagedDatapoint(INCREMENTATIONDATAPOINTNAME, memoryAgentName + ":" + processDatapoint,
									SyncMode.READWRITEBACK)));
			this.launcher.createAgent(AgentConfig.newConfig(agentName2)
					.addFunction(FunctionConfig.newConfig(ServiceName, IncrementServiceThread.class)
							.addManagedDatapoint(INCREMENTATIONDATAPOINTNAME, memoryAgentName + ":" + processDatapoint,
									SyncMode.READWRITEBACK)));
			this.launcher.createAgent(AgentConfig.newConfig(agentName3)
					.addFunction(FunctionConfig.newConfig(ServiceName, IncrementServiceThread.class)
							.addManagedDatapoint(INCREMENTATIONDATAPOINTNAME, memoryAgentName + ":" + processDatapoint,
									SyncMode.READWRITEBACK)));

			log.info("=== All agents initialized ===");

			launcher.getAgent(memoryAgentName).getCommunicator()
					.write(this.dpb.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			log.info("Datapoints on the way");

			controller.getCommunicator().execute(controller.getName() + ":" + controllerFunctionName + "/command", 
					(new Request())
					.setParameter("command", ControlCommand.START)
					.setParameter("blocking", true), 100000);

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
	 * Idea: Create an agent with the following behaviours (not jade): A controller runs every 5s. It starts a getDataFunction. When the data has been received, the publish data function is executed. Data
	 * is read from another dummy agent, which acts as a memory In the "Drivetrack-Agent", 2 values are read from a memory agent, added and published within the agent. The result is subscribed by an
	 * output agent The Outbuffer is only an empty mock, which is used as a gateway
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
			Cell topController = this.launcher.createAgent(AgentConfig.newConfig(controllerAgentName)
					.addFunction(FunctionConfig.newConfig(controllerServiceName, SimpleController.class)
							.setProperty("agentname", serviceAgentName).setProperty("servicename", serviceName)
							.setProperty("delay", "10")));
			// SystemConfig totalConfig = SystemConfig.newConfig();
			// totalConfig.addController();

			this.launcher.createAgent(AgentConfig.newConfig(memoryAgentName));
			this.launcher.createAgent(AgentConfig.newConfig(serviceAgentName)
					.addFunction(FunctionConfig.newConfig(serviceName, IncrementServiceThread.class)
							.addManagedDatapoint(INCREMENTATIONDATAPOINTNAME, memoryAgentName + ":" + processDatapoint,
									SyncMode.READWRITEBACK)));

			// totalConfig.addMemory();
			// totalConfig.setTopController(controllerAgentName);

			// totalConfig.addService();

			// === System initialization ===//

			// this.launcher.createDebugUserInterface();

			// this.launcher.init(totalConfig);
			// CellGateway topController = launcher.getTopController();
			topController.getCommunicator().setDefaultTimeout(100000);
			// Set start values
			launcher.getAgent(memoryAgentName).getCommunicator()
					.write(this.dpb.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));

			// }
			// log.info("=== All agents initialized ===");

			log.info("=== System initialized ===");
			// === System operation ===//

			topController.getCommunicator().execute(controllerAgentName + ":" + controllerServiceName + "/" + "command", 
					(new Request())
					.setParameter("command", ControlCommand.START)
					.setParameter("blocking", true), 100000);
					

			log.info("=== System operation finished. Extract results ===");
			// === Extract results ===//

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

//	/**
//	 * In this test, one controller will start 100 increment services in a sequence. The incrementservices increases the number in the memory with +1. At the end the number in the memory shall be the same
//	 * as the number of services in the system.
//	 * 
//	 */
//	@Test
//	public void aconaServiceIncrementorCountTo100() {
//		try {
//			log.info("=== Test AconaServiceStartsAconaService ===");
//
//			// === Agent names ===//
//			String serviceAgentName = "IncrementServiceAgent";
//			String controllerAgentName = "ControllerAgent";
//			String memoryAgentName = "MemoryAgent";
//
//			// === Function names ===//
//			String controllerServiceName = "controllerservice";
//
//			String serviceName = "IncrementService"; // The same name for all services
//			final String IncrementFunctionDatapointID = "increment";
//
//			// === Datappointnames ===//
//			String processDatapoint = "memory.value"; // put into memory mock agent
//
//			// === Values ===//
//			int numberOfAgents = 100;
//
//			// values
//			double startValue = 0;
//			int expectedResult = numberOfAgents;
//
//			// === Config ===//
//			// Create total config
//			// SystemConfig totalConfig = SystemConfig.newConfig();
//
//			Cell controller = this.launcher.createAgent(CellConfig.newConfig(controllerAgentName)
//					.addCellfunction(CellFunctionConfig.newConfig(controllerServiceName, LoopController.class)
//							.setProperty("agentnameprefix", serviceAgentName).setProperty("servicename", serviceName)
//							.setProperty("numberofagents", String.valueOf(numberOfAgents)).setProperty("delay", "10")));
//			// Add controller
//			// totalConfig.addController();
//
//			// Add memory
//			this.launcher.createAgent(CellConfig.newConfig(memoryAgentName));
//			// totalConfig.addMemory();
//			// totalConfig.setTopController(controllerAgentName);
//
//			// Add services
//			for (int i = 1; i <= numberOfAgents; i++) {
//				this.launcher.createAgent(CellConfig.newConfig(serviceAgentName + i)
//						.addCellfunction(CellFunctionConfig.newConfig(serviceName, IncrementService.class)
//								.addManagedDatapoint(IncrementFunctionDatapointID, processDatapoint, memoryAgentName,
//										SyncMode.READWRITEBACK)));
//				// totalConfig.addService();
//			}
//
//			// this.launcher.createDebugUserInterface();
//
//			// this.launcher.init(totalConfig);
//
//			// }
//			// log.info("=== All agents initialized ===");
//
//			launcher.getAgent(memoryAgentName).getCommunicator()
//					.write(DatapointBuilder.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
//			log.info("Datapoints on the way. Start system");
//			// memoryAgent.getCommunicator().write(Datapoint.newDatapoint(processDatapoint).setValue(new
//			// JsonPrimitive(startValue)));
//			// Start the system by setting start
//
//			// this.launcher.getAgent("AgentIncrementService1").getCommunicator().write(Datapoint.newDatapoint("Increment.command").setValue(ControlCommand.START.toString()));
//
//			// CellGateway controller = launcher.getTopController();
//
//			// controller.getCommunicator().query(Datapoint.newDatapoint("Increment.command").setValue(ControlCommand.START.toString()),
//			// agentName + 1, Datapoint.newDatapoint("Increment.state"),
//			// agentName + 1, 10000);
//
//			// controller.getCommunicator().query(Datapoint.newDatapoint(controllerServiceName
//			// + ".command").setValue(ControlCommand.START.toString()),
//			// Datapoint.newDatapoint(controllerServiceName + ".state"), 10000);
//
//			// Test the wrapper for controllers too
//			// ControllerCellGateway controllerCellGateway = new
//			// ControllerWrapper(controller);
//			ServiceState state = controller.getCommunicator().executeServiceBlocking(controllerServiceName);
//
//			log.debug("Received state={}", state);
//
//			double result = launcher.getAgent(memoryAgentName).getCommunicator().read(processDatapoint).getValue()
//					.getAsDouble();
//
//			log.debug("correct value={}, actual value={}", expectedResult, result);
//
//			assertEquals(result, expectedResult, 0.0);
//			log.info("Test passed");
//		} catch (Exception e) {
//			log.error("Error testing system", e);
//			fail("Error");
//		}
//
//	}
	
	/**
	 * In this test, one controller will start 100 increment services in a sequence. The incrementservices increases the number in the memory with +1. At the end the number in the memory shall be the same
	 * as the number of services in the system.
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

			String serviceName = "IncrementService"; // The same name for all
														// services
			final String IncrementFunctionDatapointID = "increment";

			// === Datappointnames ===//
			String processDatapoint = "memory.value"; // put into memory mock
														// agent

			// === Values ===//
			int numberOfAgents = 100;

			// values
			double startValue = 0;
			int expectedResult = numberOfAgents;

			// === Config ===//
			// Create total config
			// SystemConfig totalConfig = SystemConfig.newConfig();

			// Add controller
			Cell controller = this.launcher.createAgent(AgentConfig.newConfig(controllerAgentName)
					.addFunction(FunctionConfig.newConfig(controllerServiceName, LoopController.class)
							.setProperty("agentnameprefix", serviceAgentName)
							.setProperty("servicename", serviceName)
							.setProperty("numberofagents", String.valueOf(numberOfAgents))
							.setProperty("delay", "10")));
			// totalConfig.addController();

			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}

			// Add memory
			this.launcher.createAgent(AgentConfig.newConfig(memoryAgentName));
			// totalConfig.addMemory();
			// totalConfig.setTopController(controllerAgentName);

			synchronized (this) {
				try {
					this.wait(100);
				} catch (InterruptedException e) {

				}
			}

			// Add services
			for (int i = 1; i <= numberOfAgents; i++) {
				this.launcher.createAgent(AgentConfig.newConfig(serviceAgentName + i)
						.addFunction(FunctionConfig.newConfig(serviceName, IncrementServiceThread.class)
								.addManagedDatapoint(IncrementFunctionDatapointID, memoryAgentName + ":" + processDatapoint,
										SyncMode.READWRITEBACK)));
				synchronized (this) {
					try {
						this.wait(10);
					} catch (InterruptedException e) {

					}
				}
			}

			// this.launcher.createDebugUserInterface();

			// this.launcher.init(totalConfig);

			// }

			synchronized (this) {
				try {
					this.wait(10000);
				} catch (InterruptedException e) {

				}
			}
			// log.info("=== All agents initialized ===");

			launcher.getAgent(memoryAgentName).getCommunicator().write(this.dpb.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			log.info("Datapoints on the way. Start system");
			// memoryAgent.getCommunicator().write(Datapoint.newDatapoint(processDatapoint).setValue(new
			// JsonPrimitive(startValue)));
			// Start the system by setting start

			// this.launcher.getAgent("AgentIncrementService1").getCommunicator().write(Datapoint.newDatapoint("Increment.command").setValue(ControlCommand.START.toString()));

			// CellGateway controller = launcher.getTopController();

			// controller.getCommunicator().query(Datapoint.newDatapoint("Increment.command").setValue(ControlCommand.START.toString()),
			// agentName + 1, Datapoint.newDatapoint("Increment.state"),
			// agentName + 1, 10000);

			// controller.getCommunicator().query(Datapoint.newDatapoint(controllerServiceName
			// + ".command").setValue(ControlCommand.START.toString()),
			// Datapoint.newDatapoint(controllerServiceName + ".state"), 10000);

			// Test the wrapper for controllers too
			// ControllerCellGateway controllerCellGateway = new
			// ControllerWrapper(controller);
			controller.getCommunicator().execute(controllerAgentName + ":" + controllerServiceName + "/" + "command", 
					(new Request())
					.setParameter("command", ControlCommand.START)
					.setParameter("blocking", true), 100000);

			log.debug("Received finished");

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
	 * The agent shall replicate itself. An agent is created. On trigger, the agent creates a copy of itself. The test is passed if the second agent also contains a function from the first agent
	 */
	@Test
	public void CFReproduceAgentTester() {
		try {
			String agentName = "parentagent";
			String functionName = "increment";
			String reproduceFunction = "reproduce";
			String datapoint = agentName + ":" + "datapoint.test";
			// String destinationAddress = CFDurationThreadTester.queryDatapointID;
			// String resultAddress = CFDurationThreadTester.resultDatapointID;
			// double value = 1.3;
			// double expectedResult = value;

			// Create cell
			Cell agent = this.launcher.createAgent(AgentConfig.newConfig(agentName)
					.addFunction(FunctionConfig.newConfig(functionName, IncrementServiceThread.class)
							.addManagedDatapoint(IncrementServiceThread.ATTRIBUTEINCREMENTDATAPOINT, datapoint,
									SyncMode.SUBSCRIBEWRITEBACK))
					.addFunction(FunctionConfig.newConfig("reproduce", SimpleReproduction.class)));

			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}
			log.info("=== All agents initialized ===");

			// Run the first agent
			agent.getCommunicator().execute(agent.getName() + ":" + functionName + "/" + "command", 
					(new Request())
					.setParameter("command", ControlCommand.START)
					.setParameter("blocking", false), 100000);

			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}

			// Reproduce the agent
			//agent.getCommunicator().write(dpb.newDatapoint(reproduceFunction + ".command").setValue("START"));
			agent.getCommunicator().execute(agent.getName() + ":" + reproduceFunction + "/" + "command", 
					(new Request())
					.setParameter("command", ControlCommand.START)
					.setParameter("blocking", false), 100000);

			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}

			// Run the second agent
			Map<String, Cell> map = this.launcher.getExternalAgentControllerMap();
			Cell newAgent = null;
			for (Entry<String, Cell> k : map.entrySet()) {
				log.debug("Agents={}", k.getKey());
				if (k.getKey().contains(agentName) == true && k.getKey().equals(agentName) == false) {
					newAgent = k.getValue();
					log.info("The replicaagent is={}", k);
					break;
				}
			}

			log.info("Available agents={}", this.launcher.getExternalAgentControllerMap());
			agent.getCommunicator().execute(agent.getName() + ":" + functionName + "/" + "command", 
					(new Request())
					.setParameter("command", ControlCommand.START)
					.setParameter("blocking", false), 100000);

			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}

			log.debug("Values={}", agent.getDataStorage());

			// Read the datapoint value

			double readValue = newAgent.getCommunicator().read(datapoint).getValue().getAsDouble();
			log.debug("correct value={}, actual value={}", 2.0, readValue);

			assertEquals(2.0, readValue, 0.0);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

	/**
	 * Create an agent without any certain function. Get the agent from the container and add a function into the running system. The function shall increment a value. If the value has been incremented,
	 * the adding of the new function was successful.
	 */
	@Test
	public void addPostFunctionTester() {
		try {
			String agentName = "FunctionLessAgent";
			String functionName = "increment";
			String datapoint = agentName + ":" + "datapoint.test";

			// Create cell
			Cell agent = this.launcher.createAgent(AgentConfig.newConfig(agentName));

			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}
			log.info("=== All agents initialized ===");

			// Add the increment function post to start of the agent
			FunctionConfig functionConf = FunctionConfig.newConfig(functionName, IncrementServiceThread.class)
					.addManagedDatapoint(IncrementServiceThread.ATTRIBUTEINCREMENTDATAPOINT, datapoint, SyncMode.SUBSCRIBEWRITEBACK);


			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}

			// Run the first agent
			try {
				agent.getCommunicator().execute(agent.getName() + ":" + functionName + "/" + "command", 
						(new Request())
						.setParameter("command", ControlCommand.START)
						.setParameter("blocking", false), 100);
			} catch (Exception e) {
				log.info("No function available", e);
			}
			

			this.launcher.getAgent(agentName).addFunction(functionConf);
			
			
			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}

			log.info("Available agents={}", this.launcher.getExternalAgentControllerMap());
			//agent.getCommunicator().write(DatapointBuilder.newDatapoint(agentName + ":" + functionName + ".command").setValue("START"));
			agent.getCommunicator().execute(agent.getName() + ":" + functionName + "/" + "command", 
					(new Request())
					.setParameter("command", ControlCommand.START)
					.setParameter("blocking", false), 100000);

			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}

			log.debug("Values={}", agent.getDataStorage());

			// Read the datapoint value

			double readValue = agent.getCommunicator().read(datapoint).getValue().getAsDouble();
			log.debug("correct value={}, actual value={}", 2.0, readValue);

			assertEquals(1.0, readValue, 0.0);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

}
