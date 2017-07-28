package at.tuwien.ict.acona.cell.core.cellfunction.helpers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionImpl;
import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

public class SingleNotificationReceiver extends CellFunctionImpl {

	private static final Logger log = LoggerFactory.getLogger(SingleNotificationReceiver.class);

	private final static String SUBSCRIBEDATAPOINT = "datapoint";
	private final static String DATAPOINTID = "datapointid";

	private String agent = "";
	private String address = "";

	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void cellFunctionInit() throws Exception {
		//Subscription
		agent = this.getFunctionConfig().getProperty(SUBSCRIBEDATAPOINT).split(":")[0];
		address = this.getFunctionConfig().getProperty(SUBSCRIBEDATAPOINT).split(":")[1];

		this.addManagedDatapoint(DatapointConfig.newConfig(DATAPOINTID, address, agent, SyncMode.SUBSCRIBEONLY));
	}

	@Override
	protected void shutDownImplementation() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateDatapointsById(Map<String, Datapoint> data) {
		try {
			//Write datapoint into database
			if (data.containsKey(DATAPOINTID)) {
				try {
					this.writeLocal(data.get(DATAPOINTID));
				} catch (Exception e) {
					log.error("Cannot write datapoint locally={}", data);
					throw e;
				}
			} else {

			}

			//Unsubscribe datapoint
			try {
				this.getCommunicator().unsubscribeDatapoint(this.agent, this.address, this);
			} catch (Exception e) {
				log.error("Unsubscription error");
				throw e;
			}

			//Shutdown function
		} catch (Exception e) {
			log.error("Cannot update datapoint", e);
		}

	}

}
