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
	private String name = "";
	
	@Override
	public CellFunctionBehaviour init(String name, JsonObject conf, Cell caller) {
		if (conf!=null && conf.has("option1")==true && conf.has("option2")==true) {
			log.info("Got info from config: option1={}, option2={}", conf.getAsJsonPrimitive("option1").getAsString(), conf.getAsJsonPrimitive("option2").getAsString());
		} else {
			log.info("No configuration was passed");
		}
		
		this.name = name;
		
		return this;
		
	}

	@Override
	public void setData(Map<String, Datapoint> data) {
		log.debug("Data arrived={}", data);
		
	}

	@Override
	public void setRunPermission(boolean isAllowedToRun) {
		log.debug("Permission to run set={}", isAllowedToRun);
		
	}

	@Override
	public void startBehaviour() {
		didRun = true;
		log.info("Dummybehaviour is running");
		
	}

	@Override
	public void addBehaviourToCallerCell(Cell caller) {
		log.debug("Add itself to the caller cell={}", caller);
		
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

	@Override
	public String getName() {
		return this.name;
	}

}
