package at.tuwien.ict.acona.cell.activator.helper;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.activator.ActivationHandler;
import at.tuwien.ict.acona.cell.activator.Activator;
import at.tuwien.ict.acona.cell.activator.Condition;
import at.tuwien.ict.acona.cell.core.CellFunctionBehaviour;
import at.tuwien.ict.acona.cell.core.CellInitialization;
import at.tuwien.ict.acona.cell.storage.DataStorage;
import at.tuwien.ict.acona.cell.storage.DataStorageImpl;
import jade.core.behaviours.Behaviour;

public class DummyCell implements CellInitialization {
	
	//Genotype configuration
	protected JsonObject conf;
	
	//phenotype functions
	private final Map<String, Condition> conditionMap = new HashMap<String, Condition>();
	private final Map<String, CellFunctionBehaviour> cellFunctionBehaviourMap = new HashMap<String, CellFunctionBehaviour>();
	private final Map<String, Activator> activatorMap = new HashMap<String, Activator>();

	private DataStorage data = new DataStorageImpl().init(null);
	
	@Override
	public DataStorage getDataStorage() {
		return data;
	}

	@Override
	public ActivationHandler getActivationHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addBehaviour(Behaviour b) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DummyCell [data=");
		builder.append(data);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public void setConditionMap(Map<String, Condition> conditionMap) {
		this.conditionMap.putAll(conditionMap);
	}
	
	@Override
	public void setCellFunctionBehaviourMap(Map<String, CellFunctionBehaviour> cellFunctionBehaviourMap) {
		this.cellFunctionBehaviourMap.putAll(cellFunctionBehaviourMap);
		
	}

	@Override
	public void setActivatorMap(Map<String, Activator> activatorMap) {
		this.activatorMap.putAll(activatorMap);
	}
	
	@Override
	public Map<String, Condition> getConditionMap() {
		return this.conditionMap;
	}

	@Override
	public Map<String, CellFunctionBehaviour> getCellFunctionBehaviourMap() {
		return this.cellFunctionBehaviourMap;
	}

	@Override
	public Map<String, Activator> getActivatorMap() {
		return this.activatorMap;
	}
	
	@Override
	public JsonObject getConfiguration() {
		//Deepcopy through serialization
		return new Gson().fromJson(conf, JsonObject.class);
	}

	@Override
	public void setupCellFunctionBehaviours(JsonObject conf) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
