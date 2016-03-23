package at.tuwien.ict.acona.communicator.core;

import at.tuwien.ict.acona.cell.datastructures.Message;

public interface ListenerModule {
	public void updateValue(Message message);
}
