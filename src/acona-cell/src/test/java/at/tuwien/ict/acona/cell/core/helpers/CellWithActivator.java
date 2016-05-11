package at.tuwien.ict.acona.cell.core.helpers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import at.tuwien.ict.acona.cell.activator.Activator;
import at.tuwien.ict.acona.cell.activator.ActivatorImpl;
import at.tuwien.ict.acona.cell.activator.Condition;
import at.tuwien.ict.acona.cell.activator.conditions.ConditionIsNotEmpty;
import at.tuwien.ict.acona.cell.core.CellFunctionBehaviour;
import at.tuwien.ict.acona.cell.core.InspectorCell;
import at.tuwien.ict.acona.cell.custombehaviours.AdditionBehaviour;

public class CellWithActivator extends InspectorCell {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected void internalInit() {
		//Address
		//String datapointsource = "activator.test.address";
		String activatorName = "testactivator";
		String conditionName = "isNotEmpty";
		JsonObject additionBehaviourConf = new JsonObject();
		additionBehaviourConf.addProperty("operand1", "data.op1");
		additionBehaviourConf.addProperty("operand2", "data.op2");
		additionBehaviourConf.addProperty("result", "data.result");
		
		Activator activator = new ActivatorImpl();
		CellFunctionBehaviour activateBehaviour = new AdditionBehaviour().init("AdditionBehaviour", additionBehaviourConf, this);
		
		//Create condition
		Condition condition1 = new ConditionIsNotEmpty().init(conditionName, new JsonObject());
		Condition condition2 = new ConditionIsNotEmpty().init(conditionName, new JsonObject());

		Map<String, List<Condition>> conditionMapping = new HashMap<String, List<Condition>>();
		conditionMapping.put(additionBehaviourConf.get("operand1").getAsString(), Arrays.asList(condition1));
		conditionMapping.put(additionBehaviourConf.get("operand2").getAsString(), Arrays.asList(condition2));
		//this.addBehaviour(activateBehaviour);
		
		
		//activateBehaviour
		activator.init(activatorName, conditionMapping, "", activateBehaviour, this);
		//activator.registerCondition(new ConditionIsNotEmpty());
		
		this.getActivationHandler().registerActivatorInstance(activator);
		
		log.debug("Activator registered");
	}

}
