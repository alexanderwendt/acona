package at.tuwien.ict.acona.cell.util;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.config.CellConfigJadeBehaviour;
import at.tuwien.ict.acona.cell.core.InspectorCell;
import at.tuwien.ict.acona.cell.core.CellGatewayImpl;
import at.tuwien.ict.acona.jadelauncher.util.JadeContainerUtil;
import at.tuwien.ict.acona.jadelauncher.util.JadeException;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

/**
 * This is a wrapper class for all types of jade initialization. it starts agents and containers
 * 
 * @author wendt
 *
 */
public class JadelauncherUtil {
	
	private static Logger log = LoggerFactory.getLogger(JadelauncherUtil.class);
	private final JadeContainerUtil communicatorUtil = new JadeContainerUtil();
	private Map<String, ContainerController> agentContainerMap = new HashMap<String, ContainerController>();
	private Map<String, AgentController> agentControllerMap = new HashMap<String, AgentController>();
	private String defaultContainer = "";
	private boolean mainControllerExists = false;
	//private Gateway comm = new GatewayImpl();
	
	//Make singleton
	private static JadelauncherUtil instance = null;
	
	private JadelauncherUtil() {
		
	}
	
	public static JadelauncherUtil getUtil() {
		if (instance==null) {
			instance = new JadelauncherUtil();
		}
		
		return instance;
	}
	
	
	//=== Container methods ===//
	
	public void addAgentContainer(String name, ContainerController agentContainer) {
		this.agentContainerMap.put(name, agentContainer);
	}
	
	public ContainerController getContainerController(String name) {
		return this.agentContainerMap.get(name);
	}
	
	public void createMainContainer(String host, int port, String name) throws JadeException {
		ContainerController mainController = communicatorUtil.createMainJADEContainer(host, port, name);
		this.addAgentContainer(name, mainController);
		
		//Set default container name if no container has been set
		this.defaultContainer = name;
		
		//Set that a main container exists
		this.mainControllerExists = true;
	}
	
	public void createDebugUserInterface() throws Exception {
		this.communicatorUtil.createRMAInContainer(getDefaultContainerController());
	}
	
	public void createSubContainer(String host, int port, String name) throws Exception {
		if (this.mainControllerExists==false) {
			throw new Exception("No main controller exists");
		}
		
		ContainerController controller  = this.communicatorUtil.createAgentContainer(host, port, name); 
		this.addAgentContainer(name, controller);
	}
	
	public void setDefaultAgentContainer(String name) throws Exception {
		if (this.agentContainerMap.containsKey(name)==true) {
			this.defaultContainer = name;
		} else {
			throw new Exception("No container with the name " + name + " exists");
		}
	}
	
	public ContainerController getDefaultContainerController() {
		return this.agentContainerMap.get(defaultContainer);
	}
	
//	public void initJadeGateway() throws Exception {
//		this.comm.init();
//	}
//	
//	public Gateway getJadeGateway() {
//		return this.comm;
//	}
//	
//	public void shutDownJadeGateway() {
//		this.comm.shutDown();
//	}
	
	//=== Agent methods ===//
	
	public AgentController getAgentController(String name) {
		return this.agentControllerMap.get(name);
	}
	
	public void createAgent(CellConfigJadeBehaviour cellConfig) throws Exception {
		//Create the object
		Object[] args = new Object[1];
		args[0] = cellConfig.toJsonObject();
		
		AgentController agentController = this.communicatorUtil.createAgent(cellConfig.getName(), cellConfig.getClassToInvoke(), args, this.getContainerController(defaultContainer));
		this.agentControllerMap.put(cellConfig.getName(), agentController);
		
		log.debug("Agent state={}", agentController.getState());	
	}
	
	public CellGatewayImpl createInspectorAgent(CellConfigJadeBehaviour cellConfig) throws Exception {
		CellGatewayImpl externalController = new CellGatewayImpl();
		
		//Create the object
		Object[] args = new Object[2];
		args[0] = cellConfig.toJsonObject();
		args[1] = externalController;
				
		AgentController agentController = this.communicatorUtil.createAgent(cellConfig.getName(), InspectorCell.class, args, this.getContainerController(defaultContainer));
		this.agentControllerMap.put(cellConfig.getName(), agentController);
				
		log.debug("Agent state={}", agentController.getState());
		
		return externalController;
	}
}
