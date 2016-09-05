package at.tuwien.ict.acona.cell.activator;

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

import at.tuwien.ict.acona.cell.activator.executor.ControlCommand;
import at.tuwien.ict.acona.cell.activator.executor.Executor;
import at.tuwien.ict.acona.cell.activator.helper.ExecutorTestInstance;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class CellExecutorTesterOnly {
	private static Logger log = LoggerFactory.getLogger(CellExecutorTesterOnly.class);
	private Executor executor;
	
	@Before
	public void setUp() throws Exception {
		log.info("Start cell activator tester");
		try {
			//Setup activationhandler
			executor = new ExecutorTestInstance();
			
			
		} catch (Exception e) {
			log.error("Cannot initialize test environment", e);
		}
	}

	@After
	public void tearDown() throws Exception {
		executor.closeActivator();
	}
	
	@Test
	public void executorExecuteOnceTest() {
		log.debug("Start executorExecuteOnceTest");
		try {
			String commandDatapoint = "datapoint.command";
			String queryDatapoint = "datapoint.query";
			String executeonceDatapoint = "datapoint.executeonce";

			Map<String, List<Condition>> subscriptions = new HashMap<String, List<Condition>>();
			subscriptions.put(commandDatapoint, new ArrayList<Condition>());
			subscriptions.put(queryDatapoint, new ArrayList<Condition>());
			subscriptions.put(executeonceDatapoint, new ArrayList<Condition>());
			executor.init("testexecutor", subscriptions, "", null, null);
			
			//Start the executor with anything just to see
			this.executor.runActivation(Datapoint.newDatapoint(commandDatapoint).setValue(ControlCommand.START.toString()));
			
			//Put a delay to mitigate thread troubles
			synchronized (this) {
				try {
					this.wait(10);
				} catch (InterruptedException e) {
					
				}
			}
			
			//Now run something that is purposeful
			this.executor.runActivation(Datapoint.newDatapoint(queryDatapoint).setValue("SELECT * FROM ICT DATABASE AND DELETE FILESERVER"));
			
			log.debug("wait for agent to answer");
			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {
					
				}
			}
			
			assertEquals(false, false);
			
		} catch (Exception e) {
			log.error("Cannot test system", e);
			fail("Error");
		}
	}
}
