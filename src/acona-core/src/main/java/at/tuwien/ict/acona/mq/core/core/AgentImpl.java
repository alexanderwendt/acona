package at.tuwien.ict.acona.mq.core.core;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.mq.core.agentfunction.AgentCommunicatorFunctionImpl;
import at.tuwien.ict.acona.mq.core.agentfunction.basicfunctions.DataAccess;
import at.tuwien.ict.acona.mq.core.communication.MqttCommunicator;
import at.tuwien.ict.acona.mq.core.config.AgentConfig;
import at.tuwien.ict.acona.mq.core.config.FunctionConfig;
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
		commFunction.init(FunctionConfig.newConfig(agentName + "_" + "CommFunction", AgentCommunicatorFunctionImpl.class)
				.setHostData(conf.getHost(), conf.getUsername(), conf.getPassword()), this);

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
			//Initialize access to the database in the agent
			DataAccess dataAccessFunction = new DataAccess();
			dataAccessFunction.init(FunctionConfig.newConfig("dataaccess", DataAccess.class)
					.setHostData(conf.getHost(), conf.getUsername(), conf.getPassword()), this);
			log.info("Basic database access functions initialized");
			
			//Initialize remote adding of functions to the agent
			
			
			
			
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

	@Override
	public void addFunction(FunctionConfig cellFunctionConfig) throws Exception {
		try {
			this.getBuilder().createCellFunctionFromConfig(cellFunctionConfig.toJsonObject());
		} catch (Exception e) {
			log.error("Cannot create a cell function from the config", e);
			throw new Exception(e.getMessage());
		}

	}

	protected void internalInit() throws Exception {
		// Setup activations and behaviors
		// Overwrite method with own init

	}

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
