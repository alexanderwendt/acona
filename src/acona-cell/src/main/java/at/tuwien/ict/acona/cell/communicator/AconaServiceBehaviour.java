package at.tuwien.ict.acona.cell.communicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import at.tuwien.ict.acona.cell.cellfunction.CellFunction;
import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.types.AconaServiceType;
import at.tuwien.ict.acona.cell.datastructures.util.GsonUtils;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;

public class AconaServiceBehaviour extends SimpleAchieveREResponder {

	public static final String METHODNAME = "methodname";
	public static final String PARAMETER = "parameter";
	public static final String CONFIG = "config";
	public static final String RESULT = "result";

	public static final String SENDER = "sender";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger log = LoggerFactory.getLogger(AconaServiceBehaviour.class);
	private final static Gson gson = new Gson();
	private final CellImpl cell;

	private Map<String, Datapoint> datapointMap;
	private String senderAgent;
	private final String serviceName;
	private String methodName;
	private final CellFunction service;

	public AconaServiceBehaviour(CellImpl caller, CellFunction service) {
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
		this.cell = caller;
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

		senderAgent = request.getSender().getLocalName();

		// Extract content
		try {
			// Extract datapoints into a datapointmap. Each datapoint has an
			// instruction for the function
			String content = request.getContent();
			JsonArray object = gson.fromJson(content, JsonArray.class);
			List<Datapoint> datapoints = GsonUtils.convertJsonArrayToDatapointList(object);
			this.datapointMap = new HashMap<String, Datapoint>();
			for (Datapoint dp : datapoints) {
				this.datapointMap.put(dp.getAddress(), dp);
			}

			// Add the caller
			//this.datapointMap.put(SENDER, Datapoint.newDatapoint(SENDER).setValue(request.getSender().getLocalName()));

			// switch (input.getAddress()) {
			// case METHODNAME:
			// this.methodName = input.getValueAsString();
			// break;
			// case PARAMETER:
			// this.parameter = input.getValue().getAsJsonObject();
			// this.parameter.addProperty(CALLER, senderAgent);
			// break;
			// case CONFIG:
			// this.config = input.getValue().getAsJsonObject();
			// break;
			// default:
			// throw new Exception("Unknown datapoint " + input.getAddress());
			// }
			// this.datapointList.add();

			// if (this.serviceName.equals(AconaServiceType.QUERY) == true) {
			// log.warn("Check if service is available");
			// // TODO: Implement this
			// // Todo: Check if input matches the description
			// // Todo: Check if service is running. If yes, create queue to
			// // wait until the run has finished and then set new data,
			// // optional
			//
			// throw new UnsupportedOperationException();
			//
			// }

			temp.setPerformative(ACLMessage.AGREE);
			log.info("Service={}>OK to execute.", serviceName);

		} catch (Exception fe) {
			log.error("Service={}>Received message with sender: {}, receiver={}, service={},\n content={}", request.getOntology(), request.getSender(),
					request.getAllIntendedReceiver(), request.getContent());
			log.error("Service={}>Error handling the action.", serviceName, fe);
			temp.setPerformative(ACLMessage.REFUSE);
			throw new RefuseException("check-failed");
		}
		// }

		return temp;
	}

	@Override
	public ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
		ACLMessage msg = request.createReply();
		msg.setOntology(AconaServiceType.NONE.toString());

		List<Datapoint> resultDatapoints = new ArrayList<Datapoint>();

		try {
			// Execute the service action
			resultDatapoints.addAll(this.executeServiceAction(this.service, this.datapointMap, request.getSender().getLocalName()));

			//resultDatapoints.add(result);

			// Set performative that all is ok
			msg.setPerformative(ACLMessage.INFORM);

		} catch (Exception e) {
			log.error("Service={}>Cannot process request", this.serviceName, e);
			msg.setPerformative(ACLMessage.FAILURE);

			// send error back
			resultDatapoints.add(Datapoint.newDatapoint(RESULT).setValue("ERROR"));
			// throw new FailureException("unexpected-error");
		}

		String serializedResult = gson.toJson(resultDatapoints);
		msg.setContent(serializedResult);

		log.debug("Service={}>Received result, receiver={}, content={}", msg.getOntology(), ((AID) msg.getAllReceiver().next()).getLocalName(), (msg.getContent().length() > 1000 ? msg.getContent().substring(0, 1000) : msg.getContent()));
		return msg;
	}

	protected List<Datapoint> executeServiceAction(CellFunction function, Map<String, Datapoint> parameter, String caller) {
		// List<Datapoint> readDatapoints;
		// String serializedDatapoints = "";

		List<Datapoint> result = new ArrayList<Datapoint>();
		// try {
		result = function.performOperation(parameter, caller);

		// } catch (Exception e) {
		// log.error("Cannot execute method={} with parameter={}", methodName,
		// parameter);

		// }

		return result;

		// switch (serviceType) {
		// case WRITE:
		// // For each datapoint, write it to the database
		// this.datapointList.forEach(dp -> {
		// this.cell.getDataStorage().write(dp, senderAgent);
		// });
		// break;
		// case READ:
		// // For each datapoint, write it to the database
		// readDatapoints = new ArrayList<Datapoint>();
		// this.datapointList.forEach(dp -> {
		// readDatapoints.add(this.cell.getDataStorage().read(dp.getAddress()));
		// });
		//
		// // serialize datapoints
		// JsonArray jsonarray = new JsonArray();
		// readDatapoints.forEach(dp -> {
		// jsonarray.add(dp.toJsonObject());
		// });
		// serializedDatapoints = jsonarray.toString();
		// msg.setContent(serializedDatapoints);
		// break;
		// case SUBSCRIBE:
		// readDatapoints = new ArrayList<Datapoint>();
		//
		// // For each datapoint, write it to the database
		// this.datapointList.forEach(dp -> {
		// log.info("Sender={}", new AID(msg.getReplyWith(),
		// AID.ISGUID).getLocalName());
		// this.cell.getDataStorage().subscribeDatapoint(dp.getAddress(),
		// senderAgent);
		// readDatapoints.add(this.cell.getDataStorage().read(dp.getAddress()));
		// });
		//
		// // serialize datapoints
		// serializedDatapoints = gson.toJson(readDatapoints);
		// msg.setContent(serializedDatapoints);
		// break;
		// case UNSUBSCRIBE:
		// // For each datapoint, write it to the database
		// this.datapointList.forEach(dp -> {
		// this.cell.getDataStorage().unsubscribeDatapoint(dp.getAddress(),
		// senderAgent);
		// });
		// break;
		// case QUERY:
		// // Execute the service and create a temporary subscription queue
		// // function to wait for answer
		// // put the input in a datapoint command
		// // wait for an answer on the return datapoint
		//
		// throw new UnsupportedOperationException();
		// // break;
		// default:
		// throw new Exception("Serive type not supported");
		//
		// }

	}
}
