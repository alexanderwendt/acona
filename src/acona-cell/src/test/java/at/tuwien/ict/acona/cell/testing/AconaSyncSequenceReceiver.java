package at.tuwien.ict.acona.cell.testing;

import java.rmi.server.UID;

import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.core.behaviours.AconaServiceResponder;
import at.tuwien.ict.acona.cell.core.service.AconaServiceInterface;
import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaServiceType;
import jade.core.AID;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.lang.acl.MessageTemplate;

public class AconaSyncSequenceReceiver extends CellImpl {
	private static final long serialVersionUID = -76073396839466397L;
	
	@Override
	protected void createBasicBehaviors() {
		AconaServiceResponder queryResponder = new AconaServiceResponder(this, new AconaServiceInterface<CellImpl>() {
			private int error = 0;

			@Override
			public AconaServiceType getType() {
				return AconaServiceType.DEBUG;
			}

			@Override
			public MessageTemplate getMatchingTemplate() {
				return MessageTemplate.MatchEncoding("DEBUG");
			}

			@Override
			public Message perform(CellImpl agent, Message message) {
				return Message.toMessage(message.toString());
			}

			@Override
			public int getError() {
				return error;
			}
		});
		
		addBehaviour((new ThreadedBehaviourFactory()).wrap(queryResponder));
	}

}
