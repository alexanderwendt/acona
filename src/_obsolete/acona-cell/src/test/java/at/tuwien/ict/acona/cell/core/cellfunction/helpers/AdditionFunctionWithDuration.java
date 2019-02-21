package at.tuwien.ict.acona.cell.core.cellfunction.helpers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

public class AdditionFunctionWithDuration extends CellFunctionThreadImpl {

	private static Logger log = LoggerFactory.getLogger(AdditionFunctionWithDuration.class);

	public static final String OPERANDID1 = "operand1";
	public static final String OPERANDID2 = "operand2";
	public static final String RESULT = "result";

	private int delayInSeconds = 3;

	private int systemRuns = 0;

	private double operand1 = 0;
	private double operand2 = 0;
	private double result = 0;

	@Override
	protected void cellFunctionThreadInit() throws Exception {

	}

	private void delaySystem(int delayInSeconds) {
		for (int i = 1; i <= delayInSeconds; i++) {
			try {
				// Block profile controller
				synchronized (this) {
					this.wait(1000);
				}

			} catch (InterruptedException e) {
				log.warn("Wait interrupted client");
			}

			log.debug("waited {}ms", i * 1000);
		}
	}

	@Override
	protected void executeFunction() throws Exception {
		// Read the values needed
		try {
			this.systemRuns++;
			this.result = operand1 + operand2;
			log.info("Calculated result={} operand1={} + operand2 ={}", result, operand1, operand2);

			//Insert delay
			this.delaySystem(delayInSeconds);

			log.debug("Calculation finished. Number of system runs={}", this.systemRuns);

		} catch (Exception e) {
			log.error("Cannot calculate values", e);
			throw new Exception(e.getMessage());
		}
	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		this.getValueMap().get(RESULT).setValue(new JsonPrimitive(this.result));
		log.debug("Put {} in the value map={}", result, this.getValueMap());
	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {

	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		try {
			if (data.containsKey(OPERANDID1)) {
				this.operand1 = data.get(OPERANDID1).getValue().getAsDouble();
				log.debug("Received operand1={}", this.operand1);

				this.setStart();
			} else if (data.containsKey(OPERANDID2)) {
				this.operand2 = data.get(OPERANDID2).getValue().getAsDouble();
				log.debug("Received operand2={}", this.operand2);

				this.setStart();
			} else if (this.isSystemDatapoint(data) == false) {
				log.info("An unknown or empty command was put on the datapoint={}", data);
			}
		} catch (Exception e) {
			log.error("Problem with update", e);
		}
	}

	@Override
	protected void shutDownThreadExecutor() {
		// TODO Auto-generated method stub

	}

	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		// TODO Auto-generated method stub
		return null;
	}

}
