package at.tuwien.ict.acona.mq.core.core;

import static org.junit.Assert.fail;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.mq.core.agentfunction.specialfunctions.DatapointTransfer;
import at.tuwien.ict.acona.mq.core.config.AgentConfig;
import at.tuwien.ict.acona.mq.core.config.FunctionConfig;
import at.tuwien.ict.acona.mq.launcher.SystemControllerImpl;

public class MqCommunicatorTester {
	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private SystemControllerImpl launcher = SystemControllerImpl.getLauncher();

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
	public void testSetOtherPort() {
		try {
			String host1 = "tcp://127.0.0.1:8883";
			String host2 = "tcp://127.0.0.1:6883";
			String username = "acona1";
			String password = "acona1";

			AgentConfig acoconf = AgentConfig.newConfig("CommunicationTester").setHost(host1)
					.addFunction(FunctionConfig.newConfig(DatapointTransfer.class)
							.setProperty(DatapointTransfer.PARAMSOURCEADDRESS, "<" + "CommunicationTester" + ">/" + "origin")
							.setProperty(DatapointTransfer.PARAMDESTINATIONADDRESS, "<" + "CommunicationTester" + ">/" + "target")
							//);
							.setHostData(host2, username, password));
				
			log.debug("Do not instantiate the cell.");
				//Cell aco = this.launcher.createAgent(acoconf);
				
				synchronized (this) {
					try {
						this.wait(100);
					} catch (InterruptedException e) {

					}
				}
				
				log.info("=== Init finished ===");
			
			assert(acoconf.getHost()==host1);
			assert(acoconf.getCellfunctions().get(0).getAsJsonObject().get("host").getAsJsonPrimitive().getAsString().equals(host2));
			
			log.info("Test passed");

			// launcher.stopSystem();
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}
	}

//	/**
//	 * 
//	 */
//	@Test
//	public void testServiceRequestRespondPattern() {
//		try {
//			String host = "tcp://127.0.0.1:1883";
//			String username = "acona";
//			String password = "acona";
//
//			String functionNameResponder = "FunctionResponder";
//			String functionNameRequester = "FunctionRequester";
//
//			String agentName = "agent1";
//
//			String targetName = "<agent1>/FunctionResponder/increment";
//			int numberOfRuns = 20000;
//
//			final Semaphore sem = new Semaphore(0);
//			RequesterResponseFunction responder = new RequesterResponseFunction();
//			responder.init(sem, host, username, password, agentName, functionNameResponder, targetName, false);
//
//			RequesterResponseFunction requester = new RequesterResponseFunction();
//			requester.setNumberOfRuns(numberOfRuns);
//			requester.init(sem, host, username, password, agentName, functionNameRequester, targetName, true);
//
//			// Aquire run as both threads are finished
//			sem.acquire();
//			log.debug("test");
//
//			log.debug("correct value={}, actual value={}", numberOfRuns, requester.getValue());
//			assertEquals(numberOfRuns, requester.getValue(), 0.0);
//			log.info("Test passed");
//
//			// launcher.stopSystem();
//		} catch (Exception e) {
//			log.error("Error testing system", e);
//			fail("Error");
//		}
//	}
//
//	/**
//	 * 
//	 */
//	@Test
//	public void testReadWriteFunction() {
//		try {
//			String host = "tcp://127.0.0.1:1883";
//			String username = "acona";
//			String password = "acona";
//
//			String functionName = "DummyFunction";
//			String agentName = "agent1";
//
//			String addressToRead = "<agent1>/database/workingmemory/episode1s";
//			double value = 1.99;
//
//			// ============================================================//
//			MqttCommunicator comm = new MqttCommunicatorImpl(new DataStorageImpl());
//			comm.init(host, username, password, new CellFunctionDummy(functionName, agentName));
//			comm.setDefaultTimeout(1000);
//			// Write the topic
//
//			// Write the topic
//			comm.write((new Datapoint(addressToRead)).setValue(value));
//
//			// Read the datapoint
//			Datapoint dp = comm.read(addressToRead);
//
//			log.debug("correct value={}, actual value={}", value, dp.getValue().getAsDouble());
//			assertEquals(value, dp.getValue().getAsDouble(), 0.0);
//			log.info("Test passed");
//
//		} catch (Exception e) {
//			log.error("Error testing system", e);
//			fail("Error");
//		}
//	}
//
//	/**
//	 * 
//	 */
//	@Test
//	public void testReadEmptyChannelFunction() {
//		try {
//			String host = "tcp://127.0.0.1:1883";
//			String username = "acona";
//			String password = "acona";
//
//			String functionName = "DummyFunction";
//			String agentName = "agent1";
//
//			String addressToRead = "agent1/database/workingmemory/episode1";
//			double value = 1.99;
//			double expectedValue = 0;
//
//			int numberOfRuns = 200;
//
//			// ============================================================//
//			MqttCommunicator comm = new MqttCommunicatorImpl(new DataStorageImpl());
//			comm.init(host, username, password, new CellFunctionDummy(functionName, agentName));
//			comm.setDefaultTimeout(1000);
//			// Write the topic
//
//			//
//			// comm.write((new Datapoint(addressToRead)).setValue(value));
//
//			// Read the datapoint
//			Datapoint dp = comm.read(addressToRead);
//
//			log.debug("correct value={}, actual value={}", expectedValue, dp.getValueOrDefault(new JsonPrimitive(0)).getAsDouble());
//			assertEquals(expectedValue, dp.getValueOrDefault(new JsonPrimitive(0)).getAsDouble(), 0.0);
//			log.info("Test passed");
//
//			// launcher.stopSystem();
//		} catch (Exception e) {
//			log.error("Error testing system", e);
//			fail("Error");
//		}
//	}
}
