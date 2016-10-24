package at.tuwien.ict.kore.behaviours;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import _OLD.at.tuwien.ict.acona.cell.activator.jadebehaviour.CellFunctionBehaviourImpl;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class IncrementValueBehaviour extends CellFunctionBehaviourImpl {
	
	protected static Logger log = LoggerFactory.getLogger(IncrementValueBehaviour.class);
	
	private static final String OPERAND1ADDRESS = "operand";
	private static final String RESULTADDRESS = "result";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public IncrementValueBehaviour() {
		super();
	}

	@Override
	public void function(Map<String, Datapoint> data) {
		
		JsonElement x =  data.get(conf.get(OPERAND1ADDRESS).getAsString()).getValue();
		//Get operand1
		double operand1 = data.get(conf.get(OPERAND1ADDRESS).getAsString()).getValue().getAsDouble();
		//Get operand2
		
		//Perform operation
		double result = operand1 + 1;
		
		log.info("{}> Value incremented from {} to {}", this.name, operand1, result);
		
		//Write result in memory
		this.caller.getDataStorage().write(Datapoint.newDatapoint(conf.get(RESULTADDRESS).getAsString()).setValue(String.valueOf(result)), caller.getName());
		
		
	}

}
