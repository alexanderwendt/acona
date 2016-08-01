package at.tuwien.ict.acona.cell.core;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.config.ActivatorConfig;
import at.tuwien.ict.acona.cell.config.BehaviourConfig;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.ConditionConfig;
import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.core.InspectorCell;
import at.tuwien.ict.acona.cell.core.InspectorCellClient;
import at.tuwien.ict.acona.cell.datastructures.DatapackageImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaService;
import at.tuwien.ict.acona.communicator.core.Communicator;
import at.tuwien.ict.acona.communicator.core.CommunicatorImpl;
import at.tuwien.ict.acona.communicator.util.ACLUtils;
import at.tuwien.ict.acona.communicator.util.JadeContainerUtil;
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
			String receiver = "CellAgent";
			String datapointaddress = "testaddress";
			String value = "testvalue";
			
			//Create agent in the system
			//String[] args = {"1", "pong"};
			Object[] args = new Object[1];
			args[0] = new JsonObject();
			AgentController cellAgent = this.util.createAgent(receiver, CellImpl.class, args, agentContainer);
			
			//Send Write command
			Message writeMessage = Message.newMessage()
					.addReceiver(receiver)
					.setContent(Datapoint.newDatapoint(datapointaddress).setValue(value).toJsonObject())
					.setService(AconaService.WRITE);
			
			
			//Create message body
			//JsonObject writeBody = new JsonObject();
			//writeBody.addProperty(JsonMessage.DATAPOINTADDRESS, datapointaddress);
			//writeBody.addProperty(JsonMessage.VALUE, value);
			
			Message ack = this.comm.sendSynchronousMessageToAgent(writeMessage, 10000);
			log.debug("Data written to cell={}", ack);
			
			Message readMessage = Message.newMessage()
					.setReceiver(receiver)
					.setService(AconaService.READ)
					.setContent(Datapoint.newDatapoint(datapointaddress));
			
			Message result = this.comm.sendSynchronousMessageToAgent(readMessage, 100000);
			log.debug("Received data={}", result);
//			log.debug("wait for agent to answer");
//			synchronized (this) {
//				try {
//					this.wait(5000);
//				} catch (InterruptedException e) {
//					
//				}
//			}
			
			String answer = Datapoint.toDatapoint(result.getContent().getAsJsonObject()).getValue().getAsString();
			log.debug("correct value={}, actual value={}", value, answer);
			
			assertEquals(value, answer);
			log.info("Test passed");
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
			InspectorCellClient cellControlSubscriber = new InspectorCellClient();
			Object[] argsSubscriber = new Object[2];
			argsSubscriber[0] = new JsonObject();
			argsSubscriber[1] = cellControlSubscriber;
			//Create agent in the system
			this.util.createAgent(subscriberAgentName, InspectorCell.class, argsSubscriber, agentContainer);
			
			//Create cell inspector controller for the subscriber
			InspectorCellClient cellControlPublisher = new InspectorCellClient();
			Object[] argsPublisher = new Object[2];
			argsPublisher[0] = new JsonObject();
			argsPublisher[1] = cellControlPublisher;
			//Create agent in the system
			AgentController publisherController = this.util.createAgent(publisherAgentName, InspectorCell.class, argsPublisher, agentContainer);
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
			cellControlPublisher.getCell().getDataStorage().write(Datapoint.newDatapoint(datapointaddress).setValue(value1), cellControlPublisher.getCell().getName());
			log.debug("Get database of publisher={}", cellControlPublisher.getCell().getDataStorage());
			
			
			//Subscribe a datapoint of the publisher agent
			//Create Message
			Message message = Message.newMessage()
					.addReceiver(publisherAgentName)
					.setService(AconaService.SUBSCRIBE)
					.setContent(Datapoint.newDatapoint(datapointaddress));
			//JsonObject message = JsonMessage.createMessage(JsonMessage.toJsonObjectString(JsonMessage.DATAPOINTADDRESS, datapointaddress), publisherAgentName, JsonMessage.SERVICESUBSCRIBE);
			ACLMessage msg = ACLUtils.convertToACL(message);
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
			cellControlPublisher.getCell().getDataStorage().write(Datapoint.newDatapoint(datapointaddress).setValue(value2), cellControlPublisher.getCell().getName());
			log.debug("Get database of publisher={}", cellControlPublisher.getCell().getDataStorage());
			
			synchronized (this) {
				try {
					this.wait(minWaitTime);
				} catch (InterruptedException e) {
					
				}
			}
			
			//Check if value was updated in subscribercell
			
			log.debug("Datastorage of subscribercell={}", cellControlSubscriber.getCell().getDataStorage());
			
			String answer = cellControlSubscriber.getCell().getDataStorage().read(datapointaddress).getValue().getAsString(); //JsonMessage.getBody(result).get(datapointaddress).getAsString();
			
			assertEquals(value2, answer);
		} catch (Exception e) {
			log.error("Cannot init system", e);
			fail("Error");
		}
	}
	
	@Test
	public void unsubscribeNotifyTest() {
		int minWaitTime = 10;
		//Create 2 agents. One shall subscribe the other. One shall be written to. The subscribing agent shall be notified.
	
		
		try {
			//create message for subscription. Fields: Address
			String subscriberAgentName = "SubscriberAgent";
			String publisherAgentName = "PublisherAgent";
			String datapointaddress = "subscribe.test.address";
			String value1 = "Wrong value";
			String value2 = "MuHaahAhaAaahAAHA";
			
			//Create cell inspector controller for the subscriber
			InspectorCellClient cellControlSubscriber = new InspectorCellClient();
			Object[] argsSubscriber = new Object[2];
			argsSubscriber[0] = new JsonObject();
			argsSubscriber[1] = cellControlSubscriber;
			//Create agent in the system
			this.util.createAgent(subscriberAgentName, InspectorCell.class, argsSubscriber, agentContainer);
			
			//Create cell inspector controller for the subscriber
			InspectorCellClient cellControlPublisher = new InspectorCellClient();
			Object[] argsPublisher = new Object[2];
			argsPublisher[0] = new JsonObject();
			argsPublisher[1] = cellControlPublisher;
			//Create agent in the system
			AgentController publisherController = this.util.createAgent(publisherAgentName, InspectorCell.class, argsPublisher, agentContainer);
			log.debug("State={}", publisherController.getState());
			State state = publisherController.getState();
			
			log.debug("wait for agent to answer");
			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {
					
				}
			}
			log.debug("State={}", publisherController.getState());
			
			//Set init value
			cellControlPublisher.getCell().getDataStorage().write(Datapoint.newDatapoint(datapointaddress).setValue(value2), cellControlPublisher.getCell().getName());
			log.debug("Get database of publisher={}", cellControlPublisher.getCell().getDataStorage());
			
			
			//Subscribe a datapoint of the publisher agent
			//Create Message
			Message message = Message.newMessage()
					.addReceiver(publisherAgentName)
					.setService(AconaService.SUBSCRIBE)
					.setContent(Datapoint.newDatapoint(datapointaddress));
			//JsonObject message = JsonMessage.createMessage(JsonMessage.toJsonObjectString(JsonMessage.DATAPOINTADDRESS, datapointaddress), publisherAgentName, JsonMessage.SERVICESUBSCRIBE);
			ACLMessage msg = ACLUtils.convertToACL(message);
			msg.setPerformative(ACLMessage.REQUEST);
			//Send message from subscribercell
			cellControlSubscriber.getCell().send(msg);
			log.debug("Send message", msg);
			
			synchronized (this) {
				try {
					this.wait(minWaitTime);
				} catch (InterruptedException e) {
					
				}
			}
			
			//Both shall have the same value
			log.debug("Get database of publisher={}", cellControlPublisher.getCell().getDataStorage());
			log.debug("Get database of subscriber={}", cellControlSubscriber.getCell().getDataStorage());
			//log.debug("Registered subscribers = {}", cellControlPublisher.getCell().getDataStorage().getSubscribers());
			
			//Unsubscribe
			//Create Message
			Message message2 = Message.newMessage()
					.addReceiver(publisherAgentName)
					.setService(AconaService.UNSUBSCRIBE)
					.setContent(Datapoint.newDatapoint(datapointaddress));
			//JsonObject message = JsonMessage.createMessage(JsonMessage.toJsonObjectString(JsonMessage.DATAPOINTADDRESS, datapointaddress), publisherAgentName, JsonMessage.SERVICESUBSCRIBE);
			ACLMessage msg2 = ACLUtils.convertToACL(message2);
			//JsonObject message2 = JsonMessage.createMessage(JsonMessage.toJsonObjectString(JsonMessage.DATAPOINTADDRESS, datapointaddress), publisherAgentName, JsonMessage.SERVICEUNSUBSCRIBE);
			//ACLMessage msg2 = JsonMessage.convertToACL(message2);
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
			cellControlPublisher.getCell().getDataStorage().write(Datapoint.newDatapoint(datapointaddress).setValue(value1), cellControlPublisher.getCell().getName());
			log.debug("Get database of publisher={}", cellControlPublisher.getCell().getDataStorage());
			
			synchronized (this) {
				try {
					this.wait(minWaitTime);
				} catch (InterruptedException e) {
					
				}
			}
			
			//Check if value was updated in subscribercell
			
			log.debug("Datastorage of subscribercell={}", cellControlSubscriber.getCell().getDataStorage());
			
			String answer = cellControlSubscriber.getCell().getDataStorage().read(datapointaddress).getValue().getAsString();//JsonMessage.getBody(result).get(datapointaddress).getAsString();
			
			assertEquals(value2, answer);
		} catch (Exception e) {
			log.error("Cannot init system", e);
			fail("Error");
		}
	}
	
	@Test
	public void massOfSubscribersTest() {
		final int minWaitTime = 5;
		final int numberOfAgents =10;
		
		
		//Create 2 agents. One shall subscribe the other. One shall be written to. The subscribing agent shall be notified.
	
		
		try {
			//create message for subscription. Fields: Address
			String agentNameTemplate = "agent";
			//String publisherAgentName = "PublisherAgent";
			
			String datapointaddress = "subscribe.test.address";
			//String value1 = "init";
			String value2 = "MuHaahAhaAaahAAHA";
			
			//Create X=5 agents
			List<InspectorCellClient> inspectors = new ArrayList<InspectorCellClient>();
			List<AgentController> controllers = new ArrayList<AgentController>();
			for (int i=0; i<numberOfAgents; i++) {
				//Create cell inspector controller for the subscriber
				InspectorCellClient cellControlSubscriber = new InspectorCellClient();
				Object[] argsSubscriber = new Object[2];
				argsSubscriber[0] = new JsonObject();
				argsSubscriber[1] = cellControlSubscriber;
				//Create agent in the system
				inspectors.add(cellControlSubscriber);
				AgentController controller = this.util.createAgent(agentNameTemplate + i, InspectorCell.class, argsSubscriber, agentContainer);
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
				InspectorCellClient thisController = inspectors.get(i);
				InspectorCellClient previousController = inspectors.get(i-1);
				
				//Subscribe a datapoint of the publisher agent
				//Create Message
				Message message = Message.newMessage()
						.addReceiver(previousController.getCell().getLocalName())
						.setService(AconaService.SUBSCRIBE)
						.setContent(Datapoint.newDatapoint(datapointaddress));
				//JsonObject message = JsonMessage.createMessage(JsonMessage.toJsonObjectString(JsonMessage.DATAPOINTADDRESS, datapointaddress), publisherAgentName, JsonMessage.SERVICESUBSCRIBE);
				ACLMessage msg = ACLUtils.convertToACL(message);
				//JsonObject message = JsonMessage.createMessage(JsonMessage.toJsonObjectString(JsonMessage.DATAPOINTADDRESS, datapointaddress), previousController.getCell().getLocalName(), JsonMessage.SERVICESUBSCRIBE);
				//ACLMessage msg = JsonMessage.convertToACL(message);
				msg.setPerformative(ACLMessage.REQUEST);
				//Send message from subscribercell
				thisController.getCell().send(msg);
				log.debug("Send message={} for agent={}", msg, thisController.getCell());
			}
			
			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {
					
				}
			}
			
			//Set the first value and let the chain update itself
			//Update Datapoint in publisher. It is expected that the subscriber cell is updated too
			inspectors.get(0).getCell().getDataStorage().write(Datapoint.newDatapoint(datapointaddress).setValue(value2), inspectors.get(0).getCell().getLocalName());
			log.debug("Get database of publisher={}", inspectors.get(0).getCell().getDataStorage());
			
			//Wait for update
			//int counter = 1;
			//boolean isValueThere = inspectors.get(numberOfAgents-1).getCell().getDataStorage().equals(value2);
			//while(isValueThere==false) {
				synchronized (this) {
					try {
						this.wait(2000);
					} catch (InterruptedException e) {
						
					}
				}
				
		//		log.debug("time elapsed={}ms", counter*5000);
		//		counter++;
		//		isValueThere = inspectors.get(numberOfAgents-1).getCell().getDataStorage().equals(value2);
		//	}
			
			
			//Get the value from the last agent
			log.info("Datastorage of the last agent={}", inspectors.get(numberOfAgents-1).getCell().getDataStorage());
			
			String answer = inspectors.get(numberOfAgents-1).getCell().getDataStorage().read(datapointaddress).getValue().getAsString();//JsonMessage.getBody(result).get(datapointaddress).getAsString();
			
			assertEquals(value2, answer);
			
		} catch (Exception e) {
			log.error("Cannot init system", e);
			fail("Error");
		}
	}
	
	/**
	 * In this test, one agent is created, which is the database. Another agent is created that shall read a value from the database with a blocking read function. The test is passed, if the read value is written to a subscribed datapoint
	 * 
	 */
	@Test
	public void SynchronizedReadTest() {
		try {
			String readAddress = "storageagent.data.value";
			String triggerAddress = "readeragent.data.command";
			String resultAddress = "data.result";
			int databaseValue = 12345;
			int expectedValue = 12345;
			
			//Create config JSON for reader agent
			CellConfig cellreader = CellConfig.newConfig("ReaderAgent", "at.tuwien.ict.acona.cell.core.CellImpl");
			//cellreader.setClass(CellImpl.class);
			cellreader.addCondition(ConditionConfig.newConfig("startreadcondition", "at.tuwien.ict.acona.cell.activator.conditions.ConditionIsNotEmpty"));
			cellreader.addBehaviour(BehaviourConfig.newConfig("readBehaviour", "at.tuwien.ict.acona.cell.core.helpers.TestReadAndWriteBehaviour")
					.setProperty("agentname", "StorageAgent")
					.setProperty("timeout", "2000")
					.setProperty("result", resultAddress)
					.setProperty("readaddress", readAddress));
			cellreader.addActivator(ActivatorConfig.newConfig("ReadActivator").setBehaviour("readBehaviour").setActivatorLogic("")
					.addMapping(triggerAddress, "startreadcondition"));
			
			//Create agent in the system
			//String[] args = {"1", "pong"};
			Object[] args = new Object[1];
			args[0] = cellreader.toJsonObject();
			
			AgentController readerAgent = null;
			try {
				readerAgent = this.util.createAgent(cellreader.getName(), cellreader.getClassToInvoke(), args, agentContainer);
			} catch (Exception e) {
				log.error("error", e);
			}
			
			
			
			log.debug("State={}", readerAgent.getState());
			
			//Create config JSON for storage agent
			CellConfig cellstorage = CellConfig.newConfig("StorageAgent", "at.tuwien.ict.acona.cell.core.InspectorCell");
			//cellstorage.setClass(InspectorCell.class);
			
			//Create cell inspector controller for the subscriber
			InspectorCellClient externalController = new InspectorCellClient();
			Object[] argsPublisher = new Object[2];
			argsPublisher[0] = cellstorage.toJsonObject();
			argsPublisher[1] = externalController;
			//Create agent in the system
			AgentController agentController = this.util.createAgent(cellstorage.getName(), Class.forName(cellstorage.getClassName()), argsPublisher, agentContainer);
			
			log.debug("State={}", agentController.getState());		
			
			//Write databasevalue directly into the storage
			externalController.getCell().getDataStorage().write(Datapoint.newDatapoint(readAddress).setValue(new JsonPrimitive(databaseValue)), null);
			
			//=== Start the system ===//
			this.comm.subscribeDatapoint("ReaderAgent", readAddress);
			
			//Send Write command
			Message writeMessage = Message.newMessage()
					.addReceiver("ReaderAgent")
					.setContent(Datapoint.newDatapoint(triggerAddress).setValue("START").toJsonObject())
					.setService(AconaService.WRITE);
			
			Message ack = this.comm.sendSynchronousMessageToAgent(writeMessage, 10000);
			log.debug("Tester: Acknowledge of cell writing recieved={}", ack);
			
			//Subscribe the result
			double actualResult = this.comm.getDatapointFromAgent(20000, true).getValue().getAsInt();
			
			//this.myAgent.send(ACLUtils.convertToACL(Message.newMessage().addReceiver(msg.getSender().getLocalName()).setService(AconaService.READ).setContent(Datapoint.newDatapoint("test"))));
			
			log.debug("correct value={}, actual value={}", expectedValue, actualResult);
			
			assertEquals(expectedValue, actualResult, 0.0);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}
	}

}
