package at.tuwien.ict.acona.cell.activator;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public interface Condition {
	
	/**
	 * @param name
	 * @param settings
	 */
	public void init(String name, final JsonObject settings);
	
	/**
	 * @return
	 */
	public String getName();
	
	/**
	 * @param data
	 * @return
	 */
	public boolean testCondition(final Datapoint data);
	
	/**
	 * @return
	 */
	public String getDescription();
}
