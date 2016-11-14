package at.tuwien.ict.acona.cell.communicator;

import java.util.List;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public interface Communicator extends BaseCommunicator {

	public List<Datapoint> read(List<String> datapoints) throws Exception;

	public List<Datapoint> read(List<String> datapoints, String agentName, int timeout) throws Exception;

	public Datapoint read(String datapointName) throws Exception;

	public Datapoint read(String datapoint, String agentName) throws Exception;

	public Datapoint read(String datapoint, String agentName, int timeout) throws Exception;

	public void write(List<Datapoint> datapoints) throws Exception;

	public void write(List<Datapoint> datapoints, String agentName, int timeout, boolean blocking) throws Exception;

	public void write(Datapoint datapoint) throws Exception;

	public void write(Datapoint datapoint, String agentName) throws Exception;

	public List<Datapoint> subscribe(List<String> datapointNames, String agentName) throws Exception;

	public Datapoint subscribe(String datapointName, String agentName) throws Exception;

	public void unsubscribe(List<String> datapoints, String agentName) throws Exception;

	public void unsubscribe(String datapointName, String name) throws Exception;

	public void notifySubscriber(Datapoint datapoint, String agentName) throws Exception;

	/**
	 * Send a query and wait for an answer from the queried agent.
	 * 
	 * e.g. send START and get STATE back when a service has finished.
	 * 
	 * @param datapoint
	 *            to agent
	 * @param agentName
	 *            to agent
	 * @param timeout
	 *            to exit the query
	 * @return
	 * @throws Exception
	 */
	public Datapoint queryDatapoints(String writeAddress, JsonElement content, String writeAgentName, String resultAddress, String resultAgentName, int timeout) throws Exception;

	public Datapoint queryDatapoints(String writeAddress, String content, String writeAgentName, String resultAddress, String resultAgentName, int timeout) throws Exception;

	public Datapoint queryDatapoints(String writeAddress, JsonElement content, String resultAddress, int timeout) throws Exception;

	public Datapoint queryDatapoints(String writeAddress, String content, String resultAddress, int timeout) throws Exception;
}
