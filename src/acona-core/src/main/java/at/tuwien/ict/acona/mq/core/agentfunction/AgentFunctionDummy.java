package at.tuwien.ict.acona.mq.core.agentfunction;

import java.util.Map;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.core.communication.MqttCommunicator;
import at.tuwien.ict.acona.mq.core.config.FunctionConfig;
import at.tuwien.ict.acona.mq.core.config.DatapointConfig;
import at.tuwien.ict.acona.mq.core.core.Cell;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

public class AgentFunctionDummy implements AgentFunction {

	private final String functionName;
	private final String cellName;

	public AgentFunctionDummy(String name, String cellName) {
		this.functionName = name;
		this.cellName = cellName;
	}

	@Override
	public void init(FunctionConfig config, Cell cell) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String getFunctionName() {
		return functionName;
	}

	@Override
	public String getAgentName() {
		return cellName;
	}

	@Override
	public FunctionConfig getFunctionConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, DatapointConfig> getSubscribedDatapoints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response performOperation(String topic, Request param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateSubscribedData(String topic, JsonElement data) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void shutDownFunction() {
		// TODO Auto-generated method stub

	}

	@Override
	public ServiceState getCurrentState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MqttCommunicator getCommunicator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFunctionRootAddress() {
		// TODO Auto-generated method stub
		return null;
	}

}
