package at.tuwien.ict.acona.koreuserinterface;

import static org.junit.Assert.*;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import at.tuwien.ict.acona.demowebservice.cellfunctions.UserInterfaceCollector;
import at.tuwien.ict.acona.demowebservice.cellfunctions.WeatherService;
import at.tuwien.ict.acona.demowebservice.helpers.WeatherServiceClientMock;
import at.tuwien.ict.acona.mq.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.mq.cell.config.CellConfig;
import at.tuwien.ict.acona.mq.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.mq.cell.core.Cell;
import at.tuwien.ict.acona.mq.datastructures.ControlCommand;
import at.tuwien.ict.acona.mq.datastructures.DPBuilder;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.launcher.SystemControllerImpl;

public class KoreUITester {

	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final DPBuilder dpb = new DPBuilder();
	private SystemControllerImpl launcher = SystemControllerImpl.getLauncher();

	@Before
	public void setUp() throws Exception {
		try {

		} catch (Exception e) {
			log.error("Cannot initialize test environment", e);
		}
	}

	@After
	public void tearDown() throws Exception {
		// Clear all cells
		synchronized (this) {
			try {
				this.wait(10);
			} catch (InterruptedException e) {

			}
		}
		this.launcher.stopSystem();

		synchronized (this) {
			try {
				this.wait(10);
			} catch (InterruptedException e) {

			}
		}
	}



	
	/**
	 * Create a broker agent. Create a depot. Add money to depot, read state of depot, buy stock, sell stock, unregister depot
	 * 
	 */
	@Test
	public void functionKoreDatastructureTest() {
		try {
			String DataStructureAgent1Name = "DataStructureAgent1"; 
			//String weatherAgent2Name = "WeatherAgent2"; 
			String datageneratorservice = "DataStructureGenerator";

			CellConfig cf = CellConfig.newConfig(DataStructureAgent1Name)
					.addFunction(CellFunctionConfig.newConfig(datageneratorservice, KoreDataStructureGeneratorMock.class))
					.addFunction(CellFunctionConfig.newConfig("LamprosUI", UserInterfaceCollector.class)
							.addManagedDatapoint("KORE", DataStructureAgent1Name + ":" + datageneratorservice + "/result", SyncMode.SUBSCRIBEONLY));
			Cell weatherAgent = this.launcher.createAgent(cf);
			
			//=== Init finished ===//

			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {

				}
			}
			log.info("=== All agents initialized ===");
			
			weatherAgent.getCommunicator().execute(weatherAgent.getName() + ":" + datageneratorservice + "/command", (new Request())
					.setParameter("command", ControlCommand.START)
					.setParameter("blocking", false), 100000);
			
			//Wait while the system runs
			synchronized (this) {
				try {
					this.wait(2000000000);
				} catch (InterruptedException e) {

				}
			}
			
			//Read the state of the system
			//JsonObject systemState = weatherAgent.readLocalDatapoint(CFStateGenerator.SYSTEMSTATEADDRESS).getValue().getAsJsonObject();
			
			//String currentResult = systemState.get("hasFunction").getAsJsonArray().get(0).getAsJsonObject().get("hasState").getAsString();
			//String expectedResult = "RUNNING"; //As the system is still running, when the request is sent
			
			//weatherAgent.getCommunicator().write(DatapointBuilder.newDatapoint(weatherservice + ".command").setValue(ControlCommand.STOP));
			
			//log.info("current result={}, expected result={}", currentResult, expectedResult);
			assertEquals(true, false);
			
			log.info("Tests passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}
}
