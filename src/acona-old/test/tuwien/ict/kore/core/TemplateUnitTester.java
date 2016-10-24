package at.tuwien.ict.kore.core;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.core.CellGatewayImpl;
import at.tuwien.ict.acona.cell.core.helpers.CellWithActivator;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.jadelauncher.core.Gateway;
import at.tuwien.ict.acona.jadelauncher.core.GatewayImpl;
import at.tuwien.ict.acona.jadelauncher.util.JadeContainerUtil;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class TemplateUnitTester {
	
	private static Logger log = LoggerFactory.getLogger(TemplateUnitTester.class);
	private final JadeContainerUtil util = new JadeContainerUtil();
	private Gateway comm;
	
	private ContainerController agentContainer;
	
	@Before
	public void setUp() throws Exception {
		try {
			//Create container
			log.debug("Create or get main container");
			this.util.createMainJADEContainer("localhost", 1099, "MainContainer");
					
			log.debug("Create subcontainer");
			agentContainer = this.util.createAgentContainer("localhost", 1099, "Subcontainer"); 
			
			//log.debug("Create gui");
			//this.util.createRMAInContainer(agentContainer);
			
			//Create gateway
			log.debug("Create gateway");
			comm = new GatewayImpl();
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
	public void templateMethod() {		
		try {

			
			assertEquals(true, true);
			log.info("Test passed");
			
		} catch (Exception e) {
			log.error("Cannot test system", e);
			fail("Error");
		}
		
		fail("Not yet implemented");
	}

}
