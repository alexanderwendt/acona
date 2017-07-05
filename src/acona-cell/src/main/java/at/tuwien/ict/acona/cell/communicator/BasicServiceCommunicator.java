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
	public List<Datapoint> subscribe(String agentName, List<String> datapointNames) throws Exception;

	/**
	 * Subscribe a datapoint from an agent
	 * 
	 * @param datapointName
	 * @param agentName
	 * @return
	 * @throws Exception
	 */
	public Datapoint subscribe(String agentName, String datapointName) throws Exception;

	/**
	 * Unsubscribe a list of datapoints from an agent.
	 * 
	 * @param datapoints
	 * @param agentName
	 * @throws Exception
	 */
	public void unsubscribe(String agentName, List<String> datapoints) throws Exception;

	/**
	 * Unsubscribe a datapoint from an agent.
	 * 
	 * @param datapoints
	 * @param agentName
	 * @throws Exception
	 */
	public void unsubscribe(String name, String datapointName) throws Exception;

	/**
	 * Notify a subscriber that a datapoint has arrived. This service is
	 * triggered at the arrival of a datapoint.
	 * 
	 * @param datapoint
	 * @param agentName
	 * @throws Exception
	 */
	public void notifySubscriber(String agentName, Datapoint datapoint) throws Exception;
}
