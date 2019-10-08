package at.tuwien.ict.acona.mq.core.agentfunction.specialfunctions;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.core.agentfunction.AgentFunctionImpl;
import at.tuwien.ict.acona.mq.core.agentfunction.SyncMode;
import at.tuwien.ict.acona.mq.core.config.DatapointConfig;
import at.tuwien.ict.acona.mq.datastructures.Datapoint;

/**
 * @author wendt
 * 
 *         This function is used together with managed datapoints to subscribe values and put them into the data storage in the same agent or 
 *         at another location. The function has 2 modes (mode): True for writing the data as a datapoint and False: republish the data to 
 *         an MQTT address
 *
 */
public class DatapointTransfer extends AgentFunctionImpl {

	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public final static String PARAMSOURCEADDRESS = "source";
	public final static String PARAMDESTINATIONADDRESS = "destination";
	public final static String PARAMMODE = "mode";	//T: Write, F: Publish 

	private String source;
	private String destination;
	private boolean mode;

	@Override
	protected void agentFunctionInit() throws Exception {

		source = this.getFunctionConfig().getProperty(PARAMSOURCEADDRESS);
		destination = this.getFunctionConfig().getProperty(PARAMDESTINATIONADDRESS);
		mode = Boolean.valueOf(this.getFunctionConfig().getProperty(PARAMMODE, "true"));

		this.addManagedDatapoint(DatapointConfig.newConfig(source, source, SyncMode.SUBSCRIBEONLY));

		log.info("Datastorageupdate will happen for the following datapoint: source {} -> destination {}", source, destination);

		log.debug("{}> Datapoint Transfer function initialized. Send from {} to {}, mode {}", this.getFunctionName(), source, destination, mode);
	}

	@Override
	protected void shutDownImplementation() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateDatapointsById(String id, String topic, JsonElement data) {
		if (this.mode==true) {
			try {
				Datapoint sourceDatapoint = this.getDatapointBuilder().toDatapoint(data.toString());
				Datapoint destinationDatapoint = this.getDatapointBuilder().newDatapoint(destination).setValue(sourceDatapoint.getValue());
				log.debug("Update datapoint={}", sourceDatapoint);

				this.getAgent().getCommunicator().write(destinationDatapoint);
			} catch (Exception e) {
				log.error("Cannot write {} to datastorage", data);
			}
		} else {
			try {
				this.getCommunicator().publishTopic(destination, data);
				log.debug("Republished data from {} to {}", topic, destination);
			} catch (Exception e) {
				log.error("Cannot republish {} to {}", data, destination);
			}
		}
	}
}
