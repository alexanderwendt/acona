package at.tuwien.ict.acona.demowebservice.launcher;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.demowebservice.cellfunctions.ComparisonAlgorithmAlternative;
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
import at.tuwien.ict.acona.mq.launcher.SystemController;
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

	//private SystemController controller = SystemControllerImpl.getLauncher();

	public static void main(String[] args) {
		log.info("Welcome to the ACONA Weather Service Demonstrator");

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
			// Start JADE
			//log.info("Start JADE");
			//this.startJade();

			// === General variables ===//
			String weatherAgent1Name = "WeatherAgent1";
			String weatherAgent2Name = "WeatherAgent2";
			String weatherAgent3Name = "WeatherAgent3";
			String weatherAgent4Name = "WeatherAgent4";
			String weatherAgent5Name = "WeatherAgent5";
			String algorithmAgentName = "AlgorithmAgent";
			String algorithmService = "algorithm";
			String weatherservice = "Weather";
			String publishAddress = "helloworld.currentweather";

			// CellGatewayImpl weatherAgent1 = this.controller.createAgent(CellConfig.newConfig(weatherAgent1Name)
			// .addCellfunction(CellFunctionConfig.newConfig(weatherservice, WeatherServiceClientMock.class)
			// .addManagedDatapoint(WeatherServiceClientMock.WEATHERADDRESSID, publishAddress , weatherAgent1Name, SyncMode.WRITEONLY))
			// .addCellfunction(CellFunctionConfig.newConfig(CFStateGenerator.class)));

			Cell weatherAgent1 = this.controller.createAgent(CellConfig.newConfig(weatherAgent1Name)
					.addCellfunction(CellFunctionConfig.newConfig(weatherservice, WeatherService.class)
							.setProperty(WeatherService.CITYNAME, "Palermo")
							.setProperty(WeatherService.USERID, "5bac1f7f2b67f3fb3452350c23401903")
							.addManagedDatapoint(WeatherServiceClientMock.WEATHERADDRESSID, weatherAgent1Name + ":" + publishAddress, SyncMode.WRITEONLY))
					.addCellfunction(CellFunctionConfig.newConfig(StateMonitor.class)));

			Cell weatherAgent2 = this.controller.createAgent(CellConfig.newConfig(weatherAgent2Name)
					.addCellfunction(CellFunctionConfig.newConfig(weatherservice, WeatherService.class)
							.setProperty(WeatherService.CITYNAME, "vienna")
							.setProperty(WeatherService.USERID, "5bac1f7f2b67f3fb3452350c23401903")
							.addManagedDatapoint(WeatherServiceClientMock.WEATHERADDRESSID, weatherAgent2Name + ":" + publishAddress, SyncMode.WRITEONLY))
					.addCellfunction(CellFunctionConfig.newConfig(StateMonitor.class)));

			Cell weatherAgent3 = this.controller.createAgent(CellConfig.newConfig(weatherAgent3Name)
					.addCellfunction(CellFunctionConfig.newConfig(weatherservice, WeatherService.class)
							.setProperty(WeatherService.CITYNAME, "stockholm")
							.setProperty(WeatherService.USERID, "5bac1f7f2b67f3fb3452350c23401903")
							.addManagedDatapoint(WeatherServiceClientMock.WEATHERADDRESSID, weatherAgent3Name + ":" + publishAddress, SyncMode.WRITEONLY))
					.addCellfunction(CellFunctionConfig.newConfig(StateMonitor.class)));

			Cell weatherAgent4 = this.controller.createAgent(CellConfig.newConfig(weatherAgent4Name)
					.addCellfunction(CellFunctionConfig.newConfig(weatherservice, WeatherService.class)
							.setProperty(WeatherService.CITYNAME, "innsbruck")
							.setProperty(WeatherService.USERID, "5bac1f7f2b67f3fb3452350c23401903")
							.addManagedDatapoint(WeatherServiceClientMock.WEATHERADDRESSID, weatherAgent4Name + ":" + publishAddress, SyncMode.WRITEONLY))
					.addCellfunction(CellFunctionConfig.newConfig(StateMonitor.class)));

			Cell weatherAgent5 = this.controller.createAgent(CellConfig.newConfig(weatherAgent5Name)
					.addCellfunction(CellFunctionConfig.newConfig(weatherservice, WeatherService.class)
							.setProperty(WeatherService.CITYNAME, "Abu Dhabi")
							.setProperty(WeatherService.USERID, "5bac1f7f2b67f3fb3452350c23401903")
							.addManagedDatapoint(WeatherServiceClientMock.WEATHERADDRESSID, weatherAgent5Name + ":" + publishAddress, SyncMode.WRITEONLY))
					.addCellfunction(CellFunctionConfig.newConfig(StateMonitor.class)));

			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}

			Cell calculator = this.controller.createAgent(CellConfig.newConfig(algorithmAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(algorithmService, ComparisonAlgorithmAlternative.class)
							// .addCellfunction(CellFunctionConfig.newConfig(algorithmService, ComparisonAlgorithm.class)
							.addManagedDatapoint("Palermo", weatherAgent1Name + ":" + publishAddress, SyncMode.SUBSCRIBEONLY)
							.addManagedDatapoint("Vienna", weatherAgent2Name + ":" + publishAddress, SyncMode.SUBSCRIBEONLY)
							.addManagedDatapoint("Stockholm", weatherAgent3Name + ":" + publishAddress, SyncMode.SUBSCRIBEONLY)
							.addManagedDatapoint("Innsbruck", weatherAgent4Name + ":" + publishAddress, SyncMode.SUBSCRIBEONLY)
							.addManagedDatapoint("Abu Dhabi", weatherAgent5Name + ":" + publishAddress, SyncMode.SUBSCRIBEONLY))
					.addCellfunction(CellFunctionConfig.newConfig("LamprosUI", UserInterfaceCollector.class)
							.addManagedDatapoint(UserInterfaceCollector.SYSTEMSTATEADDRESSID, algorithmAgentName + ":" + StateMonitor.SYSTEMSTATEADDRESS, SyncMode.SUBSCRIBEONLY)
							.addManagedDatapoint("RESULT", algorithmAgentName + ":" + algorithmService + ".result", SyncMode.SUBSCRIBEONLY)
							.addManagedDatapoint("ui1", weatherAgent1Name + ":" + publishAddress, SyncMode.SUBSCRIBEONLY))
					.addCellfunction(CellFunctionConfig.newConfig(StateMonitor.class)));

			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {

				}
			}

			log.info("=== All agents initialized ===");

			weatherAgent1.getCommunicator().write(this.dpb.newDatapoint(weatherservice + "/command").setValue(ControlCommand.START));
			weatherAgent2.getCommunicator().write(this.dpb.newDatapoint(weatherservice + "/command").setValue(ControlCommand.START));
			weatherAgent3.getCommunicator().write(this.dpb.newDatapoint(weatherservice + "/command").setValue(ControlCommand.START));
			weatherAgent4.getCommunicator().write(this.dpb.newDatapoint(weatherservice + "/command").setValue(ControlCommand.START));
			weatherAgent5.getCommunicator().write(this.dpb.newDatapoint(weatherservice + "/command").setValue(ControlCommand.START));

		} catch (Exception e) {
			log.error("Cannot initialize the system", e);
			throw new Exception(e.getMessage());
		}

	}

//	private void startJade() throws Exception {
//		try {
//			// Create container
//			log.debug("Create or get main container");
//			this.controller.createMainContainer("localhost", 1099, "MainContainer");
//
//			log.debug("Create subcontainer");
//			this.controller.createSubContainer("localhost", 1099, "Subcontainer");
//
//			// log.debug("Create gui");
//			// this.commUtil.createDebugUserInterface();
//
//			// Create gateway
//			// commUtil.initJadeGateway();
//			synchronized (this) {
//				try {
//					this.wait(2000);
//				} catch (InterruptedException e) {
//
//				}
//			}
//
//		} catch (Exception e) {
//			log.error("Cannot initialize test environment", e);
//		}
//	}
//
//	private void stopJade() throws Exception {
//		synchronized (this) {
//			try {
//				this.wait(200);
//			} catch (InterruptedException e) {
//
//			}
//		}
//
//		Runtime runtime = Runtime.instance();
//		runtime.shutDown();
//		synchronized (this) {
//			try {
//				this.wait(2000);
//			} catch (InterruptedException e) {
//
//			}
//		}
//	}

}
