package at.tuwien.ict.acona.cell.cellfunction.specialfunctions;

import at.tuwien.ict.acona.cell.communicator.BasicServiceCommunicator;
import at.tuwien.ict.acona.cell.core.Cell;

public class ServiceStub {
	private final Cell cell;
	protected final BasicServiceCommunicator comm;
	protected String agentName;
	protected String serviceName;
	protected int timeout;

	public ServiceStub(Cell cell, String agentName, String serviceName, int timeout) {
		this.cell = cell;
		this.comm = this.cell.getCommunicator();
		this.init(agentName, serviceName, timeout);
	}

	public void init(String agent, String serviceName, int timeout) {
		this.agentName = agent;
		this.serviceName = serviceName;
		this.timeout = timeout;
	}

}
