package at.tuwien.ict.acona.cognitivearchitecture;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cognitivearchitecture.frameworkcodelets.ActionExecutionCodelet;
import at.tuwien.ict.acona.cognitivearchitecture.frameworkcodelets.SelectionCodelet;
import at.tuwien.ict.acona.mq.core.agentfunction.codelets.CodeletImpl;
import at.tuwien.ict.acona.mq.core.agentfunction.codelets.CodeletHandlerImpl;
import at.tuwien.ict.acona.mq.core.agentfunction.codelets.CodeletHandlerTriggerCodelet;
import at.tuwien.ict.acona.mq.core.config.AgentConfig;
import at.tuwien.ict.acona.mq.core.config.AgentFunctionConfig;

public class CognitiveProcess {

	private final static Logger log = LoggerFactory.getLogger(CognitiveProcess.class);

	// Create the agent
	// private static String cognitiveAgentName = "KORECognitiveAgent";

	// Main codelet handler
	public static final String mainCodeletHandlerName = "MainProcessCodeletHandler";

	// Codelethandler Clean before start
	private static final String startCleanCodeletTriggerName = "StartCleanCodeletHandlerTrigger";
	public static final String startCleanCodeletHandlerName = "StartCleanCodeletHandler";

	// Codelethandler Activate Concepts
	private static final String activateConceptsCodeletTriggerName = "ActivateConceptsCodeletHandlerTrigger";
	public static final String activateConceptsCodeletHandlerName = "ActivateConceptsCodeletHandler";

	// Codelethandler Create goals
	private static final String createGoalsCodeletTriggerName = "CreateGoalsCodeletHandlerTrigger";
	public static final String createGoalsCodeletHandlerName = "CreateGoalsCodeletHandler";

	// Codelethandler Activate beliefs
	private static final String activateBeliefsCodeletTriggerName = "ActivateBeliefsCodeletHandlerTrigger";
	public static final String activateBeliefsCodeletHandlerName = "ActivateBeliefsCodeletHandler";

	// Codelethandler Execute immediate actions
	private static final String immediateActionsCodeletTriggerName = "ImmediateActionsCodeletHandlerTrigger";
	public static final String immediateActionsCodeletHandlerName = "ImmediateActionsCodeletHandler";

	// CodeletHandler Propose Options
	private static final String proposeOptionsCodeletTriggerName = "ProposeOptionsCodeletHandlerTrigger";
	public static final String proposeOptionsCodeletHandlerName = "ProposeOptionsCodeletHandler";

	// CodeletHandler Propose Actions
	// private static final String proposeActionsCodeletTriggerName = "ProposeActionsCodeletHandlerTrigger";
	// public static final String proposeActionsCodeletHandlerName = "ProposeActionsCodeletHandler";

	// CodeletHandler Evaluate Options
	private static final String evaluteOptionsCodeletTriggerName = "EvaluateOptionsCodeletHandlerTrigger";
	public static final String evaluteOptionsCodeletHandlerName = "EvaluateOptionsCodeletHandler";

	// CodeletHandler Prework

	// CodeletHandler Postwork

	// Codelet Select option (here, no codelethandler is executed, just a normal codelet)
	public static final String selectOptionCodeletName = "SelectOptionCodelet";

	// Codelet Execute Action
	public static final String executeActionCodeletName = "ExecuteActionCodelet";

	// Memories
	public static final String namespaceWorkingMemory = "workingmemory";
	public static final String namespaceInternalStateMemory = "internalstatememory";
	public static final String namespaceInputBuffer = "inputbuffer";
	public static final String namespaceOutputBuffer = "outputbuffer";

	public static final String stateCollectorName = "statecollector";

	public final static String RESULTPREFIXADDRESS = namespaceWorkingMemory + "." + "result";
	public final static String INPUTSPREFIXADDRESS = namespaceWorkingMemory + ".inputs";

	public final static String INTERNALSTATEPREFIXADDRESS = namespaceInternalStateMemory + "." + "state";
	public final static String OPTIONSPREFIXADDRESS = namespaceInternalStateMemory + "." + "options";
	public final static String GOALSPREFIXADDRESS = namespaceInternalStateMemory + "." + "goals";

	public final static String SELECTEDOPTIONADDRESS = namespaceInternalStateMemory + "." + "selectedoption";
	public final static String ACTIONHISTORYADDRESS = INTERNALSTATEPREFIXADDRESS + "." + "history";

	public final static String MANUALTRIGGERADDRESS = INPUTSPREFIXADDRESS + ".trigger";

	public final static String CYCLECOUNTADDRESS = INTERNALSTATEPREFIXADDRESS + ".cyclecount";

	/**
	 * Generate a complete cognitive process with handlers and executors.
	 * 
	 * @param cognitiveAgentName
	 * @return
	 */
	public final static AgentConfig generateCognitiveProcessTemplate(String cognitiveAgentName) {

		// Main codelet handler
		String mainCodeletHandlerServiceAddress = cognitiveAgentName + ":" + mainCodeletHandlerName;

		// Generate the configuration for the KORE system
		log.info("Generate system configuration");
		// Controller
		// Controller
		AgentConfig cognitiveAgentConfig = AgentConfig.newConfig(cognitiveAgentName)
				// Cellfunctions
				//.addFunction(CellFunctionConfig.newConfig(stateCollectorName, CFStateGenerator.class)
				//		.setGenerateReponder(true)) // Responder active to be called
				// Main codelethandler
				.addFunction(AgentFunctionConfig.newConfig(mainCodeletHandlerName, CodeletHandlerImpl.class)
						.setProperty(CodeletHandlerImpl.ATTRIBUTEWORKINGMEMORYADDRESS, namespaceWorkingMemory)
						.setProperty(CodeletHandlerImpl.ATTRIBUTEINTERNALMEMORYADDRESS, namespaceInternalStateMemory))

				// Process codelethandlers
				.addFunction(AgentFunctionConfig.newConfig(startCleanCodeletHandlerName, CodeletHandlerImpl.class)
						.setProperty(CodeletHandlerImpl.ATTRIBUTEWORKINGMEMORYADDRESS, namespaceWorkingMemory)
						.setProperty(CodeletHandlerImpl.ATTRIBUTEINTERNALMEMORYADDRESS, namespaceInternalStateMemory))
				.addFunction(AgentFunctionConfig.newConfig(activateConceptsCodeletHandlerName, CodeletHandlerImpl.class)
						.setProperty(CodeletHandlerImpl.ATTRIBUTEWORKINGMEMORYADDRESS, namespaceWorkingMemory)
						.setProperty(CodeletHandlerImpl.ATTRIBUTEINTERNALMEMORYADDRESS, namespaceInternalStateMemory))
				.addFunction(AgentFunctionConfig.newConfig(createGoalsCodeletHandlerName, CodeletHandlerImpl.class)
						.setProperty(CodeletHandlerImpl.ATTRIBUTEWORKINGMEMORYADDRESS, namespaceWorkingMemory)
						.setProperty(CodeletHandlerImpl.ATTRIBUTEINTERNALMEMORYADDRESS, namespaceInternalStateMemory))
				.addFunction(AgentFunctionConfig.newConfig(activateBeliefsCodeletHandlerName, CodeletHandlerImpl.class)
						.setProperty(CodeletHandlerImpl.ATTRIBUTEWORKINGMEMORYADDRESS, namespaceWorkingMemory)
						.setProperty(CodeletHandlerImpl.ATTRIBUTEINTERNALMEMORYADDRESS, namespaceInternalStateMemory))
				.addFunction(AgentFunctionConfig.newConfig(immediateActionsCodeletHandlerName, CodeletHandlerImpl.class)
						.setProperty(CodeletHandlerImpl.ATTRIBUTEWORKINGMEMORYADDRESS, namespaceWorkingMemory)
						.setProperty(CodeletHandlerImpl.ATTRIBUTEINTERNALMEMORYADDRESS, namespaceInternalStateMemory))
				.addFunction(AgentFunctionConfig.newConfig(proposeOptionsCodeletHandlerName, CodeletHandlerImpl.class)
						.setProperty(CodeletHandlerImpl.ATTRIBUTEWORKINGMEMORYADDRESS, namespaceWorkingMemory)
						.setProperty(CodeletHandlerImpl.ATTRIBUTEINTERNALMEMORYADDRESS, namespaceInternalStateMemory))
				// .addFunction(CellFunctionConfig.newConfig(proposeActionsCodeletHandlerName, CellFunctionCodeletHandler.class)
				// .setProperty(CellFunctionCodeletHandler.ATTRIBUTEWORKINGMEMORYADDRESS, namespaceWorkingMemory)
				// .setProperty(CellFunctionCodeletHandler.ATTRIBUTEINTERNALMEMORYADDRESS, namespaceInternalStateMemory))
				.addFunction(AgentFunctionConfig.newConfig(evaluteOptionsCodeletHandlerName, CodeletHandlerImpl.class)
						.setProperty(CodeletHandlerImpl.ATTRIBUTEWORKINGMEMORYADDRESS, namespaceWorkingMemory)
						.setProperty(CodeletHandlerImpl.ATTRIBUTEINTERNALMEMORYADDRESS, namespaceInternalStateMemory))

				// Add trigger codelets that trigger the subcodelets
				.addFunction(AgentFunctionConfig.newConfig(startCleanCodeletTriggerName, CodeletHandlerTriggerCodelet.class)
						.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
						.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, 1)
						.setProperty(CodeletHandlerTriggerCodelet.codeletHandlerServiceUriName, cognitiveAgentName + ":" + startCleanCodeletHandlerName))
				.addFunction(AgentFunctionConfig.newConfig(activateConceptsCodeletTriggerName, CodeletHandlerTriggerCodelet.class)
						.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
						.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, 2)
						.setProperty(CodeletHandlerTriggerCodelet.codeletHandlerServiceUriName, cognitiveAgentName + ":" + activateConceptsCodeletHandlerName))
				.addFunction(AgentFunctionConfig.newConfig(createGoalsCodeletTriggerName, CodeletHandlerTriggerCodelet.class)
						.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
						.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, 3)
						.setProperty(CodeletHandlerTriggerCodelet.codeletHandlerServiceUriName, cognitiveAgentName + ":" + createGoalsCodeletHandlerName))
				.addFunction(AgentFunctionConfig.newConfig(activateBeliefsCodeletTriggerName, CodeletHandlerTriggerCodelet.class)
						.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
						.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, 4)
						.setProperty(CodeletHandlerTriggerCodelet.codeletHandlerServiceUriName, cognitiveAgentName + ":" + activateBeliefsCodeletHandlerName))
				.addFunction(AgentFunctionConfig.newConfig(immediateActionsCodeletTriggerName, CodeletHandlerTriggerCodelet.class)
						.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
						.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, 5)
						.setProperty(CodeletHandlerTriggerCodelet.codeletHandlerServiceUriName, cognitiveAgentName + ":" + immediateActionsCodeletHandlerName))
				.addFunction(AgentFunctionConfig.newConfig(proposeOptionsCodeletTriggerName, CodeletHandlerTriggerCodelet.class)
						.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
						.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, 6)
						.setProperty(CodeletHandlerTriggerCodelet.codeletHandlerServiceUriName, cognitiveAgentName + ":" + proposeOptionsCodeletHandlerName))
				// .addFunction(CellFunctionConfig.newConfig(proposeActionsCodeletTriggerName, CellFunctionHandlerTriggerCodelet.class)
				// .setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
				// .setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, "5")
				// .setProperty(CellFunctionHandlerTriggerCodelet.codeletHandlerServiceUriName, cognitiveAgentName + ":" + proposeActionsCodeletHandlerName))
				.addFunction(AgentFunctionConfig.newConfig(evaluteOptionsCodeletTriggerName, CodeletHandlerTriggerCodelet.class)
						.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
						.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, 7)
						.setProperty(CodeletHandlerTriggerCodelet.codeletHandlerServiceUriName, cognitiveAgentName + ":" + evaluteOptionsCodeletHandlerName))

				// Direct codelets
				.addFunction(AgentFunctionConfig.newConfig(selectOptionCodeletName, SelectionCodelet.class)
						.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
						.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, 8))
				.addFunction(AgentFunctionConfig.newConfig(executeActionCodeletName, ActionExecutionCodelet.class)
						.setProperty(CodeletImpl.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
						.setProperty(CodeletImpl.ATTRIBUTEEXECUTIONORDER, 9));

		return cognitiveAgentConfig;
	}

}
