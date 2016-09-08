package at.tuwien.ict.acona.cell.core.cellfunctionthread.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.tuwien.ict.acona.cell.activator.Activator;
import at.tuwien.ict.acona.cell.activator.Condition;
import at.tuwien.ict.acona.cell.core.CellImpl;

public class SimpleAdditionAgentFixedCellFunctions extends CellImpl {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final String COMMANDDATAPOINT = "drivetrack.controller.command";

	protected void internalInit() throws Exception {
		//Add controller
		
		
		//Add get data function
		Activator threadActivator = new CellFunctionGetData();
		Map<String, List<Condition>> subscriptions = new HashMap<String, List<Condition>>();
		subscriptions.put(COMMANDDATAPOINT, new ArrayList<Condition>());
		
		threadActivator.init("getdata", subscriptions, "", null, this);
		this.getActivationHandler().registerActivatorInstance(threadActivator);
		
		//Add publisher function
		
		
	}
}
