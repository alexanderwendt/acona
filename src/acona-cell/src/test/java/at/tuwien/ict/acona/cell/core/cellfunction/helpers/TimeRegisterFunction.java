package at.tuwien.ict.acona.cell.core.cellfunction.helpers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.DatapointBuilder;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

public class TimeRegisterFunction extends CellFunctionThreadImpl {

	private static Logger log = LoggerFactory.getLogger(TimeRegisterFunction.class);

	private long registeredTime = 0;

	//The time is written in the result

	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void executeFunction() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		if (this.isSystemDatapoint(data) == false) {
			log.info("Received subscribed update={}", data);
			this.registeredTime = System.currentTimeMillis();
			Datapoint result = DatapointBuilder.newDatapoint(this.addServiceName(RESULTSUFFIX)).setValue(String.valueOf(this.registeredTime));
			try {
				this.getCommunicator().write(result);
				log.info("Time written={}", result);
			} catch (Exception e) {
				log.error("Cannot write datapoint", e);
			}

		}

	}

	@Override
	protected void shutDownExecutor() throws Exception {
		// TODO Auto-generated method stub

	}

}
