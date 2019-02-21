package at.tuwien.ict.acona.cell.communicator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunction;
import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcError;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;
import at.tuwien.ict.acona.cell.datastructures.types.AconaServiceType;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;

public class ExternalServiceBehaviour extends SimpleAchieveREResponder {

	public static final String METHODNAME = "methodname";
	public static final String PARAMETER = "parameter";
	public static final String CONFIG = "config";
	public static final String RESULT = "result";

	public static final String SENDER = "sender";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final static Logger log = LoggerFactory.getLogger(ExternalServiceBehaviour.class);

	private final String serviceName;
	private final CellFunction service;

	private JsonRpcRequest rpcrequest;

	public ExternalServiceBehaviour(CellImpl caller, CellFunction service) {
		// In the super class, it shall use the message template here
		// super(caller, MessageTemplate.and(
		// MessageTemplate.MatchProtocol((AconaServiceType.SUBSCRIBE ==
		// serviceType
		// ? FIPANames.InteractionProtocol.FIPA_SUBSCRIBE :
		// FIPANames.InteractionProtocol.FIPA_REQUEST)),
		// MessageTemplate.MatchOntology(serviceType.toString())));
		super(caller,
				MessageTemplate.and(MessageTemplate.MatchProtocol(service.getFunctionConfig().getResponderProtocol()),
						MessageTemplate.MatchOntology(service.getFunctionName())));
		// FIPANames.InteractionProtocol.FIPA_REQUEST)
		this.service = service;
		this.serviceName = service.getFunctionName();
		//this.cell = caller;
		log.debug("Service={}>Responder ready.", service.getFunctionName());
	}

	@Override
	public ACLMessage prepareResponse(ACLMessage request) throws RefuseException {
		// Log the incoming message
		log.debug("Service={}>Received message, sender={}, receiver={}, content={}", request.getOntology(),
				request.getSender().getLocalName(), ((AID) request.getAllReceiver().next()).getLocalName(),
				(request.getContent().length() > 1000 ? request.getContent().substring(0, 1000)
						: request.getContent()));
		ACLMessage temp = null;

		// if (this.serviceType.equals(AconaServiceType.WRITE)==false) { //If
		// write, no agree is necessary
		temp = request.createReply();
		temp.setOntology(AconaServiceType.NONE.toString());

		//senderAgent = request.getSender().getLocalName();

		// Extract content
		try {
			//Check if there is a problem with the conversion
			String stringRequest = request.getContent();
			rpcrequest = new JsonRpcRequest(stringRequest);
			temp.setPerformative(ACLMessage.AGREE);
			log.info("Service={}>OK to execute.", serviceName);
		} catch (Exception fe) {
			log.error("Service={}>Error. Request refused. Received message with \nsender: {}, \nreceiver={},\nservice={},\n content={}.", serviceName, request.getOntology(), request.getSender(), request.getAllIntendedReceiver(), request.getContent(), fe);
			temp.setPerformative(ACLMessage.REFUSE);
			//throw new RefuseException("check-failed");
		}

		return temp;
	}

	@Override
	public ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
		ACLMessage msg = request.createReply();
		msg.setOntology(AconaServiceType.NONE.toString());

		JsonRpcResponse resultDatapoint = null;//new JsonRpcResponse(new JsonRpcRequest(RESULTNOTIFICATIONMETHODNAME, true, new Object[0]), new JsonRpcError("ExecutionFailure", -1, "Unknown error", "unknown error"));

		String stringRequest = "";
		try {
			//Create a JsonRpc request
			//stringRequest = request.getContent();
			//log.debug("Request as String: {}", );
			//JsonRpcRequest rpcrequest = new JsonRpcRequest(stringRequest);
			//rpcrequest.setParameterAsList(0, Arrays.asList(datapointMap.values()));

			// Execute the service action
			resultDatapoint = this.executeServiceAction(this.service, rpcrequest, request.getSender().getLocalName());

			//resultDatapoints.add(result);

			// Set performative that all is ok
			msg.setPerformative(ACLMessage.INFORM);

			if (resultDatapoint.getError() != null) {
				//throw new Exception("Error executing function. Incoming message=" + stringRequest);
				log.error("Error executing function. Incoming message={}. Error={}", request, resultDatapoint.getError().getMessage());
			}

		} catch (Exception e) {
			log.error("Service={}>Cannot process request. Incoming request={}", this.serviceName, stringRequest, e);
			msg.setPerformative(ACLMessage.FAILURE);

			resultDatapoint = new JsonRpcResponse(new JsonRpcRequest("test", true, new Object[0]), new JsonRpcError("ExecutionFailure", -1, e.getMessage(), e.getMessage()));
			// send error back
			//resultDatapoint = .add(Datapoint.newDatapoint(RESULT).setValue("ERROR"));
			// throw new FailureException("unexpected-error");
		}

		String serializedResult = resultDatapoint.toJson().toString(); //gson.toJson(resultDatapoints);
		msg.setContent(serializedResult);

		log.debug("Service={}>Received result, receiver={}, content={}", msg.getOntology(), ((AID) msg.getAllReceiver().next()).getLocalName(), (msg.getContent().length() > 1000 ? msg.getContent().substring(0, 1000) : msg.getContent()));
		return msg;
	}

	protected JsonRpcResponse executeServiceAction(CellFunction function, JsonRpcRequest parameter, String caller) {
		JsonRpcResponse result = function.performOperation(parameter, caller);
		return result;
	}
}
