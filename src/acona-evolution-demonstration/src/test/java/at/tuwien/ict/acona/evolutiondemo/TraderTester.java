package at.tuwien.ict.acona.evolutiondemo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.invoke.MethodHandles;
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
import at.tuwien.ict.acona.evolutiondemo.brokeragent.StatisticsCollector;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.SpeciesType;
import at.tuwien.ict.acona.evolutiondemo.stockmarketagent.DummyPriceGenerator;
import at.tuwien.ict.acona.evolutiondemo.traderagent.PermanentBuySellIndicator;
import at.tuwien.ict.acona.evolutiondemo.traderagent.Trader;
import at.tuwien.ict.acona.mq.cell.cellfunction.codelets.CellFunctionCodeletHandler;
import at.tuwien.ict.acona.mq.cell.config.CellConfig;
import at.tuwien.ict.acona.mq.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.mq.cell.core.Cell;
import at.tuwien.ict.acona.mq.datastructures.DPBuilder;
import at.tuwien.ict.acona.mq.datastructures.Datapoint;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;
import at.tuwien.ict.acona.mq.launcher.SystemControllerImpl;

public class TraderTester {

	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final DPBuilder dpb = new DPBuilder();
	private SystemControllerImpl controller = SystemControllerImpl.getLauncher();

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
		this.controller.stopSystem();

		synchronized (this) {
			try {
				this.wait(10);
			} catch (InterruptedException e) {

			}
		}
	}

	/**
	 * Create a trader agent with a mock function that buys if nothing has been bought yet and sells if something has been sold. Trigger the trader agent to execute 2 times. The first time, the amount of
	 * the stock and the price shall be defined. In the second run, everything shall be sold. Create the system with a controller that triggers the price generator. At new data from the price generator,
	 * the traders are triggered by the subscription of the new values from the price generator. After calculation, each trader reports finished to the controller.
	 */
	@Test
	public void buyAndSellFunctionTestAgenttest() {
		try {
			String brokerAgentName = "BrokerAgent";
			String traderAgentName = "TraderAgent";
			String stockmarketAgentName = "StockMarketAgent";
			String controllerAgentName = "ControllerAgent";
			// String traderType = "type1";
			String brokerServiceName = "BrokerService";
			String statisticsService = "statisticsService";
			String stockmarketServiceName = "StockMarketService";
			String controllerService = "controllerservice";
			String signalService = "signal";
			int numberOfAgents = 2000;

			String stockName = "Fingerprint";

			long startTimeSetup = System.currentTimeMillis();

			Cell controllerAgent = this.controller.createAgent(CellConfig.newConfig(controllerAgentName)
					.addFunction(CellFunctionConfig.newConfig(controllerService, CellFunctionCodeletHandler.class)));
			controllerAgent.getCommunicator().setDefaultTimeout(60000);

			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}

			Cell brokerAgent = this.controller.createAgent(CellConfig.newConfig(brokerAgentName)
					.addFunction(CellFunctionConfig.newConfig(brokerServiceName, Broker.class)
							.setProperty(Broker.ATTRIBUTECOMMISSION, 0.0025)
							.setProperty(Broker.PARAMPRICESOURCE, "data")
							.setProperty(Broker.ATTRIBUTESTOCKNAME, stockName))
					.addFunction(CellFunctionConfig.newConfig(statisticsService, StatisticsCollector.class)));

			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}

			Cell stockMarketAgent = this.controller.createAgent(CellConfig.newConfig(stockmarketAgentName)
					.addFunction(CellFunctionConfig.newConfig(stockmarketServiceName, DummyPriceGenerator.class)
							.setProperty(DummyPriceGenerator.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
							.setProperty(DummyPriceGenerator.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(DummyPriceGenerator.ATTRIBUTEMODE, 1)
							.setProperty(DummyPriceGenerator.ATTRIBUTESTOCKNAME, stockName))); // Puts data on datapoint StockMarketAgent:data

			// Create 100 trading agents that first buy a stock, then sell it
			for (int i = 1; i <= numberOfAgents; i++) {
				String traderType = "type";
				if (i % 2 == 0) {
					traderType += "_even";
				} else {
					traderType += "_odd";
				}

				Cell traderAgent = this.controller.createAgent(CellConfig.newConfig(traderAgentName + "_" + i)
						.addFunction(CellFunctionConfig.newConfig("trader_" + i, Trader.class)
								.setProperty(Trader.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
								.setProperty(Trader.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
								.setProperty(Trader.ATTRIBUTEAGENTTYPE, traderType)
								.setProperty(Trader.ATTRIBUTESIGNALADDRESS, signalService)
								.setProperty(Trader.ATTRIBUTEEXECUTIONORDER, 1)
								.setProperty(Trader.ATTRIBUTETIMEOUT, 60000)
								.setProperty(Trader.ATTRIBUTEBROKERADDRESS, brokerAgentName + ":" + brokerServiceName))
						.addFunction(CellFunctionConfig.newConfig(signalService, PermanentBuySellIndicator.class)));
				traderAgent.getCommunicator().setDefaultTimeout(60000);
			}

			long stopTimeSetup = System.currentTimeMillis();

			synchronized (this) {
				try {
					this.wait(10000);
				} catch (InterruptedException e) {

				}
			}

			log.info("=== All agents initialized ===");

			// Start the agents by starting the codelet handler
			// Buy the current stock
			long startTimeSystem = System.currentTimeMillis();
			//JsonRpcRequest req = new JsonRpcRequest(CellFunctionCodeletHandler.EXECUTECODELETEHANDLER, 1);
			//req.setParameterAsValue(0, false);
			//controllerAgent.getCommunicator().executeServiceQueryDatapoints(controllerAgent.getCell().getLocalName(), controllerService, req, controllerAgent.getCell().getLocalName(), controllerService + ".state", new JsonPrimitive(ServiceState.FINISHED.toString()), 60000);
			
			controllerAgent.getCommunicator().execute(controllerAgentName + ":" + controllerService + "/" + CellFunctionCodeletHandler.EXECUTECODELETMETHODNAME, new Request(), 200000);

			long stopTimeSystemTrade1 = System.currentTimeMillis();

			// Sell the current stock
			//req = new JsonRpcRequest(CellFunctionCodeletHandler.EXECUTECODELETEHANDLER, 1);
			//req.setParameterAsValue(0, false);
			//controllerAgent.getCommunicator().executeServiceQueryDatapoints(controllerAgent.getCell().getLocalName(), controllerService, req, controllerAgent.getCell().getLocalName(), controllerService + ".state", new JsonPrimitive(ServiceState.FINISHED.toString()), 60000);
			
			controllerAgent.getCommunicator().execute(controllerAgentName + ":" + controllerService + "/" + CellFunctionCodeletHandler.EXECUTECODELETMETHODNAME, new Request(), 200000);

			
			long stopTimeSystemTrade2 = System.currentTimeMillis();

			//req = new JsonRpcRequest("gettypes", 0);
			//JsonRpcResponse result = brokerAgent.getCommunicator().execute(brokerAgent.getCell().getLocalName(), statisticsService, req, 100000);
			
			Response result = brokerAgent.getCommunicator().execute(brokerAgent.getName() + ":" + statisticsService + "/" + "getstats", new Request(), 200000);

			
			List<SpeciesType> list = (new Gson()).fromJson(result.getResult().getAsJsonObject().get("types"), (new TypeToken<List<SpeciesType>>() {}.getType()));

			log.info("Got types={}", list);
			assertEquals(list.get(0).getNumber(), list.get(1).getNumber());

			// The test is to read the depot of agent 69, which shall be empty
			Depot depot = controllerAgent.getCommunicator().read(traderAgentName + "_" + "19" + ":" +  "localdepot").getValue(Depot.class);
			double money = depot.getLiquid();

			log.info("Setup duration={}, system execution duration 1 trade={}, system execution duration 2 trades={}", stopTimeSetup - startTimeSetup, stopTimeSystemTrade1 - startTimeSystem, stopTimeSystemTrade2 - startTimeSystem);

			log.info("Got money from agent 19={}. Correct answer={}", money, 1000);
			assertEquals(1000, money);
			log.info("All tests passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}
	}

//	/**
//	 * Create a trader agent with a mock function that buys if nothing has been bought yet and sells if something has been sold. Trigger the trader agent to execute 2 times. The first time, the amount of
//	 * the stock and the price shall be defined. In the second run, everything shall be sold. Create the system with a controller that triggers the price generator. At new data from the price generator,
//	 * the traders are triggered by the subscription of the new values from the price generator. After calculation, each trader reports finished to the controller.
//	 */
//	@Test
//	public void TEMPbuyAndSellFunctionTestAgenttest() {
//		try {
//			String brokerAgentName = "BrokerAgent";
//			String traderAgentName = "TraderAgent";
//			String stockmarketAgentName = "StockMarketAgent";
//			String controllerAgentName = "ControllerAgent";
//			// String traderType = "type1";
//			String brokerServiceName = "BrokerService";
//			String statisticsService = "statisticsService";
//			String stockmarketServiceName = "StockMarketService";
//			String controllerService = "controllerservice";
//			String signalService = "signal";
//			int numberOfAgents = 20;
//
//			String stockName = "Fingerprint";
//
//			long startTimeSetup = System.currentTimeMillis();
//
//			Cell controllerAgent = this.launcher.createAgent(CellConfig.newConfig(controllerAgentName)
//					.addCellfunction(CellFunctionConfig.newConfig(controllerService, CellFunctionCodeletHandler.class)));
//			controllerAgent.getCommunicator().setDefaultTimeout(60000);
//
//			synchronized (this) {
//				try {
//					this.wait(200);
//				} catch (InterruptedException e) {
//
//				}
//			}
//
//			Cell brokerAgent = this.launcher.createAgent(CellConfig.newConfig(brokerAgentName)
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
//
//			Cell stockMarketAgent = this.launcher.createAgent(CellConfig.newConfig(stockmarketAgentName)
//					.addCellfunction(CellFunctionConfig.newConfig(stockmarketServiceName, DummyPriceGenerator.class)
//							.setProperty(DummyPriceGenerator.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
//							.setProperty(DummyPriceGenerator.ATTRIBUTEEXECUTIONORDER, 0)
//							.setProperty(DummyPriceGenerator.ATTRIBUTEMODE, 1)
//							.setProperty(DummyPriceGenerator.ATTRIBUTESTOCKNAME, stockName))); // Puts data on datapoint StockMarketAgent:data
//
//			// Create 100 trading agents that first buy a stock, then sell it
//			for (int i = 1; i <= numberOfAgents; i++) {
//				String traderType = "type";
//				if (i % 2 == 0) {
//					traderType += "_even";
//				} else {
//					traderType += "_odd";
//				}
//
//				Cell traderAgent = this.launcher.createAgent(CellConfig.newConfig(traderAgentName + "_" + i)
//						.addCellfunction(CellFunctionConfig.newConfig("trader_" + i, Trader.class)
//								.setProperty(Trader.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
//								.setProperty(Trader.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
//								.setProperty(Trader.ATTRIBUTEAGENTTYPE, traderType)
//								.setProperty(Trader.ATTRIBUTESIGNALADDRESS, signalService)
//								.setProperty(Trader.ATTRIBUTEEXECUTIONORDER, 1)
//								.setProperty(Trader.ATTRIBUTETIMEOUT, 60000)
//								.setProperty(Trader.ATTRIBUTEBROKERADDRESS, brokerAgentName + ":" + brokerServiceName))
//						.addCellfunction(CellFunctionConfig.newConfig(signalService, PermanentBuySellIndicator.class)));
//				traderAgent.getCommunicator().setDefaultTimeout(60000);
//			}
//
//			long stopTimeSetup = System.currentTimeMillis();
//
//			synchronized (this) {
//				try {
//					this.wait(10000);
//				} catch (InterruptedException e) {
//
//				}
//			}
//
//			log.info("=== All agents initialized ===");
//
//			// Start the agents by starting the codelet handler
//			// Buy the current stock
//			long startTimeSystem = System.currentTimeMillis();
//			//JsonRpcRequest req = new JsonRpcRequest(CellFunctionCodeletHandler.EXECUTECODELETEHANDLER, 1);
//			//req.setParameterAsValue(0, false);
//			//controllerAgent.getCommunicator().executeServiceQueryDatapoints(controllerAgent.getCell().getLocalName(), controllerService, req, controllerAgent.getCell().getLocalName(), controllerService + ".state", new JsonPrimitive(ServiceState.FINISHED.toString()), 60000);
//			
//			controllerAgent.getCommunicator().execute(controllerAgentName + ":" + controllerService + "/" + CellFunctionCodeletHandler.EXECUTECODELETMETHODNAME, new Request(), 200000);
//			long stopTimeSystemTrade1 = System.currentTimeMillis();
//
//			// Sell the current stock
//			//req = new JsonRpcRequest(CellFunctionCodeletHandler.EXECUTECODELETEHANDLER, 1);
//			//req.setParameterAsValue(0, false);
//			//controllerAgent.getCommunicator().executeServiceQueryDatapoints(controllerAgent.getCell().getLocalName(), controllerService, req, controllerAgent.getCell().getLocalName(), controllerService + ".state", new JsonPrimitive(ServiceState.FINISHED.toString()), 60000);
//			
//			controllerAgent.getCommunicator().execute(controllerAgentName + ":" + controllerService + "/" + CellFunctionCodeletHandler.EXECUTECODELETMETHODNAME, new Request(), 200000);
//			long stopTimeSystemTrade2 = System.currentTimeMillis();
//
//			//req = new JsonRpcRequest("gettypes", 0);
//			//JsonRpcResponse result = brokerAgent.getCommunicator().execute(brokerAgent.getCell().getLocalName(), statisticsService, req, 100000);
//			
//			Response result = controllerAgent.getCommunicator().execute(brokerAgent + ":" + statisticsService + "/" + "getstats", new Request(), 200000);
//
//			List<Types> list = (new Gson()).fromJson(result.getResult().getAsJsonObject().get("type"), new TypeToken<List<Types>>() {}.getType());
//
//			log.info("Got types={}", list);
//			assertEquals(list.get(0).getNumber(), list.get(1).getNumber());
//
//			// The test is to read the depot of agent 69, which shall be empty
//			Depot depot = controllerAgent.getCommunicator().read(traderAgentName + "_" + "69" + ":" + "localdepot").getValue(Depot.class);
//			double money = depot.getLiquid();
//
//			log.info("Setup duration={}, system execution duration 1 trade={}, system execution duration 2 trades={}", stopTimeSetup - startTimeSetup, stopTimeSystemTrade1 - startTimeSystem, stopTimeSystemTrade2 - startTimeSystem);
//
//			log.info("Got money from agent 69={}. Correct answer={}", money, 1000);
//			assertEquals(1000, money, 0.0);
//			log.info("All tests passed");
//		} catch (Exception e) {
//			log.error("Error testing system", e);
//			fail("Error");
//		}
//	}

	/**
	 * Create a controller and one trader, the depot and a stock market. The trader agent shall use a moving average signal function to buy, when the faster moving average (use EMA) crosses from below and
	 * sell if the faster EMA crosses from above. The stock market shall use a sinus stock market function. The test is finished, as the trader agent has bought once and sold the stock.
	 */
	@Test
	public void buyAndSellMovingAvergeTester() {
		try {
			// === Agent names and services ===//
			String brokerAgentName = "BrokerAgent";
			String brokerServiceName = "BrokerService";
			String statisticsService = "statisticsService";

			String traderAgentName = "TraderAgent";
			String signalService = "SignalService";
			String indicatorEMALongService = "EMALongService";
			String indicatorEMAShortService = "EMAShort";
			String tradeService = "TradeService";
			String traderTypePrefix = "EMATrader";
			int EMALongPeriod = 10;
			int EMAShortPeriod = 5;

			String stockmarketAgentName = "StockMarketAgent";
			String stockmarketServiceName = "StockMarketService";

			String controllerAgentName = "ControllerAgent";
			String controllerService = "controllerservice";

			String stockName = "Fingerprint";

			// ==========================================//

			// Controller agent
			Cell controllerAgent = this.controller.createAgent(CellConfig.newConfig(controllerAgentName)
					.addFunction(CellFunctionConfig.newConfig(controllerService, CellFunctionCodeletHandler.class)));

			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}

			// Broker Agent
			Cell brokerAgent = this.controller.createAgent(CellConfig.newConfig(brokerAgentName)
					.addFunction(CellFunctionConfig.newConfig(brokerServiceName, Broker.class)
							.setProperty(Broker.ATTRIBUTECOMMISSION, 0.0025)
							.setProperty(Broker.PARAMPRICESOURCE, stockmarketAgentName + ":" + "data")
							.setProperty(Broker.ATTRIBUTESTOCKNAME, stockName))
					.addFunction(CellFunctionConfig.newConfig(statisticsService, StatisticsCollector.class)));

			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}

			// Stock market Agent
			Cell stockMarketAgent = this.controller.createAgent(CellConfig.newConfig(stockmarketAgentName)
					.addFunction(CellFunctionConfig.newConfig(stockmarketServiceName, DummyPriceGenerator.class)
							.setProperty(DummyPriceGenerator.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
							.setProperty(DummyPriceGenerator.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(DummyPriceGenerator.ATTRIBUTEMODE, 1)
							.setProperty(DummyPriceGenerator.ATTRIBUTESTOCKNAME, stockName))); // Puts data on datapoint StockMarketAgent:data

			// Single Trader agent
			Cell traderAgent = this.controller.createAgent(CellConfig.newConfig(traderAgentName)
					.addFunction(CellFunctionConfig.newConfig(tradeService, Trader.class)
							.setProperty(Trader.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
							.setProperty(Trader.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
							.setProperty(Trader.ATTRIBUTEAGENTTYPE, traderTypePrefix + "-L" + EMALongPeriod + ":S" + EMAShortPeriod)
							.setProperty(Trader.ATTRIBUTESIGNALADDRESS, signalService)
							.setProperty(Trader.ATTRIBUTEEXECUTIONORDER, 1)
							.setProperty(Trader.ATTRIBUTEBROKERADDRESS, brokerAgentName + ":" + brokerServiceName))
					.addFunction(CellFunctionConfig.newConfig(signalService, PermanentBuySellIndicator.class)));

			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {

				}
			}

			log.info("=== All agents initialized ===");

			// Start the agents by starting the codelet handler
			//JsonRpcRequest req = new JsonRpcRequest(CellFunctionCodeletHandler.EXECUTECODELETEHANDLER, 1);
			//req.setParameterAsValue(0, false);
			//controllerAgent.getCommunicator().executeServiceQueryDatapoints(controllerAgent.getCell().getLocalName(), controllerService, req, controllerAgent.getCell().getLocalName(), controllerService + ".state", new JsonPrimitive(ServiceState.FINISHED.toString()), 20000);
			
			controllerAgent.getCommunicator().execute(controllerAgentName + ":" + controllerService + "/" + CellFunctionCodeletHandler.EXECUTECODELETMETHODNAME, new Request(), 200000);
			
			
			//req = new JsonRpcRequest(CellFunctionCodeletHandler.EXECUTECODELETEHANDLER, 1);
			//req.setParameterAsValue(0, false);
			//controllerAgent.getCommunicator().executeServiceQueryDatapoints(controllerAgent.getCell().getLocalName(), controllerService, req, controllerAgent.getCell().getLocalName(), controllerService + ".state", new JsonPrimitive(ServiceState.FINISHED.toString()), 20000);

			controllerAgent.getCommunicator().execute(controllerAgentName + ":" + controllerService + "/" + CellFunctionCodeletHandler.EXECUTECODELETMETHODNAME, new Request(), 200000);
			
			//req = new JsonRpcRequest("gettypes", 0);
			//JsonRpcResponse result = brokerAgent.getCommunicator().execute(brokerAgent.getCell().getLocalName(), statisticsService, req, 100000);
			
			Response result = controllerAgent.getCommunicator().execute(brokerAgent.getName() + ":" + statisticsService + "/" + "getstats", new Request(), 200000);
			JsonElement a = result.getResult().getAsJsonObject().get("types");
			List<SpeciesType> list = (new Gson()).fromJson(a, new TypeToken<List<SpeciesType>>() {}.getType());

			log.info("Got types={}", list);
			assertEquals(list.get(0).getNumber(), 1);

			// The test is to read the depot of agent 69, which shall be empty
			Depot depot = controllerAgent.getCommunicator().read(traderAgent.getName() + ":" + "localdepot").getValue(Depot.class);
			double money = depot.getLiquid();

			log.info("Got money from agent={}. Correct answer={}", money, 1000);
			assertEquals(1000, money);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}
	}

	/**
	 * Create one trader with an account. Start the trader with permanent buy and sell. Remove all money from the depot. Then the agent shall delete itself.
	 */
	@Test
	public void killAgentOnMoneyLossTester() {
		try {
			// === Agent names and services ===//
			String brokerAgentName = "BrokerAgent";
			String brokerServiceName = "BrokerService";
			String statisticsService = "statisticsService";

			String traderAgentName = "TraderAgent";
			String signalService = "SignalService";
			String tradeService = "TradeService";

			String stockmarketAgentName = "StockMarketAgent";
			String stockmarketServiceName = "StockMarketService";

			String controllerAgentName = "ControllerAgent";
			String controllerService = "controllerservice";

			String stockName = "Fingerprint";

			// ==========================================//

			// Controller agent
			Cell controllerAgent = this.controller.createAgent(CellConfig.newConfig(controllerAgentName)
					.addFunction(CellFunctionConfig.newConfig(controllerService, CellFunctionCodeletHandler.class)));

			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}

			// Broker Agent
			Cell brokerAgent = this.controller.createAgent(CellConfig.newConfig(brokerAgentName)
					.addFunction(CellFunctionConfig.newConfig(brokerServiceName, Broker.class)
							.setProperty(Broker.ATTRIBUTECOMMISSION, 0.0025)
							.setProperty(Broker.PARAMPRICESOURCE, stockmarketAgentName + ":" + "data")
							.setProperty(Broker.ATTRIBUTESTOCKNAME, stockName))
					.addFunction(CellFunctionConfig.newConfig(statisticsService, StatisticsCollector.class)
							.setProperty(StatisticsCollector.DATAADDRESS, stockmarketAgentName + ":" + "data")));

			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}

			// Stock market Agent
			Cell stockMarketAgent = this.controller.createAgent(CellConfig.newConfig(stockmarketAgentName)
					.addFunction(CellFunctionConfig.newConfig(stockmarketServiceName, DummyPriceGenerator.class)
							.setProperty(DummyPriceGenerator.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
							.setProperty(DummyPriceGenerator.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(DummyPriceGenerator.ATTRIBUTEMODE, 1)
							.setProperty(DummyPriceGenerator.ATTRIBUTESTOCKNAME, stockName))); // Puts data on datapoint StockMarketAgent:data

			// Single Trader agent
			Cell traderAgent = this.controller.createAgent(CellConfig.newConfig(traderAgentName)
					.addFunction(CellFunctionConfig.newConfig(tradeService, Trader.class)
							.setProperty(Trader.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
							.setProperty(Trader.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
							.setProperty(Trader.ATTRIBUTEAGENTTYPE, "kamikazeType")
							.setProperty(Trader.ATTRIBUTESIGNALADDRESS, signalService)
							.setProperty(Trader.ATTRIBUTEEXECUTIONORDER, 1)
							.setProperty(Trader.ATTRIBUTEBROKERADDRESS, brokerAgentName + ":" + brokerServiceName))
					.addFunction(CellFunctionConfig.newConfig(signalService, PermanentBuySellIndicator.class)));

			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {

				}
			}

			log.info("=== All agents initialized ===");

			// Start the agents by starting the codelet handler
			//JsonRpcRequest req = new JsonRpcRequest(CellFunctionCodeletHandler.EXECUTECODELETEHANDLER, 1);
			//req.setParameterAsValue(0, false);
			//controllerAgent.getCommunicator().executeServiceQueryDatapoints(controllerAgent.getCell().getLocalName(), controllerService, req, controllerAgent.getCell().getLocalName(), controllerService + ".state", new JsonPrimitive(ServiceState.FINISHED.toString()), 20000);
			controllerAgent.getCommunicator().execute(controllerAgentName + ":" + controllerService + "/" + CellFunctionCodeletHandler.EXECUTECODELETMETHODNAME, new Request(), 200000);
			
			
			// Get the depot amount
			//JsonRpcRequest req1 = new JsonRpcRequest("getdepotinfo", 1);
			//req1.setParameterAsValue(0, traderAgentName);
			
			Response result = controllerAgent.getCommunicator().execute(brokerAgentName + ":" + brokerServiceName + "/" + "getdepotinfo", (new Request()).setParameter("agentname", traderAgentName), 200000);
			Depot depot = result.getResult(new TypeToken<Depot>() {}); //brokerAgent.getCommunicator().execute(brokerAgentName + ":" + brokerServiceName, req1).getResult(new TypeToken<Depot>() {});
			log.info("Current agent state={}", depot);

			// Delete all money from the depot
			//JsonRpcRequest req2 = new JsonRpcRequest("removemoney", 2);
			//req2.setParameterAsValue(0, traderAgentName);
			//req2.setParameterAsValue(1, depot.getLiquid() - 1);
			
			Response result1 = controllerAgent.getCommunicator().execute(brokerAgentName + ":" + brokerServiceName + "/" + "removemoney", (new Request()).setParameter("agentname", traderAgentName).setParameter("amount", depot.getLiquid() - 1), 200000);
			depot = result1.getResult(new TypeToken<Depot>() {}); //brokerAgent.getCommunicator().execute(brokerAgentName + ":" + brokerServiceName, req2).getResult(new TypeToken<Depot>() {});
			
			//Check codelet handler
			Datapoint stateres1 = controllerAgent.getCommunicator().read(controllerAgentName + ":" + controllerService + "/" + CellFunctionCodeletHandler.EXTENDEDSTATESUFFIX);
			
			
			log.info("Current agent state={}. Codelet handler state={}", depot, stateres1.getValue());

			//req = new JsonRpcRequest(CellFunctionCodeletHandler.EXECUTECODELETEHANDLER, 1);
			//req.setParameterAsValue(0, false);
			
			controllerAgent.getCommunicator().execute(controllerAgentName + ":" + controllerService + "/" + CellFunctionCodeletHandler.EXECUTECODELETMETHODNAME, new Request(), 200000);
			controllerAgent.getCommunicator().execute(controllerAgentName + ":" + controllerService + "/" + CellFunctionCodeletHandler.EXECUTECODELETMETHODNAME, new Request(), 200000);
			
			
			//controllerAgent.getCommunicator().executeServiceQueryDatapoints(controllerAgent.getName(), controllerService, req, controllerAgent.getCell().getLocalName(), controllerService + ".state", new JsonPrimitive(ServiceState.FINISHED.toString()), 5000);

			//controllerAgent.getCommunicator().executeServiceQueryDatapoints(controllerAgent.getName(), controllerService, req, controllerAgent.getCell().getLocalName(), controllerService + ".state", new JsonPrimitive(ServiceState.FINISHED.toString()), 5000);

			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {

				}
			}

			// The test is to read the depot of agent 69, which shall be empty
			Cell cell = traderAgent;
			result = controllerAgent.getCommunicator().execute(brokerAgentName + ":" + brokerServiceName + "/" + "getdepotinfo", (new Request()).setParameter("agentname", traderAgentName), 200000);
			depot = result.getResult(new TypeToken<Depot>() {}); //brokerAgent.getCommunicator().execute(brokerAgentName + ":" + brokerServiceName, req2).getResult(new TypeToken<Depot>() {});
			log.info("Current agent state={}", depot);

			//Check if there is any agent in the depot and in the codeletHandler
			boolean depotEmpty = false;
			if (depot.getOwner().equals("")) {
				depotEmpty = true;
			}
			
			//Check codelet handler
			boolean codeletUnregistered = false;
			Datapoint stateres2 = controllerAgent.getCommunicator().read(controllerAgentName + ":" + controllerService + "/" + CellFunctionCodeletHandler.EXTENDEDSTATESUFFIX);
			if (stateres2.getValue().getAsJsonObject().get("hasCodelets").getAsJsonArray().size()==1) {	//Only the broker shall be there
				codeletUnregistered = true;
			}
			
			log.info("Only one codelet in the codelet handler. State={}", stateres2.getValue().getAsJsonObject().get("hasCodelets").getAsJsonArray());
			
			//controllerservice/extendedstate:{"id":"controllerservice_EXTSTATE","state":"FINISHED","hasCodelets":[{"name":"<StockMarketAgent>/StockMarketService","state":"FINISHED"}]}
			
			assert (depotEmpty && codeletUnregistered);
			log.info("Test passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}
	}

}
