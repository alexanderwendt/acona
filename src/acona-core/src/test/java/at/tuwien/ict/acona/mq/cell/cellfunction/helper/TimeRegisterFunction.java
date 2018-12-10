package at.tuwien.ict.acona.mq.cell.cellfunction.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.cell.cellfunction.CellFunctionImpl;
import at.tuwien.ict.acona.mq.datastructures.DPBuilder;
import at.tuwien.ict.acona.mq.datastructures.Datapoint;

public class TimeRegisterFunction extends CellFunctionImpl {

	private static Logger log = LoggerFactory.getLogger(TimeRegisterFunction.class);

	private long registeredTime = 0;

	@Override
	protected void cellFunctionInit() throws Exception {
		log.info("Initialized time register function");

	}

	@Override
	protected void shutDownImplementation() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateDatapointsById(String id, String topic, JsonElement data) {
		log.info("Received subscribed update={}", data);
		this.registeredTime = System.currentTimeMillis();
		Datapoint result = (new DPBuilder()).newDatapoint(this.getFunctionRootAddress() + RESULTSUFFIX).setValue(String.valueOf(this.registeredTime));
		try {
			this.getCommunicator().write(result);
			log.info("Time written={}", result);
		} catch (Exception e) {
			log.error("Cannot write datapoint", e);
		}

	}

}
