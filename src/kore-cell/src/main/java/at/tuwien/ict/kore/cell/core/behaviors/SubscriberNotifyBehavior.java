package at.tuwien.ict.kore.cell.core.behaviors;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.kore.cell.datastructures.Datapackage;
import at.tuwien.ict.kore.cell.datastructures.DatapackageImpl;
import at.tuwien.ict.kore.cell.storage.DataStorageSubscriberNotificator;
import jade.core.behaviours.CyclicBehaviour;

public class SubscriberNotifyBehavior extends CyclicBehaviour implements DataStorageSubscriberNotificator {
	private List<String> subscribers = new LinkedList<String>();
	private String address = "";
	private Datapackage subscribedData = DatapackageImpl.newDatapackage();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger log = LoggerFactory.getLogger(SubscriberNotifyBehavior.class);
	
	@Override
	public void action() {
		log.debug("Start notify action");
		if (subscribers.isEmpty()==false && this.address.equals("")==false && this.subscribedData!=null) {
			log.debug("Notify the following subscribers={}", this.subscribers);
			
			//TODO: Fill with content
			
			//Create send message without target
			
			//Get IDs of all agents from the list of subscribers
			
			//For each agent, create a message
			
			//For each agent, send message
			
		} else {
			this.block();
		}
	}

	@Override
	public void notifySubscribers(List<String> subscribers, String address, Datapackage subscribedData) {
		this.subscribers = subscribers;
		this.address = address;
		this.subscribedData = subscribedData;
		
		this.action();
	}

}
