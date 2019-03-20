package at.tuwien.ict.acona.mq.core.agentfunction.specialfunctions;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.core.agentfunction.AgentFunctionImpl;
import at.tuwien.ict.acona.mq.core.config.DatapointConfig;
import at.tuwien.ict.acona.mq.datastructures.Datapoint;

/**
 * @author wendt
 * 
 *         This function is used together with managed datapoints to subscribe values in OTHER AGENTS and put them into the data storage of the local agent. As soon as values are triggered, the agent
 *         copies them to the own storage with the same address.
 *
 */
public class DatapointMirroring extends AgentFunctionImpl {

	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	protected void agentFunctionInit() throws Exception {

		for (DatapointConfig config : this.getFunctionConfig().getManagedDatapoints()) {

			if (config.getAgentid().equals(this.getAgent().getName())) {
				throw new Exception("Function " + this.getFunctionName() + " is not allowed to subscribe datapoints of the own agent, in order to avoid circular references. Erroneous subscription: " + config);
			}
		}

		log.debug("Datastorageupdate will happen for the following datapoints: {}", this.getSubscribedDatapoints());
	}

	@Override
	protected void shutDownImplementation() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateDatapointsById(String id, String topic, JsonElement data) {
		try {
			Datapoint dp = this.getDatapointBuilder().toDatapoint(data.toString());

			log.debug("Update datapoint={}", data);
			dp.setAgent(this.getAgent().getName());
			this.getAgent().getCommunicator().write(dp);
		} catch (Exception e) {
			log.error("Cannot write {} to datastorage", data);
		}

	}
}
