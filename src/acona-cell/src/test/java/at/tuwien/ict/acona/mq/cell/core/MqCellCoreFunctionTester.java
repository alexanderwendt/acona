package at.tuwien.ict.acona.mq.cell.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.invoke.MethodHandles;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.mq.cell.cellfunction.specialfunctions.DatapointTransfer;
import at.tuwien.ict.acona.mq.cell.config.CellConfig;
import at.tuwien.ict.acona.mq.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.mq.datastructures.DPBuilder;
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
							.setProperty(DatapointTransfer.PARAMSOURCEADDRESS, datapointSourceAddress)
							.setProperty(DatapointTransfer.PARAMDESTINATIONADDRESS, datapointDestinationAddress));
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

}
