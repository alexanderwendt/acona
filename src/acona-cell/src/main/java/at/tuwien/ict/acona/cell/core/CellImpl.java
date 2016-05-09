package at.tuwien.ict.acona.cell.core;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.activator.ActivationHandler;
import at.tuwien.ict.acona.cell.activator.ActivationHandlerImpl;
import at.tuwien.ict.acona.cell.core.behaviours.NotifyBehavior;
import at.tuwien.ict.acona.cell.core.behaviours.ReadDataServiceBehavior;
import at.tuwien.ict.acona.cell.core.behaviours.SubscribeDataServiceBehavior;
import at.tuwien.ict.acona.cell.core.behaviours.UnsubscribeDataServiceBehavior;
import at.tuwien.ict.acona.cell.core.behaviours.WriteDataServiceBehavior;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.storage.DataStorage;
import at.tuwien.ict.acona.cell.storage.DataStorageImpl;
import at.tuwien.ict.acona.cell.storage.DataStorageSubscriberNotificator;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class CellImpl extends Agent implements Cell, DataStorageSubscriberNotificator {

	private final DataStorage dataStorage = new DataStorageImpl();
	private final ActivationHandler activationHandler = new ActivationHandlerImpl();
	
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
			 log.error("Cannot setup DFservice", fe);
		 }
		 
		 //Init datastorage
		 //NotifyBehavior notificatorBehavior = new NotifyBehavior();
		 //this.addBehaviour(notificatorBehavior);
		 this.dataStorage.init(this);
		 
		 //Init activation handler
		 //this.activationHandler.
		 
		 //Create behaviors
		 this.createBasicBehaviors();
		 
		 //Init internally
		 this.internalInit();
		 
		 log.trace("Cell {}> initialized", this.getName());
	}
	
	protected void internalInit() {
		//Setup activations and behaviors
		//Overwrite method with own init

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
		this.doDelete();
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
	public ActivationHandler getActivationHandler() {
		return activationHandler;
	}

	@Override
	public void notifySubscribers(List<String> subscribers, String caller, Datapoint subscribedData) {
		//Check inputs

		//Remove the caller from the subscibers to be notified. The system shall not notify itself, except internal data exchange has happened
		if (subscribers.contains(this.getName())) {
			log.trace("activate local behaviors");
			this.activationHandler.activateLocalBehaviors(subscribedData);
		}
		
		//Revove it from the list before sending to external application
		subscribers.remove(caller);
		
		this.addBehaviour(new NotifyBehavior(subscribers, subscribedData));
			
			
		//} else {
		//	log.warn("Notify subscribers erroneously called");
		//}

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getLocalName());
		builder.append("> dataStorage=");
		builder.append(dataStorage);
		//builder.append(", activationHandler=");
		//builder.append(activationHandler);
		return builder.toString();
	}
	
	
	
	
}
