package at.tuwien.ict.kore.cell.core;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import at.tuwien.ict.kore.cell.datastructures.DatapackageImpl;
import at.tuwien.ict.kore.communicator.core.Communicator;
import at.tuwien.ict.kore.communicator.core.CommunicatorImpl;
import at.tuwien.ict.kore.communicator.core.JsonMessage;
import at.tuwien.ict.kore.communicator.util.JadeContainerUtil;
import jade.core.Runtime;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.State;

public class CellServiceTester {
	
	private static Logger log = LoggerFactory.getLogger(CellServiceTester.class);
	private final JadeContainerUtil util = new JadeContainerUtil();
	private Communicator comm;
	
	private ContainerController agentContainer;
	ContainerController mainContainerController;

	@Before
	public void setUp() throws Exception {
		try {
			//Create container
			log.debug("Create or get main container");
			mainContainerController = this.util.createMainJADEContainer("localhost", 1099, "MainContainer");
					
			log.debug("Create subcontainer");
			agentContainer = this.util.createAgentContainer("localhost", 1099, "Subcontainer"); 
			
			//log.debug("Create gui");
			//this.util.createRMAInContainer(agentContainer);
			
			//Create gateway
			log.debug("Create gateway");
			comm = new CommunicatorImpl();
			comm.init();
			
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
		this.comm.shutDown();
	}

	@Test
	public void writeAndReadTest() {
		try {
			//create message
			//String messageTypeRead = "read";
			//String messageTypeWrite  ="write";
			String receiver = "CellAgent";
			String datapointaddress = "testaddress";
			String value = "testvalue";
			
			//Create agent in the system
			//String[] args = {"1", "pong"};
			AgentController cellAgent = this.util.createAgent(receiver, CellImpl.class, agentContainer);
			
			//Send Write command
			//Create message body
			JsonObject writeBody = new JsonObject();
			writeBody.addProperty(JsonMessage.DATAPOINTADDRESS, datapointaddress);
			writeBody.addProperty(JsonMessage.VALUE, value);
			
			JsonObject ack = this.comm.sendSynchronousMessageToAgent(writeBody.toString(), receiver, JsonMessage.SERVICEWRITE, 10000);
			log.debug("Data written to cell={}", ack);
			
			JsonObject result = this.comm.sendSynchronousMessageToAgent(JsonMessage.toJsonObjectString(JsonMessage.DATAPOINTADDRESS, datapointaddress), receiver, JsonMessage.SERVICEREAD, 100000);
			log.debug("Received data={}", result);
//			log.debug("wait for agent to answer");
//			synchronized (this) {
//				try {
//					this.wait(5000);
//				} catch (InterruptedException e) {
//					
//				}
//			}
			
			String answer = JsonMessage.getBody(result).get(datapointaddress).getAsString();
			
			assertEquals(value, answer);
		} catch (Exception e) {
			log.error("Cannot init system", e);
			fail("Error");
		}
	}
	
	@Test
	public void subscribeNotifyTest() {
		int minWaitTime = 5;
		//Create 2 agents. One shall subscribe the other. One shall be written to. The subscribing agent shall be notified.
	
		
		try {
			//create message for subscription. Fields: Address
			String subscriberAgentName = "SubscriberAgent";
			String publisherAgentName = "PublisherAgent";
			String datapointaddress = "subscribe.test.address";
			String value1 = "Wrong value";
			String value2 = "MuHaahAhaAaahAAHA";
			
			//Create cell inspector controller for the subscriber
			CellInspectorController cellControlSubscriber = new CellInspectorController();
			Object[] argsSubscriber = new Object[1];
			argsSubscriber[0] = cellControlSubscriber;
			//Create agent in the system
			this.util.createAgent(subscriberAgentName, CellInspector.class, argsSubscriber, agentContainer);
			
			//Create cell inspector controller for the subscriber
			CellInspectorController cellControlPublisher = new CellInspectorController();
			Object[] argsPublisher = new Object[1];
			argsPublisher[0] = cellControlPublisher;
			//Create agent in the system
			AgentController publisherController = this.util.createAgent(publisherAgentName, CellInspector.class, argsPublisher, agentContainer);
			log.debug("State={}", publisherController.getState());
			State state = publisherController.getState();
			
			log.debug("wait for agent to answer");
			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {
					
				}
			}
			log.debug("State={}", publisherController.getState());
			
			//Set init value
			cellControlPublisher.getCell().getDataStorage().write(datapointaddress, DatapackageImpl.newDatapackage(datapointaddress, value1), cellControlPublisher.getCell().getName());
			log.debug("Get database of publisher={}", cellControlPublisher.getCell().getDataStorage());
			
			
			//Subscribe a datapoint of the publisher agent
			//Create Message
			JsonObject message = JsonMessage.createMessage(JsonMessage.toJsonObjectString(JsonMessage.DATAPOINTADDRESS, datapointaddress), publisherAgentName, JsonMessage.SERVICESUBSCRIBE);
			ACLMessage msg = JsonMessage.convertToACL(message);
			msg.setPerformative(ACLMessage.REQUEST);
			log.debug("Send message", msg);
			//Send message from subscribercell
			cellControlSubscriber.getCell().send(msg);
			
			synchronized (this) {
				try {
					this.wait(minWaitTime);
				} catch (InterruptedException e) {
					
				}
			}
			
			log.debug("Get database of publisher={}", cellControlPublisher.getCell().getDataStorage());
			log.debug("Get database of subscriber={}", cellControlSubscriber.getCell().getDataStorage());
			//log.debug("Registered subscribers = {}", cellControlPublisher.getCell().getDataStorage().getSubscribers());
			
			//Update Datapoint in publisher. It is expected that the subscriber cell is updated too
			cellControlPublisher.getCell().getDataStorage().write(datapointaddress, DatapackageImpl.newDatapackage(datapointaddress, value2), cellControlPublisher.getCell().getName());
			log.debug("Get database of publisher={}", cellControlPublisher.getCell().getDataStorage());
			
			synchronized (this) {
				try {
					this.wait(minWaitTime);
				} catch (InterruptedException e) {
					
				}
			}
			
			//Check if value was updated in subscribercell
			
			log.debug("Datastorage of subscribercell={}", cellControlSubscriber.getCell().getDataStorage());
			
			String answer = cellControlSubscriber.getCell().getDataStorage().read(datapointaddress).get(datapointaddress).getDefaultValue();//JsonMessage.getBody(result).get(datapointaddress).getAsString();
			
			assertEquals(value2, answer);
		} catch (Exception e) {
			log.error("Cannot init system", e);
			fail("Error");
		}
	}
	
	@Test
	public void unsubscribeNotifyTest() {
		int minWaitTime = 5;
		//Create 2 agents. One shall subscribe the other. One shall be written to. The subscribing agent shall be notified.
	
		
		try {
			//create message for subscription. Fields: Address
			String subscriberAgentName = "SubscriberAgent";
			String publisherAgentName = "PublisherAgent";
			String datapointaddress = "subscribe.test.address";
			String value1 = "Wrong value";
			String value2 = "MuHaahAhaAaahAAHA";
			
			//Create cell inspector controller for the subscriber
			CellInspectorController cellControlSubscriber = new CellInspectorController();
			Object[] argsSubscriber = new Object[1];
			argsSubscriber[0] = cellControlSubscriber;
			//Create agent in the system
			this.util.createAgent(subscriberAgentName, CellInspector.class, argsSubscriber, agentContainer);
			
			//Create cell inspector controller for the subscriber
			CellInspectorController cellControlPublisher = new CellInspectorController();
			Object[] argsPublisher = new Object[1];
			argsPublisher[0] = cellControlPublisher;
			//Create agent in the system
			AgentController publisherController = this.util.createAgent(publisherAgentName, CellInspector.class, argsPublisher, agentContainer);
			log.debug("State={}", publisherController.getState());
			State state = publisherController.getState();
			
			log.debug("wait for agent to answer");
			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {
					
				}
			}
			log.debug("State={}", publisherController.getState());
			
			//Set init value
			cellControlPublisher.getCell().getDataStorage().write(datapointaddress, DatapackageImpl.newDatapackage(datapointaddress, value2), cellControlPublisher.getCell().getName());
			log.debug("Get database of publisher={}", cellControlPublisher.getCell().getDataStorage());
			
			
			//Subscribe a datapoint of the publisher agent
			//Create Message
			JsonObject message = JsonMessage.createMessage(JsonMessage.toJsonObjectString(JsonMessage.DATAPOINTADDRESS, datapointaddress), publisherAgentName, JsonMessage.SERVICESUBSCRIBE);
			ACLMessage msg = JsonMessage.convertToACL(message);
			msg.setPerformative(ACLMessage.REQUEST);
			log.debug("Send message", msg);
			//Send message from subscribercell
			cellControlSubscriber.getCell().send(msg);
			
			synchronized (this) {
				try {
					this.wait(minWaitTime);
				} catch (InterruptedException e) {
					
				}
			}
			
			log.debug("Get database of publisher={}", cellControlPublisher.getCell().getDataStorage());
			log.debug("Get database of subscriber={}", cellControlSubscriber.getCell().getDataStorage());
			//log.debug("Registered subscribers = {}", cellControlPublisher.getCell().getDataStorage().getSubscribers());
			
			//Unsubscribe
			//Create Message
			JsonObject message2 = JsonMessage.createMessage(JsonMessage.toJsonObjectString(JsonMessage.DATAPOINTADDRESS, datapointaddress), publisherAgentName, JsonMessage.SERVICEUNSUBSCRIBE);
			ACLMessage msg2 = JsonMessage.convertToACL(message2);
			msg.setPerformative(ACLMessage.REQUEST);
			log.debug("Send message", msg2);
			//Send message from subscribercell
			cellControlSubscriber.getCell().send(msg2);
			
			synchronized (this) {
				try {
					this.wait(minWaitTime);
				} catch (InterruptedException e) {
					
				}
			}
			
			//Update Datapoint in publisher. It is expected that the subscriber cell is updated too
			cellControlPublisher.getCell().getDataStorage().write(datapointaddress, DatapackageImpl.newDatapackage(datapointaddress, value1), cellControlPublisher.getCell().getName());
			log.debug("Get database of publisher={}", cellControlPublisher.getCell().getDataStorage());
			
			synchronized (this) {
				try {
					this.wait(minWaitTime);
				} catch (InterruptedException e) {
					
				}
			}
			
			//Check if value was updated in subscribercell
			
			log.debug("Datastorage of subscribercell={}", cellControlSubscriber.getCell().getDataStorage());
			
			String answer = cellControlSubscriber.getCell().getDataStorage().read(datapointaddress).get(datapointaddress).getDefaultValue();//JsonMessage.getBody(result).get(datapointaddress).getAsString();
			
			assertEquals(value2, answer);
		} catch (Exception e) {
			log.error("Cannot init system", e);
			fail("Error");
		}
	}
	
	@Test
	public void massOfSubscribersTest() {
		final int minWaitTime = 5;
		final int numberOfAgents =100;
		
		
		//Create 2 agents. One shall subscribe the other. One shall be written to. The subscribing agent shall be notified.
	
		
		try {
			//create message for subscription. Fields: Address
			String agentNameTemplate = "agent";
			//String publisherAgentName = "PublisherAgent";
			
			String datapointaddress = "subscribe.test.address";
			//String value1 = "init";
			String value2 = "MuHaahAhaAaahAAHA";
			
			//Create X=5 agents
			List<CellInspectorController> inspectors = new ArrayList<CellInspectorController>();
			List<AgentController> controllers = new ArrayList<AgentController>();
			for (int i=0; i<numberOfAgents; i++) {
				//Create cell inspector controller for the subscriber
				CellInspectorController cellControlSubscriber = new CellInspectorController();
				Object[] argsSubscriber = new Object[1];
				argsSubscriber[0] = cellControlSubscriber;
				//Create agent in the system
				inspectors.add(cellControlSubscriber);
				AgentController controller = this.util.createAgent(agentNameTemplate + i, CellInspector.class, argsSubscriber, agentContainer);
				controllers.add(controller);
			}
			
			log.debug("State={}", controllers.get(controllers.size()-1).getState());
			//State state = publisherController.getState();
			log.debug("wait for agent to answer");
			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {
					
				}
			}
			log.debug("State={}", controllers.get(controllers.size()-1).getState());
			
			//Set subscriptions
			for (int i=1;i<numberOfAgents;i++) {
				CellInspectorController thisController = inspectors.get(i);
				CellInspectorController previousController = inspectors.get(i-1);
				
				//Subscribe a datapoint of the publisher agent
				//Create Message
				JsonObject message = JsonMessage.createMessage(JsonMessage.toJsonObjectString(JsonMessage.DATAPOINTADDRESS, datapointaddress), previousController.getCell().getLocalName(), JsonMessage.SERVICESUBSCRIBE);
				ACLMessage msg = JsonMessage.convertToACL(message);
				msg.setPerformative(ACLMessage.REQUEST);
				//Send message from subscribercell
				thisController.getCell().send(msg);
				log.debug("Send message={} for agent={}", msg, thisController.getCell());
			}
			
			synchronized (this) {
				try {
					this.wait(60000);
				} catch (InterruptedException e) {
					
				}
			}
			
			//Set the first value and let the chain update itself
			//Update Datapoint in publisher. It is expected that the subscriber cell is updated too
			inspectors.get(0).getCell().getDataStorage().write(datapointaddress, DatapackageImpl.newDatapackage(datapointaddress, value2), inspectors.get(0).getCell().getLocalName());
			log.debug("Get database of publisher={}", inspectors.get(0).getCell().getDataStorage());
			
			//Wait for update
			//int counter = 1;
			//boolean isValueThere = inspectors.get(numberOfAgents-1).getCell().getDataStorage().equals(value2);
			//while(isValueThere==false) {
				synchronized (this) {
					try {
						this.wait(60000);
					} catch (InterruptedException e) {
						
					}
				}
				
		//		log.debug("time elapsed={}ms", counter*5000);
		//		counter++;
		//		isValueThere = inspectors.get(numberOfAgents-1).getCell().getDataStorage().equals(value2);
		//	}
			
			
			//Get the value from the last agent
			log.info("Datastorage of the last agent={}", inspectors.get(numberOfAgents-1).getCell().getDataStorage());
			
			String answer = inspectors.get(numberOfAgents-1).getCell().getDataStorage().read(datapointaddress).get(datapointaddress).getDefaultValue();//JsonMessage.getBody(result).get(datapointaddress).getAsString();
			
			assertEquals(value2, answer);
			
		} catch (Exception e) {
			log.error("Cannot init system", e);
			fail("Error");
		}
	}

}
