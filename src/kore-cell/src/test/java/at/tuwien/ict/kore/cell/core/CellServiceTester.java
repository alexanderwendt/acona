package at.tuwien.ict.kore.cell.core;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.kore.communicator.core.Communicator;
import at.tuwien.ict.kore.communicator.core.CommunicatorImpl;
import at.tuwien.ict.kore.communicator.demoagents.PongAgent;
import at.tuwien.ict.kore.communicator.util.JadeContainerUtil;
import jade.core.Runtime;
import jade.wrapper.ContainerController;

public class CellServiceTester {
	
	private static Logger log = LoggerFactory.getLogger("main");
	private final JadeContainerUtil util = new JadeContainerUtil();
	private Communicator comm;
	
	private ContainerController agentContainer;
	ContainerController mainContainerController;

	@Before
	public void setUp() throws Exception {
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
		this.comm.shutDown();
	}

	@Test
	public void writeAndReadTest() {
		try {
			//create message
			String messageType = "read";
			String receiver = "CellAgent";
			String datapointaddress = "testaddress";
			String value = "testvalue";
			
			//Create agent in the system
			//String[] args = {"1", "pong"};
			this.util.createAgent(receiver, Cell.class, agentContainer);
						
			//Send Message
			this.comm.sendAsynchronousMessageToAgent(message, receiver);
			
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
