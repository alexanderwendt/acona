package at.tuwien.ict.kore.agentcommunicator.core;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.kore.communicator.core.Communicator;
import at.tuwien.ict.kore.communicator.core.CommunicatorImpl;
import at.tuwien.ict.kore.communicator.demoagents.InitiatorAgent;
import at.tuwien.ict.kore.communicator.demoagents.PongAgent;
import at.tuwien.ict.kore.communicator.util.JadeContainerUtil;
import jade.core.Runtime;
import jade.wrapper.ContainerController;

public class CommunicatorTest {

	private static Logger log = LoggerFactory.getLogger("main");
	private final JadeContainerUtil util = new JadeContainerUtil();
	private Communicator comm;
	
	private ContainerController agentContainer;
	ContainerController mainContainerController;
	
	@Before
	public void setup() {
		try {
			//Create container
			log.debug("Create or get main container");
			mainContainerController = this.util.createMainJADEContainer("localhost", 1099, "MainContainer");
					
			log.debug("Create subcontainer");
			agentContainer = this.util.createAgentContainer("localhost", 1099, "Subcontainer"); 
			
			//log.debug("Create gui");
			//this.util.createRMAInContainer(agentContainer);
			
			//Create gateway
			log.debug("Create gateway");
			comm = new CommunicatorImpl();
			comm.init();
			
		} catch (Exception e) {
			log.error("Cannot initialize test environment", e);
		}
	}
	
	@After
	public void takeDown() {
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
		this.comm.shutDown();
	}
	
	@Test 
	public void agentInitiatorFunctionTest() {
		try {
			String returnmessage = "pong";
			String expectedAnswer = "pingpong";
			String realanswer = "";
			
			//Create agent in the system
			Object[] args = new String[2];
			args[0] = "ControlGateway";
			args[1] = expectedAnswer;
			
			this.comm.sendAsynchronousMessageToAgent("init", "DF", "");
			
			this.util.createAgent("InitiatorAgent", InitiatorAgent.class, args, agentContainer);
			
			//createmessage
			//String message = "ping";
			
			
			//Send Message
			//this.comm.sendAsynchronousMessageToAgent(message, "PongAgent");
			
			log.debug("wait 2000ms for agent to answer");
			synchronized (this) {
				try {
					this.wait(1000);
				} catch (InterruptedException e) {
					
				}
			}
			
			log.debug("Wait ended. Take message from agent gateway");
			
			realanswer = this.comm.getMessageFromAgent(1000);
			
			log.info("Test finished. Expected message={}, received message={}", expectedAnswer, realanswer);
			assertEquals(expectedAnswer, realanswer);
		} catch (Exception e) {
			log.error("Cannot init system", e);
			fail("Error");
		}
	}

	@Test
	public void asynchronResponderTest() {
		try {
			//createmessage
			String message = "ping";
			String expectedAnswer  = "pingpong";
			String answer = "";
			
			//Create agent in the system
			String[] args = {"1", "pong"};
			this.util.createAgent("PongAgent", PongAgent.class, args, agentContainer);
						
			//Send Message
			this.comm.sendAsynchronousMessageToAgent(message, "PongAgent", "");
			
			log.debug("wait for agent to answer");
			synchronized (this) {
				try {
					this.wait(5000);
				} catch (InterruptedException e) {
					
				}
			}
			
			answer = this.comm.getMessageFromAgent();
			
			assertEquals(expectedAnswer, answer);
		} catch (Exception e) {
			log.error("Cannot init system", e);
			fail("Error");
		}
	}
	
	@Test
	public void synchronResponderTest() {
		try {
			//createmessage
			String message = "ping";
			String expectedAnswer  = "pingpong";
			String answer = "";
			
			//Create agent in the system
			String[] args = {"1", "pong"};
			this.util.createAgent("PongAgent", PongAgent.class, args, agentContainer);
			
			//Send Message
			this.comm.sendAsynchronousMessageToAgent(message, "PongAgent", "");
			
//			log.debug("wait for agent to answer");
//			synchronized (this) {
//				try {
//					this.wait(5000);
//				} catch (InterruptedException e) {
//					
//				}
//			}
			
			answer = this.comm.getMessageFromAgent();
			
			assertEquals(expectedAnswer, answer);
		} catch (Exception e) {
			log.error("Cannot init system", e);
			fail("Error");
		}
	}

}
