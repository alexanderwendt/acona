package at.tuwien.ict.acona.cell.core;

import java.util.Map;

import at.tuwien.ict.acona.cell.activator.Activator;
import at.tuwien.ict.acona.cell.activator.Condition;

public interface CellInitialization extends Cell {
	public void setConditionMap(Map<String, Condition> conditionMap);
	public void setCellFunctionBehaviourMap(Map<String, CellFunctionBehaviour> cellFunctionBehaviourMap);
	public void setActivatorMap(Map<String, Activator> activatorMap);
	public Map<String, Condition> getConditionMap();
	public Map<String, CellFunctionBehaviour> getCellFunctionBehaviourMap();
	public Map<String, Activator> getActivatorMap();
}
