package at.tuwien.ict.acona.jadelauncher.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.SystemConfig;
import at.tuwien.ict.acona.cell.core.CellGateway;
import at.tuwien.ict.acona.cell.core.CellGatewayImpl;
import at.tuwien.ict.acona.framework.interfaces.KoreExternalController;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

/**
 * This is a wrapper class for all types of jade initialization. it starts
 * agents and containers
 * 
 * @author wendt
 */
public class KoreExternalControllerImpl implements KoreExternalController {

	private static Logger log = LoggerFactory.getLogger(KoreExternalControllerImpl.class);
	/**
	 * Agent tools
	 */
	private final JadeContainerUtil communicatorUtil = new JadeContainerUtil();
	/**
	 * Map with container name and ContainerController
	 */
	private final Map<String, ContainerController> agentContainerMap = new ConcurrentHashMap<>();
	/**
	 * Map with all agent controllers from JADE
	 */
	private final Map<String, AgentController> agentControllerMap = new ConcurrentHashMap<>();
	private final Map<String, CellGatewayImpl> externalAgentControllerMap = new ConcurrentHashMap<>();

	private String topController = "";

	private final Map<String, CellGateway> controllerAgents = new ConcurrentHashMap<>();
	private final Map<String, CellGateway> serviceAgents = new ConcurrentHashMap<>();
	private final Map<String, CellGateway> memoryAgents = new ConcurrentHashMap<>();

	private String defaultContainer = "";

	private boolean mainContainerExists = false;

	// Make singleton
	private static KoreExternalControllerImpl instance = null;

	private KoreExternalControllerImpl() {

	}

	/**
	 * Get the Acona launcher
	 * 
	 * @return
	 */
	public static KoreExternalControllerImpl getLauncher() {
		if (instance == null) {
			instance = new KoreExternalControllerImpl();
		}

		return instance;
	}

	// === Container methods ===//

	/**
	 * Add new agent container
	 * 
	 * @param name
	 * @param agentContainer
	 */
	public void addAgentContainer(String name, ContainerController agentContainer) {
		this.agentContainerMap.put(name, agentContainer);
	}

	/**
	 * Get container controller
	 * 
	 * @param name
	 * @return
	 */
	public ContainerController getContainerController(String name) {
		return this.agentContainerMap.get(name);
	}

	/**
	 * Create a main container
	 * 
	 * @param host
	 * @param port
	 * @param name
	 * @throws JadeException
	 */
	public void createMainContainer(String host, int port, String name) throws JadeException {
		ContainerController mainController = communicatorUtil.createMainJADEContainer(host, port, name);
		this.addAgentContainer(name, mainController);

		// Set default container name if no container has been set
		this.defaultContainer = name;

		// Set that a main container exists
		this.mainContainerExists = true;
	}

	/**
	 * Create the visual debug user interface from JADE
	 * 
	 * @throws Exception
	 */
	public void createDebugUserInterface() throws Exception {
		this.communicatorUtil.createRMAInContainer(getDefaultContainerController());
	}

	/**
	 * Create a subcontainer
	 * 
	 * @param host
	 * @param port
	 * @param name
	 * @throws Exception
	 */
	public void createSubContainer(String host, int port, String name) throws Exception {
		if (this.mainContainerExists == false) {
			throw new Exception("No main controller exists");
		}

		ContainerController controller = this.communicatorUtil.createAgentContainer(host, port, name);
		this.addAgentContainer(name, controller);
	}

	/**
	 * Set default container
	 * 
	 * @param name
	 * @throws Exception
	 */
	public void setDefaultAgentContainer(String name) throws Exception {
		if (this.agentContainerMap.containsKey(name) == true) {
			this.defaultContainer = name;
		} else {
			throw new Exception("No container with the name " + name + " exists");
		}
	}

	/**
	 * Get the default container controller
	 * 
	 * @return
	 */
	public ContainerController getDefaultContainerController() {
		return this.agentContainerMap.get(defaultContainer);
	}

	/**
	 * Create the main container system for ACONA through JADE
	 */
	public void startDefaultSystem() {
		try {
			// Create container
			log.debug("Create or get main container");
			this.createMainContainer("localhost", 1099, "MainContainer");
			// mainContainerController =
			// this.util.createMainJADEContainer("localhost", 1099,
			// "MainContainer");

			log.debug("Create subcontainer");
			this.createSubContainer("localhost", 1099, "Subcontainer");

			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {

				}
			}

		} catch (Exception e) {
			log.error("Cannot initialize test environment", e);
		}
	}

	/**
	 * Stop the Acona system including JADE
	 */
	public void stopSystem() {
		synchronized (this) {
			try {
				this.wait(2000);
			} catch (InterruptedException e) {

			}
		}

		Runtime runtime = Runtime.instance();
		runtime.shutDown();
		synchronized (this) {
			try {
				this.wait(2000);
			} catch (InterruptedException e) {

			}
		}
	}

	// === Agent methods ===//

	/**
	 * Create an agent from a cell config
	 * 
	 * @param cellConfig
	 * @return
	 * @throws Exception
	 */
	public synchronized CellGatewayImpl createAgent(CellConfig cellConfig) throws Exception {
		// Create the object
		CellGatewayImpl externalController = new CellGatewayImpl();

		// Create the object
		Object[] args = new Object[2];
		args[0] = cellConfig;
		args[1] = externalController;

		AgentController agentController = this.communicatorUtil.createAgent(cellConfig.getName(),
				cellConfig.getClassToInvoke(), args, this.getContainerController(defaultContainer));
		this.agentControllerMap.put(cellConfig.getName(), agentController);
		this.externalAgentControllerMap.put(cellConfig.getName(), externalController);

		log.debug("Agent state={}", agentController.getState());

		return externalController;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.tuwien.ict.acona.framework.interfaces.KoreExternalController#
	 * executeUserInput(java.lang.String, java.lang.String)
	 */
	@Override
	public synchronized void executeUserInput(String command, String parameter) {
		throw new UnsupportedOperationException();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.tuwien.ict.acona.framework.interfaces.KoreExternalController#init(com.
	 * google.gson.JsonObject)
	 */
	@Override
	public KoreExternalController init(JsonObject config) throws Exception {
		return this.init(SystemConfig.newConfig(config));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.tuwien.ict.acona.framework.interfaces.KoreExternalController#init(java.
	 * lang.String)
	 */
	@Override
	public synchronized KoreExternalController init(String absolutefilePath) {
		JsonReader reader;
		try {
			reader = new JsonReader(new FileReader(absolutefilePath));
			JsonObject data = new Gson().fromJson(reader, JsonObject.class);

			this.init(data);
		} catch (FileNotFoundException e) {
			log.error("Cannot open file", e);
		} catch (Exception e) {
			log.error("Cannot load config", e);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.tuwien.ict.acona.framework.interfaces.KoreExternalController#getAgent(java
	 * .lang.String)
	 */
	@Override
	public synchronized CellGateway getAgent(String localName) {
		return externalAgentControllerMap.get(localName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.tuwien.ict.acona.framework.interfaces.KoreExternalController#
	 * getTopController()
	 */
	@Override
	public CellGateway getTopController() {
		return this.getControllerAgent(this.topController);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.tuwien.ict.acona.framework.interfaces.KoreExternalController#init(at.
	 * tuwien.ict.acona.cell.config.SystemConfig)
	 */
	@Override
	public synchronized KoreExternalController init(SystemConfig config) {

		// Set top controller
		this.setTopController(config.getTopController());

		// Init memory agents
		config.getMemories().forEach(agentConfig -> {
			try {
				CellGateway agent = this.createAgent(agentConfig);
				this.memoryAgents.put(agentConfig.getName(), agent);
			} catch (Exception e) {
				log.error("Cannot create agent={} from config={}", agentConfig.getName(), agentConfig);
			}
		});

		// Init service agents
		config.getServices().forEach(agentConfig -> {
			try {
				CellGateway agent = this.createAgent(agentConfig);
				this.serviceAgents.put(agentConfig.getName(), agent);
			} catch (Exception e) {
				log.error("Cannot create agent={} from config={}", agentConfig.getName(), agentConfig);
			}
		});

		// Init controller agents
		config.getControllers().forEach(agentConfig -> {
			try {
				CellGateway agent = this.createAgent(agentConfig);
				this.controllerAgents.put(agentConfig.getName(), agent);
			} catch (Exception e) {
				log.error("Cannot create agent={} from config={}", agentConfig.getName(), agentConfig);
			}
		});

		// Pause to init all agents
		log.debug("Wait for all agents to finish init");
		synchronized (this) {
			try {
				this.wait(1000);
			} catch (InterruptedException e) {

			}
		}

		log.info("=== All agents initialized ===");

		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.tuwien.ict.acona.framework.interfaces.KoreExternalController#
	 * setTopController(java.lang.String)
	 */
	@Override
	public void setTopController(String agentName) {
		this.topController = agentName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.tuwien.ict.acona.framework.interfaces.KoreExternalController#
	 * getControllerAgent(java.lang.String)
	 */
	@Override
	public CellGateway getControllerAgent(String localName) {
		return this.controllerAgents.get(localName);
	}

	/**
	 * Get the agent controller map
	 * 
	 * @return
	 */
	public Map<String, CellGatewayImpl> getExternalAgentControllerMap() {
		return Collections.unmodifiableMap(externalAgentControllerMap);
	}
}
