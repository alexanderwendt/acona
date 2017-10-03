package at.tuwien.ict.acona.demowebservice.launcher;

import org.eclipse.jetty.util.thread.QueuedThreadPool;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.servlet.ServletHandler;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionCodeletHandler;
import at.tuwien.ict.acona.cell.cellfunction.specialfunctions.CFStateGenerator;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.core.CellGatewayImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoints;
import at.tuwien.ict.acona.demowebservice.cellfunctions.UserInterfaceCollector;
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
	
	private String outputString;
	
	public String getoutputString(){
		return outputString;
	}

	public void setoutputString(String newString) {
		outputString = newString;
	}
	public static void main(String[] args) throws Exception {
		log.info("Welcome to the ACONA Demonstrator");
		
		launcher = new Launcher();
		try {
			launcher.init();
		} catch (Exception e) {
			log.error("System initialization failed. Quit", e);
			System.exit(-1);
		}
		log.info("---------------------------------END OF MAIN ---------------------------------------------------------------------------");
	}
		
	
	private void init() throws Exception {
		try {
			//Start JADE
			log.info("Start JADE");
			this.startJade();
			
			//=== General variables ===//
//			String weatherAgent1Name = "WeatherAgent1"; 
//			//String weatherAgent2Name = "WeatherAgent2"; 
//			String weatherservice = "Weather";
//			String publishAddress = "helloworld.currentweather";
//			
//			CellGatewayImpl controllerAgent = this.controller.createAgent(CellConfig.newConfig(weatherAgent1Name)
//					.addCellfunction(CellFunctionConfig.newConfig(weatherservice, WeatherServiceClientMock.class)
//							.addManagedDatapoint(WeatherServiceClientMock.WEATHERADDRESSID, publishAddress , weatherAgent1Name, SyncMode.WRITEONLY))
//					.addCellfunction(CellFunctionConfig.newConfig(CFStateGenerator.class)));
			
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
			
			CellGatewayImpl weatherAgent = this.controller.createAgent(cf);
			
			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}
			
			weatherAgent.getCommunicator().write(Datapoints.newDatapoint(weatherservice + ".command").setValue(ControlCommand.START));
			
			//=== Broker ===//
			String brokerAgentName = "BrokerAgent"; 
			
			String brokerServiceName = "BrokerService";
			String statisticsService = "statisticsService";
			
//			CellGatewayImpl brokerAgent = this.controller.createAgent(CellConfig.newConfig(brokerAgentName)
//					.addCellfunction(CellFunctionConfig.newConfig(brokerServiceName, Broker.class)
//							.setProperty(Broker.ATTRIBUTESTOCKNAME, stockName))
//					.addCellfunction(CellFunctionConfig.newConfig(statisticsService, StatisticsCollector.class)));
//			
//			synchronized (this) {
//				try {
//					this.wait(200);
//				} catch (InterruptedException e) {
//
//				}
//			}
			
//			//=== Stock market ===//
//			String stockmarketAgentName = "StockMarketAgent";
//			String stockmarketServiceName = "StockMarketService";
//			
//			CellGatewayImpl stockMarketAgent = this.controller.createAgent(CellConfig.newConfig(stockmarketAgentName)
//					.addCellfunction(CellFunctionConfig.newConfig(stockmarketServiceName, DummyPriceGenerator.class)
//							.setProperty(DummyPriceGenerator.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
//							.setProperty(DummyPriceGenerator.ATTRIBUTEEXECUTIONORDER, 0)
//							.setProperty(DummyPriceGenerator.ATTRIBUTEMODE, 1)
//							.setProperty(DummyPriceGenerator.ATTRIBUTESTOCKNAME, stockName)
//							.setGenerateReponder(true)));	//Puts data on datapoint StockMarketAgent:data
//
//			//=== Traders ===//
//			String traderAgentName = "TraderAgent";
//			String signalService = "signal";
//			
//			//Create 100 trading agents that first buy a stock, then sell it
//			for (int i=1;i<=10;i++) {
//				String traderType = "type";
//				if (i%2==0) {
//					traderType += "_even";
//				} else {
//					traderType += "_odd";
//				}
//				
//				CellGatewayImpl traderAgent = this.controller.createAgent(CellConfig.newConfig(traderAgentName + "_" + i)
//						.addCellfunction(CellFunctionConfig.newConfig("trader_" + i, Trader.class)
//								.setProperty(Trader.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
//								.setProperty(Trader.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
//								.setProperty(Trader.ATTRIBUTEAGENTTYPE, traderType)
//								.setProperty(Trader.ATTRIBUTESIGNALADDRESS, signalService)
//								.setProperty(Trader.ATTRIBUTEEXECUTIONORDER, 1)
//								.setProperty(Trader.ATTRIBUTEBROKERADDRESS, brokerAgentName + ":" + brokerServiceName)
//								.setGenerateReponder(true))
//						.addCellfunction(CellFunctionConfig.newConfig(signalService, PermanentBuySellIndicator.class)));
//			}

			synchronized (this) {
				try {
					this.wait(10000);
				} catch (InterruptedException e) {

				}
			}
			
			log.info("=== All agents initialized ===");
			
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
