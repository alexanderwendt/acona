package at.tuwien.ict.acona.cognitivearchitecture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionCodelet;
import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionCodeletHandler;
import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionHandlerTriggerCodelet;
import at.tuwien.ict.acona.cell.cellfunction.specialfunctions.CFStateGenerator;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cognitivearchitecture.frameworkcodelets.ActionExecutionCodelet;
import at.tuwien.ict.acona.cognitivearchitecture.frameworkcodelets.SelectionCodelet;

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

	public static final String stateCollectorName = "statecollector";

	public final static String RESULTPREFIXADDRESS = namespaceWorkingMemory + "." + "result";
	public final static String INPUTSPREFIXADDRESS = namespaceWorkingMemory + ".inputs";

	public final static String INTERNALSTATEPREFIXADDRESS = namespaceInternalStateMemory + "." + "state";
	public final static String OPTIONSPREFIXADDRESS = namespaceInternalStateMemory + "." + "options";
	public final static String GOALSPREFIXADDRESS = namespaceInternalStateMemory + "." + "goals";

	public final static String SELECTEDOPTIONADDRESS = namespaceInternalStateMemory + "." + "selectedoption";
	public final static String ACTIONHISTORYADDRESS = INTERNALSTATEPREFIXADDRESS + "." + "history";

	public final static String MANUALTRIGGERADDRESS = INPUTSPREFIXADDRESS + ".trigger";

	public final static String RECEIVEDSIMULATIONRUNADDRESS = INPUTSPREFIXADDRESS + ".currentsimulationrun";
	public final static String CURRENTSOLUTIONADDRESS = RESULTPREFIXADDRESS + ".currentsolution";
	public final static String CURRENTVARIANTADDRESS = RESULTPREFIXADDRESS + ".currentvariant";
	public final static String CURRENTUSECASEADDRESS = RESULTPREFIXADDRESS + ".currentusecase";
	public final static String CURRENTSCENARIOADDRESS = RESULTPREFIXADDRESS + ".currentscenario";
	public final static String FINALRESULTADDRESS = RESULTPREFIXADDRESS + ".result";

	public final static String REQUESTSPREFIXADDRESS = namespaceWorkingMemory + ".inputs." + "requests";
	public final static String EPISODESPREFIXADDRESS = namespaceWorkingMemory + ".episodes";
	public final static String PREVIOUSPREFIXEPISODEADDRESS = namespaceWorkingMemory + ".previousepisode";

	public final static String SOLUTIONPOTENTIALADDRESS = INTERNALSTATEPREFIXADDRESS + ".solutionpotential";
	public final static String CYCLECOUNTADDRESS = INTERNALSTATEPREFIXADDRESS + ".cyclecount";
	public final static String SIMULATIONCOUNTADDRESS = INTERNALSTATEPREFIXADDRESS + ".simulationcount";

	/**
	 * Generate a complete cognitive process with handlers and executors.
	 * 
	 * @param cognitiveAgentName
	 * @return
	 */
	public final static CellConfig generateCognitiveProcessTemplate(String cognitiveAgentName) {

		// Main codelet handler
		String mainCodeletHandlerServiceAddress = cognitiveAgentName + ":" + mainCodeletHandlerName;

		// Generate the configuration for the KORE system
		log.info("Generate system configuration");
		// Controller
		// Controller
		CellConfig cognitiveAgentConfig = CellConfig.newConfig(cognitiveAgentName)
				// Cellfunctions
				.addCellfunction(CellFunctionConfig.newConfig(stateCollectorName, CFStateGenerator.class))
				// Main codelethandler
				.addCellfunction(CellFunctionConfig.newConfig(mainCodeletHandlerName, CellFunctionCodeletHandler.class)
						.setProperty(CellFunctionCodeletHandler.ATTRIBUTEWORKINGMEMORYADDRESS, namespaceWorkingMemory)
						.setProperty(CellFunctionCodeletHandler.ATTRIBUTEINTERNALMEMORYADDRESS, namespaceInternalStateMemory))

				// Process codelethandlers
				.addCellfunction(CellFunctionConfig.newConfig(startCleanCodeletHandlerName, CellFunctionCodeletHandler.class)
						.setProperty(CellFunctionCodeletHandler.ATTRIBUTEWORKINGMEMORYADDRESS, namespaceWorkingMemory)
						.setProperty(CellFunctionCodeletHandler.ATTRIBUTEINTERNALMEMORYADDRESS, namespaceInternalStateMemory))
				.addCellfunction(CellFunctionConfig.newConfig(activateConceptsCodeletHandlerName, CellFunctionCodeletHandler.class)
						.setProperty(CellFunctionCodeletHandler.ATTRIBUTEWORKINGMEMORYADDRESS, namespaceWorkingMemory)
						.setProperty(CellFunctionCodeletHandler.ATTRIBUTEINTERNALMEMORYADDRESS, namespaceInternalStateMemory))
				.addCellfunction(CellFunctionConfig.newConfig(createGoalsCodeletHandlerName, CellFunctionCodeletHandler.class)
						.setProperty(CellFunctionCodeletHandler.ATTRIBUTEWORKINGMEMORYADDRESS, namespaceWorkingMemory)
						.setProperty(CellFunctionCodeletHandler.ATTRIBUTEINTERNALMEMORYADDRESS, namespaceInternalStateMemory))
				.addCellfunction(CellFunctionConfig.newConfig(activateBeliefsCodeletHandlerName, CellFunctionCodeletHandler.class)
						.setProperty(CellFunctionCodeletHandler.ATTRIBUTEWORKINGMEMORYADDRESS, namespaceWorkingMemory)
						.setProperty(CellFunctionCodeletHandler.ATTRIBUTEINTERNALMEMORYADDRESS, namespaceInternalStateMemory))
				.addCellfunction(CellFunctionConfig.newConfig(immediateActionsCodeletHandlerName, CellFunctionCodeletHandler.class)
						.setProperty(CellFunctionCodeletHandler.ATTRIBUTEWORKINGMEMORYADDRESS, namespaceWorkingMemory)
						.setProperty(CellFunctionCodeletHandler.ATTRIBUTEINTERNALMEMORYADDRESS, namespaceInternalStateMemory))
				.addCellfunction(CellFunctionConfig.newConfig(proposeOptionsCodeletHandlerName, CellFunctionCodeletHandler.class)
						.setProperty(CellFunctionCodeletHandler.ATTRIBUTEWORKINGMEMORYADDRESS, namespaceWorkingMemory)
						.setProperty(CellFunctionCodeletHandler.ATTRIBUTEINTERNALMEMORYADDRESS, namespaceInternalStateMemory))
				// .addCellfunction(CellFunctionConfig.newConfig(proposeActionsCodeletHandlerName, CellFunctionCodeletHandler.class)
				// .setProperty(CellFunctionCodeletHandler.ATTRIBUTEWORKINGMEMORYADDRESS, namespaceWorkingMemory)
				// .setProperty(CellFunctionCodeletHandler.ATTRIBUTEINTERNALMEMORYADDRESS, namespaceInternalStateMemory))
				.addCellfunction(CellFunctionConfig.newConfig(evaluteOptionsCodeletHandlerName, CellFunctionCodeletHandler.class)
						.setProperty(CellFunctionCodeletHandler.ATTRIBUTEWORKINGMEMORYADDRESS, namespaceWorkingMemory)
						.setProperty(CellFunctionCodeletHandler.ATTRIBUTEINTERNALMEMORYADDRESS, namespaceInternalStateMemory))

				// Add trigger codelets that trigger the subcodelets
				.addCellfunction(CellFunctionConfig.newConfig(startCleanCodeletTriggerName, CellFunctionHandlerTriggerCodelet.class)
						.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
						.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, 1)
						.setProperty(CellFunctionHandlerTriggerCodelet.codeletHandlerServiceUriName, cognitiveAgentName + ":" + startCleanCodeletHandlerName))
				.addCellfunction(CellFunctionConfig.newConfig(activateConceptsCodeletTriggerName, CellFunctionHandlerTriggerCodelet.class)
						.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
						.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, 2)
						.setProperty(CellFunctionHandlerTriggerCodelet.codeletHandlerServiceUriName, cognitiveAgentName + ":" + activateConceptsCodeletHandlerName))
				.addCellfunction(CellFunctionConfig.newConfig(createGoalsCodeletTriggerName, CellFunctionHandlerTriggerCodelet.class)
						.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
						.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, 3)
						.setProperty(CellFunctionHandlerTriggerCodelet.codeletHandlerServiceUriName, cognitiveAgentName + ":" + createGoalsCodeletHandlerName))
				.addCellfunction(CellFunctionConfig.newConfig(activateBeliefsCodeletTriggerName, CellFunctionHandlerTriggerCodelet.class)
						.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
						.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, 4)
						.setProperty(CellFunctionHandlerTriggerCodelet.codeletHandlerServiceUriName, cognitiveAgentName + ":" + activateBeliefsCodeletHandlerName))
				.addCellfunction(CellFunctionConfig.newConfig(immediateActionsCodeletTriggerName, CellFunctionHandlerTriggerCodelet.class)
						.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
						.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, 5)
						.setProperty(CellFunctionHandlerTriggerCodelet.codeletHandlerServiceUriName, cognitiveAgentName + ":" + immediateActionsCodeletHandlerName))
				.addCellfunction(CellFunctionConfig.newConfig(proposeOptionsCodeletTriggerName, CellFunctionHandlerTriggerCodelet.class)
						.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
						.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, 6)
						.setProperty(CellFunctionHandlerTriggerCodelet.codeletHandlerServiceUriName, cognitiveAgentName + ":" + proposeOptionsCodeletHandlerName))
				// .addCellfunction(CellFunctionConfig.newConfig(proposeActionsCodeletTriggerName, CellFunctionHandlerTriggerCodelet.class)
				// .setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
				// .setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, "5")
				// .setProperty(CellFunctionHandlerTriggerCodelet.codeletHandlerServiceUriName, cognitiveAgentName + ":" + proposeActionsCodeletHandlerName))
				.addCellfunction(CellFunctionConfig.newConfig(evaluteOptionsCodeletTriggerName, CellFunctionHandlerTriggerCodelet.class)
						.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
						.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, 7)
						.setProperty(CellFunctionHandlerTriggerCodelet.codeletHandlerServiceUriName, cognitiveAgentName + ":" + evaluteOptionsCodeletHandlerName))

				// Direct codelets
				.addCellfunction(CellFunctionConfig.newConfig(selectOptionCodeletName, SelectionCodelet.class)
						.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
						.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, 8))
				.addCellfunction(CellFunctionConfig.newConfig(executeActionCodeletName, ActionExecutionCodelet.class)
						.setProperty(CellFunctionCodelet.ATTRIBUTECODELETHANDLERADDRESS, mainCodeletHandlerServiceAddress)
						.setProperty(CellFunctionCodelet.ATTRIBUTEEXECUTIONORDER, 9));

		return cognitiveAgentConfig;
	}

}
