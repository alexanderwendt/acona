package _OLD_at.tuwien.ict.acona.cell.core.behaviours;

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
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;

@Deprecated
public class _OLD_WriteDataServiceBehavior extends SimpleAchieveREResponder {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger log = LoggerFactory.getLogger(_OLD_WriteDataServiceBehavior.class);
	private final static Gson gson = new Gson();
	private final CellImpl cell; 
	
	private List<Datapoint> datapointList;
	private String sender;
	
	public _OLD_WriteDataServiceBehavior(CellImpl caller) {
		//In the super class, it shall use the message template here
		super(caller, MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchOntology(AconaServiceType.WRITE.toString())));
		this.cell = caller;
		log.debug("Responder ready. Waiting for incoming WRITE request");
	}
	
	public ACLMessage prepareResponse(ACLMessage request) {
		log.debug("Received message={}", request);
		ACLMessage temp = request.createReply();
		//temp.setOntology(AconaServiceType.NONE.toString());
		temp.setOntology(AconaServiceType.NONE.toString());
		
		try { 
			//Extract datapoints
			String content = request.getContent();
			//Type listOfTestObject = new TypeToken<List<Datapoint>>(){}.getType();
			//String serializedDatapoints = gson.toJson(datapoints, listOfTestObject);
			JsonArray object = gson.fromJson(content, JsonArray.class);
			this.datapointList = new ArrayList<Datapoint>();
			object.forEach(e->{this.datapointList.add(Datapoint.toDatapoint((JsonObject)e));});
			//datapointList = gson.fromJson(content, listOfTestObject);
			//List<TestObject> list2 = gson.fromJson(s, listOfTestObject);
			sender = request.getSender().getLocalName();
			
			
			temp.setPerformative(ACLMessage.AGREE);
			log.info("OK to write");
		
		} catch (Exception fe){
			log.error("Error handling the WRITE action.", fe);
			temp.setPerformative(ACLMessage.REFUSE);
		}
		
		return temp;
	}
	
	public ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
		ACLMessage msg = request.createReply();
		msg.setOntology(AconaServiceType.NONE.toString());

		try {
			//For each datapoint, write it to the database
			this.datapointList.forEach(dp->{this.cell.getDataStorage().write(dp, sender);});
			
			msg.setPerformative(ACLMessage.INFORM);
				
		} catch (Exception e) {
			log.error("Cannot process request", e);
			msg.setPerformative(ACLMessage.FAILURE);
		}
		
		log.info("Message={}", msg);
		return msg;
	}
}
