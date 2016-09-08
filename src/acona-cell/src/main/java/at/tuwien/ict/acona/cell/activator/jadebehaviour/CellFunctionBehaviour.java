package at.tuwien.ict.acona.cell.activator.jadebehaviour;

import java.util.Map;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public interface CellFunctionBehaviour {
	public CellFunctionBehaviour init(String name, JsonObject settings, Cell caller);
	public void updateData(Map<String, Datapoint> data);
	public void setRunPermission(boolean isAllowedToRun);
	public void startBehaviour();
	public void addBehaviourToCallerCell(Cell caller);
	public String getName();
}
