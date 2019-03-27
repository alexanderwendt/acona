package at.tuwien.ict.acona.distribution;

import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.mq.core.agentfunction.ControlCommand;
import at.tuwien.ict.acona.mq.core.config.AgentConfig;
import at.tuwien.ict.acona.mq.core.core.Cell;
import at.tuwien.ict.acona.mq.datastructures.DPBuilder;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.launcher.SystemControllerImpl;

/**
 * This class manages the launching of the whole cognitive system
 * 
 * @author wendt
 *
 */
public class Launcher {

	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final DPBuilder dpb = new DPBuilder();
	private SystemControllerImpl controller = SystemControllerImpl.getLauncher();
	
	private static Launcher launcher;

	public static void main(String[] args) {
		log.info("Welcome to the ACONA Stock Market Evolution Demonstrator");

		launcher = new Launcher();
		try {
			launcher.init();
		} catch (Exception e) {
			log.error("System initialization failed. Quit", e);
			System.exit(-1);
		}

	}

	private void init() throws Exception {
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
			

		} catch (Exception e) {
			log.error("Cannot initialize the system", e);
			throw new Exception(e.getMessage());
		}

	}

}
