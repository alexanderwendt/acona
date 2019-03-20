package at.tuwien.ict.acona.distribution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.mq.core.agentfunction.codelets.CodeletHandlerImpl;
import at.tuwien.ict.acona.mq.core.config.AgentConfig;
import at.tuwien.ict.acona.mq.core.config.AgentFunctionConfig;
import at.tuwien.ict.acona.mq.core.core.Cell;
import at.tuwien.ict.acona.mq.datastructures.ControlCommand;
import at.tuwien.ict.acona.mq.datastructures.DPBuilder;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.launcher.SystemControllerImpl;

public class DistributionTester {

	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final DPBuilder dpb = new DPBuilder();
	private SystemControllerImpl controller = SystemControllerImpl.getLauncher();

	@BeforeEach
	public void setUp() throws Exception {
		try {

		} catch (Exception e) {
			log.error("Cannot initialize test environment", e);
		}
	}

	@AfterEach
	public void tearDown() throws Exception {
		// Clear all cells
		synchronized (this) {
			try {
				this.wait(10);
			} catch (InterruptedException e) {

			}
		}
		this.controller.stopSystem();

		synchronized (this) {
			try {
				this.wait(10);
			} catch (InterruptedException e) {

			}
		}
	}

	
	/**
	 * Test a conrod with X machines and one connected ACOs.
	 * 
	 */
	@Test
	public void singleServerSingleRequester() {
		try {
			Cell agent = this.controller.createAgent(AgentConfig.newConfig("Agent1")
					.addFunction("Sender", Client.class)
					.addFunction("Server", Server.class));
			
			
			//Cell agent2 = this.controller.createAgent(CellConfig.newConfig("Agent2")
			//		.addFunction("Sender", Sender.class));
					//.addFunction("Server", Server.class));
			
			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}
			
			log.info("=== All agents initialized ===");
			
			log.debug("Start execution of the sender");
			agent.getCommunicator().execute("Sender/command", (new Request())
					.setParameter("command", ControlCommand.START)
					.setParameter("blocking", false), 100000);
			

			synchronized (this) {
				try {
					this.wait(100000);
				} catch (InterruptedException e) {

				}
			}

			//log.info("Got answer={}. Correct answer={}", pname, "Conrod");
			assertEquals(true, false);
			log.info("All tests passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}
	}

}
