package at.tuwien.ict.acona.cell.communicator;

import java.util.List;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public interface BasicServiceCommunicator extends Communicator {

	/**
	 * Subscribe a list of datapoints from an agent
	 * 
	 * @param datapointNames
	 * @param agentName
	 * @return
	 * @throws Exception
	 */
	public List<Datapoint> subscribe(List<String> datapointNames, String agentName) throws Exception;

	/**
	 * Subscribe a datapoint from an agent
	 * 
	 * @param datapointName
	 * @param agentName
	 * @return
	 * @throws Exception
	 */
	public Datapoint subscribe(String datapointName, String agentName) throws Exception;

	/**
	 * Unsubscribe a list of datapoints from an agent.
	 * 
	 * @param datapoints
	 * @param agentName
	 * @throws Exception
	 */
	public void unsubscribe(List<String> datapoints, String agentName) throws Exception;

	/**
	 * Unsubscribe a datapoint from an agent.
	 * 
	 * @param datapoints
	 * @param agentName
	 * @throws Exception
	 */
	public void unsubscribe(String datapointName, String name) throws Exception;

	/**
	 * Notify a subscriber that a datapoint has arrived. This service is
	 * triggered at the arrival of a datapoint.
	 * 
	 * @param datapoint
	 * @param agentName
	 * @throws Exception
	 */
	public void notifySubscriber(Datapoint datapoint, String agentName) throws Exception;
}
