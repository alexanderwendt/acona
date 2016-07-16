package at.tuwien.ict.acona.cell.core.behaviours;

import java.util.Observable;

import at.tuwien.ict.acona.cell.core.CellImpl;

public class AconaServiceObservable <AGENT_TYPE extends CellImpl> extends Observable {
	private final AGENT_TYPE agent;
	
	public AconaServiceObservable(AGENT_TYPE callerAgent) {
		if(callerAgent == null) { throw new IllegalArgumentException("AconaServiceObservable can only be created from a valid callerAgent instance, not from null"); }
		this.agent = callerAgent;
	}

	public AGENT_TYPE getAgent() {
		return agent;
	}
}
