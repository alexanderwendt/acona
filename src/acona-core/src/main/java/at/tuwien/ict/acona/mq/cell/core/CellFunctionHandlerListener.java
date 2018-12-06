package at.tuwien.ict.acona.mq.cell.core;

public interface CellFunctionHandlerListener {
	public void notifyAddedFunction(String registeredFunction);

	public void notifyRemovedFunction(String registeredFunction);

	//public void notifyStateUpdate(String function, ServiceState state);

	public String getListenerFunction();
}
