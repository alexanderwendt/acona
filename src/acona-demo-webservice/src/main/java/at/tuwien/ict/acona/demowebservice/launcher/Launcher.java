package at.tuwien.ict.acona.demowebservice.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.cellfunction.specialfunctions.CFStateGenerator;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.core.CellGatewayImpl;
import at.tuwien.ict.acona.cell.datastructures.DatapointBuilder;
import at.tuwien.ict.acona.demowebservice.cellfunctions.ComparisonAlgorithm;
import at.tuwien.ict.acona.demowebservice.cellfunctions.ComparisonAlgorithmAlternative;
import at.tuwien.ict.acona.demowebservice.cellfunctions.UserInterfaceCollector;
import at.tuwien.ict.acona.demowebservice.cellfunctions.WeatherService;
import at.tuwien.ict.acona.demowebservice.helpers.WeatherServiceClientMock;
import at.tuwien.ict.acona.jadelauncher.util.KoreExternalControllerImpl;
import jade.core.Runtime;

/**
 * This class manages the launching of the whole cognitive system
 * 
 * @author wendt
 *
 */
public class Launcher {
	
	private final static Logger log = LoggerFactory.getLogger(Launcher.class);
	
	private static Launcher launcher;
	
	private KoreExternalControllerImpl controller = KoreExternalControllerImpl.getLauncher();

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
			//Start JADE
			log.info("Start JADE");
			this.startJade();
			
			//=== General variables ===//
			String weatherAgent1Name = "WeatherAgent1";
			String weatherAgent2Name = "WeatherAgent2"; 
			String weatherAgent3Name = "WeatherAgent3"; 
			String weatherAgent4Name = "WeatherAgent4"; 
			String algorithmAgentName = "AlgorithmAgent";
			String algorithmService = "algorithm";
			String weatherservice = "Weather";
			String publishAddress = "helloworld.currentweather";
			
			//CellGatewayImpl weatherAgent1 = this.controller.createAgent(CellConfig.newConfig(weatherAgent1Name)
			//		.addCellfunction(CellFunctionConfig.newConfig(weatherservice, WeatherServiceClientMock.class)
			//				.addManagedDatapoint(WeatherServiceClientMock.WEATHERADDRESSID, publishAddress , weatherAgent1Name, SyncMode.WRITEONLY))
			//		.addCellfunction(CellFunctionConfig.newConfig(CFStateGenerator.class)));
			
			CellGatewayImpl weatherAgent1 = this.controller.createAgent(CellConfig.newConfig(weatherAgent1Name)
					.addCellfunction(CellFunctionConfig.newConfig(weatherservice, WeatherService.class)
							.setProperty(WeatherService.CITYNAME, "abudhabi")
							.setProperty(WeatherService.USERID, "5bac1f7f2b67f3fb3452350c23401903")
							.addManagedDatapoint(WeatherServiceClientMock.WEATHERADDRESSID, publishAddress , weatherAgent2Name, SyncMode.WRITEONLY))
					.addCellfunction(CellFunctionConfig.newConfig(CFStateGenerator.class)));
			
			CellGatewayImpl weatherAgent2 = this.controller.createAgent(CellConfig.newConfig(weatherAgent2Name)
					.addCellfunction(CellFunctionConfig.newConfig(weatherservice, WeatherService.class)
							.setProperty(WeatherService.CITYNAME, "vienna")
							.setProperty(WeatherService.USERID, "5bac1f7f2b67f3fb3452350c23401903")
							.addManagedDatapoint(WeatherServiceClientMock.WEATHERADDRESSID, publishAddress , weatherAgent2Name, SyncMode.WRITEONLY))
					.addCellfunction(CellFunctionConfig.newConfig(CFStateGenerator.class)));
			
			CellGatewayImpl weatherAgent3 = this.controller.createAgent(CellConfig.newConfig(weatherAgent3Name)
					.addCellfunction(CellFunctionConfig.newConfig(weatherservice, WeatherService.class)
							.setProperty(WeatherService.CITYNAME, "stockholm")
							.setProperty(WeatherService.USERID, "5bac1f7f2b67f3fb3452350c23401903")
							.addManagedDatapoint(WeatherServiceClientMock.WEATHERADDRESSID, publishAddress , weatherAgent3Name, SyncMode.WRITEONLY))
					.addCellfunction(CellFunctionConfig.newConfig(CFStateGenerator.class)));
			
			CellGatewayImpl weatherAgent4 = this.controller.createAgent(CellConfig.newConfig(weatherAgent4Name)
					.addCellfunction(CellFunctionConfig.newConfig(weatherservice, WeatherService.class)
							.setProperty(WeatherService.CITYNAME, "innsbruck")
							.setProperty(WeatherService.USERID, "5bac1f7f2b67f3fb3452350c23401903")
							.addManagedDatapoint(WeatherServiceClientMock.WEATHERADDRESSID, publishAddress , weatherAgent3Name, SyncMode.WRITEONLY))
					.addCellfunction(CellFunctionConfig.newConfig(CFStateGenerator.class)));
			
			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}
			
			CellGatewayImpl calculator = this.controller.createAgent(CellConfig.newConfig(algorithmAgentName)
					//.addCellfunction(CellFunctionConfig.newConfig(algorithmService, ComparisonAlgorithmAlternative.class)
					.addCellfunction(CellFunctionConfig.newConfig(algorithmService, ComparisonAlgorithm.class)
							.addManagedDatapoint("Vienna", publishAddress, weatherAgent2Name, SyncMode.SUBSCRIBEONLY)
							.addManagedDatapoint("Stockholm", publishAddress, weatherAgent3Name, SyncMode.SUBSCRIBEONLY)
							.addManagedDatapoint("Mocktown", publishAddress, weatherAgent1Name, SyncMode.SUBSCRIBEONLY))
					.addCellfunction(CellFunctionConfig.newConfig("LamprosUI", UserInterfaceCollector.class)
							.addManagedDatapoint(UserInterfaceCollector.SYSTEMSTATEADDRESSID, "systemstate", algorithmAgentName, SyncMode.SUBSCRIBEONLY)
							.addManagedDatapoint("RESULT", algorithmService + ".result", algorithmAgentName, SyncMode.SUBSCRIBEONLY)
							.addManagedDatapoint("ui1", publishAddress , weatherAgent1Name, SyncMode.SUBSCRIBEONLY))
//							.addManagedDatapoint("Mocktown", publishAddress, weatherAgent1Name, SyncMode.SUBSCRIBEONLY)
//							.addManagedDatapoint("Innsbruck", publishAddress, weatherAgent4Name, SyncMode.SUBSCRIBEONLY))
					.addCellfunction(CellFunctionConfig.newConfig(CFStateGenerator.class)));
			
			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {

				}
			}
			
			log.info("=== All agents initialized ===");
			
			weatherAgent1.writeLocalDatapoint(DatapointBuilder.newDatapoint(weatherservice + ".command").setValue(ControlCommand.START));
			weatherAgent2.writeLocalDatapoint(DatapointBuilder.newDatapoint(weatherservice + ".command").setValue(ControlCommand.START));
			weatherAgent3.writeLocalDatapoint(DatapointBuilder.newDatapoint(weatherservice + ".command").setValue(ControlCommand.START));
			weatherAgent4.writeLocalDatapoint(DatapointBuilder.newDatapoint(weatherservice + ".command").setValue(ControlCommand.START));
			
		} catch (Exception e) {
			log.error("Cannot initialize the system", e);
			throw new Exception(e.getMessage());
		}
		
	}
	
	private void startJade() throws Exception {
		try {
			// Create container
			log.debug("Create or get main container");
			this.controller.createMainContainer("localhost", 1099, "MainContainer");

			log.debug("Create subcontainer");
			this.controller.createSubContainer("localhost", 1099, "Subcontainer");

			// log.debug("Create gui");
			// this.commUtil.createDebugUserInterface();

			// Create gateway
			// commUtil.initJadeGateway();
			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {
					
				}
			}

		} catch (Exception e) {
			log.error("Cannot initialize test environment", e);
		}
	}

	private void stopJade() throws Exception {
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
				this.wait(2000);
			} catch (InterruptedException e) {

			}
		}
	}

}
