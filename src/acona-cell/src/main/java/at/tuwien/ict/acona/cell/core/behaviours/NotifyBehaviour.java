package at.tuwien.ict.acona.cell.core.behaviours;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.datastructures.Datapackage;
import at.tuwien.ict.acona.cell.datastructures.DatapackageImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.datastructures.types.AconaService;
import at.tuwien.ict.acona.cell.storage.DataStorageSubscriberNotificator;
import at.tuwien.ict.acona.communicator.util.ACLUtils;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class NotifyBehaviour extends OneShotBehaviour {
	private List<String> subscribers = new LinkedList<String>();
	private Datapoint subscribedData = Datapoint.newDatapoint("");
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger log = LoggerFactory.getLogger(NotifyBehaviour.class);
	
	public NotifyBehaviour(List<String> subscribers, Datapoint subscribedData) {
		this.subscribers = subscribers;
		this.subscribedData = subscribedData;
	}
	
	@Override
	public void action() {
		log.debug("Start notify action for subscriber={}, data={}", this.subscribers, subscribedData);
		if (subscribers.isEmpty()==false && this.subscribedData.getAddress().equals("")==false && this.subscribedData!=null) {
			log.debug("Notify the following subscribers={}", this.subscribers);
			
			//Create send message without target
			ACLMessage notifyMessage = ACLUtils.convertToACL(Message.newMessage()
					.setContent(subscribedData.toJsonObject())
					.addReceivers(subscribers)
					.setService(AconaService.WRITE));
					
			notifyMessage.setPerformative(ACLMessage.REQUEST);
//			notifyMessage.setOntology(JsonMessage.SERVICEWRITE);
//			//Create message body
//			JsonObject writeBody = new JsonObject();
//			writeBody.addProperty(JsonMessage.DATAPOINTADDRESS, address);
//			writeBody.addProperty(JsonMessage.VALUE, this.subscribedData.get(address).getDefaultValue());
//			notifyMessage.setContent(writeBody.toString());
			
			//Get IDs of all agents from the list of subscribers
			//Special treatment if this cell is the subscriber. Then the activator shall be triggered instead
			
			//For each agent, create a message
//			for (String subscriber : subscribers) {
//				//if (subscriber.equals(myAgent.getLocalName())==false) {
//				notifyMessage.addReceiver(new AID(subscriber, AID.ISLOCALNAME));
//				//} else {
//				//log.warn("Activate activations");
//				//}
//			}
			
			//For each agent, send message
			myAgent.send(notifyMessage);
			
			//Reset
			//this.resetValues();
		} 
		
		//Block behavior at once
		//this.block();
		
	}
	
//	private void resetValues() {
//		subscribers = new LinkedList<String>();
//		address = "";
//		subscribedData = DatapackageImpl.newDatapackage();
//	}
//
//	@Override
//	public void notifySubscribers(List<String> subscribers, String address, Datapackage subscribedData) {
//		this.subscribers = subscribers;
//		this.address = address;
//		this.subscribedData = subscribedData;
//		
//		this.reset();
//	}

}
