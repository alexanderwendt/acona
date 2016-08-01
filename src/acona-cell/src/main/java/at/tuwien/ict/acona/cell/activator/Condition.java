package at.tuwien.ict.acona.cell.activator;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public interface Condition {
	
	/**
	 * @param name
	 * @param settings
	 * @throws Exception 
	 */
	public Condition init(String name, final JsonObject settings) throws Exception;
	
	/**
	 * @return
	 */
	public String getName();
	
	/**
	 * @param data
	 * @return
	 * @throws Exception 
	 */
	public boolean testCondition(final Datapoint data) throws Exception;
	
	/**
	 * @return
	 */
	public String getDescription();
}
