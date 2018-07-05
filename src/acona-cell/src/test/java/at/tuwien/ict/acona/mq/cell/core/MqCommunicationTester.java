package at.tuwien.ict.acona.mq.cell.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.concurrent.Semaphore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.mq.cell.cellfunction.helper.RequesterResponseFunction;
import at.tuwien.ict.acona.mq.cell.communication.MqttCommunicator;
import at.tuwien.ict.acona.mq.cell.communication.MqttCommunicatorImpl;
import at.tuwien.ict.acona.mq.datastructures.Datapoint;

public class MqCommunicationTester {
	private static Logger log = LoggerFactory.getLogger(MqCommunicationTester.class);
	// private SystemControllerImpl launcher = SystemControllerImpl.getLauncher();

	@Before
	public void setUp() throws Exception {
		try {
			// Create container
//			log.debug("Create or get main container");
//			this.launcher.createMainContainer("localhost", 1099, "MainContainer");
//
//			log.debug("Create subcontainer");
//			this.launcher.createSubContainer("localhost", 1099, "Subcontainer");

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
//		synchronized (this) {
//			try {
//				this.wait(200);
//			} catch (InterruptedException e) {
//
//			}
//		}
//
//		//launcher.stopSystem();
//
//		// Runtime runtime = Runtime.instance();
//		// runtime.shutDown();
//		synchronized (this) {
//			try {
//				this.wait(200);
//			} catch (InterruptedException e) {
//
//			}
//		}
	}

	/**
	 * 
	 */
	@Test
	public void testServiceRequestRespondPattern() {
		try {
			String host = "tcp://127.0.0.1:1883";
			String username = "acona";
			String password = "acona";

			String functionNameResponder = "FunctionResponder";
			String functionNameRequester = "FunctionRequester";

			String agentName = "agent1";

			int numberOfRuns = 200;

			final Semaphore sem = new Semaphore(0);
			RequesterResponseFunction responder = new RequesterResponseFunction();
			responder.init(sem, host, username, password, agentName, functionNameResponder, agentName + "/" + functionNameResponder + "/" + "increment", false);

			RequesterResponseFunction requester = new RequesterResponseFunction();
			requester.setNumberOfRuns(2000);
			requester.init(sem, host, username, password, agentName, functionNameRequester, agentName + "/" + functionNameResponder + "/" + "increment", true);

			// Aquire run as both threads are finished
			sem.acquire();

			log.debug("correct value={}, actual value={}", numberOfRuns, requester.getValue());
			assertEquals(numberOfRuns, requester.getValue(), 0.0);
			log.info("Test passed");

			// launcher.stopSystem();
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

	/**
	 * 
	 */
	@Test
	public void testReadWriteFunction() {
		try {
			String host = "tcp://127.0.0.1:1883";
			String username = "acona";
			String password = "acona";

			String functionName = "DummyFunction";
			String agentName = "agent1";

			String addressToRead = "agent1/database/workingmemory/episode1s";
			double value = 1.99;

			int numberOfRuns = 200;

			// ============================================================//
			MqttCommunicator comm = new MqttCommunicatorImpl();
			comm.init(host, username, password, agentName, functionName);
			comm.setDefaultTimeout(1000);
			// Write the topic

			// Write the topic
			comm.write((new Datapoint(addressToRead)).setValue(value));

			// Read the datapoint
			Datapoint dp = comm.read(addressToRead);

			log.debug("correct value={}, actual value={}", value, dp.getValue().getAsDouble());
			assertEquals(value, dp.getValue().getAsDouble(), 0.0);
			log.info("Test passed");

			// launcher.stopSystem();
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}
}
