package at.tuwien.ict.acona.cell.activator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public abstract class ConditionImpl implements Condition {

	protected static Logger log = LoggerFactory.getLogger(ConditionImpl.class);
	
	protected String name = "";
	protected JsonObject conf = new JsonObject();
	
	@Override
	public Condition init(String name, JsonObject conf) {
		this.name = name;
		this.conf = conf;
		
		//Inidividual init
		this.subInit();
		
		return this;
	}
	
	protected abstract void subInit();

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public abstract boolean testCondition(Datapoint data);
	
	@Override
	public abstract String getDescription();

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("name=");
		builder.append(name);
		return builder.toString();
	}

}
