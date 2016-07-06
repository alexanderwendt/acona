package at.tuwien.ict.acona.cell.core;

import java.rmi.server.UID;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.activator.ActivationHandler;
import at.tuwien.ict.acona.cell.activator.ActivationHandlerImpl;
import at.tuwien.ict.acona.cell.activator.Activator;
import at.tuwien.ict.acona.cell.activator.Condition;
import at.tuwien.ict.acona.cell.core.behaviours.NotifyBehaviour;
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
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;

public class CellImpl extends Agent implements CellInitialization, DataStorageSubscriberNotificator {

	private final DataStorage dataStorage = new DataStorageImpl();
	private final ActivationHandler activationHandler = new ActivationHandlerImpl();
	
	//Genotype configuration
	protected JsonObject conf;
	
	//phenotype functions
	private final Map<String, Condition> conditionMap = new HashMap<String, Condition>();
	private final Map<String, CellFunctionBehaviour> cellFunctionBehaviourMap = new HashMap<String, CellFunctionBehaviour>();
	private final Map<String, Activator> activatorMap = new HashMap<String, Activator>();
	
	/**
	 * Default serial version UID
	 */
	private static final long serialVersionUID = 1L;
	
	protected static Logger log = LoggerFactory.getLogger(CellImpl.class);

	
	@Override
	public void setConditionMap(Map<String, Condition> conditionMap) {
		this.conditionMap.putAll(conditionMap);
	}
	
	@Override
	public void setCellFunctionBehaviourMap(Map<String, CellFunctionBehaviour> cellFunctionBehaviourMap) {
		this.cellFunctionBehaviourMap.putAll(cellFunctionBehaviourMap);
		
	}

	@Override
	public void setActivatorMap(Map<String, Activator> activatorMap) {
		this.activatorMap.putAll(activatorMap);
	}
	
	@Override
	public Map<String, Condition> getConditionMap() {
		return this.conditionMap;
	}

	@Override
	public Map<String, CellFunctionBehaviour> getCellFunctionBehaviourMap() {
		return this.cellFunctionBehaviourMap;
	}

	@Override
	public Map<String, Activator> getActivatorMap() {
		return this.activatorMap;
	}
	
	@Override
	public void setupCellFunctionBehaviours(JsonObject conf) throws Exception {
		//clear the current behaviour
		conditionMap.clear();
		cellFunctionBehaviourMap.clear();
		activatorMap.clear();
		
		//TODO: Delete all activators and all running behaviours by unsubscription of the activators and deletion of the behaviours.
		
		//Set configuration
		this.conf = conf;
		
		//Create the cellbuilder and apply activators
		CellBuilder builder = new CellBuilder();
		builder.initializeCellConfig(conf, this);
	}
	
	@Override
	public JsonObject getConfiguration() {
		//Deepcopy through serialization
		return new Gson().fromJson(conf, JsonObject.class);
	}
	
	/* (non-Javadoc)
	 * @see jade.core.Agent#setup()
	 */
	protected void setup() {
		try {
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
			this.dataStorage.init(this);
			 
			//Create behaviors
			this.createBasicBehaviors();
			
			
			//Get passed arguments to start the system, in this case the system configuration
			Object[] args = this.getArguments();
			if (args!=null) {
				if (args[0] instanceof JsonObject) {
					JsonObject config = (JsonObject)args[0];
					this.setupCellFunctionBehaviours(config);
				}
				
				//controller = ((CellInspectorController)args[0]);	//Mode=0: return message in return message, Mode=1: append returnmessage, mode=2: return incoming message 
				
				log.debug("agent will use an inspector as controller");
			} else {
				throw new NullPointerException("No arguments found although necessary. Add a cell configuration. An empty cell configuration will also work");
			} 
			 
			//Init internally with local variables
			this.internalInit();
			 
			log.debug("Cell {}> initialized", this.getName());
		} catch (Exception e) {
			log.error("Cannot setup cell agent", e);
		}
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
	
	/**
	 * Helper method that provides unique ids (using {@link UID#toString()} of a newly created {@link UID}) to the {@link ACLMessage#setReplyWith(String)} and {@link ACLMessage#setConversationId(String)} methods
	 * of the given {@code message}. Unique ids on these fields are neccessary for the message to be processable by the JADE implementations of FIPA complient protocol handlers.
	 * 
	 * @param message
	 */
	protected void prepareSyncMessage(ACLMessage message) {
		message.setReplyWith(new UID().toString());
		message.setConversationId(new UID().toString());
		log.debug("ACL message after perpareSyncMessage:\n" + message.toString());
	}
	
	/**
	 * Prepares a template than can be used to identify a response to the message provided as {@code message}. The template attempty to match the protocol, conversation id and in-reply-to id
	 * 
	 * @param message The outgoing message that contains (at least) a protocol id, conversation id and reply-with id (which will turn into a in-reply-to id in the response)
	 * @return A {@link MessageTemplate} that can be used to match replies to the provided {@code message}
	 */
	protected MessageTemplate prepareMessageTemplate(ACLMessage message) {
		log.debug("Preparing message template for message with conversation id" + message.getConversationId());
		return MessageTemplate.and(MessageTemplate.and(
				MessageTemplate.MatchInReplyTo(message.getReplyWith()),
				MessageTemplate.MatchProtocol(message.getProtocol())),
				MessageTemplate.MatchConversationId(message.getConversationId()));
	}
	
	/**
	 * Sends the given {@code message} synchroniously. The method waits for a response (identified by protocol, conversation id and reply id) or the passage of {@code timeout} milliseconds before returning.
	 * This method tries to provide appliance to the FIPA Request protocol (but this is not thoroughly tested)
	 * 
	 * @param message The ACL Message to send
	 * @param timeout Maximum amount of miliseconds to wait for a reponse (use Integer.MAX_VALUE for almost-infinite waiting)
	 * @return The response of the receiver or null if a timeout occured 
	 */
	public ACLMessage syncSend(ACLMessage message, int timeout) {
		log.info("Sending synchronious message\n" + message.toString());
		
		ACLMessage response = null;
		
		prepareSyncMessage(message);
		
		send(message);
		
		log.info("Waiting for response");
		
		response = blockingReceive(prepareMessageTemplate(message), timeout);
		log.debug("Response received or timeout");
		
		log.info("Waiting completed");

		return response;
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
		//Notify local behaviours
		if (subscribers.contains(this.getName())) {
			log.trace("activate local behaviors");
			this.activationHandler.activateLocalBehaviours(subscribedData);
		}
		
		//Revove it from the list before sending to external application because this agent does not subscribe through external subscriptions
		subscribers.remove(this.getName());
		
		//Notify external agents that subscribe a value from this data storage
		if (subscribers.isEmpty()==false) {
			this.addBehaviour(new NotifyBehaviour(subscribers, subscribedData));
		}
		

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
