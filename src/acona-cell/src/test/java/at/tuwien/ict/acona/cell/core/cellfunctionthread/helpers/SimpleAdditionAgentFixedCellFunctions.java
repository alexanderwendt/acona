package at.tuwien.ict.acona.cell.core.cellfunctionthread.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import _OLD.at.tuwien.ict.acona.cell.activator.Activator;
import at.tuwien.ict.acona.cell.cellfunction.special.Condition;
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
		
		threadActivator.initWithConditions("getdata", subscriptions, "", null, this);
		this.getFunctionHandler().registerActivatorInstance(threadActivator);
		
		//Add publisher function
		
		
	}
}
