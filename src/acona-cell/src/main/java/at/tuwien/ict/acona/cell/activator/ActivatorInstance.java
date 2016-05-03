package at.tuwien.ict.acona.cell.activator;

import java.util.List;

import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import jade.core.behaviours.Behaviour;

public interface ActivatorInstance {
	public void init(String name, List<String> subscriptionAddresses, String logic, List<Condition> conditions, Behaviour behavior, CellImpl caller);
	public boolean runActivation(Datapoint subscribedData);
	public void registerCondition(Condition condition);
	public void deregisterCondition(Condition conditon);
	public String getName();
}
