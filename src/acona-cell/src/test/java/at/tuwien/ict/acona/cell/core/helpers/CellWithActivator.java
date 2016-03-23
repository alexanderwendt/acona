package at.tuwien.ict.acona.cell.core.helpers;

import at.tuwien.ict.acona.cell.activator.ActivatorInstance;
import at.tuwien.ict.acona.cell.activator.ActivatorInstanceImpl;
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
		Behaviour activateBehaviour = new AdditionBehavior(this);
		//activateBehaviour
		activator.init(activatorName, activateBehaviour, this);
		activator.registerCondition(new ConditionIsNotEmpty());
		
		this.getActivationHandler().registerActivatorInstance(datapointsource, activator);
	}

}
