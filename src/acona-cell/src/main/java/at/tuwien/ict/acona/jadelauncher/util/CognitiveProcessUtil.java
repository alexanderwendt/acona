package at.tuwien.ict.acona.jadelauncher.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionCodelet;
import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionCodeletHandler;
import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionHandlerTriggerCodelet;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.framework.modules.ActionExecutorCodelet;
import at.tuwien.ict.acona.framework.modules.OptionSelectorCodelet;

public class CognitiveProcessUtil {

	private final static Logger log = LoggerFactory.getLogger(CognitiveProcessUtil.class);

	//Create the agent
	//private static String cognitiveAgentName = "KORECognitiveAgent";

	//Main codelet handler
	public static final String mainCodeletHandlerName = "MainProcessCodeletHandler";

	//Codelethandler Activate Concepts
	private static final String activateConceptsCodeletTriggerName = "ActivateConceptsCodeletHandlerTrigger";
	public static final String activateConceptsCodeletHandlerName = "ActivateConceptsCodeletHandler";

	//Codelethandler Create goals
	private static final String createGoalsCodeletTriggerName = "CreateGoalsCodeletHandlerTrigger";
	public static final String createGoalsCodeletHandlerName = "CreateGoalsCodeletHandler";

	//Codelethandler Activate beliefs
	private static final String activateBeliefsCodeletTriggerName = "ActivateBeliefsCodeletHandlerTrigger";
	public static final String activateBeliefsCodeletHandlerName = "ActivateBeliefsCodeletHandler";

	//CodeletHandler Propose Options
	private static final String proposeOptionsCodeletTriggerName = "ProposeOptionsCodeletHandlerTrigger";
	public static final String proposeOptionsCodeletHandlerName = "ProposeOptionsCodeletHandler";

	//CodeletHandler Propose Actions
	private static final String proposeActionsCodeletTriggerName = "ProposeActionsCodeletHandlerTrigger";
	public static final String proposeActionsCodeletHandlerName = "ProposeActionsCodeletHandler";

	//CodeletHandler Evaluate Options
	private static final String evaluteOptionsCodeletTriggerName = "EvaluateOptionsCodeletHandlerTrigger";
	public static final String evaluteOptionsCodeletHandlerName = "EvaluateOptionsCodeletHandler";

	//Codelet Select option (here, no codelethandler is executed, just a normal codelet)
	public static final String selectOptionCodeletName = "SelectOptionCodelet";

	//Codelet Execute Action
	public static final String executeActionCodeletName = "ExecuteActionCodelet";

	//Memories
	public static final String namespaceWorkingMemory = "workingmemory";
	public static final String namespaceInternalStateMemory = "internalstatememory";

	/**
	 * Generate a complete cognitive process
	 * 
	 * @param cognitiveAgentName
	 * @return
	 */
	public static CellConfig generateCognitiveProcess(String cognitiveAgentName) {

		//Main codelet handler
		String mainCodeletHandlerServiceAddress = cognitiveAgentName + ":" + mainCodeletHandlerName;

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

		return cognitiveAgentConfig;
	}
}
