package at.tuwien.ict.acona.cell.core.cellfunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.core.cellfunction.helpers.CFDurationThreadTester;
import at.tuwien.ict.acona.cell.core.helpers.DummyCell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class CellExecutorTesterOnly {
	private static Logger log = LoggerFactory.getLogger(CellExecutorTesterOnly.class);
	private CellFunctionThreadImpl executor;

	@Before
	public void setUp() throws Exception {
		log.info("Start cell activator tester");
		try {
			// Setup activationhandler
			executor = new CFDurationThreadTester();

		} catch (Exception e) {
			log.error("Cannot initialize test environment", e);
		}
	}

	@After
	public void tearDown() throws Exception {
		executor.shutDown();
	}

	@Test
	public void executorExecuteOnceTest() {
		log.debug("Start executorExecuteOnceTest");
		try {
			String commandDatapointAddress = "datapoint.command";
			String queryDatapointAddress = "datapoint.query";
			String executeonceDatapointAddress = "datapoint.executeonce";
			String resultDatapointAddress = "datapoint.result";

			CellFunctionConfig config = CellFunctionConfig.newConfig("testExecutor", CFDurationThreadTester.class)
					.addManagedDatapoint(DatapointConfig.newConfig(CFDurationThreadTester.commandDatapointID, commandDatapointAddress, SyncMode.SUBSCRIBEONLY))
					.addManagedDatapoint(DatapointConfig.newConfig(CFDurationThreadTester.queryDatapointID, queryDatapointAddress, SyncMode.SUBSCRIBEONLY))
					.addManagedDatapoint(DatapointConfig.newConfig(CFDurationThreadTester.executeonceDatapointID, executeonceDatapointAddress, SyncMode.SUBSCRIBEONLY))
					.addManagedDatapoint(DatapointConfig.newConfig(CFDurationThreadTester.resultDatapointID, resultDatapointAddress, SyncMode.WRITEONLY));

			// Map<String, List<Condition>> subscriptions = new HashMap<String,
			// List<Condition>>();
			// subscriptions.put(commandDatapoint, new ArrayList<Condition>());
			// subscriptions.put(queryDatapoint, new ArrayList<Condition>());
			// subscriptions.put(executeonceDatapoint, new
			// ArrayList<Condition>());

			DummyCell cell = new DummyCell(CellConfig.newConfig("dummycell"));

			this.executor.init(config, cell);
			// executor.initWithConditions("testexecutor", subscriptions, "",
			// null, cell);

			// Start the executor with anything just to see
			// Create a datapoint to start the function
			Map<String, Datapoint> map = new HashMap<>();
			map.put(commandDatapointAddress, Datapoint.newDatapoint(commandDatapointAddress).setValue(ControlCommand.START.toString()));
			this.executor.updateSubscribedData(map, cell.getLocalName());// .runActivation(Datapoint.newDatapoint(commandDatapoint).setValue(ControlComm;and.START.toString()));

			// Put a delay to mitigate thread troubles
			synchronized (this) {
				try {
					this.wait(100);
				} catch (InterruptedException e) {

				}
			}

			// Now run something that is purposeful
			map.clear();
			map.put(queryDatapointAddress, Datapoint.newDatapoint(queryDatapointAddress).setValue("SELECT * FROM ICT DATABASE AND DELETE FILESERVER"));
			this.executor.updateSubscribedData(map, cell.getLocalName());
			// this.executor.runActivation(Datapoint.newDatapoint(queryDatapoint).setValue("SELECT
			// * FROM ICT DATABASE AND DELETE FILESERVER"));

			log.debug("wait for agent to answer");
			synchronized (this) {
				try {
					this.wait(3000);
				} catch (InterruptedException e) {

				}
			}

			String result = cell.getDataStorage().read(resultDatapointAddress).getValue().getAsString();
			log.info("Shall match={}, Received result={}", "FINISHED", result);
			assertEquals("FINISHED", result);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Cannot test system", e);
			fail("Error");
		}
	}
}
