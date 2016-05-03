package at.tuwien.ict.acona.cell.core;

import at.tuwien.ict.acona.cell.activator.ActivationHandler;
import at.tuwien.ict.acona.cell.storage.DataStorage;

public interface Cell {
	public DataStorage getDataStorage();
	public ActivationHandler getActivationHandler();
	public String getName();
	
}
