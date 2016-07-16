package at.tuwien.ict.acona.cell.core.behaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaServiceType;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.DataStore;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREResponder;

public class AconaServiceResponseBehaviour extends OneShotBehaviour {
	private static final long serialVersionUID = 559482969288446134L;
	private static Logger log = LoggerFactory.getLogger(SubscribeDataServiceBehavior.class);
//	private int error = 0;
//	
//	Map<AconaService, Observable> behaviours = new HashMap<>();
//	
//	public AconaServiceResponseBehaviour(Agent a) {
//		super(a, AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST));
//	}
//	
//	protected void setupDefaultBehaviours() {
//		registerServiceBehaviour(AconaService.NONE, new AconaServiceCallback<CellImpl>() {
//			@Override protected String onSync(CellImpl agent, Message message) { log.warn("Synchroniouse none-service message received"); return ""; }
//			@Override protected String onAsync(CellImpl agent, Message message) { log.warn("Asynchroniouse none-service message received"); return ""; }
//		});
//		
//		
//	}
//	
//	@Override
//	public void setAgent(Agent arg0) {
//		if(!(arg0 instanceof CellImpl)) { throw new IllegalArgumentException("AconaServiceResponseBehaviour can only by added to classes that implement the CellImpl interface"); }
//		super.setAgent(arg0);
//	}
//
//	public void registerServiceBehaviour(AconaService service, AconaServiceCallback<CellImpl> behaviour) {
//		if(!behaviours.containsKey(service)) {
//			behaviours.put(service, new AconaServiceObservable<CellImpl>((CellImpl) getAgent()));
//		}
//		behaviours.get(service).addObserver(behaviour);
//	}
//	
//	protected Message extractAconaMessage(ACLMessage aclMessage) {
//		Message message = Message.toMessage(aclMessage.getContent());
//		
//		return message;
//	}
//	
//	protected Message onMessage(Message message) {
//		
//		
//		return Message.newMessage();
//	}
//	
//	protected boolean isError() {
//		return error != 0;
//	}
//	
//	protected void setError(int error) {
//		this.error = error;
//	}
//	
//	@Override
//	protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
//		Message responseContent = onMessage(extractAconaMessage(request));
//		
//		ACLMessage reply = request.createReply();
//		
//		if(!isError()) {
//			reply.setPerformative(ACLMessage.INFORM);
//		} else {
//			reply.setPerformative(ACLMessage.FAILURE);
//		}
//		
//		reply.setContent(responseContent.toString());
//		
//		return reply;
//	}

	protected boolean checkRequirements() {
		return true;
	}
	
	@Override
	public void action() {
		log.info("Request handled by AconaServiceResponseBehaviour");
		
		DataStore data = getDataStore();
		
		log.info("done");
	}
}