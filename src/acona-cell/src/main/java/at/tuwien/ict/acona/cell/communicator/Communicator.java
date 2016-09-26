package at.tuwien.ict.acona.cell.communicator;

import java.util.List;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public interface Communicator {
	
	public void setDefaultTimeout(int timeout);
	public int getDefaultTimeout();
	
	public List<Datapoint> read(List<String> datapoints) throws Exception;
	public List<Datapoint> read(List<String> datapoints, String agentName, int timeout) throws Exception;
	public Datapoint read(String datapointName) throws Exception;
	public Datapoint read(String datapoint, String agentName) throws Exception;
	public Datapoint read(String datapoint, String agentName, int timeout) throws Exception;
	
	public Datapoint query(Datapoint datapoint, String agentName, int timeout) throws Exception;
	
	public void write(List<Datapoint> datapoints) throws Exception;
	public void write(List<Datapoint> datapoints, String agentName, int timeout, boolean blocking) throws Exception;
	public void write(Datapoint datapoint) throws Exception;
	public void write(Datapoint datapoint, String agentName) throws Exception;
	public void writeNonblocking(Datapoint datapoint, String agentName) throws Exception;
	
	//public List<Datapoint> subscribe(List<Datapoint> datapoints, String agentName) throws Exception;
	public List<Datapoint> subscribe(List<String> datapoints, String agentName) throws Exception;
	public void unsubscribe(List<String> datapoints, String agentName) throws Exception;
	public void unsubscribeDatapoint(String datapointName, String name) throws Exception;
}
