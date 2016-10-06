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
import at.tuwien.ict.acona.cell.datastructures.types.AconaServiceType;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;

@Deprecated
public class _OLD_ReadDataServiceBehavior extends SimpleAchieveREResponder {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger log = LoggerFactory.getLogger(_OLD_ReadDataServiceBehavior.class);
	
	private final CellImpl cell; 
	private List<Datapoint> datapointList;
	private final static Gson gson = new Gson();
	
	public _OLD_ReadDataServiceBehavior(CellImpl caller) {
		//In the super class, it shall use the message template here
		super(caller, MessageTemplate.and(MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST), MessageTemplate.MatchOntology(AconaServiceType.READ.toString())));
		this.cell = caller;
		log.debug("Responder ready. Waiting for incoming READ request");
	}
	
	public ACLMessage prepareResponse(ACLMessage request) {
		log.debug("Received message={}", request);
		ACLMessage temp = request.createReply();
		temp.setOntology(AconaServiceType.NONE.toString());
		
		try { 
			//Extract datapoints
			String content = request.getContent();
			Type listOfTestObject = new TypeToken<List<Datapoint>>(){}.getType();
			//String serializedDatapoints = gson.toJson(datapoints, listOfTestObject);
			datapointList = gson.fromJson(content, listOfTestObject);
			//List<TestObject> list2 = gson.fromJson(s, listOfTestObject);
			
			
			temp.setPerformative(ACLMessage.AGREE);
			log.info("OK to read.");
		
		} catch (Exception fe){
			log.error("Error handling the READ action.", fe);
			temp.setPerformative(ACLMessage.REFUSE);
		}
		
		return temp;
	}
	
	public ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
		ACLMessage msg = request.createReply();
		List<Datapoint> readDatapoints = new ArrayList<Datapoint>();
		
		try {
			//For each datapoint, write it to the database
			this.datapointList.forEach(dp->{
				readDatapoints.add(this.cell.getDataStorage().read(dp.getAddress()));
			});
			
			//serialize datapoints
			String serializedDatapoints = gson.toJson(readDatapoints);
			msg.setContent(serializedDatapoints);
			msg.setPerformative(ACLMessage.INFORM);
				
		} catch (Exception e) {
			log.error("Cannot process request READ", e);
			msg.setPerformative(ACLMessage.FAILURE);
		}
		
		log.info("Message={}", msg);
		return msg;
	}

}
