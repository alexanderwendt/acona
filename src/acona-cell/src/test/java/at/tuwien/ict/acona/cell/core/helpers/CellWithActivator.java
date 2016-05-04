package at.tuwien.ict.acona.cell.core.helpers;

import java.util.Arrays;

import at.tuwien.ict.acona.cell.activator.Activator;
import at.tuwien.ict.acona.cell.activator.ActivatorImpl;
import at.tuwien.ict.acona.cell.activator.Condition;
import at.tuwien.ict.acona.cell.activator.conditions.ConditionIsNotEmpty;
import at.tuwien.ict.acona.cell.core.CellFunctionBehavior;
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
		String conditionName = "isNotEmpty";
		
		Activator activator = new ActivatorImpl();
		CellFunctionBehavior activateBehaviour = new AdditionBehavior("AdditionBehavior", this);
		//Create condition
		Condition condition = new ConditionIsNotEmpty();
		condition.init(conditionName, null);
		
		
		
		//this.addBehaviour(activateBehaviour);
		
		
		//activateBehaviour
		activator.init(activatorName, Arrays.asList(datapointsource), "test", Arrays.asList(condition), activateBehaviour, this);
		//activator.registerCondition(new ConditionIsNotEmpty());
		
		this.getActivationHandler().registerActivatorInstance(datapointsource, activator);
		
		log.debug("Activator registered");
	}

}
