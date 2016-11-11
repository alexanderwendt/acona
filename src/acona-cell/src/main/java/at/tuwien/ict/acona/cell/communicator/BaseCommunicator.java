package at.tuwien.ict.acona.cell.communicator;

import java.util.List;

import at.tuwien.ict.acona.cell.cellfunction.CellFunction;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public interface BaseCommunicator {

	public void setDefaultTimeout(int timeout);

	public int getDefaultTimeout();

	public List<Datapoint> execute(String agentName, String serviceName, List<Datapoint> methodParameters, int timeout) throws Exception;

	public List<Datapoint> execute(String agentName, String serviceName, List<Datapoint> methodParameters, int timeout, boolean useSubscribeProtocol) throws Exception;

	public void executeAsynchronous(String agentName, String serviceName, List<Datapoint> methodParameters) throws Exception;

	// === Init Cellfunctions as services, which shall have exteral access ===//
	/**
	 * Any cell function can create a responder to handle its incoming requests
	 * 
	 * @param function
	 */
	public void createResponderForFunction(CellFunction function);
}
