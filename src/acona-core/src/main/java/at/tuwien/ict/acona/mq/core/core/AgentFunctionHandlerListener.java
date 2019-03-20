package at.tuwien.ict.acona.mq.core.core;

public interface AgentFunctionHandlerListener {
	public void notifyAddedFunction(String registeredFunction);

	public void notifyRemovedFunction(String registeredFunction);

	//public void notifyStateUpdate(String function, ServiceState state);

	public String getListenerFunction();
}
