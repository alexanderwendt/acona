package at.tuwien.ict.acona.cell.activator;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public interface Condition {
	public void init(String name, final JsonObject settings);
	public String getName();
	public boolean testCondition(final Datapoint data);
}
