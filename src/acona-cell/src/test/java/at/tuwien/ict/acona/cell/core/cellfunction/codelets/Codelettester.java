package at.tuwien.ict.acona.cell.core.cellfunction.codelets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionCodelet;
import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionCodeletHandler;
import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionHandlerTriggerCodelet;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.core.CellGatewayImpl;
import at.tuwien.ict.acona.cell.core.cellfunction.codelets.helpers.IncrementNumberCodelet;
import at.tuwien.ict.acona.cell.core.cellfunction.codelets.helpers.IncrementOnConditionCodelet;
import at.tuwien.ict.acona.cell.datastructures.Datapoints;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.framework.modules.ActionExecutorCodelet;
import at.tuwien.ict.acona.framework.modules.OptionSelectorCodelet;
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
			String codeletName3 = "CodeletIncrement3";
			String handlerName = "CodeletHandler";
			String controllerAgentName = "CodeletExecutorAgent";

			String processDatapoint = "workingmemory.changeme";
			// values
			double startValue = 1;
			int expectedResult = 3;

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
							.setProperty(IncrementOnConditionCodelet.attributeConditionValue, new JsonPrimitive(2)))
					.addCellfunction(CellFunctionConfig.newConfig(codeletName3, IncrementOnConditionCodelet.class)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + handlerName)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementOnConditionCodelet.attributeCheckAddress, processDatapoint)
							.setProperty(IncrementOnConditionCodelet.attributeConditionValue, new JsonPrimitive(3)));

			CellGatewayImpl controller = this.launcher.createAgent(codeletAgentConfig);

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
			controller.getCommunicator().executeServiceQueryDatapoints(controllerAgentName, handlerName, request1, controllerAgentName, handlerName + ".result", 20000);

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
			controller.getCommunicator().executeServiceQueryDatapoints(controllerAgentName, handlerName, request2, controllerAgentName, handlerName + ".result", 20000);

			//			synchronized (this) {
			//				try {
			//					this.wait(500);
			//				} catch (InterruptedException e) {
			//
			//				}
			//			}
			//controller.writeLocalDatapoint(Datapoint.newDatapoint(processDatapoint).setValue(new JsonPrimitive(3)));
			log.debug("Read if the value has been incremented");
			int x = controller.getCommunicator().read(processDatapoint).getValue().getAsInt();
			if (x == 2) {
				log.debug("Value was incremented");
			}
			log.info("Value is={}", x);

			//			//Execute codelets once again
			JsonRpcRequest request3 = new JsonRpcRequest("executecodelethandler", 1);
			request3.setParameterAsValue(0, false);

			log.debug("See if value can be incremented again");
			controller.getCommunicator().executeServiceQueryDatapoints(controllerAgentName, handlerName, request3, controllerAgentName, handlerName + ".result", 20000);

			//			synchronized (this) {
			//				try {
			//					this.wait(500);
			//				} catch (InterruptedException e) {
			//
			//				}
			//			}

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
			//String notificationDatapoint = "workingmemory.notification";
			// values
			double startValue = 1;
			int expectedResult = 3;

			//Agent with handler and 2 codelets
			CellConfig codeletAgentConfig = CellConfig.newConfig(controllerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(handlerName, CellFunctionCodeletHandler.class))
					.addCellfunction(CellFunctionConfig.newConfig(codeletName1, IncrementOnConditionCodelet.class)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + handlerName)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTEEXECUTIONORDER, "1")
							.setProperty(IncrementOnConditionCodelet.attributeCheckAddress, processDatapoint)
							.setProperty(IncrementOnConditionCodelet.attributeConditionValue, new JsonPrimitive(1)))
					.addCellfunction(CellFunctionConfig.newConfig(codeletName2, IncrementOnConditionCodelet.class)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + handlerName)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTEEXECUTIONORDER, "4")
							.setProperty(IncrementOnConditionCodelet.attributeCheckAddress, processDatapoint)
							.setProperty(IncrementOnConditionCodelet.attributeConditionValue, new JsonPrimitive(2)));

			CellGatewayImpl controller = this.launcher.createAgent(codeletAgentConfig);

			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}
			log.info("=== All agents initialized ===");

			//memoryAgent.writeLocalDatapoint(Datapoint.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			//			controller.getCommunicator().execute(controllerAgentName, handlerName, Arrays.asList(
			//					Datapoint.newDatapoint("method").setValue("executecodelethandler"),
			//					Datapoint.newDatapoint("blockingmethod").setValue(new JsonPrimitive(false))), 1000);

			JsonRpcRequest request1 = new JsonRpcRequest("executecodelethandler", 1);
			request1.setParameterAsValue(0, false);

			controller.getCommunicator().executeServiceQueryDatapoints(controllerAgentName, handlerName, request1,
					controllerAgentName, handlerName + ".result", 20000);

			//			synchronized (this) {
			//				try {
			//					this.wait(500);
			//				} catch (InterruptedException e) {
			//
			//				}
			//			}

			log.info("Datapoints on the way. Set 1");
			controller.writeLocalDatapoint(Datapoints.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			// Start the system by setting start
			//Datapoint state = controller.getCommunicator().queryDatapoints(COMMANDDATAPOINTNAME, new JsonPrimitive(ControlCommand.START.toString()), controller.getCell().getLocalName(), "state", controller.getCell().getLocalName(), 1000000);

			//			controller.getCommunicator().execute(controllerAgentName, handlerName, Arrays.asList(
			//					Datapoint.newDatapoint("method").setValue("executecodelethandler"),
			//					Datapoint.newDatapoint("blockingmethod").setValue(new JsonPrimitive(true))), 1000);

			JsonRpcRequest request2 = new JsonRpcRequest("executecodelethandler", 1);
			request2.setParameterAsValue(0, false);

			controller.getCommunicator().executeServiceQueryDatapoints(controllerAgentName, handlerName, request2,
					controllerAgentName, handlerName + ".result", 20000);

			//			synchronized (this) {
			//				try {
			//					this.wait(500);
			//				} catch (InterruptedException e) {
			//
			//				}
			//			}

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

	/**
	 * Execute the full ACONA architecture with 2 codelets for each codelet
	 * handler. They shall increment different addresses in parallel.
	 * 
	 */
	@Test
	public void cognitiveArchitectureStructureTest() {
		try {
			//Create the agent
			String cognitiveAgentName = "CognitiveAgent";

			//Main codelet handler
			String mainCodeletHandlerName = "MainProcessCodeletHandler";
			String mainCodeletHandlerServiceAddress = cognitiveAgentName + ":" + mainCodeletHandlerName;

			//Codelethandler Activate Concepts
			String activateConceptsCodeletTriggerName = "ActivateConceptsCodeletHandlerTrigger";
			String activateConceptsCodeletHandlerName = "ActivateConceptsCodeletHandler";

			//Codelethandler Create goals
			String createGoalsCodeletTriggerName = "CreateGoalsCodeletHandlerTrigger";
			String createGoalsCodeletHandlerName = "CreateGoalsCodeletHandler";

			//Codelethandler Activate beliefs
			String activateBeliefsCodeletTriggerName = "ActivateBeliefsCodeletHandlerTrigger";
			String activateBeliefsCodeletHandlerName = "ActivateBeliefsCodeletHandler";

			//CodeletHandler Propose Options
			String proposeOptionsCodeletTriggerName = "ProposeOptionsCodeletHandlerTrigger";
			String proposeOptionsCodeletHandlerName = "ProposeOptionsCodeletHandler";

			//CodeletHandler Propose Actions
			String proposeActionsCodeletTriggerName = "ProposeActionsCodeletHandlerTrigger";
			String proposeActionsCodeletHandlerName = "ProposeActionsCodeletHandler";

			//CodeletHandler Evaluate Options
			String evaluteOptionsCodeletTriggerName = "EvaluateOptionsCodeletHandlerTrigger";
			String evaluteOptionsCodeletHandlerName = "EvaluateOptionsCodeletHandler";

			//Codelet Select option (here, no codelethandler is executed, just a normal codelet)
			String selectOptionCodeletName = "SelectOptionCodelet";

			//Codelet Execute Action
			String executeActionCodeletName = "ExecuteActionCodelet";

			//Memories
			String namespaceWorkingMemory = "workingmemory";
			String namespaceInternalStateMemory = "internalstatememory";
			String namespaceLongTermMemory = "longtermmemory";

			//Generate the configuration for the KORE system
			log.info("Generate system configuration");
			// Controller
			// Controller
			CellConfig cognitiveAgentConfig = CellConfig.newConfig(cognitiveAgentName)
					//Main codelethandler
					.addCellfunction(CellFunctionConfig.newConfig(mainCodeletHandlerName, CellFunctionCodeletHandler.class)
							.setProperty(CellFunctionCodeletHandler.ATTRIBUTEWORKINGMEMORYADDRESS, namespaceWorkingMemory)
							.setProperty(CellFunctionCodeletHandler.ATTRIBUTEINTERNALMEMORYADDRESS, namespaceInternalStateMemory))
					//Process codelethandlers
					.addCellfunction(CellFunctionConfig.newConfig(activateConceptsCodeletHandlerName, CellFunctionCodeletHandler.class)
							.setProperty(CellFunctionCodeletHandler.ATTRIBUTEWORKINGMEMORYADDRESS, namespaceWorkingMemory)
							.setProperty(CellFunctionCodeletHandler.ATTRIBUTEINTERNALMEMORYADDRESS, namespaceInternalStateMemory))
					.addCellfunction(CellFunctionConfig.newConfig(createGoalsCodeletHandlerName, CellFunctionCodeletHandler.class)
							.setProperty(CellFunctionCodeletHandler.ATTRIBUTEWORKINGMEMORYADDRESS, namespaceWorkingMemory)
							.setProperty(CellFunctionCodeletHandler.ATTRIBUTEINTERNALMEMORYADDRESS, namespaceInternalStateMemory))
					.addCellfunction(CellFunctionConfig.newConfig(activateBeliefsCodeletHandlerName, CellFunctionCodeletHandler.class)
							.setProperty(CellFunctionCodeletHandler.ATTRIBUTEWORKINGMEMORYADDRESS, namespaceWorkingMemory)
							.setProperty(CellFunctionCodeletHandler.ATTRIBUTEINTERNALMEMORYADDRESS, namespaceInternalStateMemory))
					.addCellfunction(CellFunctionConfig.newConfig(proposeOptionsCodeletHandlerName, CellFunctionCodeletHandler.class)
							.setProperty(CellFunctionCodeletHandler.ATTRIBUTEWORKINGMEMORYADDRESS, namespaceWorkingMemory)
							.setProperty(CellFunctionCodeletHandler.ATTRIBUTEINTERNALMEMORYADDRESS, namespaceInternalStateMemory))
					.addCellfunction(CellFunctionConfig.newConfig(proposeActionsCodeletHandlerName, CellFunctionCodeletHandler.class)
							.setProperty(CellFunctionCodeletHandler.ATTRIBUTEWORKINGMEMORYADDRESS, namespaceWorkingMemory)
							.setProperty(CellFunctionCodeletHandler.ATTRIBUTEINTERNALMEMORYADDRESS, namespaceInternalStateMemory))
					.addCellfunction(CellFunctionConfig.newConfig(evaluteOptionsCodeletHandlerName, CellFunctionCodeletHandler.class)
							.setProperty(CellFunctionCodeletHandler.ATTRIBUTEWORKINGMEMORYADDRESS, namespaceWorkingMemory)
							.setProperty(CellFunctionCodeletHandler.ATTRIBUTEINTERNALMEMORYADDRESS, namespaceInternalStateMemory))
					//Add main process codelets
					//Add trigger codelets
					.addCellfunction(CellFunctionConfig.newConfig(activateConceptsCodeletTriggerName, CellFunctionHandlerTriggerCodelet.class)
							.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
							.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, "1")
							.setProperty(CellFunctionHandlerTriggerCodelet.codeletHandlerServiceUriName, cognitiveAgentName + ":" + activateConceptsCodeletHandlerName))
					.addCellfunction(CellFunctionConfig.newConfig(createGoalsCodeletTriggerName, CellFunctionHandlerTriggerCodelet.class)
							.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
							.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, "2")
							.setProperty(CellFunctionHandlerTriggerCodelet.codeletHandlerServiceUriName, cognitiveAgentName + ":" + createGoalsCodeletHandlerName))
					.addCellfunction(CellFunctionConfig.newConfig(activateBeliefsCodeletTriggerName, CellFunctionHandlerTriggerCodelet.class)
							.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
							.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, "3")
							.setProperty(CellFunctionHandlerTriggerCodelet.codeletHandlerServiceUriName, cognitiveAgentName + ":" + activateBeliefsCodeletHandlerName))
					.addCellfunction(CellFunctionConfig.newConfig(proposeOptionsCodeletTriggerName, CellFunctionHandlerTriggerCodelet.class)
							.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
							.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, "4")
							.setProperty(CellFunctionHandlerTriggerCodelet.codeletHandlerServiceUriName, cognitiveAgentName + ":" + proposeOptionsCodeletHandlerName))
					.addCellfunction(CellFunctionConfig.newConfig(proposeActionsCodeletTriggerName, CellFunctionHandlerTriggerCodelet.class)
							.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
							.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, "5")
							.setProperty(CellFunctionHandlerTriggerCodelet.codeletHandlerServiceUriName, cognitiveAgentName + ":" + proposeActionsCodeletHandlerName))
					.addCellfunction(CellFunctionConfig.newConfig(evaluteOptionsCodeletTriggerName, CellFunctionHandlerTriggerCodelet.class)
							.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
							.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, "6")
							.setProperty(CellFunctionHandlerTriggerCodelet.codeletHandlerServiceUriName, cognitiveAgentName + ":" + evaluteOptionsCodeletHandlerName))
					//Direct codelets
					.addCellfunction(CellFunctionConfig.newConfig(selectOptionCodeletName, OptionSelectorCodelet.class)
							.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
							.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, "7"))
					.addCellfunction(CellFunctionConfig.newConfig(executeActionCodeletName, ActionExecutorCodelet.class)
							.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
							.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, "8"));

			//Add the specific codelets
			String incrementServiceName = "incrementservice";
			String incrementDatapoint1 = "incrementme1";
			String incrementDatapoint2 = "incrementme2";

			cognitiveAgentConfig
					//.addCellfunction(CellFunctionConfig.newConfig(incrementServiceName, CFIncrementService.class)
					//		.addManagedDatapoint(DatapointConfig.newConfig(CFIncrementService.ATTRIBUTEINCREMENTDATAPOINT, namespaceWorkingMemory + "." + incrementDatapoint, SyncMode.SUBSCRIBEWRITEBACK)))
					.addCellfunction(CellFunctionConfig.newConfig("IncrementCodelet11", IncrementNumberCodelet.class)
							.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, cognitiveAgentName + ":" + activateConceptsCodeletHandlerName)
							.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESERVICENAME, incrementServiceName)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESUBADDRESS, incrementDatapoint1))
					.addCellfunction(CellFunctionConfig.newConfig("IncrementCodelet12", IncrementNumberCodelet.class)
							.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, cognitiveAgentName + ":" + createGoalsCodeletHandlerName)
							.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESERVICENAME, incrementServiceName)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESUBADDRESS, incrementDatapoint1))
					.addCellfunction(CellFunctionConfig.newConfig("IncrementCodelet13", IncrementNumberCodelet.class)
							.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, cognitiveAgentName + ":" + activateBeliefsCodeletHandlerName)
							.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESERVICENAME, incrementServiceName)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESUBADDRESS, incrementDatapoint1))
					.addCellfunction(CellFunctionConfig.newConfig("IncrementCodelet14", IncrementNumberCodelet.class)
							.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, cognitiveAgentName + ":" + proposeOptionsCodeletHandlerName)
							.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESERVICENAME, incrementServiceName)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESUBADDRESS, incrementDatapoint1))
					.addCellfunction(CellFunctionConfig.newConfig("IncrementCodelet15", IncrementNumberCodelet.class)
							.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, cognitiveAgentName + ":" + proposeActionsCodeletHandlerName)
							.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESERVICENAME, incrementServiceName)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESUBADDRESS, incrementDatapoint1))
					.addCellfunction(CellFunctionConfig.newConfig("IncrementCodelet16", IncrementNumberCodelet.class)
							.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, cognitiveAgentName + ":" + evaluteOptionsCodeletHandlerName)
							.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESERVICENAME, incrementServiceName)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESUBADDRESS, incrementDatapoint1));

			cognitiveAgentConfig
					//.addCellfunction(CellFunctionConfig.newConfig(incrementServiceName, CFIncrementService.class)
					//		.addManagedDatapoint(DatapointConfig.newConfig(CFIncrementService.ATTRIBUTEINCREMENTDATAPOINT, namespaceWorkingMemory + "." + incrementDatapoint, SyncMode.SUBSCRIBEWRITEBACK)))
					.addCellfunction(CellFunctionConfig.newConfig("IncrementCodelet21", IncrementNumberCodelet.class)
							.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, cognitiveAgentName + ":" + activateConceptsCodeletHandlerName)
							.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESERVICENAME, incrementServiceName)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESUBADDRESS, incrementDatapoint2))
					.addCellfunction(CellFunctionConfig.newConfig("IncrementCodelet22", IncrementNumberCodelet.class)
							.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, cognitiveAgentName + ":" + createGoalsCodeletHandlerName)
							.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESERVICENAME, incrementServiceName)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESUBADDRESS, incrementDatapoint2))
					.addCellfunction(CellFunctionConfig.newConfig("IncrementCodelet23", IncrementNumberCodelet.class)
							.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, cognitiveAgentName + ":" + activateBeliefsCodeletHandlerName)
							.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESERVICENAME, incrementServiceName)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESUBADDRESS, incrementDatapoint2))
					.addCellfunction(CellFunctionConfig.newConfig("IncrementCodelet24", IncrementNumberCodelet.class)
							.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, cognitiveAgentName + ":" + proposeOptionsCodeletHandlerName)
							.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESERVICENAME, incrementServiceName)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESUBADDRESS, incrementDatapoint2))
					.addCellfunction(CellFunctionConfig.newConfig("IncrementCodelet25", IncrementNumberCodelet.class)
							.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, cognitiveAgentName + ":" + proposeActionsCodeletHandlerName)
							.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESERVICENAME, incrementServiceName)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESUBADDRESS, incrementDatapoint2))
					.addCellfunction(CellFunctionConfig.newConfig("IncrementCodelet26", IncrementNumberCodelet.class)
							.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, cognitiveAgentName + ":" + evaluteOptionsCodeletHandlerName)
							.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESERVICENAME, incrementServiceName)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESUBADDRESS, incrementDatapoint2));

			log.debug("Start agent with config={}", cognitiveAgentConfig);

			CellGatewayImpl cogsys = this.launcher.createAgent(cognitiveAgentConfig);

			synchronized (this) {
				try {
					this.wait(5000);
				} catch (InterruptedException e) {

				}
			}

			//Write initial value on the incrementaddress
			cogsys.getCommunicator().write(Datapoints.newDatapoint(namespaceWorkingMemory + "." + incrementDatapoint1).setValue(0));
			cogsys.getCommunicator().write(Datapoints.newDatapoint(namespaceWorkingMemory + "." + incrementDatapoint2).setValue(0));

			log.info("=== All agents initialized ===");

			//			memoryAgent.writeLocalDatapoint(Datapoint.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			//			cogsys.getCommunicator().execute(cognitiveAgentName, mainCodeletHandlerName, Arrays.asList(
			//					Datapoint.newDatapoint("method").setValue("executecodelethandler"),
			//					Datapoint.newDatapoint("blockingmethod").setValue(new JsonPrimitive(true))), 10000);

			JsonRpcRequest request1 = new JsonRpcRequest("executecodelethandler", 1);
			request1.setParameterAsValue(0, false);

			cogsys.getCommunicator().executeServiceQueryDatapoints(cognitiveAgentName, mainCodeletHandlerName, request1, cognitiveAgentName, mainCodeletHandlerName + ".result", 200000);

			//			synchronized (this) {
			//				try {
			//					this.wait(1000);
			//				} catch (InterruptedException e) {
			//
			//				}
			//			}

			log.info("Read working memory={}", cogsys.getDataStorage());

			int result1 = (int) (cogsys.getCommunicator().read(namespaceWorkingMemory + "." + incrementDatapoint1).getValue().getAsDouble());
			int result2 = (int) (cogsys.getCommunicator().read(namespaceWorkingMemory + "." + incrementDatapoint1).getValue().getAsDouble());
			int expectedResult1 = 6;
			int expectedResult2 = 6;

			log.debug("correct value={}, actual value={}", expectedResult1, result1);

			assertEquals(expectedResult1, result1);

			log.debug("correct value={}, actual value={}", expectedResult2, result2);
			assertEquals(expectedResult2, result2);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

}
