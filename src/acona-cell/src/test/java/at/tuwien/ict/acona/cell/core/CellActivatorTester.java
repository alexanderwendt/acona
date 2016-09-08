package at.tuwien.ict.acona.cell.core;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.config.ActivatorConfigJadeBehaviour;
import at.tuwien.ict.acona.cell.config.CellConfigJadeBehaviour;
import at.tuwien.ict.acona.cell.config.BehaviourConfigJadeBehaviour;
import at.tuwien.ict.acona.cell.config.ConditionConfig;
import at.tuwien.ict.acona.cell.core.InspectorCellClient;
import at.tuwien.ict.acona.cell.core.helpers.CellWithActivator;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaServiceType;
import at.tuwien.ict.acona.jadelauncher.core.Gateway;
import at.tuwien.ict.acona.jadelauncher.core.GatewayImpl;
import at.tuwien.ict.acona.jadelauncher.util.JadeContainerUtil;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class CellActivatorTester {

	private static Logger log = LoggerFactory.getLogger(CellActivatorTester.class);
	private final JadeContainerUtil util = new JadeContainerUtil();
	private Gateway comm;
	
	private ContainerController agentContainer;
	
	@Before
	public void setUp() throws Exception {
		try {
			//Create container
			log.debug("Create or get main container");
			this.util.createMainJADEContainer("localhost", 1099, "MainContainer");
					
			log.debug("Create subcontainer");
			agentContainer = this.util.createAgentContainer("localhost", 1099, "Subcontainer"); 
			
			//log.debug("Create gui");
			//this.util.createRMAInContainer(agentContainer);
			
			//Create gateway
			log.debug("Create gateway");
			comm = new GatewayImpl();
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
	public void activatorTest() {
		try {
			String activatorAgentName = "activatoragent";
			String op1address = "data.op1";
			String op2address = "data.op2";
			String resultaddress = "data.result";
			
			double op1 = 1;
			double op2 = 2;
			double expectedResult = 3;
			
			//Create cell inspector controller for the subscriber
			InspectorCellClient cellControlPublisher = new InspectorCellClient();
			Object[] argsPublisher = new Object[2];
			argsPublisher[0] = new JsonObject();
			argsPublisher[1] = cellControlPublisher;
			//Create agent in the system
			AgentController publisherController = this.util.createAgent(activatorAgentName, CellWithActivator.class, argsPublisher, agentContainer);
			log.debug("State={}", publisherController.getState());
			
			log.debug("wait for agent to answer");
			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {
					
				}
			}
			log.debug("State={}", publisherController.getState());
			
			cellControlPublisher.getCell().getDataStorage().write(Datapoint.newDatapoint(op1address).setValue(String.valueOf(op1)), cellControlPublisher.getCell().getName());
			cellControlPublisher.getCell().getDataStorage().write(Datapoint.newDatapoint(op2address).setValue(String.valueOf(op2)), cellControlPublisher.getCell().getName());
			
			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {
					
				}
			}
			
			double actualResult = cellControlPublisher.getCell().getDataStorage().read(resultaddress).getValue().getAsDouble();
			
			assertEquals(actualResult, expectedResult, 0.0001);
			
		} catch (Exception e) {
			log.error("Cannot init system", e);
			fail("Error");
		}
	}
	
	@Test
	public void createConfigurableAdditionNetwork() {
		try {
			//Create config JSON
			CellConfigJadeBehaviour cell = CellConfigJadeBehaviour.newConfig("AdditionAgent", "at.tuwien.ict.acona.cell.core.InspectorCell");
			//cell.setClass(InspectorCell.class);
			cell.addCondition(ConditionConfig.newConfig("operand1", "at.tuwien.ict.acona.cell.activator.conditions.ConditionIsNotEmpty"));
			cell.addCondition(ConditionConfig.newConfig("operand2", "at.tuwien.ict.acona.cell.activator.conditions.ConditionIsNotEmpty"));
			cell.addBehaviour(BehaviourConfigJadeBehaviour.newConfig("additionBehaviour", "at.tuwien.ict.acona.cell.core.helpers.AdditionBehaviour")
					.setProperty("operand1", "data.op1")
					.setProperty("operand2", "data.op2")
					.setProperty("result", "data.result"));
			cell.addActivator(ActivatorConfigJadeBehaviour.newConfig("AdditionActivator").setBehaviour("additionBehaviour").setActivatorLogic("")
					.addMapping("data.op1", "operand1")
					.addMapping("data.op2", "operand2"));

			double op1 = 12;
			double op2 = 23;
			double expectedResult = 35;
			
			//Create cell inspector controller for the subscriber
			InspectorCellClient externalController = new InspectorCellClient();
			Object[] argsPublisher = new Object[2];
			argsPublisher[0] = cell.toJsonObject();
			argsPublisher[1] = externalController;
			//Create agent in the system
			AgentController agentController = this.util.createAgent(cell.getName(), Class.forName(cell.getClassName()), argsPublisher, agentContainer);
			log.debug("State={}", agentController.getState());
			
			this.comm.sendSynchronousMessageToAgent(Message.newMessage().addReceiver(cell.getName())
					.setService(AconaServiceType.WRITE)
					.setContent(Datapoint.newDatapoint("data.op1").setValue(String.valueOf(op1))));
			this.comm.sendSynchronousMessageToAgent(Message.newMessage().addReceiver(cell.getName())
					.setService(AconaServiceType.WRITE)
					.setContent(Datapoint.newDatapoint("data.op2").setValue(String.valueOf(op2))));
			
			//externalController.getCell().getDataStorage().write(Datapoint.newDatapoint("data.op1").setValue(String.valueOf(op1)), externalController.getCell().getName());
			//externalController.getCell().getDataStorage().write(Datapoint.newDatapoint("data.op2").setValue(String.valueOf(op2)), externalController.getCell().getName());
			
//			synchronized (this) {
//				try {
//					this.wait(200);
//				} catch (InterruptedException e) {
//					
//				}
//			}
			//log.debug("test");
			
			this.comm.subscribeDatapoint(cell.getName(), "data.result");
			
			double actualResult = this.comm.getMessageFromAgent(5000).getContentAsDatapoint().getValue().getAsDouble();
			//double actualResult = externalController.getCell().getDataStorage().read("data.result").getValue().getAsDouble();
			
			//Message x = this.comm.getMessageFromAgent(1000);
			assertEquals(actualResult, expectedResult, 0.0);
			log.info("Test passed");
			
		} catch (Exception e) {
			log.error("Error at test execution", e);
			fail("Error");
		}
	}

}
