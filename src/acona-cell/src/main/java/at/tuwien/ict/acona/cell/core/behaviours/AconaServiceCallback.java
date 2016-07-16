package at.tuwien.ict.acona.cell.core.behaviours;

import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.datastructures.Message;

public abstract class AconaServiceCallback <AGENT_TYPE extends CellImpl> implements Observer {
	private static final long serialVersionUID = -8927742908523511070L;
	
	abstract protected String onSync(CellImpl agent, Message message);
	abstract protected String onAsync(CellImpl agent, Message message);
	
	private static Logger log = LoggerFactory.getLogger(AconaServiceCallback.class);
	
	@Override
	public void update(Observable o, Object arg) {
		if(!(o instanceof AconaServiceObservable)) { throw new IllegalArgumentException("AconaSeriveCallback can only be triggered by AconaServiceObservables, given caller was " + o.toString() + " of type " + o.getClass().toString()); }
		if(!(arg instanceof Message)) { throw new IllegalArgumentException("AconaSeriveCallback should only be triggered when a new ACONA message was received, but the provided update argument was " + arg.toString() + " of type " + arg.getClass().toString()); }
		
		//TODO: Remove warning by checking or removing the generic type AGENT_TYPE
		AconaServiceObservable<AGENT_TYPE> observable = (AconaServiceObservable<AGENT_TYPE>)o;
		Message message = (Message)arg;
		
		switch(message.getMode()) {
		case SYNCHRONIZED:
			onSync(observable.getAgent(), message);
			break;
		case ASYNCHRONIZED:
			onAsync(observable.getAgent(), message);
			break;
		default:
			log.warn("Unknown message mode " + message.getMode() + " received - ignoring message:\n" + message.toString());
		}
	}
}
