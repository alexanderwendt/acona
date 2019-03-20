package at.tuwien.ict.acona.mq.launcher;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.mq.core.config.AgentConfig;
import at.tuwien.ict.acona.mq.core.core.Cell;
import at.tuwien.ict.acona.mq.core.core.AgentImpl;

/**
 * This is a wrapper class for all types of jade initialization. it starts agents and containers
 * 
 * @author wendt
 */
public class SystemControllerImpl implements SystemController {

	private static Logger log = LoggerFactory.getLogger(SystemControllerImpl.class);

	private final Map<String, Cell> agentControllerMap = new ConcurrentHashMap<>();

	// Make singleton
	private static SystemControllerImpl instance = null;

	private SystemControllerImpl() {

	}

	/**
	 * Get the Acona launcher
	 * 
	 * @return
	 */
	public static SystemControllerImpl getLauncher() {
		if (instance == null) {
			instance = new SystemControllerImpl();
		}

		return instance;
	}

	/**
	 * Stop the Acona system including JADE
	 */
	public void stopSystem() {
		synchronized (this) {
			try {
				this.wait(20);
			} catch (InterruptedException e) {

			}
		}

		log.info("Stopping system");

		// Get all agents in the external controller map
		for (Cell c : this.agentControllerMap.values()) {
			log.debug("Take down cell={}", c.getName());
			c.takeDownCell();
			log.debug("Cell {} closed", c.getName());
		}
		
		this.agentControllerMap.clear();

//		for (int i = 1; i <= 1; i++) {
//			synchronized (this) {
//				try {
//					this.wait(i * 1000);
//				} catch (InterruptedException e) {
//
//				}
//			}
//			log.debug("Wait {}s", i);
//		}

//		Runtime runtime = Runtime.instance();
//		runtime.shutDown();

		synchronized (this) {
			try {
				this.wait(20);
			} catch (InterruptedException e) {

			}
		}
	}

	// === Agent methods ===//

	/**
	 * Check if an agent with a given name exists.
	 * 
	 * @param name
	 * @return
	 */
	public boolean agentExists(String name) {
		boolean result = false;

		if (this.getAgent(name) != null) {
			result = true;
		}

		return result;
	}

	/**
	 * Create an agent from a cell config
	 * 
	 * @param cellConfig
	 * @return
	 * @throws Exception
	 */
	public synchronized Cell createAgent(AgentConfig cellConfig) throws Exception {
		// Check if the agent already exists
		Cell existingAgent = this.getAgent(cellConfig.getName());
		if (existingAgent != null) {
			log.error("Agent={} in cellConfig already exists. Cells={}.", cellConfig.getName(), this.agentControllerMap);
			throw new Exception("Agent " + cellConfig.getName() + " already exists.");
		}

		// Create the object
		Cell cell = new AgentImpl();
		cell.init(cellConfig);
		
		this.agentControllerMap.put(cellConfig.getName(), cell);

		log.info("Agent added={}", cell.getName());

		return cell;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see at.tuwien.ict.acona.framework.interfaces.KoreExternalController#getAgent(java .lang.String)
	 */
	@Override
	public Cell getAgent(String localName) {
		return agentControllerMap.get(localName);
	}

	/**
	 * Get the agent controller map
	 * 
	 * @return
	 */
	public Map<String, Cell> getExternalAgentControllerMap() {
		return Collections.unmodifiableMap(agentControllerMap);
	}

	@Override
	public void executeUserInput(String command, String parameter) {
		// TODO Auto-generated method stub

	}

}
