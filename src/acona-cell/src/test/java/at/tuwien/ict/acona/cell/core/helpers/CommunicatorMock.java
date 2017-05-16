package at.tuwien.ict.acona.cell.core.helpers;

import java.util.List;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.cell.cellfunction.CellFunction;
import at.tuwien.ict.acona.cell.communicator.BasicServiceCommunicator;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

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
	public List<Datapoint> read(List<String> datapoints, String agentName, int timeout) throws Exception {
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
	public void write(List<Datapoint> datapoints, String agentName, int timeout, boolean blocking) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void write(Datapoint datapoint) throws Exception {
		this.cell.getDataStorage().write(datapoint, this.cell.getLocalName());

	}

	@Override
	public void write(Datapoint datapoint, String agentName) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Datapoint> subscribe(List<String> datapoints, String agentName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void unsubscribe(List<String> datapoints, String agentName) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Datapoint> execute(String agentName, String serviceName, List<Datapoint> methodParameters, int timeout) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Datapoint> execute(String agentName, String serviceName, List<Datapoint> methodParameters, int timeout, boolean useSubscribeProtocol) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void createResponderForFunction(CellFunction function) {
		// TODO Auto-generated method stub

	}

	@Override
	public Datapoint subscribe(String datapointName, String agentName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void unsubscribe(String datapointName, String name) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void executeAsynchronous(String agentName, String serviceName, List<Datapoint> methodParameters) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifySubscriber(Datapoint datapoint, String agentName) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public Datapoint queryDatapoints(String writeAddress, JsonElement content, String writeAgentName, String resultAddress, String resultAgentName, int timeout) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datapoint queryDatapoints(String writeAddress, JsonElement content, String resultAddress, int timeout) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datapoint queryDatapoints(String writeAddress, String content, String writeAgentName, String resultAddress, String resultAgentName, int timeout) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datapoint queryDatapoints(String writeAddress, String content, String resultAddress, int timeout) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeResponderForFunction(CellFunction function) {
		// TODO Auto-generated method stub

	}

	@Override
	public Datapoint subscribeDatapoint(String agentid, String address, CellFunction callingCellfunctionName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datapoint unsubscribeDatapoint(String agentid, String address, CellFunction callingCellFunctionName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datapoint executeServiceQueryDatapoints(String writeAgentName, String serviceName, List<Datapoint> serviceParameter, String resultAgentName, String resultAddress, int timeout) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Datapoint> readWildcard(String datapointName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
