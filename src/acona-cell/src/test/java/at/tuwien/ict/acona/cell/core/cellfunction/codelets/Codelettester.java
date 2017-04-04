package at.tuwien.ict.acona.cell.core.cellfunction.codelets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionCodeletHandler;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.core.CellGatewayImpl;
import at.tuwien.ict.acona.cell.core.cellfunction.helpers.IncrementCodelet;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.jadelauncher.util.KoreExternalControllerImpl;
import jade.core.Runtime;

public class Codelettester {
	private static Logger log = LoggerFactory.getLogger(Codelettester.class);
	private KoreExternalControllerImpl launcher = KoreExternalControllerImpl.getLauncher();

	@Before
	public void setUp() throws Exception {
		try {
			// Create container
			log.debug("Create or get main container");
			this.launcher.createMainContainer("localhost", 1099, "MainContainer");

			log.debug("Create subcontainer");
			this.launcher.createSubContainer("localhost", 1099, "Subcontainer");

			// log.debug("Create gui");
			// this.commUtil.createDebugUserInterface();

			// Create gateway
			// commUtil.initJadeGateway();

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
	}

	/**
	 * 2 codelets register in the codelet handler. Both of them will increment a
	 * number by 1 if a condition applies. The condition is the number. The
	 * purpose is that the codelet handler is triggered 2 times and the codelets
	 * are executed in serie.
	 * 
	 */
	@Test
	public void CodeletHandlerWith2CodeletsTest() {
		try {
			String codeletName1 = "CodeletIncrement1"; // The same name for all services
			String codeletName2 = "CodeletIncrement2";
			String handlerName = "CodeletHandler";
			String controllerAgentName = "CodeletExecutorAgent";

			String processDatapoint = "workingmemory.changeme";
			String notificationDatapoint = "workingmemory.notification";
			// values
			double startValue = 1;
			int expectedResult = 3;

			//Agent with handler and 2 codelets
			CellConfig codeletAgentConfig = CellConfig.newConfig(controllerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(handlerName, CellFunctionCodeletHandler.class))
					.addCellfunction(CellFunctionConfig.newConfig(codeletName1, IncrementCodelet.class)
							.setProperty(IncrementCodelet.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + handlerName)
							.setProperty(IncrementCodelet.attributeCheckAddress, processDatapoint)
							.setProperty(IncrementCodelet.attributeCheckValue, new JsonPrimitive(1)))
					.addCellfunction(CellFunctionConfig.newConfig(codeletName2, IncrementCodelet.class)
							.setProperty(IncrementCodelet.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + handlerName)
							.setProperty(IncrementCodelet.attributeCheckAddress, processDatapoint)
							.setProperty(IncrementCodelet.attributeCheckValue, new JsonPrimitive(2)));

			CellGatewayImpl controller = this.launcher.createAgent(codeletAgentConfig);

			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}
			log.info("=== All agents initialized ===");

			//memoryAgent.writeLocalDatapoint(Datapoint.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			controller.getCommunicator().execute(controllerAgentName, handlerName, Arrays.asList(
					Datapoint.newDatapoint("method").setValue("executecodelethandler"),
					Datapoint.newDatapoint("notificationaddress").setValue(notificationDatapoint)), 1000);

			synchronized (this) {
				try {
					this.wait(500);
				} catch (InterruptedException e) {

				}
			}

			log.info("Datapoints on the way. Set 1");
			controller.writeLocalDatapoint(Datapoint.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			// Start the system by setting start
			//Datapoint state = controller.getCommunicator().queryDatapoints(COMMANDDATAPOINTNAME, new JsonPrimitive(ControlCommand.START.toString()), controller.getCell().getLocalName(), "state", controller.getCell().getLocalName(), 1000000);

			controller.getCommunicator().execute(controllerAgentName, handlerName, Arrays.asList(
					Datapoint.newDatapoint("method").setValue("executecodelethandler"),
					Datapoint.newDatapoint("notificationaddress").setValue(notificationDatapoint)), 1000);

			synchronized (this) {
				try {
					this.wait(500);
				} catch (InterruptedException e) {

				}
			}

			log.info("Value is={}", controller.getCommunicator().read(processDatapoint).getValue().getAsInt());

			//Execute codelets once again
			controller.getCommunicator().execute(controllerAgentName, handlerName, Arrays.asList(
					Datapoint.newDatapoint("method").setValue("executecodelethandler"),
					Datapoint.newDatapoint("notificationaddress").setValue(notificationDatapoint)), 1000);

			synchronized (this) {
				try {
					this.wait(500);
				} catch (InterruptedException e) {

				}
			}

			log.info("Value is={}", controller.getCommunicator().read(processDatapoint).getValue().getAsInt());

			double result = controller.getCommunicator().read(processDatapoint).getValue().getAsInt();

			log.debug("correct value={}, actual value={}", expectedResult, result);

			assertEquals(result, expectedResult, 0.0);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

	/**
	 * 2 codelets register in the codelet handler. Both of them will increment a
	 * number by 1 if a condition applies. The condition is the number. The
	 * purpose is that the codelet handler is triggered 1 time but execute both
	 * codelets in series because they have different execution order
	 * 
	 */
	@Test
	public void CodeletHandlerWith2CodeletsRunOrderTest() {
		try {
			String codeletName1 = "CodeletIncrement1"; // The same name for all services
			String codeletName2 = "CodeletIncrement2";
			String handlerName = "CodeletHandler";
			String controllerAgentName = "CodeletExecutorAgent";

			String processDatapoint = "workingmemory.changeme";
			String notificationDatapoint = "workingmemory.notification";
			// values
			double startValue = 1;
			int expectedResult = 3;

			//Agent with handler and 2 codelets
			CellConfig codeletAgentConfig = CellConfig.newConfig(controllerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(handlerName, CellFunctionCodeletHandler.class))
					.addCellfunction(CellFunctionConfig.newConfig(codeletName1, IncrementCodelet.class)
							.setProperty(IncrementCodelet.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + handlerName)
							.setProperty(IncrementCodelet.ATTRIBUTEEXECUTIONORDER, "1")
							.setProperty(IncrementCodelet.attributeCheckAddress, processDatapoint)
							.setProperty(IncrementCodelet.attributeCheckValue, new JsonPrimitive(1)))
					.addCellfunction(CellFunctionConfig.newConfig(codeletName2, IncrementCodelet.class)
							.setProperty(IncrementCodelet.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + handlerName)
							.setProperty(IncrementCodelet.ATTRIBUTEEXECUTIONORDER, "4")
							.setProperty(IncrementCodelet.attributeCheckAddress, processDatapoint)
							.setProperty(IncrementCodelet.attributeCheckValue, new JsonPrimitive(2)));

			CellGatewayImpl controller = this.launcher.createAgent(codeletAgentConfig);

			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}
			log.info("=== All agents initialized ===");

			//memoryAgent.writeLocalDatapoint(Datapoint.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			controller.getCommunicator().execute(controllerAgentName, handlerName, Arrays.asList(
					Datapoint.newDatapoint("method").setValue("executecodelethandler"),
					Datapoint.newDatapoint("notificationaddress").setValue(notificationDatapoint)), 1000);

			synchronized (this) {
				try {
					this.wait(500);
				} catch (InterruptedException e) {

				}
			}

			log.info("Datapoints on the way. Set 1");
			controller.writeLocalDatapoint(Datapoint.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			// Start the system by setting start
			//Datapoint state = controller.getCommunicator().queryDatapoints(COMMANDDATAPOINTNAME, new JsonPrimitive(ControlCommand.START.toString()), controller.getCell().getLocalName(), "state", controller.getCell().getLocalName(), 1000000);

			controller.getCommunicator().execute(controllerAgentName, handlerName, Arrays.asList(
					Datapoint.newDatapoint("method").setValue("executecodelethandler"),
					Datapoint.newDatapoint("notificationaddress").setValue(notificationDatapoint)), 1000);

			synchronized (this) {
				try {
					this.wait(500);
				} catch (InterruptedException e) {

				}
			}

			log.info("Value is={}", controller.getCommunicator().read(processDatapoint).getValue().getAsInt());

			//Execute codelets once again
			//			controller.getCommunicator().execute(controllerAgentName, handlerName, Arrays.asList(
			//					Datapoint.newDatapoint("method").setValue("executecodelethandler"),
			//					Datapoint.newDatapoint("notificationaddress").setValue(notificationDatapoint)), 1000);
			//
			//			synchronized (this) {
			//				try {
			//					this.wait(500);
			//				} catch (InterruptedException e) {
			//
			//				}
			//			}
			//
			//			log.info("Value is={}", controller.getCommunicator().read(processDatapoint).getValue().getAsInt());

			double result = controller.getCommunicator().read(processDatapoint).getValue().getAsInt();

			log.debug("correct value={}, actual value={}", expectedResult, result);

			assertEquals(result, expectedResult, 0.0);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

}
