package at.tuwien.ict.acona.cell.cellfunction.specialfunctions;

import java.util.List;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public interface ReadDatapoint {
	public List<Datapoint> read(final List<String> datapointList);
}
