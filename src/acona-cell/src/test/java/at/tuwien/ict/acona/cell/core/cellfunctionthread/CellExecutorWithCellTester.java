package at.tuwien.ict.acona.cell.core.cellfunctionthread;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.config.CellConfigJadeBehaviour;
import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.core.InspectorCell;
import at.tuwien.ict.acona.cell.core.CellGatewayImpl;
import at.tuwien.ict.acona.cell.core.cellfunctionthread.helpers.CellWithCellFunctionTestInstance;
import at.tuwien.ict.acona.cell.core.cellfunctionthread.helpers.SimpleAdditionAgentFixedCellFunctions;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.util.JadelauncherUtil;
import jade.core.Runtime;

public class CellExecutorWithCellTester {
	private static Logger log = LoggerFactory.getLogger(CellExecutorWithCellTester.class);
	//private final JadeContainerUtil util = new JadeContainerUtil();
	private JadelauncherUtil commUtil = JadelauncherUtil.getUtil();
	//private Gateway comm = commUtil.getJadeGateway();

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
			//commUtil.initJadeGateway();
			
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
		//this.commUtil.shutDownJadeGateway();
	}
	
	/**
	 *
	 * 
	 * 
	 */
	@Test
	public void threadExecutorInCellTester() {
		try {
			String commandDatapoint = "datapoint.command";
			String queryDatapoint = "datapoint.query";
			String executeonceDatapoint = "datapoint.executeonce";
			
			String expectedResult = "FINISHED";
			
			//Create Database agents 1-2
			CellConfigJadeBehaviour testagent = CellConfigJadeBehaviour.newConfig("testagent", CellWithCellFunctionTestInstance.class.getName());
			this.commUtil.createAgent(testagent);
			
			//Create inspector or the new gateway
			CellGatewayImpl cellControlSubscriber = this.commUtil.createInspectorAgent(CellConfigJadeBehaviour.newConfig("subscriber", InspectorCell.class.getName()));
			
			//Write the numbers in the database agents
			//this.comm.subscribeDatapoint("testagent", "datapoint.result");
			
			//this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver("testagent").setContent(Datapoint.newDatapoint(commandDatapoint).setValue(new JsonPrimitive("START"))).setService(AconaServiceType.WRITE));
			cellControlSubscriber.subscribeForeignDatapoint("datapoint.result", "testagent");
			cellControlSubscriber.getCell().getCommunicator().write(Datapoint.newDatapoint(queryDatapoint).setValue("SELECT * FILESERVER"), "testagent");
			
			//this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver("testagent").setContent(Datapoint.newDatapoint(queryDatapoint).setValue(new JsonPrimitive("SELECT * FILESERVER"))).setService(AconaServiceType.WRITE));
					
			//Get the result from the result receiver agent
			//String result = this.comm.getDatapointFromAgent(100000, true).getValue().getAsString();
			
			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {
					
				}
			}
			
			String result = cellControlSubscriber.readLocalDatapoint("datapoint.result").getValueAsString();
			
			log.debug("correct value={}, actual value={}", "FINISHED", result);
			
			assertEquals(result, expectedResult);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}
		
	}
	
	/**
	 * Idea: Create an agent with the following behaviours (not jade): A controller runs every 5s. It starts a getDataFunction. When the
	 * data has been received, the publish data function is executed. Data is read from another dummy agent, which acts as a memory
	 * In the "Drivetrack-Agent", 2 values are read from a memory agent, added and published within the agent. The result is subscribed by an output agent
	 * The Outbuffer is only an empty mock, which is used as a gateway
	 * 
	 */
	@Test
	public void complexCellTester() {
		try {
			//define all datapoints that shall be used
			String memorydatapoint1 = "inputmemory.variable1";	//put into memory mock agent
			String memorydatapoint2 = "inputmemory.variable2";	//put into memory mock agent
			
			//drivetrack data
			String commandDatapoint = "drivetrack.controller.command";
			String modedatapoint = "drivetrack.controller.mode";
			String executeinterval = "drivetrack.controller.executioninterval";
			
			
			//Output memory agent
			String resultdatapoint = "outputmemory.result";
			
			//Define results
			int value1 = 12;
			int value2 = 13;
			int expectedResult = 25;
			
			//Define agent names and info
			String inputMemoryAgentName = "InputBufferAgent";
			String outputmemoryAgentName = "OutputBufferAgent";
			String drivetrackAgentName = "DriveTrackAgent";
			
			//Create Database agents 1-2
			//INFO: By using an inspector agent, automatically, a gateway is created
			CellConfigJadeBehaviour inputMemoryAgent = CellConfigJadeBehaviour.newConfig(inputMemoryAgentName, CellImpl.class.getName());
			CellGatewayImpl client1 = this.commUtil.createInspectorAgent(inputMemoryAgent);
		
			CellConfigJadeBehaviour outputMemoryAgent = CellConfigJadeBehaviour.newConfig(outputmemoryAgentName, CellImpl.class.getName());
			CellGatewayImpl client2 = this.commUtil.createInspectorAgent(outputMemoryAgent);
			
			//Create the drive track agent
			CellConfigJadeBehaviour drivetrackAgent = CellConfigJadeBehaviour.newConfig(drivetrackAgentName, SimpleAdditionAgentFixedCellFunctions.class.getName());
			this.commUtil.createAgent(drivetrackAgent);
			
			
			
			//Write the numbers in the database agents
			//this.comm.subscribeDatapoint(drivetrackAgentName, "datapoint.result");
			
			//this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver("testagent").setContent(Datapoint.newDatapoint(commandDatapoint).setValue(new JsonPrimitive("START"))).setService(AconaServiceType.WRITE));
			client1.getDataStorage().write(Datapoint.newDatapoint(memorydatapoint1).setValue(String.valueOf(value1)), "nothing");
			client1.getDataStorage().write(Datapoint.newDatapoint(memorydatapoint2).setValue(String.valueOf(value2)), "nothing");
			
			client1.getCell().getCommunicator().write(Datapoint.newDatapoint(commandDatapoint).setValue(new JsonPrimitive("START")), drivetrackAgentName);
			//this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver(drivetrackAgentName).setContent(Datapoint.newDatapoint(commandDatapoint).setValue(new JsonPrimitive("START"))).setService(AconaServiceType.WRITE));
			
			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {
					
				}
			}
			
			client1.getDataStorage().write(Datapoint.newDatapoint(memorydatapoint1).setValue(String.valueOf(value1+1)), "nothing");
			client1.getDataStorage().write(Datapoint.newDatapoint(memorydatapoint2).setValue(String.valueOf(value2+2)), "nothing");
			
			client1.getCell().getCommunicator().write(Datapoint.newDatapoint(commandDatapoint).setValue(new JsonPrimitive("START")), drivetrackAgentName);
			//this.comm.sendAsynchronousMessageToAgent(Message.newMessage().addReceiver(drivetrackAgentName).setContent(Datapoint.newDatapoint(commandDatapoint).setValue(new JsonPrimitive("START"))).setService(AconaServiceType.WRITE));
			
			//Get the result from the result receiver agent
			String result = client2.getCommunicator().read(Datapoint.newDatapoint(resultdatapoint)).getValueAsString();
			
			log.debug("correct value={}, actual value={}", "FINISHED", result);
			
			assertEquals(result, expectedResult);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}
		
	}

}
