package at.tuwien.ict.acona.mq.core.core;

import at.tuwien.ict.acona.mq.core.communication.MqttCommunicator;
import at.tuwien.ict.acona.mq.core.config.AgentConfig;
import at.tuwien.ict.acona.mq.core.config.AgentFunctionConfig;
import at.tuwien.ict.acona.mq.core.storage.DataStorage;

public class DummyAgent implements Cell {

	private final String name;

	public DummyAgent(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public MqttCommunicator getCommunicator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataStorage getDataStorage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AgentFunctionHandler getFunctionHandler() {
		return new AgentFunctionHandlerImpl();
	}

	@Override
	public AgentConfig getConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(AgentConfig conf) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void addFunction(AgentFunctionConfig cellFunctionConfig) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeCellFunction(String cellFunctionName) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void takeDownCell() {
		// TODO Auto-generated method stub

	}

}
