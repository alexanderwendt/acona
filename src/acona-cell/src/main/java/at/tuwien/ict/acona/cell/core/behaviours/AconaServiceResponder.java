package at.tuwien.ict.acona.cell.core.behaviours;

import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.core.service.AconaServiceInterface;
import at.tuwien.ict.acona.cell.datastructures.Message;
//import at.tuwien.ict.acona.cell.testing.GlobalLogger;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

public class AconaServiceResponder extends AchieveREResponder {
	private static final long serialVersionUID = 874230859531429805L;
	private final AconaServiceInterface<CellImpl> service;
	private int error = 0;
	
	public AconaServiceResponder(CellImpl agent, AconaServiceInterface<CellImpl> service) {
		super(agent, MessageTemplate.and(AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST), service.getMatchingTemplate()));
		this.service = service;
	}

	protected boolean isError() {
		return error != 0;
	}
	
	protected void setError(int error) {
		this.error = error;
	}
	
	@Override
	protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
		//GlobalLogger.log(getAgent(), request);
		
		Message aconaMessage = Message.toMessage(request.getContent());
		
		Message aconaResponse = service.perform((CellImpl)getAgent(), aconaMessage);
		
		ACLMessage response = request.createReply();
		
		response.setContent(aconaResponse.toString());
		response.setEncoding(request.getEncoding());
		
		if(service.getError() == 0) {
			response.setPerformative(ACLMessage.INFORM);
		} else {
			response.setPerformative(ACLMessage.FAILURE);
		}
		
		return response;
		
	}
}
