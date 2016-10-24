package at.tuwien.ict.kore.core;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import _OLD.at.tuwien.ict.acona.cell.config.ActivatorConfigJadeBehaviour;
import _OLD.at.tuwien.ict.acona.cell.config.BehaviourConfigJadeBehaviour;
import _OLD.at.tuwien.ict.acona.cell.config.CellConfigJadeBehaviour;
import _OLD.at.tuwien.ict.acona.cell.config.ConditionConfig;
import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.core.CellGatewayImpl;
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

public class StefansFunctionsTester {
	
	private static Logger log = LoggerFactory.getLogger(StefansFunctionsTester.class);
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
	public void subtractionTest() {		
		try {
			//Create config JSON
			CellConfigJadeBehaviour cell = CellConfigJadeBehaviour.newConfig("SubtractionAgent", "at.tuwien.ict.acona.cell.core.cellImpl");
			cell.setClass(CellImpl.class);
			
			cell.addCondition(ConditionConfig.newConfig("operand1Checker", "at.tuwien.ict.kore.conditions.ConditionGreaterThan")
					.setProperty("referencevalue", String.valueOf(200.0)));
			cell.addCondition(ConditionConfig.newConfig("operand2Checker", "at.tuwien.ict.kore.conditions.ConditionIsNotEmpty"));
			
			cell.addBehaviour(BehaviourConfigJadeBehaviour.newConfig("subtractionBehaviour", "at.tuwien.ict.kore.behaviours.SubtractionBehaviour")
					.setProperty("operand1", "data.op1")
					.setProperty("operand2", "data.op2")
					.setProperty("result", "data.result"));
			cell.addActivator(ActivatorConfigJadeBehaviour.newConfig("SubtractionActivator").setBehaviour("subtractionBehaviour").setActivatorLogic("")
					.addMapping("data.op1", "operand1Checker")
					.addMapping("data.op2", "operand2Checker"));
			
			double op1 = 201;
			double op2 = 4;
			double expectedResult = 197;
			
			//Create cell inspector controller for the subscriber
			Object[] argsPublisher = new Object[1];
			argsPublisher[0] = cell.toJsonObject();

			
			//Create agent in the system
			AgentController agentController = this.util.createAgent(cell.getName(), cell.getClassToInvoke(), argsPublisher, agentContainer);
			log.debug("State={}", agentController.getState());
			
			this.comm.sendSynchronousMessageToAgent(Message.newMessage().addReceiver(cell.getName())
					.setService(AconaServiceType.WRITE)
					.setContent(Datapoint.newDatapoint("data.op1").setValue(String.valueOf(op1))));
			this.comm.sendSynchronousMessageToAgent(Message.newMessage().addReceiver(cell.getName())
					.setService(AconaServiceType.WRITE)
					.setContent(Datapoint.newDatapoint("data.op2").setValue(String.valueOf(op2))));
			
			this.comm.subscribeDatapoint(cell.getName(), "data.result");
			
			double actualResult = this.comm.getMessageFromAgent(5000).getContentAsDatapoint().getValue().getAsDouble();
			//double actualResult = externalController.getCell().getDataStorage().read("data.result").getValue().getAsDouble();
			
			//Message x = this.comm.getMessageFromAgent(1000);
			assertEquals(actualResult, expectedResult, 0.0);
			log.info("Test passed");
			
		} catch (Exception e) {
			log.error("Cannot test system", e);
			fail("Error");
		}
		
	}

}
