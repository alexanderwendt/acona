package at.tuwien.ict.acona.cell.core;

import at.tuwien.ict.acona.cell.activator.ActivationHandler;
import at.tuwien.ict.acona.cell.storage.DataStorage;
import jade.core.behaviours.Behaviour;

public interface Cell {
	public DataStorage getDataStorage();
	public ActivationHandler getActivationHandler();
	public String getName();
	public void addBehaviour(Behaviour b);
	
}
