package at.tuwien.ict.acona.cell.core.cellfunctionthread.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.tuwien.ict.acona.cell.activator.Activator;
import at.tuwien.ict.acona.cell.activator.Condition;
import at.tuwien.ict.acona.cell.core.CellImpl;

public class CellWithCellFunctionTestInstance extends CellImpl {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected void internalInit() throws Exception {
		
		Activator threadActivator = new CellFunctionTestInstance();
		String commandDatapoint = "datapoint.command";
		String queryDatapoint = "datapoint.query";
		String executeonceDatapoint = "datapoint.executeonce";

		Map<String, List<Condition>> subscriptions = new HashMap<String, List<Condition>>();
		subscriptions.put(commandDatapoint, new ArrayList<Condition>());
		subscriptions.put(queryDatapoint, new ArrayList<Condition>());
		subscriptions.put(executeonceDatapoint, new ArrayList<Condition>());
		
		threadActivator.init("testbehaviour", subscriptions, "", null, this);
		this.getActivationHandler().registerActivatorInstance(threadActivator);
		
		log.debug("Executor registered");
	}
}
