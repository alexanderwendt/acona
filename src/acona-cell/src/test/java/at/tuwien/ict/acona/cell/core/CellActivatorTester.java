package at.tuwien.ict.acona.cell.core;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.core.CellInspectorController;
import at.tuwien.ict.acona.cell.core.helpers.CellWithActivator;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.communicator.core.Communicator;
import at.tuwien.ict.acona.communicator.core.CommunicatorImpl;
import at.tuwien.ict.acona.communicator.util.JadeContainerUtil;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.State;

public class CellActivatorTester {

	private static Logger log = LoggerFactory.getLogger(CellActivatorTester.class);
	private final JadeContainerUtil util = new JadeContainerUtil();
	private Communicator comm;
	
	private ContainerController agentContainer;
	private ContainerController mainContainerController;

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
	public void activatorTest() {
		try {
			String activatorAgentName = "activatoragent";
			String op1address = "data.op1";
			String op2address = "data.op2";
			String resultaddress = "data.result";
			
			double op1 = 1;
			double op2 = 2;
			double expectedResult = 3;
			
			//Create cell inspector controller for the subscriber
			CellInspectorController cellControlPublisher = new CellInspectorController();
			Object[] argsPublisher = new Object[1];
			argsPublisher[0] = cellControlPublisher;
			//Create agent in the system
			AgentController publisherController = this.util.createAgent(activatorAgentName, CellWithActivator.class, argsPublisher, agentContainer);
			log.debug("State={}", publisherController.getState());
			
			log.debug("wait for agent to answer");
			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {
					
				}
			}
			log.debug("State={}", publisherController.getState());
			
			cellControlPublisher.getCell().getDataStorage().write(Datapoint.newDatapoint(op1address).setValue(String.valueOf(op1)), cellControlPublisher.getCell().getName());
			cellControlPublisher.getCell().getDataStorage().write(Datapoint.newDatapoint(op2address).setValue(String.valueOf(op2)), cellControlPublisher.getCell().getName());
			
			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {
					
				}
			}
			
			double actualResult = cellControlPublisher.getCell().getDataStorage().read(resultaddress).getValue().getAsDouble();
			
			assertEquals(actualResult, expectedResult, 0.0001);
			
		} catch (Exception e) {
			log.error("Cannot init system", e);
			fail("Error");
		}
	}

}
