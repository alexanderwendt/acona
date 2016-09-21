package at.tuwien.ict.acona.cell.core;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.config.CellConfigJadeBehaviour;
import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.core.InspectorCell;
import at.tuwien.ict.acona.cell.core.CellGatewayImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.util.JadelauncherUtil;
import at.tuwien.ict.acona.jadelauncher.util.JadeContainerUtil;
import jade.core.Runtime;
import jade.wrapper.ContainerController;

public class CellServiceTester {
	
	private static Logger log = LoggerFactory.getLogger(CellServiceTester.class);
	private final JadeContainerUtil util = new JadeContainerUtil();
	private JadelauncherUtil launchUtil = JadelauncherUtil.getUtil();
	//private Gateway comm = launchUtil.getJadeGateway();
	
	
	private ContainerController agentContainer;
	ContainerController mainContainerController;

	/**
	 * Setup the JADE communication. No Jade Gateway necessary
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		try {
			
			//Create container
			log.debug("Create or get main container");
			this.launchUtil.createMainContainer("localhost", 1099, "MainContainer");
			//mainContainerController = this.util.createMainJADEContainer("localhost", 1099, "MainContainer");
					
			log.debug("Create subcontainer");
			this.launchUtil.createSubContainer("localhost", 1099, "Subcontainer"); 
			
			//launchUtil.initJadeGateway();
			
			//log.debug("Create gui");
			//this.launchUtil.createDebugUserInterface();
			
			//Create gateway
//			log.debug("Create gateway");
//			comm = new GatewayImpl();
//			comm.init();
			
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
		//this.launchUtil.shutDownJadeGateway();
	}
	
	/**
	 * First write a value, then read the same value. The test is passed if the read value is equal to the original one.
	 */
	@Test
	public void writeAndReadTest() {
		try {
			String receiver = "CellAgent";
			String datapointaddress = "testaddress";
			String value = "testvalue";
			
			//Create inspectoragent
			CellGatewayImpl client = this.launchUtil.createInspectorAgent(CellConfigJadeBehaviour.newConfig("inspectorgateway", InspectorCell.class.getName()));
			//Create receiver agent
			this.launchUtil.createAgent(CellConfigJadeBehaviour.newConfig(receiver, CellImpl.class.getName()));
			
//			synchronized (this) {
//				try {
//					this.wait(200);
//				} catch (InterruptedException e) {
//					
//				}
//			}
			
			client.getCommunicator().write(Datapoint.newDatapoint(datapointaddress).setValue(value), receiver);
			Datapoint resultdp = client.getCell().getCommunicator().read(Datapoint.newDatapoint(datapointaddress), receiver, 100000);

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
	 * A subscriber subscribes a value at the publisher. A value is injected into the publisher and the subscriber gets notified. 
	 * 
	 * Test is passed if the value written to the publisher is the same as the notified value at the subscriber.
	 */
	@Test
	public void subscribeNotifyTest() {
		//int minWaitTime = 5;
		//Create 2 agents. One shall subscribe the other. One shall be written to. The subscribing agent shall be notified.
	
		
		try {
			//create message for subscription. Fields: Address
			String subscriberAgentName = "SubscriberAgent";
			String publisherAgentName = "PublisherAgent";
			String datapointaddress = "subscribe.test.address";
			String value1 = "Wrong value";
			String value2 = "MuHaahAhaAaahAAHA";
			
			CellGatewayImpl cellControlSubscriber = this.launchUtil.createInspectorAgent(CellConfigJadeBehaviour.newConfig(subscriberAgentName, InspectorCell.class.getName()));
			CellGatewayImpl cellControlPublisher = this.launchUtil.createInspectorAgent(CellConfigJadeBehaviour.newConfig(publisherAgentName, InspectorCell.class.getName()));
			
			//Set init value
			cellControlPublisher.getCell().getDataStorage().write(Datapoint.newDatapoint(datapointaddress).setValue(value1), cellControlPublisher.getCell().getName());
			log.debug("Get database of publisher={}", cellControlPublisher.getCell().getDataStorage());

			
			cellControlSubscriber.getCommunicator().subscribe(Arrays.asList(Datapoint.newDatapoint(datapointaddress)), publisherAgentName);
			
			log.debug("Get database of publisher={}", cellControlPublisher.getCell().getDataStorage());
			log.debug("Get database of subscriber={}", cellControlSubscriber.getCell().getDataStorage());
			//log.debug("Registered subscribers = {}", cellControlPublisher.getCell().getDataStorage().getSubscribers());
			
			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {
				
				}
			}
			
			//Update Datapoint in publisher. It is expected that the subscriber cell is updated too
			cellControlPublisher.getCell().getCommunicator().write(Datapoint.newDatapoint(datapointaddress).setValue(value2), publisherAgentName);
			log.debug("Get database of publisher={}", cellControlPublisher.getCell().getDataStorage());
			
			//Check if value was updated in subscribercell
			
			log.debug("Datastorage of subscribercell={}", cellControlSubscriber.getCell().getDataStorage());
			
			String answer = cellControlSubscriber.readLocalDatapoint(datapointaddress).getValue().getAsJsonPrimitive().getAsString(); //JsonMessage.getBody(result).get(datapointaddress).getAsString();
		
			log.info("Received result={}. Expected result={}", answer, value2);
			assertEquals(value2, answer);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Cannot test system", e);
			fail("Error");
		}
	}
	
	/**
	 * A subscriber subscribes a value at a publisher. The value is changed at the publisher. Then unsubscribe is executed. A secaond value is 
	 * written into the publisher. 
	 * 
	 * The test is passed if the value of the subscriber is the first written value into the publisher.
	 */
	@Test
	public void unsubscribeNotifyTest() {
		//int minWaitTime = 10;
		//Create 2 agents. One shall subscribe the other. One shall be written to. The subscribing agent shall be notified.
	
		
		try {
			//create message for subscription. Fields: Address
			String subscriberAgentName = "SubscriberAgent";
			String publisherAgentName = "PublisherAgent";
			String datapointaddress = "subscribe.test.address";
			String value1 = "Wrong value1";
			String value2 = "MuHaahAhaAaahAAHA2";
			
			CellGatewayImpl cellControlSubscriber = this.launchUtil.createInspectorAgent(CellConfigJadeBehaviour.newConfig(subscriberAgentName, InspectorCell.class.getName()));
			CellGatewayImpl cellControlPublisher = this.launchUtil.createInspectorAgent(CellConfigJadeBehaviour.newConfig(publisherAgentName, InspectorCell.class.getName()));
			
			//Set init value
			cellControlPublisher.writeLocalDatapoint(Datapoint.newDatapoint(datapointaddress).setValue(value1));
			log.debug("Get database of publisher={}", cellControlPublisher.getCell().getDataStorage());
			
			
			//Subscribe a datapoint of the publisher agent
			cellControlSubscriber.subscribeForeignDatapoint(datapointaddress, publisherAgentName);
			
			cellControlPublisher.writeLocalDatapoint(Datapoint.newDatapoint(datapointaddress).setValue(value1));
			//Both shall have the same value
			log.debug("Get database of publisher={}", cellControlPublisher.getCell().getDataStorage());
			log.debug("Get database of subscriber={}", cellControlSubscriber.getCell().getDataStorage());
			//log.debug("Registered subscribers = {}", cellControlPublisher.getCell().getDataStorage().getSubscribers());
			
			//Unsubscribe
			cellControlSubscriber.unsubscribeLocalDatapoint(datapointaddress, publisherAgentName);
			
			//Update Datapoint in publisher. It is expected that the subscriber cell is updated too
			cellControlPublisher.writeLocalDatapoint(Datapoint.newDatapoint(datapointaddress).setValue(value2));
			log.debug("Get database of publisher={}", cellControlPublisher.getCell().getDataStorage());
			
			synchronized (this) {
				try {
					this.wait(100);
				} catch (InterruptedException e) {
					
				}
			}
			
			//Check if value was updated in subscribercell
			
			log.debug("Datastorage of subscribercell={}", cellControlSubscriber.getCell().getDataStorage());
			
			String answer = cellControlSubscriber.readLocalDatapoint(datapointaddress).getValue().getAsString();//JsonMessage.getBody(result).get(datapointaddress).getAsString();
			
			log.info("Received result={}. Expected result={}", answer, value1);
			
			assertEquals(value1, answer);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Cannot init system", e);
			fail("Error");
		}
	}
	
	/**
	 * Instantiate 3 or more agents. Agent n subscribes a datapoint from agent n-1. In agent 0, a value is set and propagated through the system to agent n. 
	 * 
	 * This value is measured and checked if it is the input value. 
	 * 
	 * The test is passed if input value of the system is equal to the value of the last agent.
	 * 
	 */
	@Test
	public void massOfSubscribersTest() {
		//final int minWaitTime = 5;
		final int numberOfAgents = 10;	//If there are errors with nullpointers. Set the timeouts of the queues in the communication!!
		
		
		//Create 2 agents. One shall subscribe the other. One shall be written to. The subscribing agent shall be notified.
	
		
		try {
			//create message for subscription. Fields: Address
			String agentNameTemplate = "agent";
			//String publisherAgentName = "PublisherAgent";
			
			String datapointaddress = "subscribe.test.address";
			//String value1 = "init";
			String value2 = "MuHaahAhaAaahAAHA";
			
			//Create X=5 agents
			List<CellGatewayImpl> inspectors = new ArrayList<CellGatewayImpl>();
			for (int i=0; i<numberOfAgents; i++) {
				CellGatewayImpl cell = (this.launchUtil.createInspectorAgent(CellConfigJadeBehaviour.newConfig(agentNameTemplate + i, InspectorCell.class.getName())));
				inspectors.add(cell);
				cell.getCell().getCommunicator().setDefaultTimeout(10000);
			}
			
			//Set subscriptions
			for (int i=1;i<numberOfAgents;i++) {
				CellGatewayImpl thisController = inspectors.get(i);
				CellGatewayImpl previousController = inspectors.get(i-1);
				
				thisController.subscribeForeignDatapoint(datapointaddress, previousController.getCell().getLocalName());
			}
			
			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {
					
				}
			}
			
			//Set the first value and let the chain update itself
			//Update Datapoint in publisher. It is expected that the subscriber cell is updated too
			inspectors.get(0).getCell().getDataStorage().write(Datapoint.newDatapoint(datapointaddress).setValue(value2), "nothing");
			log.debug("Get database of publisher={}", inspectors.get(0).getCell().getDataStorage());
			
			//Get the value from the last agent
			log.info("Datastorage of the last agent={}", inspectors.get(numberOfAgents-1).getCell().getDataStorage());
			
			String answer = inspectors.get(numberOfAgents-1).readLocalDatapoint(datapointaddress).getValue().getAsString();//JsonMessage.getBody(result).get(datapointaddress).getAsString();
			
			assertEquals(value2, answer);
			
		} catch (Exception e) {
			log.error("Cannot init system", e);
			fail("Error");
		}
	}


}
