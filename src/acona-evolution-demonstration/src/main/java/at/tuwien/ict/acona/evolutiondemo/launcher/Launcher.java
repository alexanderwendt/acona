package at.tuwien.ict.acona.evolutiondemo.launcher;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.evolutiondemo.brokeragent.Broker;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.DepotStaticticsGraphToolFunction;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.Evaluator;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.StatisticsCollector;
import at.tuwien.ict.acona.evolutiondemo.controlleragent.ConsoleRequestReceiver;
import at.tuwien.ict.acona.evolutiondemo.stockmarketagent.DummyPriceGenerator;
import at.tuwien.ict.acona.evolutiondemo.stockmarketagent.PriceGraphToolFunction;
import at.tuwien.ict.acona.evolutiondemo.traderagent.EMAIndicator;
import at.tuwien.ict.acona.evolutiondemo.traderagent.PermanentBuySellIndicator;
import at.tuwien.ict.acona.evolutiondemo.traderagent.RandomBuySellIndicator;
import at.tuwien.ict.acona.evolutiondemo.traderagent.Trader;
import at.tuwien.ict.acona.evolutiondemo.webserver.EvolutionService;
import at.tuwien.ict.acona.evolutiondemo.webserver.JerseyRestServer;
import at.tuwien.ict.acona.mq.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.mq.cell.cellfunction.codelets.CellFunctionCodeletHandler;
import at.tuwien.ict.acona.mq.cell.config.CellConfig;
import at.tuwien.ict.acona.mq.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.mq.cell.core.Cell;
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
			// === General variables ===//
			String stockName = "Fingerprint";

			// === Controller ===//
			String controllerAgentName = "ControllerAgent";
			String controllerService = "controllerservice";

			// === Stock Market agent ===//
			String stockmarketAgentName = "StockMarketAgent";
			String stockmarketServiceName = "StockMarketService";

			// === Broker ===//
			String brokerAgentName = "BrokerAgent";
			String brokerServiceName = "BrokerService";
			String statisticsService = "statisticsService";

			String statisticsDatapointName = brokerAgentName + ":" + "stats";

			// === Traders ===//
			String traderAgentName = "TraderAgent";
			String signalService = "signal";
			
			// === Server === //
			String serverAgentName = "Server";
			

			// === Controller agent implementation === //
			Cell controllerAgent = this.controller.createAgent(CellConfig.newConfig(controllerAgentName)
					// Here a codelethandler is used. The agents are codelets of the codelet handler. Agents
					.addCellfunction(CellFunctionConfig.newConfig(controllerService, CellFunctionCodeletHandler.class))
					// The codelet handler ist controller request receiver funtion
					.addCellfunction(CellFunctionConfig.newConfig("controller", ConsoleRequestReceiver.class)
							.setProperty(ConsoleRequestReceiver.ATTRIBUTECONTROLLERSERVICE, controllerService)));

			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}

			// === Broker ===//

			Cell brokerAgent = this.controller.createAgent(CellConfig.newConfig(brokerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(brokerServiceName, Broker.class)
							.setProperty(Broker.ATTRIBUTESTOCKNAME, stockName))
					.addCellfunction(CellFunctionConfig.newConfig(statisticsService, StatisticsCollector.class)
							.setProperty(StatisticsCollector.DATAADDRESS, stockmarketAgentName + ":" + "data"))
					.addCellfunction(CellFunctionConfig.newConfig("EvaluatorService", Evaluator.class)
							.setProperty(Evaluator.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
							.setProperty(Evaluator.ATTRIBUTEEXECUTIONORDER, 2)
							.setProperty(Evaluator.STATISTICSCOLLECTORSERVICENAME, brokerAgentName + ":" + statisticsService + "/" + StatisticsCollector.GETSTATISTICSSUFFIX)
							.setProperty(Evaluator.STATISTICSDATAPOINTNAME, statisticsDatapointName))
					.addCellfunction(CellFunctionConfig.newConfig("TypesGraph", DepotStaticticsGraphToolFunction.class)
							.addManagedDatapoint(statisticsDatapointName, SyncMode.SUBSCRIBEONLY)));

			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}

			// === Stock market ===//

			Cell stockMarketAgent = this.controller.createAgent(CellConfig.newConfig(stockmarketAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(stockmarketServiceName, DummyPriceGenerator.class)
							.setProperty(DummyPriceGenerator.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
							.setProperty(DummyPriceGenerator.ATTRIBUTEEXECUTIONORDER, 0) 		// First, the stock market generates a price, run order 0
							.setProperty(DummyPriceGenerator.ATTRIBUTEMODE, 0)					//1=constant, 0=sin
							.setProperty(DummyPriceGenerator.ATTRIBUTESTOCKNAME, stockName))
					.addCellfunction(CellFunctionConfig.newConfig("OHLCGraph", PriceGraphToolFunction.class) // Stock market graph
							.addManagedDatapoint("Fingdata", "data", SyncMode.SUBSCRIBEONLY))); // Puts data on datapoint StockMarketAgent:data); // Puts data on datapoint StockMarketAgent:data

			// === Traders ===//

			// Create 100 trading agents that first buy a stock, then sell it
//			for (int i = 1; i <= 0; i++) {
//				String traderType = "type";
//				if (i % 3 == 0) {
//					traderType += "_even";
//				} else {
//					traderType += "_odd";
//				}
//
//				Cell traderAgent = this.controller.createAgent(CellConfig.newConfig(traderAgentName + "_" + i)
//						.addCellfunction(CellFunctionConfig.newConfig("trader_" + i, Trader.class)
//								.setProperty(Trader.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
//								.setProperty(Trader.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
//								.setProperty(Trader.ATTRIBUTEAGENTTYPE, traderType)
//								.setProperty(Trader.ATTRIBUTESIGNALADDRESS, signalService)
//								.setProperty(Trader.ATTRIBUTEEXECUTIONORDER, 1) // Second, the traderstrade
//								.setProperty(Trader.ATTRIBUTEBROKERADDRESS, brokerAgentName + ":" + brokerServiceName))
//						//.addCellfunction(CellFunctionConfig.newConfig(signalService, PermanentBuySellIndicator.class)));
//						.addCellfunction(CellFunctionConfig.newConfig(signalService, RandomBuySellIndicator.class)));
//			}
			
			List<String> l = new ArrayList<String>();
			for (int i = 1; i <= 50; i++) {
				
				int longMA = 0;
				int shortMA = 0;
				boolean breaker = true;
				String key = "LS";
				do {
					longMA = (int)(Math.random()*100);
					shortMA = (int)(Math.random()*longMA);
					key = "L" + longMA + "S" + shortMA;
					if (l.contains(key)==true) {
						log.info("L {}, S {} already exists", longMA, shortMA);
						breaker=false;
					} else {
						breaker=true;
						log.info("Added agent, L {}, S {}", longMA, shortMA);
						break;
					}
				} while (breaker==false);
				
				l.add(key);
				String traderType = key;
	
				Cell traderAgent = this.controller.createAgent(CellConfig.newConfig(traderAgentName + "_" + traderType)
						.addCellfunction(CellFunctionConfig.newConfig("trader_" + traderType, Trader.class)
								.setProperty(Trader.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
								.setProperty(Trader.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
								.setProperty(Trader.ATTRIBUTEAGENTTYPE, traderType)
								.setProperty(Trader.ATTRIBUTESIGNALADDRESS, signalService)
								.setProperty(Trader.ATTRIBUTEEXECUTIONORDER, 1) // Second, the traderstrade
								.setProperty(Trader.ATTRIBUTEBROKERADDRESS, brokerAgentName + ":" + brokerServiceName))
						//.addCellfunction(CellFunctionConfig.newConfig(signalService, PermanentBuySellIndicator.class)));
						.addCellfunction(CellFunctionConfig.newConfig(signalService, EMAIndicator.class)
								.setProperty(EMAIndicator.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
								.setProperty(EMAIndicator.ATTRIBUTEEMALONG, longMA)
								.setProperty(EMAIndicator.ATTRIBUTEEMASHORT, shortMA)));
			}
			
//			Cell traderAgentRand = this.controller.createAgent(CellConfig.newConfig(traderAgentName + "_" + "Rnd")
//			.addCellfunction(CellFunctionConfig.newConfig("trader_" + "Rnd", Trader.class)
//					.setProperty(Trader.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
//					.setProperty(Trader.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
//					.setProperty(Trader.ATTRIBUTEAGENTTYPE, "Random")
//					.setProperty(Trader.ATTRIBUTESIGNALADDRESS, signalService)
//					.setProperty(Trader.ATTRIBUTEEXECUTIONORDER, 1) // Second, the traderstrade
//					.setProperty(Trader.ATTRIBUTEBROKERADDRESS, brokerAgentName + ":" + brokerServiceName))
//			//.addCellfunction(CellFunctionConfig.newConfig(signalService, PermanentBuySellIndicator.class)));
//			.addCellfunction(CellFunctionConfig.newConfig(signalService, RandomBuySellIndicator.class)));
			
//			Cell traderAgent2 = this.controller.createAgent(CellConfig.newConfig(traderAgentName + "_" + "EMA1020")
//					.addCellfunction(CellFunctionConfig.newConfig("trader_1020", Trader.class)
//							.setProperty(Trader.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
//							.setProperty(Trader.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
//							.setProperty(Trader.ATTRIBUTEAGENTTYPE, "EMA1020")
//							.setProperty(Trader.ATTRIBUTESIGNALADDRESS, signalService)
//							.setProperty(Trader.ATTRIBUTEEXECUTIONORDER, 1) // Second, the traderstrade
//							.setProperty(Trader.ATTRIBUTEBROKERADDRESS, brokerAgentName + ":" + brokerServiceName))
//					//.addCellfunction(CellFunctionConfig.newConfig(signalService, PermanentBuySellIndicator.class)));
//					//.addCellfunction(CellFunctionConfig.newConfig(signalService, RandomBuySellIndicator.class)));
//					.addCellfunction(CellFunctionConfig.newConfig(signalService, EMAIndicator.class)
//							.setProperty(EMAIndicator.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
//							.setProperty(EMAIndicator.ATTRIBUTEEMALONG, 20)
//							.setProperty(EMAIndicator.ATTRIBUTEEMASHORT, 10)));
//			
//			Cell traderAgent3 = this.controller.createAgent(CellConfig.newConfig(traderAgentName + "_" + "EMA05200")
//					.addCellfunction(CellFunctionConfig.newConfig("trader_05200", Trader.class)
//							.setProperty(Trader.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
//							.setProperty(Trader.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
//							.setProperty(Trader.ATTRIBUTEAGENTTYPE, "EMA05200")
//							.setProperty(Trader.ATTRIBUTESIGNALADDRESS, signalService)
//							.setProperty(Trader.ATTRIBUTEEXECUTIONORDER, 1) // Second, the traderstrade
//							.setProperty(Trader.ATTRIBUTEBROKERADDRESS, brokerAgentName + ":" + brokerServiceName))
//					//.addCellfunction(CellFunctionConfig.newConfig(signalService, PermanentBuySellIndicator.class)));
//					//.addCellfunction(CellFunctionConfig.newConfig(signalService, RandomBuySellIndicator.class)));
//					.addCellfunction(CellFunctionConfig.newConfig(signalService, EMAIndicator.class)
//							.setProperty(EMAIndicator.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
//							.setProperty(EMAIndicator.ATTRIBUTEEMALONG, 30)
//							.setProperty(EMAIndicator.ATTRIBUTEEMASHORT, 2)));
			
			//Jsersey server to receive commands
			CellConfig server = CellConfig.newConfig(serverAgentName)
					// Here a codelethandler is used. The agents are codelets of the codelet handler. Agents
					.addCellfunction(CellFunctionConfig.newConfig("jerseyserver", JerseyRestServer.class)
							.setProperty(EvolutionService.PARAMAGENTNAMES, controllerAgent.getName())
							.setProperty(EvolutionService.PARAMCONTROLLERADDRESS, controllerAgent + ":" + controllerService));

			//Cell serverCell = this.controller.createAgent(server);
					
			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {

				}
			}
			
			JsonElement value = controllerAgent.getCommunicator().read(controllerAgent.getName() + ":" + controllerService + "/" + CellFunctionCodeletHandler.EXTENDEDSTATESUFFIX).getValue();
			log.info("Registered codelets: {}", value);
			
			log.info("=== All agents initialized ===");
			
			for (int i = 1; i <= 10000; i++) {
				//if (this.runAllowed == true) {
					log.info("run {}/{}", i, 10000);
					// Execute the codelet handler once
					controllerAgent.getCommunicator().execute(controllerAgent.getName() + ":" + controllerService + "/" + CellFunctionCodeletHandler.EXECUTECODELETMETHODNAME, new Request(), 200000);

				//} else {
				//	log.warn("Running of simulator interrupted after {} runs", i);
				//	break;
				//}

			}

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
