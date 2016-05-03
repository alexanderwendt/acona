package at.tuwien.ict.acona.cell.core.helpers;

import java.util.ArrayList;
import java.util.List;

import at.tuwien.ict.acona.cell.activator.ActivatorInstance;
import at.tuwien.ict.acona.cell.activator.ActivatorInstanceImpl;
import at.tuwien.ict.acona.cell.activator.Condition;
import at.tuwien.ict.acona.cell.activator.conditions.ConditionIsNotEmpty;
import at.tuwien.ict.acona.cell.core.CellInspector;
import at.tuwien.ict.acona.cell.custombehaviors.AdditionBehavior;
import jade.core.behaviours.Behaviour;

public class CellWithActivator extends CellInspector {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected void internalInit() {
		//Address
		String datapointsource = "activator.test.address";
		String activatorName = "testactivator";
		
		ActivatorInstance activator = new ActivatorInstanceImpl();
		Behaviour activateBehaviour = new AdditionBehavior("AdditionBehavior", this);
		//activateBehaviour
		activator.init(activatorName, new ArrayList<String>(), "", new ArrayList<Condition>(), activateBehaviour, this);
		activator.registerCondition(new ConditionIsNotEmpty());
		
		this.getActivationHandler().registerActivatorInstance(datapointsource, activator);
		
		log.debug("Activator registered");
	}

}
