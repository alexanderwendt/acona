package at.tuwien.ict.acona.cell.cellfunction.specialfunctions;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionImpl;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcError;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

/**
 * @author wendt
 * 
 *         This function is used together with managed datapoints to subscribe values in OTHER AGENTS and put them into the data storage of the local agent. As soon as values are triggered, the agent
 *         copies them to the own storage with the same address.
 *
 */
public class CFDataStorageUpdate extends CellFunctionImpl {

	private static Logger log = LoggerFactory.getLogger(CFDataStorageUpdate.class);

	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {

		JsonRpcResponse result = new JsonRpcResponse(parameterdata, new JsonRpcError("No such method", -1, "No such method", "No such method"));

		return result;
	}

	@Override
	protected void cellFunctionInit() throws Exception {

		for (DatapointConfig config : this.getFunctionConfig().getManagedDatapoints()) {

			if (config.getAgentid(this.getCell().getLocalName()).equals(this.getCell().getLocalName())) {
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
	protected void updateDatapointsById(Map<String, Datapoint> data) {
		data.values().forEach(dp -> {
			try {
				log.debug("Update datapoint={}", dp);
				dp.setAgent(this.getCell().getLocalName());
				this.getCell().getCommunicator().write(dp);
			} catch (Exception e) {
				log.error("Cannot write {} to datastorage", dp);
			}
		});
	}

}
