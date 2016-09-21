package at.tuwien.ict.acona.jadelauncher.util;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

/**
 * Util class for creating containers and agents
 * 
 * @author wendt
 *
 */
public class JadeContainerUtil {
	
	
	
	/**
	 * Create an agent within a container
	 * 
	 * @param name
	 * @param clzz
	 * @param containerController
	 * @return
	 * @throws StaleProxyException
	 */
	public AgentController createAgent(String name, Class<?> clzz, ContainerController containerController) throws StaleProxyException {
		return createAgent(name, clzz, null, containerController);
	}
	
	/**
	 * Create an agent within a container
	 * 
	 * @param name
	 * @param clzz
	 * @param containerController
	 * @return
	 * @throws StaleProxyException
	 */
	public AgentController createAgent(String name, Class<?> clzz, Object[] args, ContainerController containerController, boolean starAgent) throws StaleProxyException {
		AgentController agentController = null;
		String className = clzz.getName();
		
		agentController = containerController.createNewAgent(name, className, args);
		if(starAgent) {
			agentController.start();
			
			//Wait until the agent has been initialized
			//FIXME: This is a unsaubere solution. Listener would be better
			while (agentController.getState().getCode()!=3) {
				synchronized (this) {
					try {
						this.wait(10);
					} catch (InterruptedException e) {
						
					}
				}
			}
		}
		
		return agentController;
	}
	
	/**
	 * Create an agent within a container
	 * 
	 * @param name
	 * @param clzz
	 * @param containerController
	 * @return
	 * @throws StaleProxyException
	 */
	public AgentController createAgent(String name, Class<?> clzz, Object[] args, ContainerController containerController) throws StaleProxyException {
		return createAgent(name, clzz, args, containerController, true);
	}
	
	/**
	 * Create an RMA agent
	 * 
	 * @param container
	 * @throws StaleProxyException
	 */
	public void createRMAInContainer(ContainerController container) throws StaleProxyException {
		AgentController rma = this.createAgent("rma", jade.tools.rma.rma.class, container);
		rma.start();
	}
	
	/**
	 * Create the main JADE container
	 * 
	 * @param host
	 * @param port
	 * @param name
	 * @return
	 * @throws JadeException
	 */
	public ContainerController createMainJADEContainer(String host, int port, String name) throws JadeException {
		ContainerController containerController = null;
		
		//String host = "localhost";
		//String port = "-1"; //negative number for default port number
		//String name = "agent1";
		
		//Get JADE runtime
		Runtime runtime = Runtime.instance();
		
		Profile profile = new ProfileImpl();
		profile.setParameter(Profile.MAIN_HOST, host);
		profile.setParameter(Profile.MAIN_PORT, String.valueOf(port));
		profile.setParameter(Profile.CONTAINER_NAME, name);
		
		containerController = runtime.createMainContainer(profile);
		if (containerController==null) {
			throw new JadeException("Cannot start Jade container");
		} 
		
		return containerController;
	}
	
	/**
	 * Create a sub-container
	 * 
	 * @param host
	 * @param port
	 * @param containerName
	 * @return
	 * @throws JadeException
	 */
	public ContainerController createAgentContainer(String host, int port, String containerName) throws JadeException {
		ContainerController containerController = null;
		
		//String host = "localhost";
		//String port = "-1"; //negative number for default port number
		//String name = "agent1";
		
		//Get JADE runtime
		Runtime runtime = Runtime.instance();
		
		Profile profile = new ProfileImpl();
		profile.setParameter(Profile.MAIN_HOST, host);
		profile.setParameter(Profile.MAIN_PORT, String.valueOf(port));
		profile.setParameter(Profile.CONTAINER_NAME, containerName);
		
		//profile.setParameter(Profile.MAIN_HOST, "localhost");
		//profile.setParameter(Profile.MAIN_PORT, "1099");
		
		containerController = runtime.createAgentContainer(profile);
		if (containerController==null) {
			throw new JadeException("Cannot start Jade container");
		} 
		
		return containerController;
	}
	
//	public AgentController startAgent() {
//		AgentController agentController = null;
//		String host = "localhost";
//		String port = "-1"; //negative number for default port number
//		String name = "agent1";
//		
//		//Get JADE runtime
//		Runtime runtime = Runtime.instance();
//		
//		Profile profile = new ProfileImpl();
//		profile.setParameter(Profile.MAIN_HOST, host);
//		profile.setParameter(Profile.MAIN_PORT, port);
//		
//		ContainerController containerController = runtime.createAgentContainer(profile);
//		if (containerController!=null) {
//					
//			
//			
//		}
//		
//		
//		return agentController;
//	}
}
