package at.tuwien.ict.acona.mq.cell.core;

import at.tuwien.ict.acona.mq.cell.communication.MqttCommunicator;
import at.tuwien.ict.acona.mq.cell.storage.DataStorage;

public class CellGatewayImpl implements CellGateway {
	private CellImpl cell;

	@Override
	public void init(CellImpl cell) {
		this.cell = cell;
	}

	@Override
	public CellImpl getCell() {
		return this.cell;
	}

	@Override
	public MqttCommunicator getCommunicator() {
		return this.getCell().getCommunicator();
	}

	// @Override
	// public void setCustomAgentSetting(String key, String value) {
	// throw new UnsupportedOperationException();
	//
	// }

	@Override
	public DataStorage getDataStorage() {
		return this.getCell().getDataStorage();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(cell.getName());
		return builder.toString();
	}
}
