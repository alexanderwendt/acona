package at.tuwien.ict.acona.mq.cell.core;

import at.tuwien.ict.acona.mq.cell.communication.MqttCommunicator;
import at.tuwien.ict.acona.mq.cell.config.CellConfig;
import at.tuwien.ict.acona.mq.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.mq.cell.storage.DataStorage;

public class DummyCell implements Cell {

	private final String name;

	public DummyCell(String name) {
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
	public CellFunctionHandler getFunctionHandler() {
		return new CellFunctionHandlerImpl();
	}

	@Override
	public CellConfig getConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(CellConfig conf) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void addFunction(CellFunctionConfig cellFunctionConfig) throws Exception {
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
