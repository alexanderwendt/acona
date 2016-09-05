package at.tuwien.ict.acona.cell.activator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.activator.conditions.ConditionHasValue;
import at.tuwien.ict.acona.cell.config.ActivatorConfig;
import at.tuwien.ict.acona.cell.config.BehaviourConfig;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.ConditionConfig;
import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.core.CellSendTester;
import at.tuwien.ict.acona.cell.core.helpers.ReadOperandBehaviour;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaServiceType;
import at.tuwien.ict.acona.cell.util.CommUtil;
import at.tuwien.ict.acona.communicator.core.Communicator;
import jade.core.Runtime;

public class CellExecutorWithCellTester {
	private static Logger log = LoggerFactory.getLogger(CellSendTester.class);
	//private final JadeContainerUtil util = new JadeContainerUtil();
	private CommUtil commUtil = CommUtil.getUtil();
	private Communicator comm = commUtil.getJadeGateway();

	@Before
	public void setUp() throws Exception {
		try {
			//Create container
			log.debug("Create or get main container");
			this.commUtil.createMainContainer("localhost", 1099, "MainContainer");
			
			log.debug("Create subcontainer");
			this.commUtil.createSubContainer("localhost", 1099, "Subcontainer");
			
			//log.debug("Create gui");
			//this.commUtil.createDebugUserInterface();
			
			//Create gateway
			commUtil.initJadeGateway();
			
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
		this.commUtil.shutDownJadeGateway();
	}
	
	/**
	 * Idea: Create a reader behaviour that reads 2 values from 2 different agents. It has the "state=1". 2 agents provide 2 datapoints with double values. The reader behaviour sends read requests to all these agents and 
	 * sets a status datapoint to 2 . The 2 agents respond. If all 3 messages arrive, a process behaviour is triggered "state=2". It sums the operands and sends the result to a 3rd agent. If the sum is correct, the test is passed.
	 * 
	 * 
	 */
//	//@Test
//	public void ExecutorThatExecutesSomethingTest() {
//		try {
//			String commandDatapoint = "datapoint.command";
//			String queryDatapoint = "datapoint.query";
//			String executeonceDatapoint = "datapoint.executeonce";
//			
//			//Create Database agents 1-2
//			CellConfig dbAgent1 = CellConfig.newConfig("dbagent1", CellImpl.class.getName());
//			this.commUtil.createAgent(dbAgent1);
//			
//			//Create the calculator agent
//			//Create the basic information for any agent
//			CellConfig additionAgent = CellConfig.newConfig("AdditionAgent", "at.tuwien.ict.acona.cell.core.CellImpl");
//			
//			//Create conditions that can be used in the agents, only the name of the condition and their classes
//			//Readerconditions
//			additionAgent.addCondition(ConditionConfig.newConfig("starttrigger", ConditionHasValue.class.getName())
//					.setProperty("comparestring", "START"));
//			
//			//Create behaviours that will be used by the agents
//			//Add the reader
//			additionAgent.addBehaviour(BehaviourConfig.newConfig("S1", ReadOperandBehaviour.class.getName())
//					.setProperty("op1agent", "dbagent1")
//					.setProperty("op1address", operand1address)
//					.setProperty("op2agent", "dbagent2")
//					.setProperty("op2address", operand2address)
//					.setProperty("successstateid", "OK")
//					.setProperty("stateaddress", stateaddress));
//			
//			//Add activators
//			//Add reader activator
//			additionAgent.addActivator(ActivatorConfig.newConfig("T0").setBehaviour("S1").setActivatorLogic("")
//					.addMapping(triggeraddress, "starttrigger"));
//			
//			this.commUtil.createAgent(additionAgent);
//			
//			
//			//Create result receiver agent
//			CellConfig receiverAgent = CellConfig.newConfig("receiveragent", "at.tuwien.ict.acona.cell.core.CellImpl");
//			this.commUtil.createAgent(receiverAgent);
//			
//			//subscribe the result without timeout
//			this.comm.subscribeDatapoint("receiveragent", resultAddress);
//			
//			//Write the numbers in the database agents
//			this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver("dbagent1").setContent(Datapoint.newDatapoint(operand1address).setValue(new JsonPrimitive(11.))).setService(AconaServiceType.WRITE));
//			
//			//Trigger the calculator agent
//			this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver("AdditionAgent").setContent(Datapoint.newDatapoint(triggeraddress).setValue(new JsonPrimitive("START"))).setService(AconaServiceType.WRITE));
//			
//			
//			synchronized (this) {
//				try {
//					this.wait(2000);
//				} catch (InterruptedException e) {
//					
//				}
//			}
//			
//			//Get the result from the result receiver agent
//			double actualResult = this.comm.getDatapointFromAgent(100000, true).getValue().getAsDouble();
//			
//			log.debug("correct value={}, actual value={}", 33, actualResult);
//			
//			assertEquals(33, actualResult, 0.0);
//			log.info("Test passed");
//		} catch (Exception e) {
//			log.error("Error testing system", e);
//			fail("Error");
//		}
//		
//	}

}
