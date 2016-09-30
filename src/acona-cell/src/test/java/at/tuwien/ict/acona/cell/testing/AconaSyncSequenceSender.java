package at.tuwien.ict.acona.cell.testing;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;

import at.tuwien.ict.acona.cell.core.CellImpl;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

@Deprecated
public class AconaSyncSequenceSender extends CellImpl {
	private static final long serialVersionUID = -3396424145035230755L;
	List<ACLMessage> messages = null;
	List<ACLMessage> originalMessages = new ArrayList<>();
	int timeout = 0;
	boolean infinite = false;
	
	@Override
	protected void setup() {
		// TODO Auto-generated method stub
		super.setup();
		
		messages = getArgumentList(1, ACLMessage.class);
		for(ACLMessage message : messages) {
			originalMessages.add((ACLMessage)message.clone());
		}
		timeout = getArgument(2, Integer.class);
		infinite = getArgument(3, Boolean.class);
		
		addBehaviour(new Behaviour() {
			private static final long serialVersionUID = 4192738743782463565L;
			
			@Override
			public boolean done() {
				return messages.isEmpty();
			}
			
			@Override
			public void action() {
				ACLMessage message = messages.remove(0);
				
				ACLMessage response = syncSend(message, timeout);
				
				GlobalLogger.log(getAgent(), response);
				
				if(messages.isEmpty() && infinite) {
					for(ACLMessage origMessage : originalMessages) {
						messages.add((ACLMessage)origMessage.clone());
					}
				}
			}
		});
	}
}
