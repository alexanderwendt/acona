package at.tuwien.ict.acona.cell.core;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.communicator.BasicServiceCommunicator;
import at.tuwien.ict.acona.cell.communicator.CellFunctionHandler;
import at.tuwien.ict.acona.cell.communicator.CellFunctionHandlerImpl;
import at.tuwien.ict.acona.cell.communicator.CommunicatorImpl;
import at.tuwien.ict.acona.cell.communicator.SubscriptionHandler;
import at.tuwien.ict.acona.cell.communicator.SubscriptionHandlerImpl;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.storage.DataStorage;
import at.tuwien.ict.acona.cell.storage.DataStorageImpl;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class CellImpl extends Agent implements CellInitialization {

	private final DataStorage dataStorage = new DataStorageImpl();
	private CellNotificator notificator;
	private final CellFunctionHandler functionHandler = new CellFunctionHandlerImpl();
	private final SubscriptionHandler subscriptionHandler = new SubscriptionHandlerImpl();
	private CommunicatorImpl comm;
	private CellGateway controller;
	private DFAgentDescription dfDescriptionAgentDescription;
	// Create the cellbuilder and apply activators
	private CellBuilder builder = new CellBuilder();

	// Genotype configuration
	protected CellConfig conf;

	// phenotype functions
	// private final Map<String, Condition> conditionMap = new HashMap<String,
	// Condition>();
	// private final Map<String, CellFunction> cellFunctionBehaviourMap = new
	// HashMap<String, CellFunction>();
	// private final Map<String, CellFunction> cellFunctionMap = new
	// HashMap<String, CellFunction>();

	/**
	 * Default serial version UID
	 */
	private static final long serialVersionUID = 1L;

	protected static Logger log = LoggerFactory.getLogger(CellImpl.class);

	@Override
	public void setupCellFunctions(CellConfig conf) throws Exception {

		// TODO: Delete all activators and all running behaviours by
		// unsubscription of the activators and deletion of the behaviours.

		// Set configuration
		this.conf = conf;

		builder.initializeCellConfig(this.conf, this);
	}

	@Override
	public CellConfig getConfiguration() {
		// Deepcopy through serialization
		return this.conf;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jade.core.Agent#setup()
	 */
	@Override
	protected void setup() {
		// Behaviours start first after setup
		try {
			// Start the controller for the cell that can be used outside of
			// jade
			Object[] args = this.getArguments();
			if (args != null && args.length > 1) {
				controller = this.getArgument(1, CellGatewayImpl.class);// ((CellGatewayImpl)args[1]);
																		// //Mode=0:
																		// return
																		// message
																		// in
																		// return
																		// message,
																		// Mode=1:
																		// append
																		// returnmessage,
																		// mode=2:
																		// return
																		// incoming
																		// message
																		// log.debug("agent will use an inspector as controller");
			} else {
				throw new NullPointerException("No arguments found although necessary. Add controller to argument 1");
			}

			this.controller.init(this);

			// Create behaviors
			// Create communication
			this.comm = new CommunicatorImpl(this);

			// Init function handler
			this.functionHandler.init(this);

			// Init subscription handler
			this.subscriptionHandler.init(functionHandler, this.getLocalName());

			// Create notifications
			this.notificator = new CellNotificator(this);

			// Init datastorage
			this.dataStorage.init(this.notificator);

			// Register the basic cell service in the yellow pages
			dfDescriptionAgentDescription = new DFAgentDescription();
			dfDescriptionAgentDescription.setName(getAID());
			try {
				DFService.register(this, dfDescriptionAgentDescription);
			} catch (FIPAException fe) {
				log.error("Cannot setup DFservice", fe);
			}

			// FIXME: In the function init, subscriptions shall be performed.
			// However, as long as initAsBehaviour is running no new behaviours
			// can be added
			ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory();
			Behaviour initBehaviour = new initAsBehaviour();
			this.addBehaviour(tbf.wrap(initBehaviour));
			// FIXME: nonthreaded behaviours an optional celloption. There is a
			// problem with the blocking behaviours. They block each other
			// and then first end at timeout.

		} catch (Exception e) {
			log.error("Cannot setup cell agent", e);
		}
	}

	private class initAsBehaviour extends OneShotBehaviour {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void action() {
			CellConfig config = getArgument(0, CellConfig.class);
			try {
				setupCellFunctions(config);
				// controller = ((CellInspectorController)args[0]); //Mode=0:
				// return message in return message, Mode=1: append
				// returnmessage, mode=2: return incoming message
				// log.debug("agent will use an inspector as controller");

				// Init internally with local variables
				internalInit();

				log.debug("Cell {}> initialized", getName());
			} catch (Exception e) {
				log.error("Cannot init cell", e);
			}

		}

	}

	@Override
	public void registerService(String name) {
		ServiceDescription sericeDescription = createServiceDescription();
		sericeDescription.setName(name);
		dfDescriptionAgentDescription.addServices(sericeDescription);
	}

	protected void internalInit() throws Exception {
		// Setup activations and behaviors
		// Overwrite method with own init

	}

	private ServiceDescription createServiceDescription() {
		ServiceDescription sericeDescription = new ServiceDescription();
		sericeDescription.setType("Cell");
		sericeDescription.setName("BasicService");
		return sericeDescription;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jade.core.Agent#takeDown()
	 */
	@Override
	protected void takeDown() {
		this.notificator.shutDown();

		this.getCommunicator().shutDown();

		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			log.error("Cannot deregister agent", fe);
		}

		this.doDelete();
		// Printout a dismissal message
		log.info("Cell agent" + getAID().getName() + " terminated.");
	}

	@Override
	public void takeDownCell() {
		this.takeDown();
	}

	@Override
	public DataStorage getDataStorage() {
		return this.dataStorage;
	}

	@Override
	public CellFunctionHandler getFunctionHandler() {
		return functionHandler;
	}

	@Override
	public SubscriptionHandler getSubscriptionHandler() {
		return subscriptionHandler;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getLocalName());
		builder.append("> dataStorage=");
		builder.append(dataStorage);
		// builder.append(", activationHandler=");
		// builder.append(activationHandler);
		return builder.toString();
	}

	/**
	 * Convenience function to access the arguments provided to the agent via {@link Agent#getArguments()} in a typesafe manner. Note: the argument is cast from the Object stored in the object array
	 * provided on agent generation
	 * 
	 * @param index
	 *            The index of the request argument in the arguments list
	 * @param type
	 *            The type of the requested argument
	 * @return The argument at the given {@code index}, cast to the given {@code type}
	 */
	protected <TYPE> TYPE getArgument(int index, Class<TYPE> type) {
		if (getArguments() == null) {
			throw new IndexOutOfBoundsException(
					"Agent " + this.getName() + " has not been provided with an argument list. Argument at index "
							+ index + " of type " + type.getName() + " is therefore out of bounds");
		}
		if (getArguments().length <= index) {
			throw new IndexOutOfBoundsException("Argument list index " + index + " is out of bounds ("
					+ getArguments().length + " arguments are available)");
		}
		if (type.isInstance(getArguments()[index])) {
			return type.cast(getArguments()[index]);
		} else {
			throw new ClassCastException("Argument " + index + " in argument list " + getArguments()
					+ " can not be cast to " + type.getName());
		}
	}

	/**
	 * Convenience function to access a List provided to the agent via {@link Agent#getArguments()} in a typesafe manner. The provided type is the type of the objects contained in the list. For example:
	 * if argument #1 is of type {@code List<ACLMessage>} then the call would look like this: {@code List
	 * <ACLMessage> result = getArgumentList(1, ACLMessage.class);} Note: each argument is cast from Object to the type stored in the list Also Note: this method can not provide additional type checking
	 * for further containers within the list and does not support other container types than list
	 * 
	 * @param index
	 *            The index of the request argument in the arguments list
	 * @param type
	 *            The content type of the requested list
	 * @return The argument at the given {@code index}, cast to a list of the given {@code type}
	 */
	protected <TYPE> List<TYPE> getArgumentList(int index, Class<TYPE> type) {
		List<TYPE> result = new ArrayList<>();

		for (Object obj : getArgument(index, List.class)) {
			if (!type.isInstance(obj)) {
				throw new ClassCastException("Argument " + index + " in argument list " + getArguments()
						+ " can not be cast to " + type.getName());
			} else {
				result.add(type.cast(obj));
			}
		}
		return result;
	}

	/**
	 * Shortcut method used to access the argument at the given {@code index} as string This method calls: {@code getArgument(index, String.class)}
	 * 
	 * @param index
	 * @return
	 */
	protected String getArgument(int index) {
		return getArgument(index, String.class);
	}

	/**
	 * Shortcut method used to access the argument list at the given {@code index} as list of strings This method calls: {@code getArgumentList(index, String.class)}
	 * 
	 * @param index
	 * @return
	 */
	protected List<String> getArgumentList(int index) {
		return getArgumentList(index, String.class);
	}

	@Override
	public BasicServiceCommunicator getCommunicator() {
		return this.comm;
	}

}
