package at.tuwien.ict.acona.cell.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.cellfunction.ServiceState;
import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionCodeletHandler;
import at.tuwien.ict.acona.cell.cellfunction.specialfunctions.CFStateGenerator;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.core.cellfunction.codelets.helpers.IncrementOnConditionCodelet;
import at.tuwien.ict.acona.cell.datastructures.Chunk;
import at.tuwien.ict.acona.cell.datastructures.Datapoints;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.jadelauncher.util.KoreExternalControllerImpl;
import jade.core.Runtime;

public class CellStateTester {
	private static Logger log = LoggerFactory.getLogger(CellStateTester.class);
	// private final JadeContainerUtil util = new JadeContainerUtil();
	private KoreExternalControllerImpl launchUtil = KoreExternalControllerImpl.getLauncher();

	/**
	 * Setup the JADE communication. No Jade Gateway necessary
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		try {

			// Create container
			log.debug("Create or get main container");
			this.launchUtil.createMainContainer("localhost", 1099, "MainContainer");
			// mainContainerController =
			// this.util.createMainJADEContainer("localhost", 1099,
			// "MainContainer");

			log.debug("Create subcontainer");
			this.launchUtil.createSubContainer("localhost", 1099, "Subcontainer");

			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {

				}
			}

		} catch (Exception e) {
			log.error("Cannot initialize test environment", e);
		}
	}

	/**
	 * Tear down the JADE container
	 * 
	 * @throws Exception
	 */
	@After
	public void tearDown() throws Exception {
		synchronized (this) {
			try {
				this.wait(2000);
			} catch (InterruptedException e) {

			}
		}

		Runtime runtime = Runtime.instance();
		runtime.shutDown();
		synchronized (this) {
			try {
				this.wait(2000);
			} catch (InterruptedException e) {

			}
		}
	}

	/**
	 * Create a cell with some functions and a state collector function. Execute
	 * 4 function in the cell, read the state of one of the functions at the end
	 * by the the state collector function
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

			//Agent with handler and 2 codelets
			CellConfig codeletAgentConfig = CellConfig.newConfig(controllerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(handlerName, CellFunctionCodeletHandler.class))
					.addCellfunction(CellFunctionConfig.newConfig(codeletName1, IncrementOnConditionCodelet.class)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + handlerName)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementOnConditionCodelet.attributeCheckAddress, processDatapoint)
							.setProperty(IncrementOnConditionCodelet.attributeConditionValue, new JsonPrimitive(1)))
					.addCellfunction(CellFunctionConfig.newConfig(codeletName2, IncrementOnConditionCodelet.class)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + handlerName)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementOnConditionCodelet.attributeCheckAddress, processDatapoint)
							.setProperty(IncrementOnConditionCodelet.attributeConditionValue, new JsonPrimitive(1)))
					.addCellfunction(CellFunctionConfig.newConfig(codeletName3, IncrementOnConditionCodelet.class)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + handlerName)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementOnConditionCodelet.attributeCheckAddress, processDatapoint)
							.setProperty(IncrementOnConditionCodelet.attributeConditionValue, new JsonPrimitive(1)))
					.addCellfunction(CellFunctionConfig.newConfig(CFStateGenerator.class));

			CellGatewayImpl controller = this.launchUtil.createAgent(codeletAgentConfig);

			synchronized (this) {
				try {
					this.wait(500);
				} catch (InterruptedException e) {

				}
			}
			log.info("=== All agents initialized ===");

			JsonRpcRequest request1 = new JsonRpcRequest("executecodelethandler", 1);
			request1.setParameterAsValue(0, false);

			log.debug("Send request to codeletHandler={} and see that it fails because the condition does not match", request1);
			controller.getCommunicator().executeServiceQueryDatapoints(controllerAgentName, handlerName, request1, controllerAgentName, handlerName + ".state", new JsonPrimitive(ServiceState.FINISHED.toString()), 20000);

			//			synchronized (this) {
			//				try {
			//					this.wait(500);
			//				} catch (InterruptedException e) {
			//
			//				}
			//			}

			log.info("Datapoints on the way. Set datapoint value={} to 1.0", processDatapoint);
			controller.writeLocalDatapoint(Datapoints.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			// Start the system by setting start

			JsonRpcRequest request2 = new JsonRpcRequest("executecodelethandler", 1);
			request2.setParameterAsValue(0, false);

			log.debug("Start codelet handler again");
			controller.getCommunicator().executeServiceQueryDatapoints(controllerAgentName, handlerName, request2, controllerAgentName, handlerName + ".state", new JsonPrimitive(ServiceState.FINISHED.toString()), 20000);

			//			synchronized (this) {
			//				try {
			//					this.wait(500);
			//				} catch (InterruptedException e) {
			//
			//				}
			//			}
			//controller.writeLocalDatapoint(Datapoint.newDatapoint(processDatapoint).setValue(new JsonPrimitive(3)));
			//			log.debug("Read if the value has been incremented");
			//			int x = controller.getCommunicator().read(processDatapoint).getValue().getAsInt();
			//			if (x == 2) {
			//				log.debug("Value was incremented");
			//			} else {
			//				log.warn("Value is not 2 as expected");
			//			}
			//			log.info("Value is={}", x);

			//			//Execute codelets once again
			JsonRpcRequest request3 = new JsonRpcRequest("executecodelethandler", 1);
			request3.setParameterAsValue(0, false);

			log.debug("See if value can be incremented again");
			controller.getCommunicator().executeServiceQueryDatapoints(controllerAgentName, handlerName, request3, controllerAgentName, handlerName + ".state", new JsonPrimitive(ServiceState.FINISHED.toString()), 20000);

			//			synchronized (this) {
			//				try {
			//					this.wait(500);
			//				} catch (InterruptedException e) {
			//
			//				}
			//			}

			//log.info("Value is={}", controller.getCommunicator().read(processDatapoint).getValue().getAsInt());

			Chunk state = Chunk.newChunk(controller.getCommunicator().read(CFStateGenerator.SYSTEMSTATEADDRESS).getValue().getAsJsonObject());
			Chunk result = state.getFirstAssociatedContentFromAttribute("hasFunction", "hasName", codeletName2);
			log.debug("correct value={}, actual value={}", expectedResult, result.getValue("hasState"));

			assertEquals(expectedResult, ServiceState.valueOf(result.getValue("hasState")));
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}
}
