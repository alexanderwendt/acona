package at.tuwien.ict.acona.cell.activator;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import jade.core.behaviours.Behaviour;

public class ActivatorInstanceImpl implements ActivatorInstance {

	protected static Logger log = LoggerFactory.getLogger(ActivatorInstanceImpl.class);
	
	private String name;
	private Behaviour behavior;
	private CellImpl caller;
	private final ArrayList<Condition> conditions = new ArrayList<Condition>(); 
	

	@Override
	public void init(String name, List<String> subscriptionAddresses, String logic, List<Condition> conditions, Behaviour behavior, CellImpl caller) {
		this.name = name;
		this.behavior = behavior;
		caller.addBehaviour(behavior);
		this.caller = caller;
		
	}

	@Override
	public boolean runActivation(Datapoint subscribedData) {
		//Check all conditions
		boolean isActivate=true;
		for (Condition c : conditions) {
			if (c.testCondition(subscribedData)==false) {
				isActivate=false;
				break;
			}
		}
		
		if (isActivate==true) {
			behavior.restart();
		}
		
		return isActivate;
	}

	@Override
	public void registerCondition(Condition condition) {
		if (this.conditions.contains(condition)==false) {
			this.conditions.add(condition);
		}
		
	}

	@Override
	public void deregisterCondition(Condition condition) {
		this.conditions.remove(condition);
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("name=");
		builder.append(name);
		builder.append(", behavior=");
		builder.append(behavior);
		builder.append(", caller=");
		builder.append(caller);
		builder.append(", conditions=");
		builder.append(conditions);
		return builder.toString();
	}

}
