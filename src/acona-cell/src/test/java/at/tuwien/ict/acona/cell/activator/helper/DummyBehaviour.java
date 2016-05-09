package at.tuwien.ict.acona.cell.activator.helper;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.core.CellFunctionBehaviour;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class DummyBehaviour implements CellFunctionBehaviour {
	private static Logger log = LoggerFactory.getLogger(DummyBehaviour.class);

	private boolean didRun=false;
	
	@Override
	public CellFunctionBehaviour init(String name, JsonObject settings) {
		return null;
		
	}

	@Override
	public void setData(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRunPermission(boolean isAllowedToRun) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startBehaviour() {
		didRun = true;
		log.info("Dummybehaviour is running");
		
	}

	@Override
	public void addBehaviourToCallerCell(Cell caller) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean hasRun() {
		return this.didRun;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DummyBehaviour [didRun=");
		builder.append(didRun);
		builder.append("]");
		return builder.toString();
	}

}
