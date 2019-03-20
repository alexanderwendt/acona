package at.tuwien.ict.acona.mq.core.core;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.mq.core.agentfunction.AgentCommunicatorFunctionImpl;
import at.tuwien.ict.acona.mq.core.agentfunction.basicfunctions.DataAccess;
import at.tuwien.ict.acona.mq.core.communication.MqttCommunicator;
import at.tuwien.ict.acona.mq.core.config.AgentConfig;
import at.tuwien.ict.acona.mq.core.config.AgentFunctionConfig;
import at.tuwien.ict.acona.mq.core.storage.DataStorage;
import at.tuwien.ict.acona.mq.core.storage.DataStorageImpl;

public class AgentImpl implements Cell, Runnable {

	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final DataStorage dataStorage = new DataStorageImpl();
	private final AgentFunctionHandler functionHandler = new AgentFunctionHandlerImpl();
	private MqttCommunicator comm;
	// Create the cellbuilder and apply activators
	private AgentBuilder builder = new AgentBuilder(this);

	private String agentName = "";

	private AgentNotificator notificator;

	// Genotype configuration
	protected AgentConfig conf;

	// private Thread t;

	@Override
	public void init(AgentConfig conf) throws Exception {

		// TODO: Delete all activators and all running behaviours by
		// unsubscription of the activators and deletion of the behaviours.

		// Set configuration
		this.conf = conf;
		this.agentName = conf.getName();

		// Add the basic function for communication
		AgentCommunicatorFunctionImpl commFunction = new AgentCommunicatorFunctionImpl();
		commFunction.init(AgentFunctionConfig.newConfig(agentName + "_" + "CommFunction", AgentCommunicatorFunctionImpl.class), this);

		// Get the communicator from the communicator cell function
		// Important: Only cell functions can have communicators
		this.comm = commFunction.getCommunicator();

		// Create notifications
		this.notificator = new AgentNotificator(this.comm);

		// Init datastorage
		this.dataStorage.init(this.notificator);

		// Initialize default functions
		this.initializeDefaultCellFunctions();

		// Initialize all functions
		this.getBuilder().initializeCellConfig(this.conf);

		// Initialize function handler
		this.functionHandler.init(this);

		this.internalInit();
	}

	/**
	 * Put all cell functions that shall be initialized in the cell by default.
	 * 
	 * @throws Exception
	 * 
	 */
	private void initializeDefaultCellFunctions() throws Exception {
		try {
			DataAccess dataAccessFunction = new DataAccess();
			dataAccessFunction.init(AgentFunctionConfig.newConfig("dataaccess", DataAccess.class), this);

			log.info("Basic database access functions initialized");
		} catch (Exception e) {
			log.error("Cannot initialize default cell functions", e);
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public AgentConfig getConfiguration() {
		// Deepcopy through serialization
		return this.conf;
	}

	private AgentBuilder getBuilder() {
		return builder;
	}

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see jade.core.Agent#setup()
//	 */
//	@Override
//	protected void setup() {
//		// Behaviours start first after setup
//		try {
//			// Start the controller for the cell that can be used outside of
//			// jade
//
//			// Create behaviors
//			// Create communication
//
//			// Init function handler
//			// this.functionHandler.init(this);
//
//			// Init subscription handler
//			// this.subscriptionHandler.init(functionHandler, this.getLocalName());
//
//			// Create notifications
//			this.notificator = new CellNotificator(this);
//
//			// Init datastorage
//			this.dataStorage.init(this.notificator);
//
//			// Register the basic cell service in the yellow pages
////			dfDescriptionAgentDescription = new DFAgentDescription();
////			dfDescriptionAgentDescription.setName(getAID());
////			try {
////				DFService.register(this, dfDescriptionAgentDescription);
////			} catch (FIPAException fe) {
////				log.error("Cannot setup DFservice", fe);
////			}
//
//			// FIXME: In the function init, subscriptions shall be performed.
//			// However, as long as initAsBehaviour is running no new behaviours
//			// can be added
//			// ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();
//			// Behaviour initBehaviour = new initAsBehaviour();
//			// this.addBehaviour(tbf.wrap(initBehaviour));
//			// FIXME: nonthreaded behaviours an optional celloption. There is a
//			// problem with the blocking behaviours. They block each other
//			// and then first end at timeout.
//
//		} catch (Exception e) {
//			log.error("Cannot setup cell agent", e);
//		}
//	}

//	private class initAsBehaviour extends OneShotBehaviour {
//
//		/**
//		 * 
//		 */
//		private static final long serialVersionUID = 1L;
//
//		@Override
//		public void action() {
//			CellConfig config = getArgument(0, CellConfig.class);
//			try {
//				init(config);
//				// controller = ((CellInspectorController)args[0]); //Mode=0:
//				// return message in return message, Mode=1: append
//				// returnmessage, mode=2: return incoming message
//				// log.debug("agent will use an inspector as controller");
//
//				// Init internally with local variables
//				internalInit();
//
//				log.debug("Cell {}> initialized", getName());
//			} catch (Exception e) {
//				log.error("Cannot init cell", e);
//			}
//
//		}
//
//	}

	@Override
	public void addFunction(AgentFunctionConfig cellFunctionConfig) throws Exception {
		try {
			this.getBuilder().createCellFunctionFromConfig(cellFunctionConfig.toJsonObject());
		} catch (Exception e) {
			log.error("Cannot create a cell function from the config", e);
			throw new Exception(e.getMessage());
		}

	}

//	@Override
//	public void registerService(String name) {
//		ServiceDescription sericeDescription = createServiceDescription();
//		sericeDescription.setName(name);
//		dfDescriptionAgentDescription.addServices(sericeDescription);
//	}

	protected void internalInit() throws Exception {
		// Setup activations and behaviors
		// Overwrite method with own init

	}

//	private ServiceDescription createServiceDescription() {
//		ServiceDescription sericeDescription = new ServiceDescription();
//		sericeDescription.setType("Cell");
//		sericeDescription.setName("BasicService");
//		return sericeDescription;
//	}

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see jade.core.Agent#takeDown()
//	 */
//	@Override
//	protected void takeDown() {
//		// Deregister from the yellow pages
////		try {
////			DFAgentDescription[] result = DFService.search(this, this.dfDescriptionAgentDescription);
////			if (result.length > 0) {
////				log.debug("Found agent in DFService. Agent={}", result[0].getName());
////				DFService.deregister(this);
////			}
////
////		} catch (FIPAException fe) {
////			log.error("Cannot deregister agent {} as it is not-registered", this.getLocalName(), fe);
////		}
//
//		this.notificator.shutDown();
//
//		this.getCommunicator().shutDown();
//
//		// this.doDelete();
//		// Printout a dismissal message
//		log.info("Cell agent" + this.getName() + " terminated.");
//	}

	@Override
	public void takeDownCell() {
		// Close all functions
		try {
			this.getFunctionHandler().getCellFunctionNames().forEach(name -> this.getFunctionHandler().getCellFunction(name).shutDownFunction());
		} catch (Exception e) {
			log.error("Cannot unregister functions", e);
		}
		
		// Close notificator
		try {
			this.notificator.shutDown();
		} catch (Exception e) {
			log.error("Cannot stop notifacator", e);
		}
		
		
		
		// this.doDelete();
		// Printout a dismissal message
		log.info("Cell" + this.getName() + " terminated.");
	}

	@Override
	public DataStorage getDataStorage() {
		return this.dataStorage;
	}

	@Override
	public AgentFunctionHandler getFunctionHandler() {
		return functionHandler;
	}

//	@Override
//	public SubscriptionHandler getSubscriptionHandler() {
//		return subscriptionHandler;
//	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getName());
		builder.append("> dataStorage=");
		builder.append(dataStorage);
		// builder.append(", activationHandler=");
		// builder.append(activationHandler);
		return builder.toString();
	}

//	/**
//	 * Convenience function to access the arguments provided to the agent via {@link Agent#getArguments()} in a typesafe manner. Note: the argument is cast from the Object stored in the object array
//	 * provided on agent generation
//	 * 
//	 * @param index
//	 *            The index of the request argument in the arguments list
//	 * @param type
//	 *            The type of the requested argument
//	 * @return The argument at the given {@code index}, cast to the given {@code type}
//	 */
//	protected <TYPE> TYPE getArgument(int index, Class<TYPE> type) {
//		if (getArguments() == null) {
//			throw new IndexOutOfBoundsException(
//					"Agent " + this.getName() + " has not been provided with an argument list. Argument at index "
//							+ index + " of type " + type.getName() + " is therefore out of bounds");
//		}
//		if (getArguments().length <= index) {
//			throw new IndexOutOfBoundsException("Argument list index " + index + " is out of bounds ("
//					+ getArguments().length + " arguments are available)");
//		}
//		if (type.isInstance(getArguments()[index])) {
//			return type.cast(getArguments()[index]);
//		} else {
//			throw new ClassCastException("Argument " + index + " in argument list " + getArguments()
//					+ " can not be cast to " + type.getName());
//		}
//	}

//	/**
//	 * Convenience function to access a List provided to the agent via {@link Agent#getArguments()} in a typesafe manner. The provided type is the type of the objects contained in the list. For example:
//	 * if argument #1 is of type {@code List<ACLMessage>} then the call would look like this: {@code List
//	 * <ACLMessage> result = getArgumentList(1, ACLMessage.class);} Note: each argument is cast from Object to the type stored in the list Also Note: this method can not provide additional type checking
//	 * for further containers within the list and does not support other container types than list
//	 * 
//	 * @param index
//	 *            The index of the request argument in the arguments list
//	 * @param type
//	 *            The content type of the requested list
//	 * @return The argument at the given {@code index}, cast to a list of the given {@code type}
//	 */
//	protected <TYPE> List<TYPE> getArgumentList(int index, Class<TYPE> type) {
//		List<TYPE> result = new ArrayList<>();
//
//		for (Object obj : getArgument(index, List.class)) {
//			if (!type.isInstance(obj)) {
//				throw new ClassCastException("Argument " + index + " in argument list " + getArguments()
//						+ " can not be cast to " + type.getName());
//			} else {
//				result.add(type.cast(obj));
//			}
//		}
//		return result;
//	}

//	/**
//	 * Shortcut method used to access the argument at the given {@code index} as string This method calls: {@code getArgument(index, String.class)}
//	 * 
//	 * @param index
//	 * @return
//	 */
//	protected String getArgument(int index) {
//		return getArgument(index, String.class);
//	}
//
//	/**
//	 * Shortcut method used to access the argument list at the given {@code index} as list of strings This method calls: {@code getArgumentList(index, String.class)}
//	 * 
//	 * @param index
//	 * @return
//	 */
//	protected List<String> getArgumentList(int index) {
//		return getArgumentList(index, String.class);
//	}

	@Override
	public MqttCommunicator getCommunicator() {
		return this.comm;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return this.agentName;
	}

	@Override
	public void removeCellFunction(String cellFunctionRootAddress) throws Exception {
		this.functionHandler.deregisterActivatorInstance(cellFunctionRootAddress);

	}

}
