package at.tuwien.ict.acona.cell.communicator;

import java.util.List;

import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public interface CommunicatorToCellFunction {
	
	public void setDefaultTimeout(int timeout);
	public int getDefaultTimeout();
	
	public List<Datapoint> read(List<Datapoint> datapoints) throws Exception;
	public List<Datapoint> read(List<Datapoint> datapoints, String agentName, int timeout) throws Exception;
	public Datapoint read(Datapoint datapoint) throws Exception;
	public Datapoint read(Datapoint datapoint, String agentName) throws Exception;
	public Datapoint read(Datapoint datapoint, String agentName, int timeout) throws Exception;
	
	public void write(List<Datapoint> datapoints) throws Exception;
	public void write(List<Datapoint> datapoints, String agentName, int timeout, boolean blocking) throws Exception;
	public void write(Datapoint datapoints) throws Exception;
	public void write(Datapoint datapoints, String agentName) throws Exception;
	
	public List<Datapoint> subscribe(List<Datapoint> datapoints, String agentName) throws Exception;
	public void unsubscribe(List<Datapoint> datapoints, String agentName) throws Exception;
}
