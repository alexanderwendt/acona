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

public class MqCellCoreTester {
	private static Logger log = LoggerFactory.getLogger(MqCellCoreTester.class);
	// private SystemControllerImpl launcher = SystemControllerImpl.getLauncher();

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
	 * Create 2 cells.
	 * 
	 * 
	 */
	@Test
	public void testCellToCellCommunicationThroughDatapoints() {
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
			requester.setNumberOfRuns(200);
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

}
