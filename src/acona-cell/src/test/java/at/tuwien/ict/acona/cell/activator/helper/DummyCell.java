package at.tuwien.ict.acona.cell.activator.helper;

import at.tuwien.ict.acona.cell.activator.ActivationHandler;
import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.storage.DataStorage;
import at.tuwien.ict.acona.cell.storage.DataStorageImpl;
import jade.core.behaviours.Behaviour;

public class DummyCell implements Cell {

	private DataStorage data = new DataStorageImpl().init(null);
	
	@Override
	public DataStorage getDataStorage() {
		return data;
	}

	@Override
	public ActivationHandler getActivationHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addBehaviour(Behaviour b) {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DummyCell [data=");
		builder.append(data);
		builder.append("]");
		return builder.toString();
	}

}
