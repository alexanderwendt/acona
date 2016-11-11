package at.tuwien.ict.acona.cell.cellfunction.specialfunctions;

import java.util.List;

import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class ReadDatapointStub extends ServiceStub implements ReadDatapoint {

	public ReadDatapointStub(Cell cell, String agentName, String serviceName, int timeout) {
		super(cell, agentName, serviceName, timeout);
	}

	@Override
	public List<Datapoint> read(List<Datapoint> datapointList) {
		//Convert Datapointlist

		//execute communicator
		//execute(agentName, "read", parameter2, 100000)

		//Convert in desired format
		//this.comm.execute(agentName, "READ", methodParameters, timeout)
		return null;
	}

}
