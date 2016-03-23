package at.tuwien.ict.acona.cell.activator;

import java.util.ArrayList;

import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapackage;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import jade.core.behaviours.Behaviour;

public class ActivatorInstanceImpl implements ActivatorInstance {

	private String name;
	private Behaviour behavior;
	private CellImpl caller;
	private final ArrayList<Condition> conditions = new ArrayList<Condition>(); 
	

	@Override
	public void init(String name, Behaviour behavior, CellImpl caller) {
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deregisterCondition() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
