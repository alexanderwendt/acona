package at.tuwien.ict.acona.mq.cell.cellfunction;

import java.util.Map;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.cell.communication.MqttCommunicator;
import at.tuwien.ict.acona.mq.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.mq.cell.config.DatapointConfig;
import at.tuwien.ict.acona.mq.cell.core.Cell;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

public class CellFunctionDummy implements CellFunction {

	private final String functionName;
	private final String cellName;

	public CellFunctionDummy(String name, String cellName) {
		this.functionName = name;
		this.cellName = cellName;
	}

	@Override
	public void init(CellFunctionConfig config, Cell cell) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String getFunctionName() {
		return functionName;
	}

	@Override
	public String getCellName() {
		return cellName;
	}

	@Override
	public CellFunctionConfig getFunctionConfig() {
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
