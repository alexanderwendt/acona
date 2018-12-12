package at.tuwien.ict.acona.mq.cell.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.invoke.MethodHandles;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.mq.cell.cellfunction.ServiceState;
import at.tuwien.ict.acona.mq.cell.cellfunction.codelets.CellFunctionCodeletHandler;
import at.tuwien.ict.acona.mq.cell.cellfunction.helper.IncrementOnConditionCodelet;
import at.tuwien.ict.acona.mq.cell.cellfunction.specialfunctions.StateMonitor;
import at.tuwien.ict.acona.mq.cell.config.CellConfig;
import at.tuwien.ict.acona.mq.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.mq.datastructures.Chunk;
import at.tuwien.ict.acona.mq.datastructures.ChunkBuilder;
import at.tuwien.ict.acona.mq.datastructures.DPBuilder;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.launcher.SystemControllerImpl;

public class CellStateTester {
	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final DPBuilder dpb = new DPBuilder();
	private SystemControllerImpl launcher = SystemControllerImpl.getLauncher();

	@Before
	public void setUp() throws Exception {
		try {

		} catch (Exception e) {
			log.error("Cannot initialize test environment", e);
		}
	}

	@After
	public void tearDown() throws Exception {
		// Clear all cells
		synchronized (this) {
			try {
				this.wait(10);
			} catch (InterruptedException e) {

			}
		}
		this.launcher.stopSystem();

		synchronized (this) {
			try {
				this.wait(10);
			} catch (InterruptedException e) {

			}
		}
	}

	/**
	 * Create a cell with some functions and a state collector function. Execute 4 function in the cell, read the state of one of the functions at the end by the the state collector function
	 * 
	 * 
	 */
	@Test
	public void stateMonitorWith3CodeletsTest() {
		try {
			String codeletName1 = "CodeletIncrement1"; // The same name for all services
			String codeletName2 = "CodeletIncrement2";
			String codeletName3 = "CodeletIncrement3";
			String handlerName = "CodeletHandler";
			String controllerAgentName = "CodeletExecutorAgent";

			String processDatapoint = "workingmemory.changeme";
			// values
			double startValue = 1;
			ServiceState expectedResult = ServiceState.FINISHED;

			// Agent with handler and 2 codelets
			CellConfig codeletAgentConfig = CellConfig.newConfig(controllerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(handlerName, CellFunctionCodeletHandler.class))
					.addCellfunction(CellFunctionConfig.newConfig(codeletName1, IncrementOnConditionCodelet.class)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTECODELETHANDLERADDRESS,
									controllerAgentName + ":" + handlerName)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementOnConditionCodelet.attributeCheckAddress, processDatapoint)
							.setProperty(IncrementOnConditionCodelet.attributeConditionValue, new JsonPrimitive(1)))
					.addCellfunction(CellFunctionConfig.newConfig(codeletName2, IncrementOnConditionCodelet.class)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTECODELETHANDLERADDRESS,
									controllerAgentName + ":" + handlerName)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementOnConditionCodelet.attributeCheckAddress, processDatapoint)
							.setProperty(IncrementOnConditionCodelet.attributeConditionValue, new JsonPrimitive(1)))
					.addCellfunction(CellFunctionConfig.newConfig(codeletName3, IncrementOnConditionCodelet.class)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTECODELETHANDLERADDRESS,
									controllerAgentName + ":" + handlerName)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementOnConditionCodelet.attributeCheckAddress, processDatapoint)
							.setProperty(IncrementOnConditionCodelet.attributeConditionValue, new JsonPrimitive(1)))
					.addCellfunction(CellFunctionConfig.newConfig(StateMonitor.class));

			Cell controller = this.launcher.createAgent(codeletAgentConfig);

			synchronized (this) {
				try {
					this.wait(500);
				} catch (InterruptedException e) {

				}
			}
			log.info("=== All agents initialized ===");

			//JsonRpcRequest request1 = new JsonRpcRequest("executecodelethandler", 1);
			//request1.setParameterAsValue(0, false);

			log.debug("Send request to codeletHandler and see that it fails because the condition does not match");
			controller.getCommunicator().execute(controllerAgentName + ":" + handlerName + "/" + CellFunctionCodeletHandler.EXECUTECODELETMETHODNAME, new Request(), 200000);

			// synchronized (this) {
			// try {
			// this.wait(500);
			// } catch (InterruptedException e) {
			//
			// }
			// }

			log.info("Datapoints on the way. Set datapoint value={} to 1.0", processDatapoint);
			controller.getCommunicator()
					.write(this.dpb.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			// Start the system by setting start

			log.debug("Start codelet handler again");
			controller.getCommunicator().execute(controllerAgentName + ":" + handlerName + "/" + CellFunctionCodeletHandler.EXECUTECODELETMETHODNAME, new Request(), 200000);

			// synchronized (this) {
			// try {
			// this.wait(500);
			// } catch (InterruptedException e) {
			//
			// }
			// }
			// controller.writeLocalDatapoint(Datapoint.newDatapoint(processDatapoint).setValue(new
			// JsonPrimitive(3)));
			// log.debug("Read if the value has been incremented");
			// int x =
			// controller.getCommunicator().read(processDatapoint).getValue().getAsInt();
			// if (x == 2) {
			// log.debug("Value was incremented");
			// } else {
			// log.warn("Value is not 2 as expected");
			// }
			// log.info("Value is={}", x);

			// //Execute codelets once again
			log.debug("See if value can be incremented again");
			controller.getCommunicator().execute(controllerAgentName + ":" + handlerName + "/" + CellFunctionCodeletHandler.EXECUTECODELETMETHODNAME, new Request(), 200000);

			// synchronized (this) {
			// try {
			// this.wait(500);
			// } catch (InterruptedException e) {
			//
			// }
			// }

			// log.info("Value is={}",
			// controller.getCommunicator().read(processDatapoint).getValue().getAsInt());

			Chunk state = ChunkBuilder.newChunk(controller.getCommunicator().read(StateMonitor.SYSTEMSTATEADDRESS).getValue().getAsJsonObject());
			Chunk result = state.getFirstAssociatedContentFromAttribute("hasFunction", "hasName", "<" + controllerAgentName + ">/" + codeletName2);
			log.debug("correct value={}, actual value={}", expectedResult, result.getValue("hasState"));

			assertEquals(expectedResult, ServiceState.valueOf(result.getValue("hasState")));
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

	/**
	 * Create a cell with some functions and a state collector function. Execute 4 function in the cell, read the state of one of the functions at the end by the the state collector function
	 * 
	 * 
	 */
	@Test
	public void stateMonitorWithXCodeletsTest() {
		try {
			String codeletName = "CodeletIncrement"; // The same name for all services
			// String codeletName2 = "CodeletIncrement2";
			// String codeletName3 = "CodeletIncrement3";
			String handlerName = "CodeletHandler";
			String controllerAgentName = "CodeletExecutorAgent";

			String processDatapoint = "workingmemory.changeme";
			// values
			double startValue = 1;
			ServiceState expectedResult = ServiceState.FINISHED;

			// Agent with handler and 2 codelets
			CellConfig codeletAgentConfig = CellConfig.newConfig(controllerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(handlerName, CellFunctionCodeletHandler.class));

			for (int i = 1; i <= 100; i++) {
				codeletAgentConfig.addCellfunction(CellFunctionConfig
						.newConfig(codeletName + i, IncrementOnConditionCodelet.class)
						.setProperty(IncrementOnConditionCodelet.ATTRIBUTECODELETHANDLERADDRESS,
								controllerAgentName + ":" + handlerName)
						.setProperty(IncrementOnConditionCodelet.ATTRIBUTEEXECUTIONORDER, 0)
						.setProperty(IncrementOnConditionCodelet.attributeCheckAddress, processDatapoint)
						.setProperty(IncrementOnConditionCodelet.attributeConditionValue, new JsonPrimitive(i)));
			}

			codeletAgentConfig.addCellfunction(CellFunctionConfig.newConfig(StateMonitor.class));

			Cell controller = this.launcher.createAgent(codeletAgentConfig);

			synchronized (this) {
				try {
					this.wait(5000);
				} catch (InterruptedException e) {

				}
			}
			log.info("=== All agents initialized ===");

			log.debug("Send request to codeletHandler and see that it fails because the condition does not match");
			controller.getCommunicator().execute(controllerAgentName + ":" + handlerName + "/" + CellFunctionCodeletHandler.EXECUTECODELETMETHODNAME, new Request(), 200000);

			synchronized (this) {
				try {
					this.wait(500);
				} catch (InterruptedException e) {

				}
			}

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonElement state = controller.getCommunicator().read(StateMonitor.SYSTEMSTATEADDRESS).getValue();
			String value = gson.toJson(state);

			log.debug("Value={}", value);

			synchronized (this) {
				try {
					this.wait(10000);
				} catch (InterruptedException e) {

				}
			}
			
			assertEquals(ServiceState.RUNNING.toString(), state.getAsJsonObject().get("hasState").getAsString());
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}
}
