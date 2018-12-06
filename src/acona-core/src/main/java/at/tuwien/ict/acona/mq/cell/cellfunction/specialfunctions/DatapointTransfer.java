package at.tuwien.ict.acona.mq.cell.cellfunction.specialfunctions;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.cell.cellfunction.CellFunctionImpl;
import at.tuwien.ict.acona.mq.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.mq.cell.config.DatapointConfig;
import at.tuwien.ict.acona.mq.datastructures.Datapoint;

/**
 * @author wendt
 * 
 *         This function is used together with managed datapoints to subscribe values and put them into the data storage in the same agent or at another location.
 *
 */
public class DatapointTransfer extends CellFunctionImpl {

	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public final static String PARAMSOURCEADDRESS = "source";
	public final static String PARAMDESTINATIONADDRESS = "destination";

	private String source;
	private String destination;

	@Override
	protected void cellFunctionInit() throws Exception {

		source = this.getFunctionConfig().getProperty(PARAMSOURCEADDRESS);
		destination = this.getFunctionConfig().getProperty(PARAMDESTINATIONADDRESS);

		this.addManagedDatapoint(DatapointConfig.newConfig(source, source, SyncMode.SUBSCRIBEONLY));

		log.debug("Datastorageupdate will happen for the following datapoint: source {} -> destination {}", source, destination);

		log.info("{}>Datapoint Transfer function initialized", this.getFunctionName());
	}

	@Override
	protected void shutDownImplementation() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateDatapointsById(String id, JsonElement data) {
		try {
			Datapoint sourceDatapoint = this.getDatapointBuilder().toDatapoint(data.toString());
			Datapoint destinationDatapoint = this.getDatapointBuilder().newDatapoint(destination).setValue(sourceDatapoint.getValue());
			log.debug("Update datapoint={}", sourceDatapoint);

			this.getCell().getCommunicator().write(destinationDatapoint);
		} catch (Exception e) {
			log.error("Cannot write {} to datastorage", data);
		}

	}
}
