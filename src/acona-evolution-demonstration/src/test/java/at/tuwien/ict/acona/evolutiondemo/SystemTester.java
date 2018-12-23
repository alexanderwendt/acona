package at.tuwien.ict.acona.evolutiondemo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import at.tuwien.ict.acona.evolutiondemo.brokeragent.Broker;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.Depot;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.DepotStaticticsGraphToolFunction;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.Evaluator;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.StatisticsCollector;
import at.tuwien.ict.acona.evolutiondemo.controlleragent.ConsoleRequestReceiver;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.SpeciesType;
import at.tuwien.ict.acona.evolutiondemo.stockmarketagent.DummyPriceGenerator;
import at.tuwien.ict.acona.evolutiondemo.stockmarketagent.PriceGraphToolFunction;
import at.tuwien.ict.acona.evolutiondemo.traderagent.EMAIndicator;
import at.tuwien.ict.acona.evolutiondemo.traderagent.PermanentBuySellIndicator;
import at.tuwien.ict.acona.evolutiondemo.traderagent.Trader;
import at.tuwien.ict.acona.evolutiondemo.webserver.EvolutionService;
import at.tuwien.ict.acona.evolutiondemo.webserver.JerseyRestServer;
import at.tuwien.ict.acona.mq.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.mq.cell.cellfunction.codelets.CellFunctionCodeletHandler;
import at.tuwien.ict.acona.mq.cell.cellfunction.specialfunctions.SimpleReproduction;
import at.tuwien.ict.acona.mq.cell.config.CellConfig;
import at.tuwien.ict.acona.mq.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.mq.cell.core.Cell;
import at.tuwien.ict.acona.mq.datastructures.DPBuilder;
import at.tuwien.ict.acona.mq.datastructures.Datapoint;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;
import at.tuwien.ict.acona.mq.launcher.SystemControllerImpl;

public class SystemTester {

	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final DPBuilder dpb = new DPBuilder();
	private SystemControllerImpl launcher = SystemControllerImpl.getLauncher();

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
		this.launcher.stopSystem();

		synchronized (this) {
			try {
				this.wait(10);
			} catch (InterruptedException e) {

			}
		}
	}

	/**
	 * 
	 * 
	 */
	@Test
	public void CompleteSystemReproduceTester() {
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
			
			String reproduceFunction = "reproduce";
			
			// === Server === //
			String serverAgentName = "Server";
			

			// === Controller agent implementation === //
			Cell controllerAgent = this.launcher.createAgent(CellConfig.newConfig(controllerAgentName)
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

			Cell brokerAgent = this.launcher.createAgent(CellConfig.newConfig(brokerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(brokerServiceName, Broker.class)
							.setProperty(Broker.ATTRIBUTESTOCKNAME, stockName)
							.setProperty(Broker.ATTRIBUTECOMMISSION, 0.0025))
					.addCellfunction(CellFunctionConfig.newConfig(statisticsService, StatisticsCollector.class)
							.setProperty(StatisticsCollector.DATAADDRESS, stockmarketAgentName + ":" + "data"))
					.addCellfunction(CellFunctionConfig.newConfig("EvaluatorService", Evaluator.class)
							.setProperty(Evaluator.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
							.setProperty(Evaluator.ATTRIBUTEEXECUTIONORDER, 2)
							.setProperty(Evaluator.STATISTICSCOLLECTORSERVICENAME, brokerAgentName + ":" + statisticsService + "/" + StatisticsCollector.GETSTATISTICSSUFFIX)
							.setProperty(Evaluator.STATISTICSDATAPOINTNAME, statisticsDatapointName)));
					//.addCellfunction(CellFunctionConfig.newConfig("TypesGraph", DepotStaticticsGraphToolFunction.class)
					//		.addManagedDatapoint(statisticsDatapointName, SyncMode.SUBSCRIBEONLY)));

			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}

			// === Stock market ===//

			Cell stockMarketAgent = this.launcher.createAgent(CellConfig.newConfig(stockmarketAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(stockmarketServiceName, DummyPriceGenerator.class)
							.setProperty(DummyPriceGenerator.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
							.setProperty(DummyPriceGenerator.ATTRIBUTEEXECUTIONORDER, 0) 		// First, the stock market generates a price, run order 0
							.setProperty(DummyPriceGenerator.ATTRIBUTEMODE, 0)					//1=constant, 0=sin
							.setProperty(DummyPriceGenerator.ATTRIBUTESTOCKNAME, stockName)));
					//.addCellfunction(CellFunctionConfig.newConfig("OHLCGraph", PriceGraphToolFunction.class) // Stock market graph
					//		.addManagedDatapoint("Fingdata", "data", SyncMode.SUBSCRIBEONLY))); // Puts data on datapoint StockMarketAgent:data); // Puts data on datapoint StockMarketAgent:data

			// === Traders ===//

			// Create 100 trading agents that first buy a stock, then sell it
//						for (int i = 1; i <= 0; i++) {
//							String traderType = "type";
//							if (i % 3 == 0) {
//								traderType += "_even";
//							} else {
//								traderType += "_odd";
//							}
//
//							Cell traderAgent = this.controller.createAgent(CellConfig.newConfig(traderAgentName + "_" + i)
//									.addCellfunction(CellFunctionConfig.newConfig("trader_" + i, Trader.class)
//											.setProperty(Trader.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
//											.setProperty(Trader.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
//											.setProperty(Trader.ATTRIBUTEAGENTTYPE, traderType)
//											.setProperty(Trader.ATTRIBUTESIGNALADDRESS, signalService)
//											.setProperty(Trader.ATTRIBUTEEXECUTIONORDER, 1) // Second, the traderstrade
//											.setProperty(Trader.ATTRIBUTEBROKERADDRESS, brokerAgentName + ":" + brokerServiceName))
//									//.addCellfunction(CellFunctionConfig.newConfig(signalService, PermanentBuySellIndicator.class)));
//									.addCellfunction(CellFunctionConfig.newConfig(signalService, RandomBuySellIndicator.class)));
//						}
			
//			List<String> l = new ArrayList<String>();
//			for (int i = 1; i <= 50; i++) {
//				
//				int longMA = 0;
//				int shortMA = 0;
//				boolean breaker = true;
//				String key = "LS";
//				do {
//					longMA = (int)(Math.random()*100);
//					shortMA = (int)(Math.random()*longMA);
//					key = "L" + longMA + "S" + shortMA;
//					if (l.contains(key)==true) {
//						log.info("L {}, S {} already exists", longMA, shortMA);
//						breaker=false;
//					} else {
//						breaker=true;
//						log.info("Added agent, L {}, S {}", longMA, shortMA);
//						break;
//					}
//				} while (breaker==false);
//				
//				l.add(key);
//				String traderType = key;
//	
//				Cell traderAgent = this.launcher.createAgent(CellConfig.newConfig(traderAgentName + "_" + traderType)
//						.addCellfunction(CellFunctionConfig.newConfig("trader_" + traderType, Trader.class)
//								.setProperty(Trader.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
//								.setProperty(Trader.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
//								.setProperty(Trader.ATTRIBUTEAGENTTYPE, traderType)
//								.setProperty(Trader.ATTRIBUTESIGNALADDRESS, signalService)
//								.setProperty(Trader.ATTRIBUTEEXECUTIONORDER, 1) // Second, the traderstrade
//								.setProperty(Trader.ATTRIBUTEBROKERADDRESS, brokerAgentName + ":" + brokerServiceName))
//						//.addCellfunction(CellFunctionConfig.newConfig(signalService, PermanentBuySellIndicator.class)));
//						.addCellfunction(CellFunctionConfig.newConfig(signalService, EMAIndicator.class)
//								.setProperty(EMAIndicator.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
//								.setProperty(EMAIndicator.ATTRIBUTEEMALONG, longMA)
//								.setProperty(EMAIndicator.ATTRIBUTEEMASHORT, shortMA)));
//			}
			
			Cell traderAgentRepro = this.launcher.createAgent(CellConfig.newConfig(traderAgentName + "_" + "L33S11")
			.addCellfunction(CellFunctionConfig.newConfig("trader_" + "L33S11", Trader.class)
					.setProperty(Trader.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
					.setProperty(Trader.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
					.setProperty(Trader.ATTRIBUTEAGENTTYPE, "L33S11")
					.setProperty(Trader.ATTRIBUTESIGNALADDRESS, signalService)
					.setProperty(Trader.ATTRIBUTEEXECUTIONORDER, 1) // Second, the traderstrade
					.setProperty(Trader.ATTRIBUTEBROKERADDRESS, brokerAgentName + ":" + brokerServiceName)
					.setProperty(Trader.ATTRIBUTEMULTIPLY, true))
			//.addCellfunction(CellFunctionConfig.newConfig(signalService, PermanentBuySellIndicator.class)));
			//.addCellfunction(CellFunctionConfig.newConfig(signalService, RandomBuySellIndicator.class)));
			.addCellfunction(CellFunctionConfig.newConfig(signalService, EMAIndicator.class)
					.setProperty(EMAIndicator.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
					.setProperty(EMAIndicator.ATTRIBUTEEMALONG, 33)
					.setProperty(EMAIndicator.ATTRIBUTEEMASHORT, 11))
			.addCellfunction(CellFunctionConfig.newConfig(reproduceFunction, SimpleReproduction.class)));
			
//						Cell traderAgent2 = this.controller.createAgent(CellConfig.newConfig(traderAgentName + "_" + "EMA1020")
//								.addCellfunction(CellFunctionConfig.newConfig("trader_1020", Trader.class)
//										.setProperty(Trader.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
//										.setProperty(Trader.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
//										.setProperty(Trader.ATTRIBUTEAGENTTYPE, "EMA1020")
//										.setProperty(Trader.ATTRIBUTESIGNALADDRESS, signalService)
//										.setProperty(Trader.ATTRIBUTEEXECUTIONORDER, 1) // Second, the traderstrade
//										.setProperty(Trader.ATTRIBUTEBROKERADDRESS, brokerAgentName + ":" + brokerServiceName))
//								//.addCellfunction(CellFunctionConfig.newConfig(signalService, PermanentBuySellIndicator.class)));
//								//.addCellfunction(CellFunctionConfig.newConfig(signalService, RandomBuySellIndicator.class)));
//								.addCellfunction(CellFunctionConfig.newConfig(signalService, EMAIndicator.class)
//										.setProperty(EMAIndicator.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
//										.setProperty(EMAIndicator.ATTRIBUTEEMALONG, 20)
//										.setProperty(EMAIndicator.ATTRIBUTEEMASHORT, 10)));
//						
//						Cell traderAgent3 = this.controller.createAgent(CellConfig.newConfig(traderAgentName + "_" + "EMA05200")
//								.addCellfunction(CellFunctionConfig.newConfig("trader_05200", Trader.class)
//										.setProperty(Trader.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
//										.setProperty(Trader.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
//										.setProperty(Trader.ATTRIBUTEAGENTTYPE, "EMA05200")
//										.setProperty(Trader.ATTRIBUTESIGNALADDRESS, signalService)
//										.setProperty(Trader.ATTRIBUTEEXECUTIONORDER, 1) // Second, the traderstrade
//										.setProperty(Trader.ATTRIBUTEBROKERADDRESS, brokerAgentName + ":" + brokerServiceName))
//								//.addCellfunction(CellFunctionConfig.newConfig(signalService, PermanentBuySellIndicator.class)));
//								//.addCellfunction(CellFunctionConfig.newConfig(signalService, RandomBuySellIndicator.class)));
//								.addCellfunction(CellFunctionConfig.newConfig(signalService, EMAIndicator.class)
//										.setProperty(EMAIndicator.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
//										.setProperty(EMAIndicator.ATTRIBUTEEMALONG, 30)
//										.setProperty(EMAIndicator.ATTRIBUTEEMASHORT, 2)));
			
			//Jsersey server to receive commands
//			CellConfig server = CellConfig.newConfig(serverAgentName)
//					// Here a codelethandler is used. The agents are codelets of the codelet handler. Agents
//					.addCellfunction(CellFunctionConfig.newConfig("jerseyserver", JerseyRestServer.class)
//							.setProperty(EvolutionService.PARAMAGENTNAMES, controllerAgent.getName())
//							.setProperty(EvolutionService.PARAMCONTROLLERADDRESS, controllerAgent + ":" + controllerService));

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
			int total=100000;
			for (int i = 1; i <= total; i++) {
				long starttime=System.currentTimeMillis();
				//if (this.runAllowed == true) {
				
					// Execute the codelet handler once
				controllerAgent.getCommunicator().execute(controllerAgent.getName() + ":" + controllerService + "/" + CellFunctionCodeletHandler.EXECUTECODELETMETHODNAME, new Request(), 400000);

				//} else {
				//	log.warn("Running of simulator interrupted after {} runs", i);
				//	break;
				//}
				long endTime=System.currentTimeMillis() - starttime;
				log.info("run {}/{}. Duration={}s", i, total, ((double)endTime)/1000);

			}
			
			synchronized (this) {
				try {
					this.wait(2000000);
				} catch (InterruptedException e) {

				}
			}

			log.info("Got money from agent 19={}. Correct answer={}", 1001, 1000);
			assertEquals(1000, 1001);
			log.info("All tests passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}
	}

}
