package at.tuwien.ict.acona.cell.communicator;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.types.AconaServiceType;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;

public class AconaServiceBehaviour extends SimpleAchieveREResponder {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger log = LoggerFactory.getLogger(AconaServiceBehaviour.class);
	private final static Gson gson = new Gson();
	private final CellImpl cell;

	private List<Datapoint> datapointList;
	private String sender;
	private final AconaServiceType serviceType;

	public AconaServiceBehaviour(CellImpl caller, AconaServiceType serviceType) {
		// In the super class, it shall use the message template here
		super(caller, MessageTemplate.and(
				MessageTemplate.MatchProtocol((AconaServiceType.SUBSCRIBE == serviceType ? FIPANames.InteractionProtocol.FIPA_SUBSCRIBE : FIPANames.InteractionProtocol.FIPA_REQUEST)),
				MessageTemplate.MatchOntology(serviceType.toString())));
		// FIPANames.InteractionProtocol.FIPA_REQUEST)
		this.serviceType = serviceType;
		this.cell = caller;
		log.debug("Responder ready. Waiting for incoming {} request", serviceType);
	}

	@Override
	public ACLMessage prepareResponse(ACLMessage request) throws RefuseException {
		log.debug("Received message={}", request);
		ACLMessage temp = null;

		// if (this.serviceType.equals(AconaServiceType.WRITE)==false) { //If
		// write, no agree is necessary
		temp = request.createReply();
		// temp.setOntology(AconaServiceType.NONE.toString());
		temp.setOntology(AconaServiceType.NONE.toString());

		try {
			// Extract datapoints
			String content = request.getContent();
			JsonArray object = gson.fromJson(content, JsonArray.class);
			this.datapointList = new ArrayList<Datapoint>();
			object.forEach(e -> {
				this.datapointList.add(Datapoint.toDatapoint((JsonObject) e));
			});

			if (this.serviceType.equals(AconaServiceType.QUERY) == true) {
				log.warn("Check if service is available");
				// TODO: Implement this
				//Todo: Check if input matches the description
				//Todo: Check if service is running. If yes, create queue to wait until the run has finished and then set new data, optional

				throw new UnsupportedOperationException();

			}

			sender = request.getSender().getLocalName();
			temp.setPerformative(ACLMessage.AGREE);
			log.info("OK to execute service {}", serviceType);

		} catch (Exception fe) {
			log.error("Received message with sender: {}, receiver={}, service={},\n content={}", request.getSender(), request.getAllIntendedReceiver(), request.getOntology(), request.getContent());
			log.error("Error handling the {} action.", serviceType, fe);
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

		try {
			// Execute the service action
			this.executeServiceAction(this.serviceType, msg);

			// Set performative that all is ok
			msg.setPerformative(ACLMessage.INFORM);

		} catch (Exception e) {
			log.error("Cannot process request", e);
			msg.setPerformative(ACLMessage.FAILURE);
			throw new FailureException("unexpected-error");
		}

		log.info("Message={}", msg);
		return msg;
	}

	protected void executeServiceAction(AconaServiceType serviceType, ACLMessage msg) throws Exception {
		List<Datapoint> readDatapoints;
		String serializedDatapoints = "";
		switch (serviceType) {
		case WRITE:
			// For each datapoint, write it to the database
			this.datapointList.forEach(dp -> {
				this.cell.getDataStorage().write(dp, sender);
			});
			break;
		case READ:
			// For each datapoint, write it to the database
			readDatapoints = new ArrayList<Datapoint>();
			this.datapointList.forEach(dp -> {
				readDatapoints.add(this.cell.getDataStorage().read(dp.getAddress()));
			});

			// serialize datapoints
			JsonArray jsonarray = new JsonArray();
			readDatapoints.forEach(dp -> {
				jsonarray.add(dp.toJsonObject());
			});
			serializedDatapoints = jsonarray.toString();
			msg.setContent(serializedDatapoints);
			break;
		case SUBSCRIBE:
			readDatapoints = new ArrayList<Datapoint>();

			// For each datapoint, write it to the database
			this.datapointList.forEach(dp -> {
				log.info("Sender={}", new AID(msg.getReplyWith(), AID.ISGUID).getLocalName());
				this.cell.getDataStorage().subscribeDatapoint(dp.getAddress(), sender);
				readDatapoints.add(this.cell.getDataStorage().read(dp.getAddress()));
			});

			// serialize datapoints
			serializedDatapoints = gson.toJson(readDatapoints);
			msg.setContent(serializedDatapoints);
			break;
		case UNSUBSCRIBE:
			// For each datapoint, write it to the database
			this.datapointList.forEach(dp -> {
				this.cell.getDataStorage().unsubscribeDatapoint(dp.getAddress(), sender);
			});
			break;
		case QUERY:
			//Execute the service and create a temporary subscription queue function to wait for answer

			throw new UnsupportedOperationException();
			// break;
		default:
			throw new Exception("Serive type not supported");

		}

	}
}
