package at.tuwien.ict.kore.communicator.main;

import jade.wrapper.ContainerController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.kore.communicator.core.Communicator;
import at.tuwien.ict.kore.communicator.core.CommunicatorImpl;
import at.tuwien.ict.kore.communicator.core.ListenerModule;
import at.tuwien.ict.kore.communicator.demoagents.PongAgent;
import at.tuwien.ict.kore.communicator.util.JadeContainerUtil;

public class CommunicatorLauncher extends Thread implements ListenerModule {
	
	private static CommunicatorLauncher launcher;
	private static Logger log = LoggerFactory.getLogger("main");
	
	private JadeContainerUtil util = new JadeContainerUtil();
	private Communicator comm;
	private boolean isActive = true;
	private final static String MESSAGE = "ping";
	private final static String RETURNMESSAGE = "pong";
	private final static String TARGETAGENT = "PongAgent";
	

	public static void main(String[] args) {
		
		CommunicatorLauncher.launcher = new CommunicatorLauncher();
		try {
			launcher.init();
		} catch (Exception e) {
			log.error("System could not be started", e);
		}
		
	}
	
	public void init() throws Exception {
		try {
			log.debug("Create or get main container");
			ContainerController mainContainerController = this.util.createMainJADEContainer("localhost", 1099, "MainContainer");
					
			log.debug("Create subcontainer");
			ContainerController agentContainer = this.util.createAgentContainer("localhost", 1099, "Subcontainer"); 
			
			log.debug("Create gui");
			this.util.createRMAInContainer(agentContainer);
			
			//Create gateway
			log.debug("Create Gateway");
			comm = new CommunicatorImpl();
			comm.init();
			comm.addListener(this);
			
			//Create agent in the system
			String[] args = {"1", RETURNMESSAGE};
			this.util.createAgent("PongAgent", PongAgent.class, args, agentContainer);
			
			
		} catch (Exception e) {
			log.error("Cannot init system", e);
			throw e;
		}
		
		super.start();
	}
	
	public void run() {
		log.debug("Start Agent");
		while (this.isActive==true) {
			try {
				try {
					this.comm.sendAsynchronousMessageToAgent(MESSAGE, TARGETAGENT, "");
					log.debug("Message={} sent to agent={}", MESSAGE, TARGETAGENT);
				} catch (Exception e2) {
					log.error("Cannot send message to agent");
					throw e2;
				}
				
				synchronized (this) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						
					}
				}
				
				log.debug("Message received from agent");
			} catch (Exception e) {
				log.error("Error in the communication", e);
			}
			
		}
	}

	@Override
	public void updateValue(String message) {
		log.info("Message received from agent system={}", message);
		synchronized (this) {
			this.notify();
		}
		
		
	}

}
