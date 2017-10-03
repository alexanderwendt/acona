package at.tuwien.ict.acona.demowebservice;

import static org.junit.Assert.*;

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

import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionCodeletHandler;
import at.tuwien.ict.acona.cell.cellfunction.specialfunctions.CFStateGenerator;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.core.CellGatewayImpl;
import at.tuwien.ict.acona.cell.core.cellfunction.codelets.Codelettester;
import at.tuwien.ict.acona.cell.core.cellfunction.codelets.helpers.IncrementOnConditionCodelet;
import at.tuwien.ict.acona.cell.datastructures.Datapoints;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;
import at.tuwien.ict.acona.demowebservice.cellfunctions.UserInterfaceCollector;
import at.tuwien.ict.acona.demowebservice.cellfunctions.WeatherService;
import at.tuwien.ict.acona.demowebservice.helpers.WeatherServiceClientMock;
import at.tuwien.ict.acona.jadelauncher.util.KoreExternalControllerImpl;
import jade.core.Runtime;

public class DemoWebServiceTester {

	private static final Logger log = LoggerFactory.getLogger(DemoWebServiceTester.class);
	private KoreExternalControllerImpl launcher = KoreExternalControllerImpl.getLauncher();

	@Before
	public void setUp() throws Exception {
		try {
			// Create container
			log.debug("Create or get main container");
			this.launcher.createMainContainer("localhost", 1099, "MainContainer");

			log.debug("Create subcontainer");
			this.launcher.createSubContainer("localhost", 1099, "Subcontainer");

			// log.debug("Create gui");
			// this.commUtil.createDebugUserInterface();

			// Create gateway
			// commUtil.initJadeGateway();

		} catch (Exception e) {
			log.error("Cannot initialize test environment", e);
		}
	}

	@After
	public void tearDown() throws Exception {
		synchronized (this) {
			try {
				this.wait(2000);
			} catch (InterruptedException e) {

			}
		}

		Runtime runtime = Runtime.instance();
		runtime.shutDown();
		synchronized (this) {
			try {
				this.wait(2000);
			} catch (InterruptedException e) {

			}
		}
	}

	/**
	 * Create a broker agent. Create a depot. Add money to depot, read state of depot, buy stock, sell stock, unregister depot
	 * 
	 */
	@Test
	public void functionStateTest() {
		try {
			String weatherAgent1Name = "WeatherAgent1"; 
			//String weatherAgent2Name = "WeatherAgent2"; 
			String weatherservice = "Weather";
			String publishAddress = "helloworld.currentweather";

			CellConfig cf = CellConfig.newConfig(weatherAgent1Name)
					.addCellfunction(CellFunctionConfig.newConfig(weatherservice, WeatherServiceClientMock.class)
							.addManagedDatapoint(WeatherServiceClientMock.WEATHERADDRESSID, publishAddress , weatherAgent1Name, SyncMode.WRITEONLY))
					.addCellfunction(CellFunctionConfig.newConfig(CFStateGenerator.class))
					.addCellfunction(CellFunctionConfig.newConfig("LamprosUI", UserInterfaceCollector.class)
							.addManagedDatapoint("ui1", publishAddress , weatherAgent1Name, SyncMode.SUBSCRIBEONLY)
							.addManagedDatapoint("state", CFStateGenerator.SYSTEMSTATEADDRESS, weatherAgent1Name, SyncMode.SUBSCRIBEONLY));
			
			CellGatewayImpl weatherAgent = this.launcher.createAgent(cf);
			
			//=== Init finished ===//

			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {

				}
			}
			log.info("=== All agents initialized ===");
			
			weatherAgent.getCommunicator().write(Datapoints.newDatapoint(weatherservice + ".command").setValue(ControlCommand.START));
			
			//Wait while the system runs
			synchronized (this) {
				try {
					this.wait(20000);
				} catch (InterruptedException e) {

				}
			}
			
			//Read the state of the system
			JsonObject systemState = weatherAgent.readLocalDatapoint(CFStateGenerator.SYSTEMSTATEADDRESS).getValue().getAsJsonObject();
			
			String currentResult = systemState.get("hasFunction").getAsJsonArray().get(0).getAsJsonObject().get("hasState").getAsString();
			String expectedResult = "RUNNING"; //As the system is still running, when the request is sent
			
			weatherAgent.getCommunicator().write(Datapoints.newDatapoint(weatherservice + ".command").setValue(ControlCommand.STOP));
			
			log.info("current result={}, expected result={}", currentResult, expectedResult);
			assertEquals(currentResult, expectedResult);
			
			log.info("Tests passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}
}
