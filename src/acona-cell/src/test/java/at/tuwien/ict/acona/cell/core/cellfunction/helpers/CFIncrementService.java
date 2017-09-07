package at.tuwien.ict.acona.cell.core.cellfunction.helpers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.ServiceState;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Datapoints;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

public class CFIncrementService extends CellFunctionThreadImpl {

	private static Logger log = LoggerFactory.getLogger(CFIncrementService.class);

	//public final static String INCREMENTDATAPOINTATTRIBUTE = "incrementDatapointAddress";
	public final static String ATTRIBUTEINCREMENTDATAPOINT = "increment"; //This is the key for the actual address

	// private static final String R = "rawdata";

	@Override
	protected void executeFunction() throws Exception {
		// Get the datapoint to increment with 1

		// Get settings from config
		// CellConfig config2 = this.getConfig().getProperty("test",
		// CellConfig.class);

		// JsonObject rawdata = this.readLocalAsJson(R).getAsJsonObject();
		String address = "";
		try {
			log.info("{}>Start execution. Local sync datapoints = {}", this.getFunctionName(), this.getSyncDatapointConfigs().keySet());
			//address = this.getSyncDatapoints().get(ATTRIBUTEINCREMENTDATAPOINT).getAddress();
			double value = this.getValueMap().get(ATTRIBUTEINCREMENTDATAPOINT).getValue().getAsDouble();
			log.info("Read value={}", value);
			value++;
			log.info("New value={}", value);
			// write new value back to the same datapoint
			this.getValueMap().put(this.getSyncDatapointConfigs().get(ATTRIBUTEINCREMENTDATAPOINT).getAddress(), Datapoints.newDatapoint(this.getSyncDatapointConfigs().get(ATTRIBUTEINCREMENTDATAPOINT).getAddress()).setValue(String.valueOf(value)));
			log.debug("Function execution finished");
		} catch (Exception e) {
			log.error("Cannot execute incrementation service. Often the problem is that the value of the address {} has not been initialized yet", address, e);
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {

		//Syntax
		//address: command, value START, STOP, EXIT
		//get command
		//if (parameterdata.containsKey("command")) {
		log.debug("Execute method Setcommand with parameter {}", parameterdata);
		JsonRpcResponse result = new JsonRpcResponse(parameterdata, this.executeCommandStart().toJsonObject());
		//}

		return result;
	}

	private Datapoint executeCommandStart() {
		String message = ServiceState.FINISHED.toString();

		try {
			//Start the incrementor
			this.setCommand("START");

			//			//Get the blocker
			//			boolean blockState = false;
			//			try {
			//				blockState = this.getBlocker().poll(10000, TimeUnit.MICROSECONDS);
			//			} catch (InterruptedException e) {
			//				log.error("Queue interrupted");
			//			}
			//
			//			if (blockState == false) {
			//				throw new Exception("Timeout");
			//			}
		} catch (Exception e) {
			log.error("Error", e);
			message = ServiceState.ERROR.toString();
		}

		return Datapoints.newDatapoint("state").setValue(message);

	}

	@Override
	protected void shutDownExecutor() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		log.info("Init service={}", this.getFunctionName());

	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		this.setServiceState(ServiceState.FINISHED);
		//this.getCommunicator().write(Datapoints.newDatapoint(this.addServiceName(RESULTSUFFIX)).setValue(this.getCurrentState().toString()));

	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub

	}

}
