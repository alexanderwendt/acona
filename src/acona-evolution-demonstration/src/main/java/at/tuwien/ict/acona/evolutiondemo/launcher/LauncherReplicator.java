package at.tuwien.ict.acona.evolutiondemo.launcher;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.evolutiondemo.brokeragent.Broker;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.DepotStaticticsGraphToolFunction;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.Evaluator;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.StatisticsCollector;
import at.tuwien.ict.acona.evolutiondemo.controlleragent.ConsoleRequestReceiver;
import at.tuwien.ict.acona.evolutiondemo.stockmarketagent.PriceGraphToolFunction;
import at.tuwien.ict.acona.evolutiondemo.stockmarketagent.PriceLoaderGenerator;
import at.tuwien.ict.acona.evolutiondemo.traderagent.EMAIndicator;
import at.tuwien.ict.acona.evolutiondemo.traderagent.Trader;
import at.tuwien.ict.acona.mq.core.agentfunction.SyncMode;
import at.tuwien.ict.acona.mq.core.agentfunction.codelets.CodeletHandlerImpl;
import at.tuwien.ict.acona.mq.core.agentfunction.specialfunctions.SimpleReproduction;
import at.tuwien.ict.acona.mq.core.config.AgentConfig;
import at.tuwien.ict.acona.mq.core.config.FunctionConfig;
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
public class LauncherReplicator {

	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final DPBuilder dpb = new DPBuilder();
	private SystemControllerImpl controller = SystemControllerImpl.getLauncher();
	
	private static LauncherReplicator launcher;

	public static void main(String[] args) {
		log.info("Welcome to the ACONA Stock Market Evolution Demonstrator");

		launcher = new LauncherReplicator();
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
			String traderAgentName = "T";
			String signalService = "signal";
			
			String reproduceFunction = "reproduce";
			
			// === Server === //
			String serverAgentName = "Server";
			

			// === Controller agent implementation === //
			Cell controllerAgent = this.controller.createAgent(AgentConfig.newConfig(controllerAgentName)
					// Here a codelethandler is used. The agents are codelets of the codelet handler. Agents
					.addFunction(FunctionConfig.newConfig(controllerService, CodeletHandlerImpl.class))
					// The codelet handler ist controller request receiver funtion
					.addFunction(FunctionConfig.newConfig("controller", ConsoleRequestReceiver.class)
							.setProperty(ConsoleRequestReceiver.ATTRIBUTECONTROLLERSERVICE, controllerService)));

			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}

			// === Broker ===//

			Cell brokerAgent = this.controller.createAgent(AgentConfig.newConfig(brokerAgentName)
					.addFunction(FunctionConfig.newConfig(brokerServiceName, Broker.class)
							.setProperty(Broker.ATTRIBUTESTOCKNAME, stockName)
							.setProperty(Broker.ATTRIBUTECOMMISSION, 0.0025)
							.setProperty(Broker.PARAMPRICESOURCE, stockmarketAgentName + ":" + "data"))
					.addFunction(FunctionConfig.newConfig(statisticsService, StatisticsCollector.class)
							.setProperty(StatisticsCollector.DATAADDRESS, stockmarketAgentName + ":" + "data"))
					.addFunction(FunctionConfig.newConfig("EvaluatorService", Evaluator.class)
							.setProperty(Evaluator.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
							.setProperty(Evaluator.ATTRIBUTEEXECUTIONORDER, 100)
							.setProperty(Evaluator.STATISTICSCOLLECTORSERVICENAME, brokerAgentName + ":" + statisticsService + "/" + StatisticsCollector.GETSTATISTICSSUFFIX)
							.setProperty(Evaluator.STATISTICSDATAPOINTNAME, statisticsDatapointName))
					.addFunction(FunctionConfig.newConfig("TypesGraph", DepotStaticticsGraphToolFunction.class)
							.addManagedDatapoint(statisticsDatapointName, SyncMode.SUBSCRIBEONLY)));

			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}

			// === Stock market ===//

			Cell stockMarketAgent = this.controller.createAgent(AgentConfig.newConfig(stockmarketAgentName)
					//.addFunction(CellFunctionConfig.newConfig(stockmarketServiceName, DummyPriceGenerator.class)
					//		.setProperty(DummyPriceGenerator.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
					//		.setProperty(DummyPriceGenerator.ATTRIBUTEEXECUTIONORDER, 0) 		// First, the stock market generates a price, run order 0
					//		.setProperty(DummyPriceGenerator.ATTRIBUTEMODE, 0)					//1=constant, 0=sin
					//		.setProperty(DummyPriceGenerator.ATTRIBUTESTOCKNAME, stockName))
					.addFunction(FunctionConfig.newConfig(stockmarketServiceName, PriceLoaderGenerator.class, Map.of(
							PriceLoaderGenerator.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService,
							PriceLoaderGenerator.ATTRIBUTEEXECUTIONORDER, 0,
							PriceLoaderGenerator.ATTRIBUTESTOCKNAME, stockName)))	// First, the stock market generates a price, run order 0
					.addFunction(FunctionConfig.newConfig("OHLCGraph", PriceGraphToolFunction.class) // Stock market graph
							.addManagedDatapoint("Fingdata", "data", SyncMode.SUBSCRIBEONLY))); // Puts data on datapoint StockMarketAgent:data); // Puts data on datapoint StockMarketAgent:data

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
			
			//int cell1ShortMA = 50;
			//int cell1LongMA = 200;
			
			int cell1ShortMA = 91;
			int cell1LongMA = 95;
			
			Cell traderAgentRepro1 = this.controller.createAgent(AgentConfig.newConfig(traderAgentName + "_" + "L" + cell1LongMA +  "S" + cell1ShortMA)
			.addFunction(FunctionConfig.newConfig("TraderFunction", Trader.class)
					.setProperty(Trader.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
					.setProperty(Trader.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
					.setProperty(Trader.ATTRIBUTEAGENTTYPE, "L" + cell1LongMA +  "S" + cell1ShortMA)
					.setProperty(Trader.ATTRIBUTESIGNALADDRESS, signalService)
					.setProperty(Trader.ATTRIBUTEEXECUTIONORDER, 1) // Second, the traderstrade
					.setProperty(Trader.ATTRIBUTEBROKERADDRESS, brokerAgentName + ":" + brokerServiceName)
					.setProperty(Trader.ATTRIBUTEMULTIPLY, true)
					.setProperty(Trader.ATTRIBUTEMUTATE, false))
			//.addCellfunction(CellFunctionConfig.newConfig(signalService, PermanentBuySellIndicator.class)));
			//.addCellfunction(CellFunctionConfig.newConfig(signalService, RandomBuySellIndicator.class)));
			.addFunction(FunctionConfig.newConfig(signalService, EMAIndicator.class)
					.setProperty(EMAIndicator.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
					.setProperty(EMAIndicator.ATTRIBUTEEMALONG, cell1LongMA)
					.setProperty(EMAIndicator.ATTRIBUTEEMASHORT, cell1ShortMA))
			.addFunction(FunctionConfig.newConfig(reproduceFunction, SimpleReproduction.class)));
			
			
//			int cell2ShortMA = 10;
//			int cell2LongMA = 50;
//			
//			Cell traderAgentRepro2 = this.controller.createAgent(CellConfig.newConfig(traderAgentName + "_" + "L" + cell2LongMA +  "S" + cell2ShortMA)
//			.addFunction(CellFunctionConfig.newConfig("trader_" + "L" + cell2LongMA +  "S" + cell2ShortMA, Trader.class, Map.of(
//					Trader.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService,
//					Trader.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data",
//					Trader.ATTRIBUTEAGENTTYPE, "L" + cell2LongMA +  "S" + cell2ShortMA,
//					Trader.ATTRIBUTESIGNALADDRESS, signalService,
//					Trader.ATTRIBUTEEXECUTIONORDER, 1,
//					Trader.ATTRIBUTEBROKERADDRESS, brokerAgentName + ":" + brokerServiceName,
//					Trader.ATTRIBUTEMULTIPLY, true)))
//			.addFunction(CellFunctionConfig.newConfig(signalService, EMAIndicator.class, Map.of(
//					EMAIndicator.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data",
//					EMAIndicator.ATTRIBUTEEMALONG, cell2LongMA,
//					EMAIndicator.ATTRIBUTEEMASHORT, cell2ShortMA)))
//			.addFunction(CellFunctionConfig.newConfig(reproduceFunction, SimpleReproduction.class)));
//			
//			int cell3ShortMA = 2;
//			int cell3LongMA = 20;
			
//			Cell traderAgentRepro3 = this.controller.createAgent(CellConfig.newConfig(traderAgentName + "_" + "L" + cell3LongMA +  "S" + cell3ShortMA)
//			.addFunction(CellFunctionConfig.newConfig("trader_" + "L" + cell3LongMA +  "S" + cell3ShortMA, Trader.class, Map.of(
//					Trader.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService,
//					Trader.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data",
//					Trader.ATTRIBUTEAGENTTYPE, "L" + cell3LongMA +  "S" + cell3ShortMA,
//					Trader.ATTRIBUTESIGNALADDRESS, signalService,
//					Trader.ATTRIBUTEEXECUTIONORDER, 1,
//					Trader.ATTRIBUTEBROKERADDRESS, brokerAgentName + ":" + brokerServiceName,
//					Trader.ATTRIBUTEMULTIPLY, true)))
//			.addFunction(CellFunctionConfig.newConfig(signalService, EMAIndicator.class, Map.of(
//					EMAIndicator.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data",
//					EMAIndicator.ATTRIBUTEEMALONG, cell3LongMA,
//					EMAIndicator.ATTRIBUTEEMASHORT, cell3ShortMA)))
//			.addFunction(CellFunctionConfig.newConfig(reproduceFunction, SimpleReproduction.class)));
			
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
			
			JsonElement value = controllerAgent.getCommunicator().read(controllerAgent.getName() + ":" + controllerService + "/" + CodeletHandlerImpl.EXTENDEDSTATESUFFIX).getValue();
			log.info("Registered codelets: {}", value);
			
			log.info("=== All agents initialized ===");
			int total=100000;
			for (int i = 1; i <= total; i++) {
				log.info("============= Start new run: {}/{}. ===============",i, total);
				long starttime=System.currentTimeMillis();
				//if (this.runAllowed == true) {
				
					// Execute the codelet handler once
				try {
					controllerAgent.getCommunicator().execute(controllerAgent.getName() + ":" + controllerService + "/" + CodeletHandlerImpl.EXECUTECODELETMETHODNAME, new Request(), 200000);
				} catch (Exception e) {
					log.error("Controller service timeout. Continue.", e);
				}
				

				//} else {
				//	log.warn("Running of simulator interrupted after {} runs", i);
				//	break;
				//}
				long endTime=System.currentTimeMillis() - starttime;
				log.info("Run {}/{} finished. Duration={}s", i, total, ((double)endTime)/1000);

			}
			
			synchronized (this) {
				try {
					this.wait(2000000);
				} catch (InterruptedException e) {

				}
			}

			log.info("Exit");
		} catch (Exception e) {
			log.error("Error testing system", e);
		}

	}

}
