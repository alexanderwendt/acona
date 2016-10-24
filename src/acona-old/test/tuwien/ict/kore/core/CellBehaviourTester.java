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
import at.tuwien.ict.acona.cell.core.InspectorCell;
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

public class CellBehaviourTester {
	
	private static Logger log = LoggerFactory.getLogger(CellBehaviourTester.class);
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
	public void doubleBehaviourTester() {		
		try {
			//Create config JSON
			CellConfigJadeBehaviour cell = CellConfigJadeBehaviour.newConfig("IncrementorAgent", "at.tuwien.ict.acona.cell.core.cellImpl");
			cell.setClass(CellImpl.class);
			
			cell.addCondition(ConditionConfig.newConfig("operandChecker", "at.tuwien.ict.kore.conditions.ConditionGreaterThan")
					.setProperty("referencevalue", String.valueOf(2.0)));
			cell.addBehaviour(BehaviourConfigJadeBehaviour.newConfig("incrementBehaviour", "at.tuwien.ict.kore.behaviours.IncrementValueBehaviour")
					.setProperty("operand", "data.firstvalue")
					.setProperty("result", "data.secondvalue"));
			cell.addActivator(ActivatorConfigJadeBehaviour.newConfig("Increment1Activator").setBehaviour("incrementBehaviour").setActivatorLogic("")
					.addMapping("data.firstvalue", "operandChecker"));

			cell.addCondition(ConditionConfig.newConfig("operandIsNotNull", "at.tuwien.ict.kore.conditions.ConditionIsNotEmpty"));
			cell.addBehaviour(BehaviourConfigJadeBehaviour.newConfig("incrementBehaviour2", "at.tuwien.ict.kore.behaviours.IncrementValueBehaviour")
					.setProperty("operand", "data.secondvalue")
					.setProperty("result", "data.result"));
			cell.addActivator(ActivatorConfigJadeBehaviour.newConfig("Increment2Activator").setBehaviour("incrementBehaviour2").setActivatorLogic("")
					.addMapping("data.secondvalue", "operandChecker"));
			
			double op1 = 3;
			double expectedResult = 5;
			
			//Create cell inspector controller for the subscriber
			//InspectorCellClient externalController = new InspectorCellClient();
			Object[] argsPublisher = new Object[1];
			argsPublisher[0] = cell.toJsonObject();
			//argsPublisher[1] = externalController;
			//Create agent in the system
			AgentController agentController = this.util.createAgent(cell.getName(), cell.getClassToInvoke(), argsPublisher, agentContainer);
			log.debug("State={}", agentController.getState());
			
			this.comm.sendSynchronousMessageToAgent(Message.newMessage().addReceiver(cell.getName())
					.setService(AconaServiceType.WRITE)
					.setContent(Datapoint.newDatapoint("data.firstvalue").setValue(String.valueOf(op1))));
			
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
		
		fail("Not yet implemented");
	}

}
