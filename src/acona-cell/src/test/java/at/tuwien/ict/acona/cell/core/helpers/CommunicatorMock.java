package at.tuwien.ict.acona.cell.core.helpers;

import java.util.List;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.cell.cellfunction.CellFunction;
import at.tuwien.ict.acona.cell.communicator.BasicServiceCommunicator;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

public class CommunicatorMock implements BasicServiceCommunicator {

	private Cell cell;

	public CommunicatorMock(Cell cell) {
		this.cell = cell;
	}

	@Override
	public void setDefaultTimeout(int timeout) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getDefaultTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Datapoint> read(List<String> datapoints) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datapoint read(String datapointName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datapoint read(String datapoint, String agentName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datapoint read(String datapoint, String agentName, int timeout) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void write(List<Datapoint> datapoints) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(Datapoint datapoint) throws Exception {
		this.cell.getDataStorage().write(datapoint, this.cell.getLocalName());

	}

	@Override
	public void createResponderForFunction(CellFunction function) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeResponderForFunction(CellFunction function) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Datapoint> readWildcard(String datapointName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonRpcResponse execute(String agentName, String serviceName, JsonRpcRequest methodParameters, int timeout) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonRpcResponse execute(String agentName, String serviceName, JsonRpcRequest methodParameters, int timeout, boolean useSubscribeProtocol) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void executeAsynchronous(String agentName, String serviceName, JsonRpcRequest methodParameters) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Datapoint> read(String agentName, List<String> datapoints, int timeout) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void write(String agentName, List<Datapoint> datapoints, int timeout, boolean blocking) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(String agentName, Datapoint datapoint) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Datapoint> subscribe(String agentName, List<String> datapointNames) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void unsubscribe(String agentName, List<String> datapoints) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifySubscriber(String agentName, Datapoint datapoint) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void remove(String datapoint) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void remove(List<String> datapoint) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void remove(String agentName, List<String> datapoint, int timeout) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public JsonRpcResponse execute(String agentNameAndService, JsonRpcRequest methodParameters) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datapoint subscribeDatapoint(String key, String callingCellfunctionName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void unsubscribeDatapoint(String key, String callingCellFunctionName) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public Datapoint subscribe(String completeAddress) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void unsubscribe(String completeAddress) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public Datapoint queryDatapoints(String writeAgentName, String writeAddress, JsonElement content, String resultAgentName, String resultAddress, JsonElement resultContent, int timeout) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datapoint queryDatapoints(String writeAgentName, String writeAddress, String content, String resultAgentName, String resultAddress, JsonElement resultContent, int timeout) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datapoint queryDatapoints(String writeAddress, JsonElement content, String resultAddress, JsonElement resultContent, int timeout) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datapoint queryDatapoints(String writeAddress, String content, String resultAddress, String resultContent, int timeout) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datapoint executeServiceQueryDatapoints(String writeAgentName, String serviceName, JsonRpcRequest serviceParameter, String resultAgentName, String resultAddress, JsonElement expectedResult, int timeout) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
