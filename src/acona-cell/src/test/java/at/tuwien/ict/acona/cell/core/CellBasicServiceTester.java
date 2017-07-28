package at.tuwien.ict.acona.cell.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.cellfunction.specialfunctions.CFDataStorageUpdate;
import at.tuwien.ict.acona.cell.cellfunction.specialfunctions.CFQuery;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.core.cellfunction.helpers.CFDurationThreadTester;
import at.tuwien.ict.acona.cell.core.cellfunction.helpers.SingleNotificationReceiver;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Datapoints;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;
import at.tuwien.ict.acona.jadelauncher.util.KoreExternalControllerImpl;
import jade.core.Runtime;

public class CellBasicServiceTester {

	private static Logger log = LoggerFactory.getLogger(CellBasicServiceTester.class);
	// private final JadeContainerUtil util = new JadeContainerUtil();
	private KoreExternalControllerImpl launchUtil = KoreExternalControllerImpl.getLauncher();
	// private Gateway comm = launchUtil.getJadeGateway();

	// private ContainerController agentContainer;
	// ContainerController mainContainerController;

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

			// launchUtil.initJadeGateway();

			// log.debug("Create gui");
			// this.launchUtil.createDebugUserInterface();

			// Create gateway
			// log.debug("Create gateway");
			// comm = new GatewayImpl();
			// comm.init();
			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}

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
				this.wait(1000);
			} catch (InterruptedException e) {

			}
		}

		Runtime runtime = Runtime.instance();
		runtime.shutDown();
		synchronized (this) {
			try {
				this.wait(1000);
			} catch (InterruptedException e) {

			}
		}
		// this.launchUtil.shutDownJadeGateway();
	}

	/**
	 * First write a value, then read the same value. The test is passed if the
	 * read value is equal to the original one.
	 */
	@Test
	public void executeAsWriteTest() {
		try {
			String receiver = "Receiver";
			String datapointaddress = "testaddress";
			String value = "testvalue";

			// Create inspectoragent
			CellGatewayImpl client = this.launchUtil.createAgent(CellConfig.newConfig("client", CellImpl.class.getName()));
			// Create receiver agent
			CellGatewayImpl server = this.launchUtil.createAgent(CellConfig.newConfig(receiver, CellImpl.class.getName()));

			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}

			List<Datapoint> parameter = new ArrayList<>();
			//parameter.add(Datapoint.newDatapoint("method").setValue("write"));

			List<Datapoint> sendList = new ArrayList<>();
			sendList.add(Datapoints.newDatapoint(datapointaddress).setValue(value));
			JsonRpcRequest req = new JsonRpcRequest("write", 1);
			req.setParameterAsList(0, sendList);

			//JsonArray array = GsonUtils.convertListToJsonArray(sendList);
			//parameter.add(Datapoint.newDatapoint("datapoints").setValue(array));

			//Execute read method
			client.getCommunicator().execute(receiver, "write", req, 100000);
			client.getCommunicator().execute(receiver, "write", req, 100000);
			client.getCommunicator().execute(receiver, "write", req, 100000);
			client.getCommunicator().execute(receiver, "write", req, 100000);

			//List<Datapoint> parameter2 = new ArrayList<>();
			//parameter2.add(Datapoint.newDatapoint("method").setValue("read"));

			List<String> sendList2 = new ArrayList<>();
			sendList2.add(datapointaddress);
			JsonRpcRequest req2 = new JsonRpcRequest("read", 1);
			req2.setParameterAsList(0, sendList2);

			JsonRpcResponse resultdp = client.getCommunicator().execute(receiver, "read", req2, 100000);

			//Datapoint resultdp = client.getCell().getCommunicator().read(datapointaddress, receiver, 10000000);

			String result = resultdp.getResult(new TypeToken<List<Datapoint>>() {
			}).get(0).getValueAsString();
			log.info("Received result={}. Expected result={}", result, value);

			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}

			assertEquals(value, result);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Cannot test system", e);
			fail("Error");
		}
	}

	/**
	 * First write a value, then read the same value. The test is passed if the
	 * read value is equal to the original one.
	 */
	@Test
	public void writeAndReadTest() {
		try {
			String receiver = "Server";
			String datapointaddress = "testaddress";
			String value = "testvalue";

			// Create inspectoragent
			CellGatewayImpl client = this.launchUtil
					.createAgent(CellConfig.newConfig("client", CellImpl.class.getName()));
			// Create receiver agent
			CellGatewayImpl receivergw = this.launchUtil
					.createAgent(CellConfig.newConfig(receiver, CellImpl.class.getName()));

			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}

			client.getCommunicator().setDefaultTimeout(100000);
			receivergw.getCommunicator().setDefaultTimeout(1000000);
			client.getCommunicator().write(receiver, Datapoints.newDatapoint(datapointaddress).setValue(value));
			log.debug("Now read values");
			Datapoint resultdp = client.getCommunicator().read(receiver, datapointaddress, 1000000);

			String result = resultdp.getValue().getAsString();
			log.info("Received result={}. Expected result={}", result, value);

			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}

			assertEquals(value, result);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Cannot init system", e);
			fail("Error");
		}
	}

	/**
	 * A subscriber subscribes a value at the publisher. A value is injected
	 * into the publisher and the subscriber gets notified.
	 * 
	 * Test is passed if the value written to the publisher is the same as the
	 * notified value at the subscriber.
	 */
	@Test
	public void subscribeNotifyTest() {
		// int minWaitTime = 5;
		// Create 2 agents. One shall subscribe the other. One shall be written
		// to. The subscribing agent shall be notified.

		try {
			// create message for subscription. Fields: Address
			String subscriberAgentName = "SubscriberAgent";
			String publisherAgentName = "PublisherAgent";
			String datapointaddress = "subscribe.test.address";
			String value1 = "Wrong value";
			String value2 = "MuHaahAhaAaahAAHA";

			CellGatewayImpl cellControlPublisher = this.launchUtil.createAgent(CellConfig.newConfig(publisherAgentName));

			CellGatewayImpl cellControlSubscriber = this.launchUtil.createAgent(CellConfig.newConfig(subscriberAgentName)
					.addCellfunction(CellFunctionConfig.newConfig("updater", CFDataStorageUpdate.class)
							.addManagedDatapoint(datapointaddress, datapointaddress, publisherAgentName, SyncMode.SUBSCRIBEONLY)));

			synchronized (this) {
				try {
					this.wait(10000);
				} catch (InterruptedException e) {

				}
			}

			cellControlSubscriber.getCommunicator().setDefaultTimeout(200000);
			cellControlPublisher.getCommunicator().setDefaultTimeout(200000);

			// Set init value
			cellControlPublisher.getCommunicator().write(Datapoints.newDatapoint(datapointaddress).setValue(value1));
			log.debug("Get database of publisher={}. Database of subscriber={}", cellControlPublisher.getDataStorage(), cellControlSubscriber.getDataStorage());

			//List<Datapoint> originalValue = cellControlSubscriber.subscribeForeignDatapoint(address, agentName);
			//cellControlSubscriber.getCommunicator().write(originalValue);

			log.debug("Get database of publisher={}, data storage of subscriber={}", cellControlPublisher.getDataStorage(), cellControlSubscriber.getDataStorage());
			// log.debug("Registered subscribers = {}",
			// cellControlPublisher.getDataStorage().getSubscribers());

			synchronized (this) {
				try {
					this.wait(100);
				} catch (InterruptedException e) {

				}
			}

			// Update Datapoint in publisher. It is expected that the subscriber
			// cell is updated too
			cellControlPublisher.getCommunicator().write(Datapoints.newDatapoint(datapointaddress).setValue(value2));
			log.debug("Get database of publisher={}, data storage of subscriber={}", cellControlPublisher.getDataStorage(), cellControlSubscriber.getDataStorage());

			// Check if value was updated in subscribercell

			synchronized (this) {
				try {
					this.wait(100);
				} catch (InterruptedException e) {

				}
			}

			log.debug("Get database of publisher={}, data storage of subscriber={}", cellControlPublisher.getDataStorage(), cellControlSubscriber.getDataStorage());

			String answer = cellControlSubscriber.readLocalDatapoint(datapointaddress).getValue().getAsJsonPrimitive().getAsString();

			log.info("Received result={}. Expected result={}", answer, value2);
			assertEquals(value2, answer);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Cannot test system", e);
			fail("Error");
		}
	}

	/**
	 * A subscriber subscribes a value at a publisher. The value is changed at
	 * the publisher. Then unsubscribe is executed. A secaond value is written
	 * into the publisher.
	 * 
	 * The test is passed if the value of the subscriber is the first written
	 * value into the publisher.
	 */
	@Test
	public void externalUnsubscribeNotifyTest() {
		// int minWaitTime = 10;
		// Create 2 agents. One shall subscribe the other. One shall be written
		// to. The subscribing agent shall be notified.

		try {
			// create message for subscription. Fields: Address
			String subscriberAgentName = "SubscriberAgent";
			String publisherAgentName = "PublisherAgent";
			String datapointaddress = "subscribe.test.address";
			String value1 = "Wrong value1";
			String value2 = "MuHaahAhaAaahAAHA2";

			CellGatewayImpl cellControlPublisher = this.launchUtil.createAgent(CellConfig.newConfig(publisherAgentName, CellImpl.class.getName()));
			CellGatewayImpl cellControlSubscriber = this.launchUtil
					.createAgent(CellConfig.newConfig(subscriberAgentName, CellImpl.class.getName())
							.addCellfunction(CellFunctionConfig.newConfig(CFDataStorageUpdate.class)
									.addManagedDatapoint(datapointaddress, datapointaddress, publisherAgentName, SyncMode.SUBSCRIBEONLY)));

			synchronized (this) {
				try {
					this.wait(100);
				} catch (InterruptedException e) {

				}
			}

			cellControlSubscriber.getCommunicator().setDefaultTimeout(20000);
			cellControlPublisher.getCommunicator().setDefaultTimeout(20000);

			// Set init value
			cellControlPublisher.writeLocalDatapoint(Datapoints.newDatapoint(datapointaddress).setValue(value1));
			//log.debug("Get database of publisher={}", cellControlPublisher.getDataStorage());

			// Subscribe a datapoint of the publisher agent
			//cellControlSubscriber.subscribeForeignDatapoint(datapointaddress, publisherAgentName);

			synchronized (this) {
				try {
					this.wait(100);
				} catch (InterruptedException e) {

				}
			}

			cellControlPublisher.writeLocalDatapoint(Datapoints.newDatapoint(datapointaddress).setValue(value1));
			// Both shall have the same value
			log.debug("Get database of publisher={}", cellControlPublisher.getDataStorage());
			log.debug("Get database of subscriber={}", cellControlSubscriber.getDataStorage());
			// log.debug("Registered subscribers = {}",
			// cellControlPublisher.getDataStorage().getSubscribers());

			synchronized (this) {
				try {
					this.wait(100);
				} catch (InterruptedException e) {

				}
			}

			// Unsubscribe
			cellControlSubscriber.getCell().getCommunicator().unsubscribe(publisherAgentName, datapointaddress);

			// Update Datapoint in publisher. It is expected that the subscriber
			// cell is updated too
			cellControlPublisher.writeLocalDatapoint(Datapoints.newDatapoint(datapointaddress).setValue(value2));
			log.debug("Get database of publisher={}", cellControlPublisher.getDataStorage());
			log.debug("Get database of subscriber={}", cellControlSubscriber.getDataStorage());

			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}

			// Check if value was updated in subscribercell

			log.debug("Get database of publisher={}", cellControlPublisher.getDataStorage());
			log.debug("Datastorage of subscribercell={}", cellControlSubscriber.getDataStorage());

			String answer = cellControlSubscriber.readLocalDatapoint(datapointaddress).getValue().getAsString();// JsonMessage.getBody(result).get(datapointaddress).getAsString();

			log.info("Received result={}. Expected result={}", answer, value1);

			assertEquals(value1, answer);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Cannot test system", e);
			fail("Error");
		}
	}

	/**
	 * A subscriber subscribes a value at a publisher. The value is changed at
	 * the publisher. Then unsubscribe is executed. A secaond value is written
	 * into the publisher.
	 * 
	 * The test is passed if the value of the subscriber is the first written
	 * value into the publisher.
	 */
	@Test
	public void internalsubscribeNotifyTest() {
		// int minWaitTime = 10;
		// Create 2 agents. One shall subscribe the other. One shall be written
		// to. The subscribing agent shall be notified.
		log.info("internalsubscribeNotifyTest");

		try {
			// create message for subscription. Fields: Address
			String subscriberAgentName = "SubscriberAgent";
			String publisherAgentName = "PublisherAgent";
			String datapointaddress = "subscribe.test.address";
			String value1 = "Wrong value1";
			String value2 = "MuHaahAhaAaahAAHA2";

			CellGatewayImpl cellControlPublisher = this.launchUtil.createAgent(CellConfig.newConfig(publisherAgentName, CellImpl.class.getName()));
			CellGatewayImpl cellControlSubscriber = this.launchUtil
					.createAgent(CellConfig.newConfig(subscriberAgentName, CellImpl.class.getName())
							.addCellfunction(CellFunctionConfig.newConfig(SingleNotificationReceiver.class)
									.setProperty("datapoint", publisherAgentName + ":" + datapointaddress)));

			synchronized (this) {
				try {
					this.wait(5000);
				} catch (InterruptedException e) {

				}
			}

			cellControlSubscriber.getCommunicator().setDefaultTimeout(20000);
			cellControlPublisher.getCommunicator().setDefaultTimeout(20000);

			// Set init value
			//cellControlPublisher.writeLocalDatapoint(Datapoint.newDatapoint(datapointaddress).setValue(value1));
			//log.debug("Get database of publisher={}", cellControlPublisher.getDataStorage());
			log.debug("Get database of publisher={}", cellControlPublisher.getDataStorage());
			log.debug("Get database of subscriber={}", cellControlSubscriber.getDataStorage());

			// Subscribe a datapoint of the publisher agent
			//cellControlSubscriber.subscribeForeignDatapoint(datapointaddress, publisherAgentName);

			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}

			cellControlPublisher.writeLocalDatapoint(Datapoints.newDatapoint(datapointaddress).setValue(value1));
			// Both shall have the same value
			log.debug("Get database of publisher={}", cellControlPublisher.getDataStorage());
			log.debug("Get database of subscriber={}", cellControlSubscriber.getDataStorage());
			// log.debug("Registered subscribers = {}",
			// cellControlPublisher.getDataStorage().getSubscribers());

			synchronized (this) {
				try {
					this.wait(100);
				} catch (InterruptedException e) {

				}
			}

			// Unsubscribe
			//cellControlSubscriber.getCell().getCommunicator().unsubscribe(datapointaddress, publisherAgentName);

			// Update Datapoint in publisher. It is expected that the subscriber
			// cell is updated too
			cellControlPublisher.writeLocalDatapoint(Datapoints.newDatapoint(datapointaddress).setValue(value2));
			log.debug("Get database of publisher={}", cellControlPublisher.getDataStorage());
			log.debug("Get database of subscriber={}", cellControlSubscriber.getDataStorage());

			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}

			// Check if value was updated in subscribercell

			log.debug("Get database of publisher={}", cellControlPublisher.getDataStorage());
			log.debug("Datastorage of subscribercell={}", cellControlSubscriber.getDataStorage());

			String answer = cellControlSubscriber.readLocalDatapoint(datapointaddress).getValue().getAsString();// JsonMessage.getBody(result).get(datapointaddress).getAsString();

			log.info("Received result={}. Expected result={}", answer, value1);

			assertEquals(value1, answer);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Cannot test system", e);
			fail("Error");
		}
	}

	/**
	 * A subscriber subscribes a value at a publisher. The value is changed at
	 * the publisher. Then unsubscribe is executed. A secaond value is written
	 * into the publisher.
	 * 
	 * The test is passed if the value of the subscriber is the first written
	 * value into the publisher.
	 */
	//@Test
	public void internalfailedsubscribeTest() {
		// int minWaitTime = 10;
		// Create 2 agents. One shall subscribe the other. One shall be written
		// to. The subscribing agent shall be notified.
		log.info("internalfailedsubscribeTest");

		try {
			// create message for subscription. Fields: Address
			String subscriberAgentName = "SubscriberAgent";
			String publisherAgentName = "PublisherAgent";
			String datapointaddress = "subscribe.test.address";
			String value1 = "Wrong value1";
			String value2 = "MuHaahAhaAaahAAHA2";

			CellGatewayImpl cellControlSubscriber = this.launchUtil
					.createAgent(CellConfig.newConfig(subscriberAgentName, CellImpl.class.getName())
							.addCellfunction(CellFunctionConfig.newConfig(SingleNotificationReceiver.class)
									.setProperty("datapoint", publisherAgentName + ":" + datapointaddress)));

			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {

				}
			}

			cellControlSubscriber.getCommunicator().setDefaultTimeout(20000);

			synchronized (this) {
				try {
					this.wait(100);
				} catch (InterruptedException e) {

				}
			}

			// Unsubscribe
			//cellControlSubscriber.getCell().getCommunicator().unsubscribe(datapointaddress, publisherAgentName);

			// Update Datapoint in publisher. It is expected that the subscriber
			// cell is updated too
			//cellControlPublisher.writeLocalDatapoint(Datapoints.newDatapoint(datapointaddress).setValue(value2));
			//log.debug("Get database of publisher={}", cellControlPublisher.getDataStorage());
			log.debug("Get database of subscriber={}", cellControlSubscriber.getDataStorage());

			synchronized (this) {
				try {
					this.wait(20000);
				} catch (InterruptedException e) {

				}
			}

			// Check if value was updated in subscribercell

			//log.debug("Get database of publisher={}", cellControlPublisher.getDataStorage());
			log.debug("Datastorage of subscribercell={}", cellControlSubscriber.getDataStorage());

			String answer = cellControlSubscriber.readLocalDatapoint(datapointaddress).getValue().getAsString();// JsonMessage.getBody(result).get(datapointaddress).getAsString();

			log.info("Received result={}. Expected result={}", answer, value1);

			assertEquals(value1, answer);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Cannot test system", e);
			fail("Error");
		}
	}

	/**
	 * Instantiate 3 or more agents. Agent n subscribes a datapoint from agent
	 * n-1. In agent 0, a value is set and propagated through the system to
	 * agent n.
	 * 
	 * This value is measured and checked if it is the input value.
	 * 
	 * The test is passed if input value of the system is equal to the value of
	 * the last agent.
	 * 
	 */
	@Test
	public void massOfSubscribersTest() {
		// final int minWaitTime = 5;
		final int numberOfAgents = 10; // If there are errors with nullpointers.
										// Set the timeouts of the queues in the
										// communication!!

		// Create 2 agents. One shall subscribe the other. One shall be written
		// to. The subscribing agent shall be notified.

		try {
			// create message for subscription. Fields: Address
			String agentNameTemplate = "agent";
			// String publisherAgentName = "PublisherAgent";

			String datapointaddress = "subscribe.test.address";
			// String value1 = "init";
			String value2 = "MuHaahAhaAaahAAHA";

			// Create X=5 agents
			List<CellGatewayImpl> inspectors = new ArrayList<>();
			CellGatewayImpl firstCell = this.launchUtil.createAgent(CellConfig.newConfig(agentNameTemplate + 0));
			inspectors.add(firstCell);
			for (int i = 1; i < numberOfAgents; i++) {
				CellGatewayImpl cell = (this.launchUtil
						.createAgent(CellConfig.newConfig(agentNameTemplate + i)
								.addCellfunction(CellFunctionConfig.newConfig("updater", CFDataStorageUpdate.class)
										.addManagedDatapoint(datapointaddress, datapointaddress, inspectors.get(i - 1).getCell().getLocalName(), SyncMode.SUBSCRIBEONLY))));
				inspectors.add(cell);
				cell.getCommunicator().setDefaultTimeout(10000);

			}

			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {

				}
			}

			// Set subscriptions
			for (int i = 1; i < numberOfAgents; i++) {
				CellGatewayImpl thisController = inspectors.get(i);
				CellGatewayImpl previousController = inspectors.get(i - 1);

				//thisController.subscribeForeignDatapoint(datapointaddress, previousController.getCell().getLocalName());
			}

			synchronized (this) {
				try {
					this.wait(10000);
				} catch (InterruptedException e) {

				}
			}

			// Set the first value and let the chain update itself
			// Update Datapoint in publisher. It is expected that the subscriber
			// cell is updated too

			//Start tic
			long starttime = System.currentTimeMillis();
			log.info("=================Start time measurement: {}=====================", starttime);
			inspectors.get(0).writeLocalDatapoint(Datapoints.newDatapoint(datapointaddress).setValue(value2));
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
			String answer = inspectors.get(numberOfAgents - 1).readLocalDatapoint(datapointaddress).getValue().getAsString();// JsonMessage.getBody(result).get(datapointaddress).getAsString();

			assertEquals(value2, answer);

		} catch (Exception e) {
			log.error("Cannot test system", e);
			fail("Error");
		}
	}

	/**
	 * Idea: The agent shall write a datapoint
	 * 
	 */
	@Test
	public void CFQueryOnOneAgentTester() {
		try {
			String agentName = "agent";
			String destinationAddress = CFDurationThreadTester.queryDatapointID;
			String resultAddress = CFDurationThreadTester.resultDatapointID;
			double value = 1.3;
			double expectedResult = value;

			//Create cell
			CellGatewayImpl agent = this.launchUtil.createAgent(CellConfig.newConfig(agentName).addCellfunction(
					CellFunctionConfig.newConfig(CFDurationThreadTester.class)
							.addManagedDatapoint(CFDurationThreadTester.queryDatapointID, destinationAddress, "", SyncMode.SUBSCRIBEONLY)
							.addManagedDatapoint(CFDurationThreadTester.resultDatapointID, resultAddress, "", SyncMode.WRITEONLY)));

			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}
			log.info("=== All agents initialized ===");
			CFQuery q = new CFQuery();
			Datapoint resultDP = q.newQuery(agentName, destinationAddress, new JsonPrimitive(value), agentName, resultAddress, 100000, agent.getCell());

			String result = resultDP.getValue().getAsString();
			log.debug("correct value={}, actual value={}", expectedResult, result);

			assertEquals(result, "FINISHED");
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

}
