package at.tuwien.ict.acona.evolutiondemo.launcher;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.evolutiondemo.brokeragent.Broker;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.DepotStaticticsGraphToolFunction;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.Evaluator;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.StatisticsCollector;
import at.tuwien.ict.acona.evolutiondemo.controlleragent.ConsoleRequestReceiver;
import at.tuwien.ict.acona.evolutiondemo.stockmarketagent.DummyPriceGenerator;
import at.tuwien.ict.acona.evolutiondemo.stockmarketagent.PriceGraphToolFunction;
import at.tuwien.ict.acona.evolutiondemo.traderagent.PermanentBuySellIndicator;
import at.tuwien.ict.acona.evolutiondemo.traderagent.Trader;
import at.tuwien.ict.acona.mq.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.mq.cell.cellfunction.codelets.CellFunctionCodeletHandler;
import at.tuwien.ict.acona.mq.cell.config.CellConfig;
import at.tuwien.ict.acona.mq.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.mq.cell.core.Cell;
import at.tuwien.ict.acona.mq.datastructures.DPBuilder;
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
			// Start JADE
			//log.info("Start JADE");
			//this.startJade();

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

			String statisticsDatapointName = "stats";

			// === Traders ===//
			String traderAgentName = "TraderAgent";
			String signalService = "signal";

			// === Controller agent implementation === //
			Cell controllerAgent = this.controller.createAgent(CellConfig.newConfig(controllerAgentName)
					// Here a codelethandler is used. The agents are codelets of the codelet handler. Agents
					.addCellfunction(CellFunctionConfig.newConfig(controllerService, CellFunctionCodeletHandler.class))
					// The codelet handler ist controller request receiver funtion
					.addCellfunction(CellFunctionConfig.newConfig("userconsole", ConsoleRequestReceiver.class)
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
							.setProperty(Evaluator.STATISTICSCOLLECTORSERVICENAME, statisticsService)
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
							.setProperty(DummyPriceGenerator.ATTRIBUTEEXECUTIONORDER, 0) // First, the stock market generates a price
							.setProperty(DummyPriceGenerator.ATTRIBUTEMODE, 1)
							.setProperty(DummyPriceGenerator.ATTRIBUTESTOCKNAME, stockName))
					.addCellfunction(CellFunctionConfig.newConfig("OHLCGraph", PriceGraphToolFunction.class) // Stock market graph
							.addManagedDatapoint("Fingdata", "data", SyncMode.SUBSCRIBEONLY))); // Puts data on datapoint StockMarketAgent:data); // Puts data on datapoint StockMarketAgent:data

			// === Traders ===//

			// Create 100 trading agents that first buy a stock, then sell it
			for (int i = 1; i <= 10; i++) {
				String traderType = "type";
				if (i % 3 == 0) {
					traderType += "_even";
				} else {
					traderType += "_odd";
				}

				Cell traderAgent = this.controller.createAgent(CellConfig.newConfig(traderAgentName + "_" + i)
						.addCellfunction(CellFunctionConfig.newConfig("trader_" + i, Trader.class)
								.setProperty(Trader.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
								.setProperty(Trader.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
								.setProperty(Trader.ATTRIBUTEAGENTTYPE, traderType)
								.setProperty(Trader.ATTRIBUTESIGNALADDRESS, signalService)
								.setProperty(Trader.ATTRIBUTEEXECUTIONORDER, 1) // Second, the traderstrade
								.setProperty(Trader.ATTRIBUTEBROKERADDRESS, brokerAgentName + ":" + brokerServiceName))
						.addCellfunction(CellFunctionConfig.newConfig(signalService, PermanentBuySellIndicator.class)));
			}

			synchronized (this) {
				try {
					this.wait(3000);
				} catch (InterruptedException e) {

				}
			}

			log.info("=== All agents initialized ===");

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
