package at.tuwien.ict.kore.cell.core;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.kore.cell.core.behaviors.ReadDataServiceBehavior;
import at.tuwien.ict.kore.cell.core.behaviors.SubscribeDataServiceBehavior;
import at.tuwien.ict.kore.cell.core.behaviors.NotifyBehavior;
import at.tuwien.ict.kore.cell.core.behaviors.UnsubscribeDataServiceBehavior;
import at.tuwien.ict.kore.cell.core.behaviors.WriteDataServiceBehavior;
import at.tuwien.ict.kore.cell.datastructures.Datapackage;
import at.tuwien.ict.kore.cell.storage.DataStorage;
import at.tuwien.ict.kore.cell.storage.DataStorageImpl;
import at.tuwien.ict.kore.cell.storage.DataStorageSubscriberNotificator;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class CellImpl extends Agent implements Cell, DataStorageSubscriberNotificator {

	private DataStorage dataStorage = new DataStorageImpl();
	
	/**
	 * Default serial version UID
	 */
	private static final long serialVersionUID = 1L;
	
	protected static Logger log = LoggerFactory.getLogger(CellImpl.class);

	
	/* (non-Javadoc)
	 * @see jade.core.Agent#setup()
	 */
	protected void setup() {
		 // Register the basic cell service in the yellow pages
		 DFAgentDescription dfDescriptionAgent = new DFAgentDescription();
		 dfDescriptionAgent.setName(getAID());
		 ServiceDescription sericeDescription = createServiceDescription();
		 dfDescriptionAgent.addServices(sericeDescription);
		 try {
			 DFService.register(this, dfDescriptionAgent);
		 } catch (FIPAException fe) {
			 fe.printStackTrace();
		 }
		 
		 //Init datastorage
		 //NotifyBehavior notificatorBehavior = new NotifyBehavior();
		 //this.addBehaviour(notificatorBehavior);
		 this.dataStorage.init(this);
		 
		 //Create behaviors
		 this.createBasicBehaviors();
	}
	
	private void createBasicBehaviors() {
		//Create readbehavior
		ReadDataServiceBehavior readDataService = new ReadDataServiceBehavior(this);
		this.addBehaviour(readDataService);
		//Create writebehavior
		WriteDataServiceBehavior writeDataService = new WriteDataServiceBehavior(this);
		this.addBehaviour(writeDataService);
		SubscribeDataServiceBehavior subscribeDataServiceBehavior = new SubscribeDataServiceBehavior(this);
		this.addBehaviour(subscribeDataServiceBehavior);
		UnsubscribeDataServiceBehavior unsubscribeDataServiceBehavior = new UnsubscribeDataServiceBehavior(this);
		this.addBehaviour(unsubscribeDataServiceBehavior);
	}
	
	private ServiceDescription createServiceDescription() {
		ServiceDescription sericeDescription = new ServiceDescription();
		sericeDescription.setType("Cell");
		sericeDescription.setName("BasicService");
		return sericeDescription;
	}
	
	/* (non-Javadoc)
	 * @see jade.core.Agent#takeDown()
	 */
	protected void takeDown() {
		 // Deregister from the yellow pages
		 try {
			 DFService.deregister(this);
		 }
		 catch (FIPAException fe) {
			 fe.printStackTrace();
		 }
		 	// Printout a dismissal message
		 	log.info("Cell agent" +getAID().getName()+ " is terminating.");
		 }

	
	@Override
	public DataStorage getDataStorage() {
		return this.dataStorage;
	}

	@Override
	public void notifySubscribers(List<String> subscribers, String address, Datapackage subscribedData) {
		//Check inputs
		//if (subscribers.isEmpty()==false && address.equals("")==false && subscribedData!=null) {
		this.addBehaviour(new NotifyBehavior(subscribers, address, subscribedData));
			
			
		//} else {
		//	log.warn("Notify subscribers erroneously called");
		//}
		
		
		
	}
	
	
	
	
}
