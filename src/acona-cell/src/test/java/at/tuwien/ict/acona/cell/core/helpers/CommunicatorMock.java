package at.tuwien.ict.acona.cell.core.helpers;

import java.util.List;

import at.tuwien.ict.acona.cell.communicator.Communicator;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class CommunicatorMock implements Communicator {

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
	public void writeNonblocking(Datapoint datapoint, String agentName) throws Exception {
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
	public void unsubscribeDatapoint(String datapointName, String name) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public Datapoint query(Datapoint datapointtowrite, String agentNameToWrite, Datapoint result,
			String agentNameResult, int timeout) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datapoint query(Datapoint datapointtowrite, Datapoint result, int timeout) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
