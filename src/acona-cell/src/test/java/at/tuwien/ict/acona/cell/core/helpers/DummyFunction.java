package at.tuwien.ict.acona.cell.core.helpers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.cellfunction.CellFunction;
import at.tuwien.ict.acona.cell.cellfunction.CellFunctionImpl;
import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class DummyFunction extends CellFunctionImpl {
	private static Logger log = LoggerFactory.getLogger(DummyFunction.class);

	private boolean didRun=false;
	private String name = "";
	
	@Override
	public void cellFunctionInit() {
		if (this.getConfig().getProperty("TESTPROPERTY1")!=null) {
			log.info("Got info from config: option1={}", this.getConfig().getProperty("TESTPROPERTY1"));
		} else {
			log.info("No configuration was passed");
		}
	}

//	@Override
//	public void updateData(Map<String, Datapoint> data) {
//		log.debug("Data arrived={}", data);
//		
//		try {
//			this.executeFunction();
//		} catch (Exception e) {
//			log.error("Cannot execute function", e);
//		}
//	}
	
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
	protected void executeFunction() throws Exception {
		log.info("Execute cell function");
	}

	@Override
	protected void executePostProcessing() {
		log.info("Postprocessing");
		
	}

	@Override
	protected void executePreProcessing() {
		log.info("Preprocessing");
		
	}

	@Override
	public void setCommand(ControlCommand command) {
		log.info("Command={} was set", command);
		
	}

	@Override
	protected void updateDatapointsById(Map<String, Datapoint> data) {
		log.debug("Data arrived={}", data);
		
		try {
			this.executeFunction();
		} catch (Exception e) {
			log.error("Cannot execute function", e);
		}
	}

}
