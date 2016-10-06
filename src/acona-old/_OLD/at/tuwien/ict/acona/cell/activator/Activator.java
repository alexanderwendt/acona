package _OLD.at.tuwien.ict.acona.cell.activator;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

@Deprecated
public interface Activator {
	/**
	 * @param name
	 * @param subscriptionAddresses
	 * @param logic
	 * @param conditions
	 * @param behavior
	 * @param caller
	 */
	public Activator init(String name, JsonObject settings, Cell caller) throws Exception;
	
	/**
	 * run the activatior and test if the subscribed data can activate something
	 * 
	 * @param subscribedData
	 * @return true if the datapoint triggers activation. It returns false if the datapoint does not trigger any activation
	 * @throws Exception 
	 */
	public boolean runActivation(Datapoint subscribedData) throws Exception;

	/**
	 * Get the name of the activator
	 * 
	 * @return
	 */
	public String getActivatorName();
	
	/**
	 * Get all datapoints, which are subscribed (linked) to this activator
	 * 
	 * @return
	 */
	public List<String> getSubscribedDatapoints();
	
//	/**
//	 * Get the condition mapping
//	 * 
//	 * @return
//	 */
//	public Map<String, List<ActivatorConditionManager>> getConditionMapping();
	
	/**
	 * Close the activator
	 */
	public void closeActivator();
	
	public void setCommand(ControlCommand command);
	
}
