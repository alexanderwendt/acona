package _OLD_at.tuwien.ict.acona.cell.core.behaviours;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaServiceType;
import at.tuwien.ict.acona.cell.datastructures.types.AconaSync;
import at.tuwien.ict.acona.jadelauncher.util.ACLUtils;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;

@Deprecated
public class _OLD_UnsubscribeDataServiceBehavior extends SimpleAchieveREResponder {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger log = LoggerFactory.getLogger(_OLD_SubscribeDataServiceBehavior.class);
	
	private final CellImpl cell; 
	private List<Datapoint> datapointList;
	private final static Gson gson = new Gson();
	
	public _OLD_UnsubscribeDataServiceBehavior(CellImpl caller) {
		//In the super class, it shall use the message template here
		super(caller, MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchOntology(AconaServiceType.UNSUBSCRIBE.toString())));
		this.cell = caller;
		log.debug("Responder ready. Waiting for incoming UNSUBSCRIBE request");
	}
	
	public ACLMessage prepareResponse(ACLMessage request) {
		log.debug("Received message={}", request);
		ACLMessage temp = request.createReply();
		
		try { 
			//Extract datapoints
			String content = request.getContent();
			Type listOfTestObject = new TypeToken<List<Datapoint>>(){}.getType();
			//String serializedDatapoints = gson.toJson(datapoints, listOfTestObject);
			datapointList = gson.fromJson(content, listOfTestObject);
			//List<TestObject> list2 = gson.fromJson(s, listOfTestObject);
			
			temp.setPerformative(ACLMessage.AGREE);
			log.info("OK to unsubscribe.");
		
		} catch (Exception fe){
			log.error("Error handling the UNSUBSCRIBE action.", fe);
			temp.setPerformative(ACLMessage.REFUSE);
		}
		
		return temp;
	}
	
	public ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
		ACLMessage msg = request.createReply();
		
		try {
			//For each datapoint, write it to the database
			this.datapointList.forEach(dp->{
				this.cell.getDataStorage().unsubscribeDatapoint(dp.getAddress(), msg.getSender().getLocalName());
			});
			
			msg.setPerformative(ACLMessage.INFORM);
				
		} catch (Exception e) {
			log.error("Cannot process request UNSUBSCRIBE", e);
			msg.setPerformative(ACLMessage.FAILURE);
		}
		
		log.info("Message={}", msg);
		return msg;
	}

}
