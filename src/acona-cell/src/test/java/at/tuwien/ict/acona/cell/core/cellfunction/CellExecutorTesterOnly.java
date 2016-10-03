package at.tuwien.ict.acona.cell.core.cellfunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.activator.helper.DummyCell;
import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.cellfunction.special.Condition;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.core.cellfunction.helpers.CFDurationThreadTester;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class CellExecutorTesterOnly {
	private static Logger log = LoggerFactory.getLogger(CellExecutorTesterOnly.class);
	private CellFunctionThreadImpl executor;
	
	@Before
	public void setUp() throws Exception {
		log.info("Start cell activator tester");
		try {
			//Setup activationhandler
			executor = new CFDurationThreadTester();
			
			
		} catch (Exception e) {
			log.error("Cannot initialize test environment", e);
		}
	}

	@After
	public void tearDown() throws Exception {
		executor.setExit();
	}
	
	@Test
	public void executorExecuteOnceTest() {
		log.debug("Start executorExecuteOnceTest");
		try {
			String commandDatapoint = "datapoint.command";
			String queryDatapoint = "datapoint.query";
			String executeonceDatapoint = "datapoint.executeonce";

			CellFunctionConfig config = CellFunctionConfig.newConfig("testExecutor", CFDurationThreadTester.class)
					.addSubscription(DatapointConfig.newConfig("command", commandDatapoint))
					.addSubscription(DatapointConfig.newConfig("query", queryDatapoint))
					.addSubscription(DatapointConfig.newConfig("executeonce", executeonceDatapoint));
			
//			Map<String, List<Condition>> subscriptions = new HashMap<String, List<Condition>>();
//			subscriptions.put(commandDatapoint, new ArrayList<Condition>());
//			subscriptions.put(queryDatapoint, new ArrayList<Condition>());
//			subscriptions.put(executeonceDatapoint, new ArrayList<Condition>());
			
			
			
			DummyCell cell = new DummyCell();
			
			this.executor.init(config, cell);
			//executor.initWithConditions("testexecutor", subscriptions, "", null, cell);
			
			//Start the executor with anything just to see
			//Create a datapoint to start the function
			Map<String, Datapoint> map = new HashMap<String, Datapoint>();
			map.put(commandDatapoint, Datapoint.newDatapoint(commandDatapoint).setValue(ControlCommand.START.toString()));		
			this.executor.updateData(map);//.runActivation(Datapoint.newDatapoint(commandDatapoint).setValue(ControlComm;and.START.toString()));
			
			//Put a delay to mitigate thread troubles
			synchronized (this) {
				try {
					this.wait(10);
				} catch (InterruptedException e) {
					
				}
			}
			
			//Now run something that is purposeful
			map.clear();
			map.put(queryDatapoint, Datapoint.newDatapoint(queryDatapoint).setValue("SELECT * FROM ICT DATABASE AND DELETE FILESERVER"));
			this.executor.updateData(map);
			//this.executor.runActivation(Datapoint.newDatapoint(queryDatapoint).setValue("SELECT * FROM ICT DATABASE AND DELETE FILESERVER"));
			
			log.debug("wait for agent to answer");
			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {
					
				}
			}
			
			String result = cell.getDataStorage().read("datapoint.result").getValue().getAsString();
			log.info("Shall match={}, Received result={}", "FINISHED", result);
			assertEquals("FINISHED", result);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Cannot test system", e);
			fail("Error");
		}
	}
}
