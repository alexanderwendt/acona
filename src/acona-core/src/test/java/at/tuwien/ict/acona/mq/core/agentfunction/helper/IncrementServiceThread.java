package at.tuwien.ict.acona.mq.core.agentfunction.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.mq.core.agentfunction.AgentFunctionThreadImpl;
import at.tuwien.ict.acona.mq.core.agentfunction.ServiceState;

public class IncrementServiceThread extends AgentFunctionThreadImpl {

	private static Logger log = LoggerFactory.getLogger(IncrementServiceThread.class);

	// public final static String INCREMENTDATAPOINTATTRIBUTE = "incrementDatapointAddress";
	public final static String ATTRIBUTEINCREMENTDATAPOINT = "increment"; // This is the key for the actual address as a managed datapoint

	// private static final String R = "rawdata";

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		this.setFinishedAfterSingleRun(true);
		log.info("Add a managed datapoint for {} to subscribe and write back", ATTRIBUTEINCREMENTDATAPOINT);
		log.info("Init service={}", this.getFunctionName());

	}

	@Override
	protected void executeFunction() throws Exception {
		// Get the datapoint to increment with 1
		String address = "";
		try {
			log.info("{}>Start execution. Local sync datapoints = {}", this.getFunctionName(), this.getSyncDatapointConfigs().keySet());
			// address = this.getSyncDatapoints().get(ATTRIBUTEINCREMENTDATAPOINT).getAddress();
			double value = this.getValueMap().get(ATTRIBUTEINCREMENTDATAPOINT).getValueOrDefault(new JsonPrimitive(0.0)).getAsDouble();
			log.info("Read value={}", value);
			value++;
			log.info("New value={}", value);
			// write new value back to the same datapoint
			this.getValueMap().get(ATTRIBUTEINCREMENTDATAPOINT).setValue(String.valueOf(value));
			log.debug("Function execution finished");
		} catch (Exception e) {
			log.error("Cannot execute incrementation service. Often the problem is that the value of the address {} has not been initialized yet", address, e);
			throw new Exception(e.getMessage());
		}
	}

	@Override
	protected void shutDownThreadExecutor() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		//this.setServiceState(ServiceState.FINISHED);
		// this.getCommunicator().write(Datapoints.newDatapoint(this.addServiceName(RESULTSUFFIX)).setValue(this.getCurrentState().toString()));

	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateCustomDatapointsById(String id, JsonElement data) {
		log.debug("Nothing shall be subscribed {}:{}", id, data);
		

	}

}
