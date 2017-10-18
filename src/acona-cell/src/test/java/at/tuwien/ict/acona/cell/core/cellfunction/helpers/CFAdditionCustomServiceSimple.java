package at.tuwien.ict.acona.cell.core.cellfunction.helpers;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.ServiceState;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.DatapointBuilder;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

public class CFAdditionCustomServiceSimple extends CellFunctionThreadImpl {

	private static Logger log = LoggerFactory.getLogger(CFAdditionCustomServiceSimple.class);

	private final String COMMANDDATAPOINTNAME = "command";
	private final String STATUSDATAPOINTNAME = "status";
	private final String OPERAND1 = "operand1";
	private final String OPERAND2 = "operand2";
	private final String RESULT = "result";

	private final Map<String, DatapointConfig> trackedDatapoints = new HashMap<>();

	private double operand1;
	private double operand2;

	// private final String inputMemoryAgentName = "InputBufferAgent";
	// private final String memorydatapoint1 = "inputmemory.variable1"; //put
	// into memory mock agent
	// private final String memorydatapoint2 = "inputmemory.variable2"; //put
	// into memory mock agent

	public CFAdditionCustomServiceSimple() {
		this.setExecuteOnce(true); // Run only on demand from controller
	}

	@Override
	protected void executeFunction() throws Exception {
		// Read the values needed
		try {
			log.debug("Read from datapoint for OP1={}", this.trackedDatapoints.get(OPERAND1));
			operand1 = this.getCommunicator().read(this.trackedDatapoints.get(OPERAND1).getAgentid(this.getCell().getLocalName()), this.trackedDatapoints.get(OPERAND1).getAddress(), 1000000).getValue().getAsDouble();
			operand2 = this.getCommunicator().read(this.trackedDatapoints.get(OPERAND2).getAgentid(this.getCell().getLocalName()), this.trackedDatapoints.get(OPERAND2).getAddress(), 1000000).getValue().getAsDouble();

			log.info("read operand1={} and operand2={}", operand1, operand2);
		} catch (Exception e) {
			log.error("Cannot read datapoint", e);
			throw new Exception(e.getMessage());
		}

		// Add the values
		double result = operand1 + operand2;
		log.info("result={}", result);

		// Now send the result to a result datapoint
		this.getCommunicator().write(this.trackedDatapoints.get(RESULT).getAgentid(this.getCell().getLocalName()), DatapointBuilder.newDatapoint(this.trackedDatapoints.get(RESULT).getAddress())
				.setValue(new JsonPrimitive(result)));
	}

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		// Add the datapoints from the config to the subscriptions
		this.trackedDatapoints.put(OPERAND1,
				DatapointConfig.newConfig(this.getFunctionConfig().getPropertyAsJsonObject(OPERAND1)));
		this.trackedDatapoints.put(OPERAND2,
				DatapointConfig.newConfig(this.getFunctionConfig().getPropertyAsJsonObject(OPERAND2)));
		this.trackedDatapoints.put(RESULT, DatapointConfig.newConfig(this.getFunctionConfig().getPropertyAsJsonObject(RESULT)));

	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		// Set status that process is finished. Use it to release subscriptions
		this.getCommunicator().write(DatapointBuilder.newDatapoint(STATUSDATAPOINTNAME).setValue(ServiceState.FINISHED.toString()));
		log.info("Function end after setting status={}", this.getCommunicator().read(STATUSDATAPOINTNAME));
	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		// Set status that the system is running
		// this.getCommunicator().write(Datapoint.newDatapoint(this.getSubscribedDatapoints().get(STATUSDATAPOINTNAME).getAddress()).setValue(ServiceState.RUNNING.toString()));

	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		// React on the start trigger
		//JsonElement value = data.get(COMMANDDATAPOINTNAME).getValue();
		if (data.containsKey(COMMANDDATAPOINTNAME) && data.get(COMMANDDATAPOINTNAME).getValue().isJsonPrimitive() == true) {
			try {
				this.setCommand(data.get(COMMANDDATAPOINTNAME).getValue().getAsString());
			} catch (Exception e) {
				log.error("Cannot read command", e);
			}
		} else {
			log.info("An unknown or empty command was put on the datapoint={}", data);
		}

	}

	@Override
	protected void shutDownExecutor() {
		// TODO Auto-generated method stub

	}

	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		// TODO Auto-generated method stub
		return null;
	}

}
