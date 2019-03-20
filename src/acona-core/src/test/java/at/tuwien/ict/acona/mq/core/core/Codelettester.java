package at.tuwien.ict.acona.mq.core.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.mq.core.agentfunction.codelets.CodeletImpl;
import at.tuwien.ict.acona.mq.core.agentfunction.codelets.CodeletHandlerImpl;
import at.tuwien.ict.acona.mq.core.agentfunction.codelets.CodeletHandlerTriggerCodelet;
import at.tuwien.ict.acona.mq.core.agentfunction.helper.IncrementNumberCodelet;
import at.tuwien.ict.acona.mq.core.agentfunction.helper.IncrementOnConditionCodelet;
import at.tuwien.ict.acona.mq.core.config.AgentConfig;
import at.tuwien.ict.acona.mq.core.config.AgentFunctionConfig;
import at.tuwien.ict.acona.mq.core.core.Cell;
import at.tuwien.ict.acona.mq.datastructures.DPBuilder;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.launcher.SystemController;
import at.tuwien.ict.acona.mq.launcher.SystemControllerImpl;

public class Codelettester {
	private static Logger log = LoggerFactory.getLogger(Codelettester.class);
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
		synchronized (this) {
			try {
				this.wait(2000);
			} catch (InterruptedException e) {

			}
		}

		this.launcher.stopSystem();

		synchronized (this) {
			try {
				this.wait(2000);
			} catch (InterruptedException e) {

			}
		}
	}
	
	/**
	 * 2 codelets register in the codelet handler. Both of them will increment a number by 1 if a condition applies. The condition is the number. The purpose is that the codelet handler is triggered 2
	 * times and the codelets are executed in serie.
	 * 
	 */
	@Test
	public void CodeletHandler1CodeletTest() {
		try {
			String codeletName1 = "CodeletIncrement1"; // The same name for all services
			String handlerName = "CodeletHandler";
			String controllerAgentName = "CodeletExecutorAgent";

			String processDatapoint = "workingmemory.changeme";
			// values
			double startValue = 1;
			int expectedResult = 2;

			// Agent with handler and 3 codelets
			AgentConfig codeletAgentConfig = AgentConfig.newConfig(controllerAgentName)
					.addFunction(AgentFunctionConfig.newConfig(handlerName, CodeletHandlerImpl.class))
					.addFunction(AgentFunctionConfig.newConfig(codeletName1, IncrementOnConditionCodelet.class)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTECODELETHANDLERADDRESS,
									controllerAgentName + ":" + handlerName)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementOnConditionCodelet.attributeCheckAddress, processDatapoint)
							.setProperty(IncrementOnConditionCodelet.attributeConditionValue, new JsonPrimitive(1)));

			Cell controller = this.launcher.createAgent(codeletAgentConfig);

			synchronized (this) {
				try {
					this.wait(500);
				} catch (InterruptedException e) {

				}
			}
			log.info("=== All agents initialized ===");

			//Request request1 = new Request();

			log.debug("Send request to codeletHandler={} and see that it fails because the condition does not match", controllerAgentName + ":" + handlerName);
			controller.getCommunicator().execute(controllerAgentName + ":" + handlerName + "/" + CodeletHandlerImpl.EXECUTECODELETMETHODNAME, new Request(), 200000);

			log.info("Datapoints on the way. Set datapoint value={} to 1.0", processDatapoint);
			controller.getCommunicator().write(this.dpb.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			
			// Start the system by setting start
			log.debug("Start codelet handler again");
			controller.getCommunicator().execute(controllerAgentName + ":" + handlerName + "/" + CodeletHandlerImpl.EXECUTECODELETMETHODNAME, new Request(), 200000);

			log.debug("Read if the value has been incremented");
			int x = controller.getCommunicator().read(processDatapoint).getValue().getAsInt();
			if (x == 2) {
				log.debug("Value was incremented");
			} else {
				log.warn("Value is not 2 as expected");
			}
			log.info("Value is={}", x);

			//Execute codelets once again
			//log.debug("See if value can be incremented again");
			//controller.getCommunicator().execute(controllerAgentName + ":" + handlerName + "/" + CellFunctionCodeletHandler.EXECUTECODELETMETHODNAME, new Request(), 20000);

			log.info("Value is={}", controller.getCommunicator().read(processDatapoint).getValue().getAsInt());

			double result = controller.getCommunicator().read(processDatapoint).getValue().getAsInt();

			log.debug("correct value={}, actual value={}", expectedResult, result);

			assertEquals(expectedResult, result, 0.0);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

	/**
	 * 2 codelets register in the codelet handler. Both of them will increment a number by 1 if a condition applies. The condition is the number. The purpose is that the codelet handler is triggered 2
	 * times and the codelets are executed in serie.
	 * 
	 */
	@Test
	public void CodeletHandlerWith3CodeletsTest() {
		try {
			String codeletName1 = "CodeletIncrement1"; // The same name for all services
			String codeletName2 = "CodeletIncrement2";
			String codeletName3 = "CodeletIncrement3";
			String handlerName = "CodeletHandler";
			String controllerAgentName = "CodeletExecutorAgent";

			String processDatapoint = "workingmemory.changeme";
			// values
			double startValue = 1;
			int expectedResult = 4;

			// Agent with handler and 3 codelets
			AgentConfig codeletAgentConfig = AgentConfig.newConfig(controllerAgentName)
					.addFunction(AgentFunctionConfig.newConfig(handlerName, CodeletHandlerImpl.class))
					.addFunction(AgentFunctionConfig.newConfig(codeletName1, IncrementOnConditionCodelet.class)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTECODELETHANDLERADDRESS,
									controllerAgentName + ":" + handlerName)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementOnConditionCodelet.attributeCheckAddress, processDatapoint)
							.setProperty(IncrementOnConditionCodelet.attributeConditionValue, new JsonPrimitive(1)))
					.addFunction(AgentFunctionConfig.newConfig(codeletName2, IncrementOnConditionCodelet.class)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTECODELETHANDLERADDRESS,
									controllerAgentName + ":" + handlerName)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTEEXECUTIONORDER, 1)
							.setProperty(IncrementOnConditionCodelet.attributeCheckAddress, processDatapoint)
							.setProperty(IncrementOnConditionCodelet.attributeConditionValue, new JsonPrimitive(2)))
					.addFunction(AgentFunctionConfig.newConfig(codeletName3, IncrementOnConditionCodelet.class)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTECODELETHANDLERADDRESS,
									controllerAgentName + ":" + handlerName)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTEEXECUTIONORDER, 2)
							.setProperty(IncrementOnConditionCodelet.attributeCheckAddress, processDatapoint)
							.setProperty(IncrementOnConditionCodelet.attributeConditionValue, new JsonPrimitive(3)));

			Cell controller = this.launcher.createAgent(codeletAgentConfig);

			synchronized (this) {
				try {
					this.wait(500);
				} catch (InterruptedException e) {

				}
			}
			log.info("=== All agents initialized ===");

			//Request request1 = new Request();

			log.debug("Send request to codeletHandler={} and see that it fails because the condition does not match", controllerAgentName + ":" + handlerName);
			controller.getCommunicator().execute(controllerAgentName + ":" + handlerName + "/" + CodeletHandlerImpl.EXECUTECODELETMETHODNAME, new Request(), 20000);

			log.info("Datapoints on the way. Set datapoint value={} to 1.0", processDatapoint);
			controller.getCommunicator().write(this.dpb.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			
			// Start the system by setting start
			log.debug("Start codelet handler again");
			controller.getCommunicator().execute(controllerAgentName + ":" + handlerName + "/" + CodeletHandlerImpl.EXECUTECODELETMETHODNAME, new Request(), 20000);

			log.debug("Read if the value has been incremented");
			int x = controller.getCommunicator().read(processDatapoint).getValue().getAsInt();
			if (x == 2) {
				log.debug("Value was incremented");
			} else {
				log.warn("Value is not 2 as expected");
			}
			log.info("Value is={}", x);

			//Execute codelets once again
			log.debug("See if value can be incremented again");
			controller.getCommunicator().execute(controllerAgentName + ":" + handlerName + "/" + CodeletHandlerImpl.EXECUTECODELETMETHODNAME, new Request(), 20000);

			log.info("Value is={}", controller.getCommunicator().read(processDatapoint).getValue().getAsInt());

			double result = controller.getCommunicator().read(processDatapoint).getValue().getAsInt();

			log.debug("correct value={}, actual value={}", expectedResult, result);

			assertEquals(expectedResult, result, 0.0);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

	/**
	 * 2 codelets register in the codelet handler. Both of them will increment a number by 1 if a condition applies. The condition is the number. The purpose is that the codelet handler is triggered 1
	 * time but execute both codelets in series because they have different execution order
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
			// String notificationDatapoint = "workingmemory.notification";
			// values
			double startValue = 1;
			int expectedResult = 3;

			// Agent with handler and 2 codelets
			AgentConfig codeletAgentConfig = AgentConfig.newConfig(controllerAgentName)
					.addFunction(AgentFunctionConfig.newConfig(handlerName, CodeletHandlerImpl.class))
					.addFunction(AgentFunctionConfig.newConfig(codeletName1, IncrementOnConditionCodelet.class)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTECODELETHANDLERADDRESS,
									controllerAgentName + ":" + handlerName)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTEEXECUTIONORDER, "1")
							.setProperty(IncrementOnConditionCodelet.attributeCheckAddress, processDatapoint)
							.setProperty(IncrementOnConditionCodelet.attributeConditionValue, new JsonPrimitive(1)))
					.addFunction(AgentFunctionConfig.newConfig(codeletName2, IncrementOnConditionCodelet.class)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTECODELETHANDLERADDRESS,
									controllerAgentName + ":" + handlerName)
							.setProperty(IncrementOnConditionCodelet.ATTRIBUTEEXECUTIONORDER, "4")
							.setProperty(IncrementOnConditionCodelet.attributeCheckAddress, processDatapoint)
							.setProperty(IncrementOnConditionCodelet.attributeConditionValue, new JsonPrimitive(2)));

			Cell controller = this.launcher.createAgent(codeletAgentConfig);

			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {

				}
			}
			log.info("=== All agents initialized ===");

			controller.getCommunicator().execute(controllerAgentName + ":" + handlerName + "/" + CodeletHandlerImpl.EXECUTECODELETMETHODNAME, new Request(), 20000);
			
//			JsonRpcRequest request1 = new JsonRpcRequest("executecodelethandler", 1);
//			request1.setParameterAsValue(0, false);
//
//			controller.getCommunicator().executeServiceQueryDatapoints(controllerAgentName, handlerName, request1,
//					controllerAgentName, handlerName + ".state", new JsonPrimitive(ServiceState.FINISHED.toString()),
//					20000);

			// synchronized (this) {
			// try {
			// this.wait(500);
			// } catch (InterruptedException e) {
			//
			// }
			// }

			log.info("Datapoints on the way. Set 1");
			controller.getCommunicator().write(this.dpb.newDatapoint(processDatapoint).setValue(new JsonPrimitive(startValue)));
			// Start the system by setting start
			// Datapoint state =
			// controller.getCommunicator().queryDatapoints(COMMANDDATAPOINTNAME, new
			// JsonPrimitive(ControlCommand.START.toString()),
			// controller.getCell().getLocalName(), "state",
			// controller.getCell().getLocalName(), 1000000);

			// controller.getCommunicator().execute(controllerAgentName, handlerName,
			// Arrays.asList(
			// Datapoint.newDatapoint("method").setValue("executecodelethandler"),
			// Datapoint.newDatapoint("blockingmethod").setValue(new JsonPrimitive(true))),
			// 1000);

//			JsonRpcRequest request2 = new JsonRpcRequest("executecodelethandler", 1);
//			request2.setParameterAsValue(0, false);
//
//			controller.getCommunicator().executeServiceQueryDatapoints(controllerAgentName, handlerName, request2,
//					controllerAgentName, handlerName + ".state", new JsonPrimitive(ServiceState.FINISHED.toString()),
//					20000);
			
			controller.getCommunicator().execute(controllerAgentName + ":" + handlerName + "/" + CodeletHandlerImpl.EXECUTECODELETMETHODNAME, new Request(), 20000);

			// synchronized (this) {
			// try {
			// this.wait(500);
			// } catch (InterruptedException e) {
			//
			// }
			// }

			log.info("Value is={}", controller.getCommunicator().read(processDatapoint).getValue().getAsInt());

			// Execute codelets once again
			// controller.getCommunicator().execute(controllerAgentName, handlerName,
			// Arrays.asList(
			// Datapoint.newDatapoint("method").setValue("executecodelethandler"),
			// Datapoint.newDatapoint("notificationaddress").setValue(notificationDatapoint)),
			// 1000);
			//
			// synchronized (this) {
			// try {
			// this.wait(500);
			// } catch (InterruptedException e) {
			//
			// }
			// }
			//
			// log.info("Value is={}",
			// controller.getCommunicator().read(processDatapoint).getValue().getAsInt());

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
	 * Execute the full ACONA architecture with 2 codelets for each codelet handler. They shall increment different addresses in parallel.
	 * 
	 */
	@Test
	public void cognitiveArchitectureStructureTest() {
		try {
			// Create the agent
			String cognitiveAgentName = "CognitiveAgent";

			// Main codelet handler
			String mainCodeletHandlerName = "MainProcessCodeletHandler";
			String mainCodeletHandlerServiceAddress = cognitiveAgentName + ":" + mainCodeletHandlerName;

			// Codelethandler Activate Concepts
			String activateConceptsCodeletTriggerName = "ActivateConceptsCodeletHandlerTrigger";
			String activateConceptsCodeletHandlerName = "ActivateConceptsCodeletHandler";

			// Codelethandler Create goals
			String createGoalsCodeletTriggerName = "CreateGoalsCodeletHandlerTrigger";
			String createGoalsCodeletHandlerName = "CreateGoalsCodeletHandler";

			// Codelethandler Activate beliefs
			String activateBeliefsCodeletTriggerName = "ActivateBeliefsCodeletHandlerTrigger";
			String activateBeliefsCodeletHandlerName = "ActivateBeliefsCodeletHandler";

			// CodeletHandler Propose Options
			String proposeOptionsCodeletTriggerName = "ProposeOptionsCodeletHandlerTrigger";
			String proposeOptionsCodeletHandlerName = "ProposeOptionsCodeletHandler";

			// CodeletHandler Propose Actions
			String proposeActionsCodeletTriggerName = "ProposeActionsCodeletHandlerTrigger";
			String proposeActionsCodeletHandlerName = "ProposeActionsCodeletHandler";

			// CodeletHandler Evaluate Options
			String evaluteOptionsCodeletTriggerName = "EvaluateOptionsCodeletHandlerTrigger";
			String evaluteOptionsCodeletHandlerName = "EvaluateOptionsCodeletHandler";

			// Codelet Select option (here, no codelethandler is executed, just a normal
			// codelet)
			String selectOptionCodeletName = "SelectOptionCodelet";

			// Codelet Execute Action
			String executeActionCodeletName = "ExecuteActionCodelet";

			// Memories
			String namespaceWorkingMemory = "workingmemory";
			String namespaceInternalStateMemory = "internalstatememory";
			String namespaceLongTermMemory = "longtermmemory";

			// Generate the configuration for the KORE system
			log.info("Generate system configuration");
			// Controller
			AgentConfig cognitiveAgentConfig = AgentConfig.newConfig(cognitiveAgentName)
					// Main codelethandler
					.addFunction(
							AgentFunctionConfig.newConfig(mainCodeletHandlerName, CodeletHandlerImpl.class)
									.setProperty(CodeletHandlerImpl.ATTRIBUTEWORKINGMEMORYADDRESS,
											namespaceWorkingMemory)
									.setProperty(CodeletHandlerImpl.ATTRIBUTEINTERNALMEMORYADDRESS,
											namespaceInternalStateMemory))
					// Process codelethandlers
					.addFunction(AgentFunctionConfig
							.newConfig(activateConceptsCodeletHandlerName, CodeletHandlerImpl.class)
							.setProperty(CodeletHandlerImpl.ATTRIBUTEWORKINGMEMORYADDRESS,
									namespaceWorkingMemory)
							.setProperty(CodeletHandlerImpl.ATTRIBUTEINTERNALMEMORYADDRESS,
									namespaceInternalStateMemory))
					.addFunction(AgentFunctionConfig
							.newConfig(createGoalsCodeletHandlerName, CodeletHandlerImpl.class)
							.setProperty(CodeletHandlerImpl.ATTRIBUTEWORKINGMEMORYADDRESS,
									namespaceWorkingMemory)
							.setProperty(CodeletHandlerImpl.ATTRIBUTEINTERNALMEMORYADDRESS,
									namespaceInternalStateMemory))
					.addFunction(AgentFunctionConfig
							.newConfig(activateBeliefsCodeletHandlerName, CodeletHandlerImpl.class)
							.setProperty(CodeletHandlerImpl.ATTRIBUTEWORKINGMEMORYADDRESS,
									namespaceWorkingMemory)
							.setProperty(CodeletHandlerImpl.ATTRIBUTEINTERNALMEMORYADDRESS,
									namespaceInternalStateMemory))
					.addFunction(AgentFunctionConfig
							.newConfig(proposeOptionsCodeletHandlerName, CodeletHandlerImpl.class)
							.setProperty(CodeletHandlerImpl.ATTRIBUTEWORKINGMEMORYADDRESS,
									namespaceWorkingMemory)
							.setProperty(CodeletHandlerImpl.ATTRIBUTEINTERNALMEMORYADDRESS,
									namespaceInternalStateMemory))
					.addFunction(AgentFunctionConfig
							.newConfig(proposeActionsCodeletHandlerName, CodeletHandlerImpl.class)
							.setProperty(CodeletHandlerImpl.ATTRIBUTEWORKINGMEMORYADDRESS,
									namespaceWorkingMemory)
							.setProperty(CodeletHandlerImpl.ATTRIBUTEINTERNALMEMORYADDRESS,
									namespaceInternalStateMemory))
					.addFunction(AgentFunctionConfig
							.newConfig(evaluteOptionsCodeletHandlerName, CodeletHandlerImpl.class)
							.setProperty(CodeletHandlerImpl.ATTRIBUTEWORKINGMEMORYADDRESS,
									namespaceWorkingMemory)
							.setProperty(CodeletHandlerImpl.ATTRIBUTEINTERNALMEMORYADDRESS,
									namespaceInternalStateMemory))
					// Add main process codelets
					// Add trigger codelets
					.addFunction(AgentFunctionConfig
							.newConfig(activateConceptsCodeletTriggerName, CodeletHandlerTriggerCodelet.class)
							.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS,
									mainCodeletHandlerServiceAddress)
							.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, "1")
							.setProperty(CodeletHandlerTriggerCodelet.codeletHandlerServiceUriName,
									cognitiveAgentName + ":" + activateConceptsCodeletHandlerName))
					.addFunction(AgentFunctionConfig
							.newConfig(createGoalsCodeletTriggerName, CodeletHandlerTriggerCodelet.class)
							.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS,
									mainCodeletHandlerServiceAddress)
							.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, "2")
							.setProperty(CodeletHandlerTriggerCodelet.codeletHandlerServiceUriName,
									cognitiveAgentName + ":" + createGoalsCodeletHandlerName))
					.addFunction(AgentFunctionConfig
							.newConfig(activateBeliefsCodeletTriggerName, CodeletHandlerTriggerCodelet.class)
							.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS,
									mainCodeletHandlerServiceAddress)
							.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, "3")
							.setProperty(CodeletHandlerTriggerCodelet.codeletHandlerServiceUriName,
									cognitiveAgentName + ":" + activateBeliefsCodeletHandlerName))
					.addFunction(AgentFunctionConfig
							.newConfig(proposeOptionsCodeletTriggerName, CodeletHandlerTriggerCodelet.class)
							.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS,
									mainCodeletHandlerServiceAddress)
							.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, "4")
							.setProperty(CodeletHandlerTriggerCodelet.codeletHandlerServiceUriName,
									cognitiveAgentName + ":" + proposeOptionsCodeletHandlerName))
					.addFunction(AgentFunctionConfig
							.newConfig(proposeActionsCodeletTriggerName, CodeletHandlerTriggerCodelet.class)
							.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS,
									mainCodeletHandlerServiceAddress)
							.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, "5")
							.setProperty(CodeletHandlerTriggerCodelet.codeletHandlerServiceUriName,
									cognitiveAgentName + ":" + proposeActionsCodeletHandlerName))
					.addFunction(AgentFunctionConfig
							.newConfig(evaluteOptionsCodeletTriggerName, CodeletHandlerTriggerCodelet.class)
							.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS,
									mainCodeletHandlerServiceAddress)
							.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, "6")
							.setProperty(CodeletHandlerTriggerCodelet.codeletHandlerServiceUriName,
									cognitiveAgentName + ":" + evaluteOptionsCodeletHandlerName));
			// Direct codelets
			// .addCellfunction(CellFunctionConfig.newConfig(selectOptionCodeletName,
			// OptionSelectorCodelet.class)
			// .setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS,
			// mainCodeletHandlerServiceAddress)
			// .setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, "7"))
			// .addCellfunction(CellFunctionConfig.newConfig(executeActionCodeletName,
			// ActionExecutorCodelet.class)
			// .setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS,
			// mainCodeletHandlerServiceAddress)
			// .setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, "8"));

			// Add the specific codelets
			String incrementServiceName = "incrementservice";
			String incrementDatapoint1 = "incrementme1";
			String incrementDatapoint2 = "incrementme2";

			cognitiveAgentConfig
					// .addCellfunction(CellFunctionConfig.newConfig(incrementServiceName,
					// CFIncrementService.class)
					// .addManagedDatapoint(DatapointConfig.newConfig(CFIncrementService.ATTRIBUTEINCREMENTDATAPOINT,
					// namespaceWorkingMemory + "." + incrementDatapoint,
					// SyncMode.SUBSCRIBEWRITEBACK)))
					.addFunction(AgentFunctionConfig.newConfig("IncrementCodelet11", IncrementNumberCodelet.class)
							.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS,
									cognitiveAgentName + ":" + activateConceptsCodeletHandlerName)
							.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESERVICENAME, incrementServiceName)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESUBADDRESS, incrementDatapoint1))
					.addFunction(AgentFunctionConfig.newConfig("IncrementCodelet12", IncrementNumberCodelet.class)
							.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS,
									cognitiveAgentName + ":" + createGoalsCodeletHandlerName)
							.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESERVICENAME, incrementServiceName)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESUBADDRESS, incrementDatapoint1))
					.addFunction(AgentFunctionConfig.newConfig("IncrementCodelet13", IncrementNumberCodelet.class)
							.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS,
									cognitiveAgentName + ":" + activateBeliefsCodeletHandlerName)
							.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESERVICENAME, incrementServiceName)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESUBADDRESS, incrementDatapoint1))
					.addFunction(AgentFunctionConfig.newConfig("IncrementCodelet14", IncrementNumberCodelet.class)
							.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS,
									cognitiveAgentName + ":" + proposeOptionsCodeletHandlerName)
							.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESERVICENAME, incrementServiceName)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESUBADDRESS, incrementDatapoint1))
					.addFunction(AgentFunctionConfig.newConfig("IncrementCodelet15", IncrementNumberCodelet.class)
							.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS,
									cognitiveAgentName + ":" + proposeActionsCodeletHandlerName)
							.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESERVICENAME, incrementServiceName)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESUBADDRESS, incrementDatapoint1))
					.addFunction(AgentFunctionConfig.newConfig("IncrementCodelet16", IncrementNumberCodelet.class)
							.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS,
									cognitiveAgentName + ":" + evaluteOptionsCodeletHandlerName)
							.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESERVICENAME, incrementServiceName)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESUBADDRESS, incrementDatapoint1));

			cognitiveAgentConfig
					// .addCellfunction(CellFunctionConfig.newConfig(incrementServiceName,
					// CFIncrementService.class)
					// .addManagedDatapoint(DatapointConfig.newConfig(CFIncrementService.ATTRIBUTEINCREMENTDATAPOINT,
					// namespaceWorkingMemory + "." + incrementDatapoint,
					// SyncMode.SUBSCRIBEWRITEBACK)))
					.addFunction(AgentFunctionConfig.newConfig("IncrementCodelet21", IncrementNumberCodelet.class)
							.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS,
									cognitiveAgentName + ":" + activateConceptsCodeletHandlerName)
							.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESERVICENAME, incrementServiceName)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESUBADDRESS, incrementDatapoint2))
					.addFunction(AgentFunctionConfig.newConfig("IncrementCodelet22", IncrementNumberCodelet.class)
							.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS,
									cognitiveAgentName + ":" + createGoalsCodeletHandlerName)
							.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESERVICENAME, incrementServiceName)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESUBADDRESS, incrementDatapoint2))
					.addFunction(AgentFunctionConfig.newConfig("IncrementCodelet23", IncrementNumberCodelet.class)
							.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS,
									cognitiveAgentName + ":" + activateBeliefsCodeletHandlerName)
							.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESERVICENAME, incrementServiceName)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESUBADDRESS, incrementDatapoint2))
					.addFunction(AgentFunctionConfig.newConfig("IncrementCodelet24", IncrementNumberCodelet.class)
							.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS,
									cognitiveAgentName + ":" + proposeOptionsCodeletHandlerName)
							.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESERVICENAME, incrementServiceName)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESUBADDRESS, incrementDatapoint2))
					.addFunction(AgentFunctionConfig.newConfig("IncrementCodelet25", IncrementNumberCodelet.class)
							.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS,
									cognitiveAgentName + ":" + proposeActionsCodeletHandlerName)
							.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESERVICENAME, incrementServiceName)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESUBADDRESS, incrementDatapoint2))
					.addFunction(AgentFunctionConfig.newConfig("IncrementCodelet26", IncrementNumberCodelet.class)
							.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS,
									cognitiveAgentName + ":" + evaluteOptionsCodeletHandlerName)
							.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESERVICENAME, incrementServiceName)
							.setProperty(IncrementNumberCodelet.ATTRIBUTESUBADDRESS, incrementDatapoint2));

			log.debug("Start agent with config={}", cognitiveAgentConfig);

			Cell cogsys = this.launcher.createAgent(cognitiveAgentConfig);

			synchronized (this) {
				try {
					this.wait(5000);
				} catch (InterruptedException e) {

				}
			}

			// Write initial value on the incrementaddress
			cogsys.getCommunicator().write(this.dpb.newDatapoint(namespaceWorkingMemory + "." + incrementDatapoint1).setValue(0));
			cogsys.getCommunicator().write(this.dpb.newDatapoint(namespaceWorkingMemory + "." + incrementDatapoint2).setValue(0));

			log.info("=== All agents initialized ===");

			// memoryAgent.getCommunicator().write(Datapoint.newDatapoint(processDatapoint).setValue(new
			// JsonPrimitive(startValue)));
			// cogsys.getCommunicator().execute(cognitiveAgentName, mainCodeletHandlerName,
			// Arrays.asList(
			// Datapoint.newDatapoint("method").setValue("executecodelethandler"),
			// Datapoint.newDatapoint("blockingmethod").setValue(new JsonPrimitive(true))),
			// 10000);

			cogsys.getCommunicator().execute(cognitiveAgentName + ":" + mainCodeletHandlerName + "/" + CodeletHandlerImpl.EXECUTECODELETMETHODNAME, new Request(), 20000);
			
			// synchronized (this) {
			// try {
			// this.wait(1000);
			// } catch (InterruptedException e) {
			//
			// }
			// }

			// log.info("Read working memory={}", cogsys.getDataStorage());

			int result1 = (int) (cogsys.getCommunicator().read(namespaceWorkingMemory + "." + incrementDatapoint1)
					.getValue().getAsDouble());
			int result2 = (int) (cogsys.getCommunicator().read(namespaceWorkingMemory + "." + incrementDatapoint1)
					.getValue().getAsDouble());
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
