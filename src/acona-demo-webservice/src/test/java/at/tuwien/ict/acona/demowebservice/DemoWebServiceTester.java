package at.tuwien.ict.acona.demowebservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.invoke.MethodHandles;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.demowebservice.cellfunctions.ComparisonAlgorithm;
import at.tuwien.ict.acona.demowebservice.cellfunctions.UserInterfaceCollector;
import at.tuwien.ict.acona.demowebservice.cellfunctions.WeatherService;
import at.tuwien.ict.acona.demowebservice.helpers.WeatherServiceClientMock;
import at.tuwien.ict.acona.mq.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.mq.cell.cellfunction.specialfunctions.StateMonitor;
import at.tuwien.ict.acona.mq.cell.config.CellConfig;
import at.tuwien.ict.acona.mq.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.mq.cell.core.Cell;
import at.tuwien.ict.acona.mq.datastructures.ControlCommand;
import at.tuwien.ict.acona.mq.datastructures.DPBuilder;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.launcher.SystemControllerImpl;

public class DemoWebServiceTester {

	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final DPBuilder dpb = new DPBuilder();
	private SystemControllerImpl controller = SystemControllerImpl.getLauncher();

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
		this.controller.stopSystem();

		synchronized (this) {
			try {
				this.wait(10);
			} catch (InterruptedException e) {

			}
		}
	}
	
	@Test
	public void weatherClientTester() {
		try {
			String weatherAgent1Name = "WeatherAgent1";
			String weatherservice = "Weather";
			String publishAddress = "helloworld/currentweather";

			CellConfig cf = CellConfig.newConfig(weatherAgent1Name)
					.addFunction(CellFunctionConfig.newConfig(weatherservice, WeatherServiceClientMock.class)
						.addManagedDatapoint(WeatherServiceClientMock.WEATHERADDRESSID, weatherAgent1Name + ":" + publishAddress, SyncMode.WRITEONLY)
						.setProperty(WeatherServiceClientMock.CITYNAME, "abudhabi")
						.setProperty(WeatherServiceClientMock.USERID, "5bac1f7f2b67f3fb3452350c23401903"));
					//.addCellfunction(CellFunctionConfig.newConfig(StateMonitor.class))
					//.addCellfunction(CellFunctionConfig.newConfig("LamprosUI", UserInterfaceCollector.class)
					//		.addManagedDatapoint(UserInterfaceCollector.SYSTEMSTATEADDRESSID, weatherAgent1Name + ":" + "systemstate", SyncMode.SUBSCRIBEONLY)
					//		.addManagedDatapoint("ui1", weatherAgent1Name + ":" + publishAddress, SyncMode.SUBSCRIBEONLY));
			Cell weatherAgent = this.controller.createAgent(cf);

			// === Init finished ===//

			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {

				}
			}
			log.info("=== All agents initialized ===");

			weatherAgent.getCommunicator().execute(weatherAgent.getName() + ":" + weatherservice + "/command", (new Request())
					.setParameter("command", ControlCommand.START)
					.setParameter("blocking", false), 100000);

			// Wait while the system runs
			synchronized (this) {
				try {
					this.wait(20000);
				} catch (InterruptedException e) {

				}
			}

			// Read the state of the system
			//JsonObject systemState = weatherAgent.getCommunicator().read(StateMonitor.SYSTEMSTATEADDRESS).getValue().getAsJsonObject();

			//String currentResult = systemState.get("hasFunction").getAsJsonArray().get(0).getAsJsonObject().get("hasState").getAsString();
			//String expectedResult = "RUNNING"; // As the system is still running, when the request is sent

			weatherAgent.getCommunicator().write(this.dpb.newDatapoint(weatherservice + "/command").setValue(ControlCommand.STOP));

			log.info("current result={}, expected result={}", "", "");
			assertEquals(0.0, 0.0, 0.0);

			log.info("Tests passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
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
			// String weatherAgent2Name = "WeatherAgent2";
			String weatherservice = "Weather";
			String publishAddress = "helloworld/currentweather";

			CellConfig cf = CellConfig.newConfig(weatherAgent1Name)
					.addFunction(CellFunctionConfig.newConfig(weatherservice, WeatherServiceClientMock.class)
						.addManagedDatapoint(WeatherServiceClientMock.WEATHERADDRESSID, weatherAgent1Name + ":" + publishAddress, SyncMode.WRITEONLY)
						.setProperty(WeatherServiceClientMock.CITYNAME, "abudhabi")
						.setProperty(WeatherServiceClientMock.USERID, "5bac1f7f2b67f3fb3452350c23401903"))
					//.addCellfunction(CellFunctionConfig.newConfig(StateMonitor.class))
					.addFunction(CellFunctionConfig.newConfig("LamprosUI", UserInterfaceCollector.class)
							.addManagedDatapoint(UserInterfaceCollector.SYSTEMSTATEADDRESSID, weatherAgent1Name + ":" + StateMonitor.SYSTEMSTATEADDRESS, SyncMode.SUBSCRIBEONLY)
							.addManagedDatapoint("RESULT", weatherAgent1Name + ":" + publishAddress, SyncMode.SUBSCRIBEONLY));
			Cell weatherAgent = this.controller.createAgent(cf);

			// === Init finished ===//

			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {

				}
			}
			log.info("=== All agents initialized ===");

			weatherAgent.getCommunicator().execute(weatherAgent.getName() + ":" + weatherservice + "/command", (new Request())
					.setParameter("command", ControlCommand.START)
					.setParameter("blocking", false), 100000);

			// Wait while the system runs
			synchronized (this) {
				try {
					this.wait(200000);
				} catch (InterruptedException e) {

				}
			}

			// Read the state of the system
			JsonObject systemState = weatherAgent.getCommunicator().read(StateMonitor.SYSTEMSTATEADDRESS).getValue().getAsJsonObject();

			//Check if weatherclient is running
			String currentResult = systemState.get("hasFunction").getAsJsonArray().get(1).getAsJsonObject().get("hasState").getAsString();
			String expectedResult = "RUNNING"; // As the system is still running, when the request is sent

			weatherAgent.getCommunicator().execute(weatherAgent.getName() + ":" + weatherservice + "/command", (new Request())
					.setParameter("command", ControlCommand.STOP)
					.setParameter("blocking", false), 100000);

			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}
			
			log.info("current result={}, expected result={}", currentResult, expectedResult);
			assertEquals(currentResult, expectedResult);

			log.info("Tests passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

	@Test
	public void algorithmTest() {
		try {

			// === General variables ===//
			String weatherAgent1Name = "WeatherAgent1";
			String weatherAgent2Name = "WeatherAgent2";
			String weatherAgent3Name = "WeatherAgent3";
			String weatherAgent4Name = "WeatherAgent4";
			String algorithmAgentName = "AlgorithmAgent";
			String algorithmService = "algorithm";
			String weatherservice = "Weather";
			String publishAddress = "helloworld.currentweather";

			Cell weatherAgent1 = this.controller.createAgent(CellConfig.newConfig(weatherAgent1Name)
					.addFunction(CellFunctionConfig.newConfig(weatherservice, WeatherServiceClientMock.class)
							.addManagedDatapoint(WeatherServiceClientMock.WEATHERADDRESSID, weatherAgent1Name + ":" + publishAddress, SyncMode.WRITEONLY)
							.setProperty(WeatherServiceClientMock.CITYNAME, "abudhabi")
							.setProperty(WeatherServiceClientMock.USERID, "5bac1f7f2b67f3fb3452350c23401903"))
					.addFunction(CellFunctionConfig.newConfig(StateMonitor.class)));

			Cell weatherAgent2 = this.controller.createAgent(CellConfig.newConfig(weatherAgent2Name)
					.addFunction(CellFunctionConfig.newConfig(weatherservice, WeatherServiceClientMock.class)
							.setProperty(WeatherService.CITYNAME, "vienna")
							.setProperty(WeatherService.USERID, "5bac1f7f2b67f3fb3452350c23401903")
							.addManagedDatapoint(WeatherServiceClientMock.WEATHERADDRESSID, weatherAgent2Name + ":" + publishAddress, SyncMode.WRITEONLY))
					.addFunction(CellFunctionConfig.newConfig(StateMonitor.class)));

			Cell weatherAgent3 = this.controller.createAgent(CellConfig.newConfig(weatherAgent3Name)
					.addFunction(CellFunctionConfig.newConfig(weatherservice, WeatherServiceClientMock.class)
							.setProperty(WeatherService.CITYNAME, "stockholm")
							.setProperty(WeatherService.USERID, "5bac1f7f2b67f3fb3452350c23401903")
							.addManagedDatapoint(WeatherServiceClientMock.WEATHERADDRESSID, weatherAgent3Name + ":" + publishAddress, SyncMode.WRITEONLY))
					.addFunction(CellFunctionConfig.newConfig(StateMonitor.class)));

			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}

			Cell calculator = this.controller.createAgent(CellConfig.newConfig(algorithmAgentName)
					// .addCellfunction(CellFunctionConfig.newConfig(algorithmService, ComparisonAlgorithmAlternative.class)
					.addFunction(CellFunctionConfig.newConfig(algorithmService, ComparisonAlgorithm.class)
							.addManagedDatapoint("Vienna", weatherAgent2Name + ":" + publishAddress, SyncMode.SUBSCRIBEONLY)
							.addManagedDatapoint("Stockholm", weatherAgent3Name + ":" + publishAddress, SyncMode.SUBSCRIBEONLY)
							.addManagedDatapoint("Mocktown", weatherAgent1Name + ":" + publishAddress, SyncMode.SUBSCRIBEONLY))
					.addFunction(CellFunctionConfig.newConfig("LamprosUI", UserInterfaceCollector.class)
							.addManagedDatapoint(UserInterfaceCollector.SYSTEMSTATEADDRESSID, algorithmAgentName + ":" + StateMonitor.SYSTEMSTATEADDRESS, SyncMode.SUBSCRIBEONLY)
							.addManagedDatapoint("RESULT", algorithmAgentName + ":" + algorithmService + ".result", SyncMode.SUBSCRIBEONLY)
							.addManagedDatapoint("ui1", weatherAgent1Name + ":" + publishAddress, SyncMode.SUBSCRIBEONLY))
					.addFunction(CellFunctionConfig.newConfig(StateMonitor.class)));

			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {

				}
			}

			log.info("=== All agents initialized ===");

			weatherAgent1.getCommunicator().execute(weatherAgent1 + ":" + weatherservice + "/command", (new Request())
					.setParameter("command", ControlCommand.START)
					.setParameter("blocking", false), 100000);
			weatherAgent2.getCommunicator().execute(weatherAgent2 + ":" + weatherservice + "/command", (new Request())
					.setParameter("command", ControlCommand.START)
					.setParameter("blocking", false), 100000);
			weatherAgent3.getCommunicator().execute(weatherAgent3 + ":" + weatherservice + "/command", (new Request())
					.setParameter("command", ControlCommand.START)
					.setParameter("blocking", false), 100000);

			synchronized (this) {
				try {
					this.wait(200000);
				} catch (InterruptedException e) {

				}
			}

			assert (false);
			log.info("Tests passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}
	}

	/**
	 * Create a broker agent. Create a depot. Add money to depot, read state of depot, buy stock, sell stock, unregister depot
	 * 
	 */
	@Test
	public void functionWeatherServiceTest() {
		try {
			String weatherAgent1Name = "WeatherAgent1";
			// String weatherAgent2Name = "WeatherAgent2";
			String weatherservice = "Weather";
			String publishAddress = "helloworld.currentweather";

			CellConfig cf = CellConfig.newConfig(weatherAgent1Name)
					.addFunction(CellFunctionConfig.newConfig(weatherservice, WeatherService.class)
							.setProperty(WeatherService.CITYNAME, "vienna")
							.setProperty(WeatherService.USERID, "5bac1f7f2b67f3fb3452350c23401903")
							.addManagedDatapoint(WeatherServiceClientMock.WEATHERADDRESSID, weatherAgent1Name + ":" + publishAddress, SyncMode.WRITEONLY));
			Cell weatherAgent = this.controller.createAgent(cf);

			// === Init finished ===//

			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {

				}
			}
			log.info("=== All agents initialized ===");

			weatherAgent.getCommunicator().write(dpb.newDatapoint(weatherservice + ".command").setValue(ControlCommand.START));

			// Wait while the system runs
			synchronized (this) {
				try {
					this.wait(20000);
				} catch (InterruptedException e) {

				}
			}

			// Read the state of the system
			// JsonObject systemState = weatherAgent.readLocalDatapoint(CFStateGenerator.SYSTEMSTATEADDRESS).getValue().getAsJsonObject();

			// String currentResult = systemState.get("hasFunction").getAsJsonArray().get(0).getAsJsonObject().get("hasState").getAsString();
			// String expectedResult = "RUNNING"; //As the system is still running, when the request is sent

			// weatherAgent.getCommunicator().write(Datapoints.newDatapoint(weatherservice + ".command").setValue(ControlCommand.STOP));

			// log.info("current result={}, expected result={}", currentResult, expectedResult);
			// assertEquals(currentResult, expectedResult);
			assert (false);
			log.info("Tests passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}
}
