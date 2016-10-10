package _OLD_at.tuwien.ict.acona.cell.testing;

import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import _OLD.at.tuwien.ict.acona.cell.config.CellConfigJadeBehaviour;
import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.core.CellGatewayImpl;
import at.tuwien.ict.acona.jadelauncher.util.JadeContainerUtil;
import at.tuwien.ict.acona.jadelauncher.util.JadeException;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.AgentState;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;


public abstract class BaseCellTester {
	private static Logger log = LoggerFactory.getLogger(BaseCellTester.class);
	protected final JadeContainerUtil util = new JadeContainerUtil();
	private ContainerController agentContainer = null;
	
	protected ContainerController getAgentContainer() {
		return agentContainer;
	}

	protected void setAgentContainer(ContainerController agentContainer) {
		this.agentContainer = agentContainer;
	}

	protected String defaultHost() {
		return "localhost";
	}
	
	protected int defaultPort() {
		return 1099;
	}
	
	protected ContainerController createMainContainer(String name) throws JadeException {
		log.debug("Create or get main container");
		return util.createMainJADEContainer(defaultHost(), defaultPort(), name);
	}
	
	protected ContainerController createAgentContainer(String name) throws JadeException {
		log.debug("Create subcontainer");
		setAgentContainer(util.createAgentContainer(defaultHost(), defaultPort(), name));
		
		return getAgentContainer();
	}
	
	protected static void startAgent(AgentController controller) throws StaleProxyException {
		controller.start();
		int i = 0;
		while(controller.getState().getCode() == AgentState.cAGENT_STATE_INITIATED) {
			i++;
		}
		
		log.info("Started agent " + controller.getName() + " - attempts to state change: {}", i);
	}
	
	protected <AGENT_TYPE extends CellImpl> AgentController newAgent(String name, Class<AGENT_TYPE> type, Object... additionalArgs) throws StaleProxyException {
		return newAgent(name, type, CellConfigJadeBehaviour.newConfig(name, type.getName()), additionalArgs);
	}
	
	protected <AGENT_TYPE extends CellImpl> AgentController newAgent(String name, Class<AGENT_TYPE> type, CellConfigJadeBehaviour cellConfig, Object... additionalArgs) throws StaleProxyException {
		//Create agent in the system
		Object[] args = new Object[additionalArgs.length + 1];
		args[0] = cellConfig.toJsonObject();
		System.arraycopy(additionalArgs, 0, args, 1, additionalArgs.length);
		return util.createAgent(name, type, args, getAgentContainer(), false);
	}
	
	protected abstract void createAgents() throws StaleProxyException;
	
	@Before
	public void setUp() throws Exception {
		try {
			createMainContainer("Maincontainer");
			
			createAgentContainer("Agentcontainer");
			
			createAgents();
		} catch (Exception e) {
			log.error("Cannot initialize test environment", e);
		}
	}

	@After
	public void tearDown() throws Exception {
		synchronized (this) {
			try {
				this.wait(200);
			} catch (InterruptedException e) {
				
			}
		}
		
		Runtime runtime = Runtime.instance();
		runtime.shutDown();
		synchronized (this) {
			try {
				this.wait(200);
			} catch (InterruptedException e) {
				
			}
		}
	}
}
