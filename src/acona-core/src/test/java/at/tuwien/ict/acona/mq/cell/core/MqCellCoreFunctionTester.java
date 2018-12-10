package at.tuwien.ict.acona.mq.cell.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.mq.cell.cellfunction.ServiceState;
import at.tuwien.ict.acona.mq.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.mq.cell.cellfunction.helper.IncrementServiceThread;
import at.tuwien.ict.acona.mq.cell.cellfunction.helper.SequenceController;
import at.tuwien.ict.acona.mq.cell.cellfunction.helper.SimpleController;
import at.tuwien.ict.acona.mq.cell.cellfunction.helper.TimeRegisterFunction;
import at.tuwien.ict.acona.mq.cell.cellfunction.specialfunctions.DatapointMirroring;
import at.tuwien.ict.acona.mq.cell.cellfunction.specialfunctions.DatapointTransfer;
import at.tuwien.ict.acona.mq.cell.config.CellConfig;
import at.tuwien.ict.acona.mq.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.mq.cell.config.DatapointConfig;
import at.tuwien.ict.acona.mq.datastructures.ControlCommand;
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
				this.wait(1000);
			} catch (InterruptedException e) {

			}
		}
		this.launcher.stopSystem();

		synchronized (this) {
			try {
				this.wait(1000);
			} catch (InterruptedException e) {

			}
		}
	}

	/**
	 * Create 1 cell. Write a value to the storage. Read the same value again.
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
			CellConfig serverConfig = CellConfig.newConfig(agentNameServer);
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
	 * Create 2 cells. Write a value from the client to the serve.r Read the same value again.
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
			CellConfig serverConfig = CellConfig.newConfig(agentNameServer);
			Cell server = launcher.createAgent(serverConfig);

			CellConfig clientConfig = CellConfig.newConfig(agentNameClient);
			Cell client = launcher.createAgent(clientConfig);

			log.info("=== System initialized ===");

			// Write a value from the client to the server
			client.getCommunicator().write(dpb.newDatapoint(agentNameServer + ":" + datapointAddress).setValue(value));

			log.debug("Written value");
			// Read that value from the server
			String result = client.getCommunicator().read(agentNameServer + ":" + datapointAddress).getValueAsString();

//			// Create the client agent
//
//			// Create inspectoragent
//			CellGatewayImpl client = this.launchUtil
//					.createAgent(CellConfig.newConfig("client", CellImpl.class.getName()));
//			// Create receiver agent
//			CellGatewayImpl receivergw = this.launchUtil
//					.createAgent(CellConfig.newConfig(receiver, CellImpl.class.getName()));
//
//			synchronized (this) {
//				try {
//					this.wait(200);
//				} catch (InterruptedException e) {
//
//				}
//			}

//			client.getCommunicator().setDefaultTimeout(100000);
//			receivergw.getCommunicator().setDefaultTimeout(1000000);
//			client.getCommunicator().write(receiver, DatapointBuilder.newDatapoint(datapointaddress).setValue(value));
//			log.debug("Now read values");
//			Datapoint resultdp = client.getCommunicator().read(receiver, datapointaddress, 1000000);
//
//			String result = resultdp.getValue().getAsString();
//			log.info("Received result={}. Expected result={}", result, value);
//
//			synchronized (this) {
//				try {
//					this.wait(200);
//				} catch (InterruptedException e) {
//
//				}
//			}

//			assertEquals(value, result);
//			log.info("Test passed");

//			int numberOfRuns = 2000;
//
//			final Semaphore sem = new Semaphore(0);
//			RequesterResponseFunction responder = new RequesterResponseFunction();
//			responder.init(sem, host, username, password, agentName, functionNameResponder, agentName + "/" + functionNameResponder + "/" + "increment", false);
//
//			RequesterResponseFunction requester = new RequesterResponseFunction();
//			requester.setNumberOfRuns(200);
//			requester.init(sem, host, username, password, agentName, functionNameRequester, agentName + "/" + functionNameResponder + "/" + "increment", true);

			// Aquire run as both threads are finished
//			sem.acquire();
//
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
			CellConfig serverConfig = CellConfig.newConfig(agentNameServer)
					.addCellfunction(CellFunctionConfig.newConfig(DatapointTransfer.class)
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
	 * Create 2 cells. Write a value from the client to the serve.r Read the same value again.
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
			CellConfig serverConfig = CellConfig.newConfig(agentNameServer);
			Cell server = launcher.createAgent(serverConfig);

			// Create the server agent
			CellConfig clientConfig = CellConfig.newConfig(agentNameClient)
					.addCellfunction(CellFunctionConfig.newConfig(DatapointTransfer.class)
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
		final int numberOfAgents = 10; // If there are errors with nullpointers.
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
			Cell firstCell = this.launcher.createAgent(CellConfig.newConfig(agentNameTemplate + 0));
			inspectors.add(firstCell);
			for (int i = 1; i < numberOfAgents; i++) {
				Cell cell = (this.launcher.createAgent(CellConfig.newConfig(agentNameTemplate + i)
						.addCellfunction(CellFunctionConfig.newConfig("updater", DatapointMirroring.class)
								.addManagedDatapoint(datapointaddress, inspectors.get(i - 1).getName() + ":" + datapointaddress, SyncMode.SUBSCRIBEONLY))));
				inspectors.add(cell);
				cell.getFunctionHandler().getCellFunction("updater").getCommunicator().setDefaultTimeout(60000);
			}

			// Add special time function
			Cell timeRegister = this.launcher.createAgent(CellConfig.newConfig("TimeRegister")
					.addCellfunction(CellFunctionConfig.newConfig("TimeRegisterFunction", TimeRegisterFunction.class)
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
			CellConfig controllerAgentConfig = CellConfig.newConfig(controllerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(controllerFunctionName, SimpleController.class)
							.setProperty("agent1", controllerAgentName)
							.setProperty("servicename", ServiceName)
							.setProperty("delay", "1000"))
					.addCellfunction(CellFunctionConfig.newConfig(ServiceName, IncrementServiceThread.class)
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
			Cell memoryAgent = this.launcher.createAgent(CellConfig.newConfig(memoryAgentName));

			// Controller
			CellConfig controllerAgentConfig = CellConfig.newConfig(controllerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(controllerFunctionName, SequenceController.class)
							.setProperty("agent1", agentName1)
							.setProperty("agent2", agentName2)
							.setProperty("agent3", agentName3)
							.setProperty("servicename", ServiceName)
							.setProperty("delay", "1000"));
			Cell controller = this.launcher.createAgent(controllerAgentConfig);

			controller.getCommunicator().write(this.dpb.newDatapoint(memoryAgentName + ":Test"));

			// Create services
			CellConfig serviceAgent1 = CellConfig.newConfig(agentName1)
					.addCellfunction(CellFunctionConfig.newConfig(ServiceName, IncrementServiceThread.class)
							.addManagedDatapoint(DatapointConfig.newConfig(INCREMENTATIONDATAPOINTNAME,
									memoryAgentName + ":" + processDatapoint, SyncMode.SUBSCRIBEWRITEBACK)));
			Cell service1 = this.launcher.createAgent(serviceAgent1);

			CellConfig serviceAgent2 = CellConfig.newConfig(agentName2)
					.addCellfunction(CellFunctionConfig.newConfig(ServiceName, IncrementServiceThread.class)
							.addManagedDatapoint(DatapointConfig.newConfig(INCREMENTATIONDATAPOINTNAME,
									memoryAgentName + ":" + processDatapoint, SyncMode.SUBSCRIBEWRITEBACK)));
			Cell service2 = this.launcher.createAgent(serviceAgent2);

			CellConfig serviceAgent3 = CellConfig.newConfig(agentName3)
					.addCellfunction(CellFunctionConfig.newConfig(ServiceName, IncrementServiceThread.class)
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
			CellGateway controller = this.launcher.createAgent(CellConfig.newConfig(controllerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig("controllerservice", SequenceController.class)
							.setProperty("agent1", agentName1).setProperty("agent2", agentName2)
							.setProperty("agent3", agentName3).setProperty("servicename", ServiceName)
							.setProperty("delay", "1")
							.addManagedDatapoint(DatapointConfig.newConfig(COMMANDDATAPOINTNAME,
									COMMANDDATAPOINTNAME, SyncMode.SUBSCRIBEONLY))));
			this.launcher.createAgent(CellConfig.newConfig(memoryAgentName));
			this.launcher.createAgent(CellConfig.newConfig(agentName1)
					.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
							.addManagedDatapoint(INCREMENTATIONDATAPOINTNAME, processDatapoint, memoryAgentName,
									SyncMode.READWRITEBACK)));
			this.launcher.createAgent(CellConfig.newConfig(agentName2)
					.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
							.addManagedDatapoint(INCREMENTATIONDATAPOINTNAME, processDatapoint, memoryAgentName,
									SyncMode.READWRITEBACK)));
			this.launcher.createAgent(CellConfig.newConfig(agentName3)
					.addCellfunction(CellFunctionConfig.newConfig(ServiceName, CFIncrementService.class)
							.addManagedDatapoint(INCREMENTATIONDATAPOINTNAME, processDatapoint, memoryAgentName,
									SyncMode.READWRITEBACK)));

//			SystemConfig totalConfig = SystemConfig.newConfig()
//					.addController()
//					.addMemory()
//					.addService()
//					.addService()
//					.addService()
//					.setTopController(controllerAgentName);

			// this.launcher.createDebugUserInterface();

			// this.launcher.init(totalConfig);
			log.info("=== All agents initialized ===");

			launcher.getAgent(memoryAgentName).getCommunicator()
					.write(DatapointBuilder.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			log.info("Datapoints on the way");
			// memoryAgent.getCommunicator().write(Datapoint.newDatapoint(processDatapoint).setValue(new
			// JsonPrimitive(startValue)));
			// Start the system by setting start

			// CellGateway controller = launcher.getTopController();

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
			CellGateway topController = this.launcher.createAgent(CellConfig.newConfig(controllerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(controllerServiceName, SimpleControllerService.class)
							.setProperty("agentname", serviceAgentName).setProperty("servicename", serviceName)
							.setProperty("delay", "10")));
			// SystemConfig totalConfig = SystemConfig.newConfig();
			// totalConfig.addController();

			this.launcher.createAgent(CellConfig.newConfig(memoryAgentName));
			this.launcher.createAgent(CellConfig.newConfig(serviceAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(serviceName, CFIncrementService.class)
							.addManagedDatapoint(INCREMENTATIONDATAPOINTNAME, processDatapoint, memoryAgentName,
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

}
