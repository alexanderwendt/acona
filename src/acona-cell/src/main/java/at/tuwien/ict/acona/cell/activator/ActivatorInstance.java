package at.tuwien.ict.acona.cell.activator;

import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import jade.core.behaviours.Behaviour;

public interface ActivatorInstance {
	public void init(String name, Behaviour behavior, CellImpl caller);
	public boolean runActivation(Datapoint subscribedData);
	public void registerCondition(Condition condition);
	public void deregisterCondition();
	public String getName();
}
