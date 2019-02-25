package at.tuwien.ict.acona.evolutiondemo;

import java.lang.invoke.MethodHandles;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.mq.datastructures.DPBuilder;
import at.tuwien.ict.acona.mq.launcher.SystemControllerImpl;

public class StockMarketTester {

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

//	/**
//	 * Create a broker agent. Create a depot. Add money to depot, read state of depot, buy stock, sell stock, unregister depot
//	 * 
//	 */
//	@Test
//	public void stockmarketGenerationTester() {
//		try {
//			String brokerAgentName = "BrokerAgent";
//			String traderAgentName = "TraderAgent";
//			String traderType = "type1";
//			String brokerServiceName = "BrokerService";
//			String stockName = "Fingerprint";
//
//			// === Controller ===//
//			String controllerAgentName = "ControllerAgent";
//			String controllerService = "controllerservice";
//
//			Cell controllerAgent = this.launcher.createAgent(CellConfig.newConfig(controllerAgentName)
//					.addCellfunction(CellFunctionConfig.newConfig(controllerService, CellFunctionCodeletHandler.class))
//					.addCellfunction(CellFunctionConfig.newConfig("userconsole", ConsoleRequestReceiver.class)
//							.setProperty(ConsoleRequestReceiver.ATTRIBUTECONTROLLERSERVICE, controllerService)));
//
//			synchronized (this) {
//				try {
//					this.wait(200);
//				} catch (InterruptedException e) {
//
//				}
//			}
//
//			// === Stock market ===//
//			String stockmarketAgentName = "StockMarketAgent";
//			String stockmarketServiceName = "StockMarketService";
//
//			Cell stockMarketAgent = this.launcher.createAgent(CellConfig.newConfig(stockmarketAgentName)
//					.addCellfunction(CellFunctionConfig.newConfig(stockmarketServiceName, DummyPriceGenerator.class)
//							.setProperty(DummyPriceGenerator.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
//							.setProperty(DummyPriceGenerator.ATTRIBUTEEXECUTIONORDER, 0)
//							.setProperty(DummyPriceGenerator.ATTRIBUTEMODE, 0)
//							.setProperty(DummyPriceGenerator.ATTRIBUTESTOCKNAME, stockName))
//					.addCellfunction(CellFunctionConfig.newConfig("OHLCGraph", PriceGraphToolFunction.class) // Stock market graph
//							.addManagedDatapoint("Fingdata", "data", SyncMode.SUBSCRIBEONLY))); // Puts data on datapoint StockMarketAgent:data
//
//			// === Init finished ===//
//
//			synchronized (this) {
//				try {
//					this.wait(2000000);
//				} catch (InterruptedException e) {
//
//				}
//			}
//			log.info("=== All agents initialized ===");
//
////			JsonRpcRequest request1 = new JsonRpcRequest("registerdepot", 0);
////			//request1.setParameterAsValue(0, traderAgentName);
////			//request1.setParameterAsValue(1, traderType);
////			request1.setParameters(traderAgentName, traderType);
////			
////			//traderAgent.getCommunicator().write(Datapoints.newDatapoint(brokerAgentName + ":" + "test").setValue("test"));
////			
////			//JsonRpcRequest request = new JsonRpcRequest("write", false, new Object[1]);
////			//request.setParameterAsList(0, Arrays.asList(Datapoints.newDatapoint("test").setValue("test")));
////
////			//JsonRpcResponse result = traderAgent.getCommunicator().execute(brokerAgentName, "write", request, 100);
////
////			JsonRpcResponse result = traderAgent.getCommunicator().execute(brokerAgentName, brokerServiceName, request1, 2000);
////			Depot depot = brokerAgent.readLocalDatapoint("depot." + traderAgentName).getValue(Depot.class);
////			
////			log.info("Registered depot={}", depot);
////			assertEquals(traderAgentName, depot.getOwner());
////			
////			request1 = new JsonRpcRequest("addmoney", 0);
////			//request1.setParameterAsValue(0, traderAgentName);
////			//request1.setParameterAsValue(1, traderType);
////			request1.setParameters(traderAgentName, 1000);
////			result = traderAgent.getCommunicator().execute(brokerAgentName, brokerServiceName, request1, 2000);
////			depot = result.getResult(new TypeToken<Depot>(){});
////			
////			log.info("Added money={}", depot);
////			assertEquals(1000, depot.getLiquid(), 0.0);
////			
////			request1 = new JsonRpcRequest("buy", 0);
////			//request1.setParameterAsValue(0, traderAgentName);
////			//request1.setParameterAsValue(1, traderType);
////			request1.setParameters(traderAgentName, stockName, 59.75, 10);
////			result = traderAgent.getCommunicator().execute(brokerAgentName, brokerServiceName, request1, 20000);
////			depot = result.getResult(new TypeToken<Depot>(){});
////			
////			log.info("Bought stock={}", depot);
////			assertEquals(stockName, depot.getAssets().get(0).getStockName());
////			
////			request1 = new JsonRpcRequest("sell", 0);
////			//request1.setParameterAsValue(0, traderAgentName);
////			//request1.setParameterAsValue(1, traderType);
////			request1.setParameters(traderAgentName, stockName, 70.50, 5);
////			result = traderAgent.getCommunicator().execute(brokerAgentName, brokerServiceName, request1, 20000);
////			depot = result.getResult(new TypeToken<Depot>(){});
////			
////			log.info("Sold stock={}", depot);
////			assertEquals(5, depot.getAssets().get(0).getVolume(), 0.0);
////			
////			request1 = new JsonRpcRequest("unregisterdepot", 0);
////			//request1.setParameterAsValue(0, traderAgentName);
////			//request1.setParameterAsValue(1, traderType);
////			request1.setParameters(traderAgentName);
////			result = traderAgent.getCommunicator().execute(brokerAgentName, brokerServiceName, request1, 20000);
////			JsonElement e = brokerAgent.readLocalDatapoint("depot." + traderAgentName).getValue();
//
//			// log.info("unregistered depot={}", e);
//			assertEquals(true, false);
//
//			log.info("All tests passed");
//		} catch (Exception e) {
//			log.error("Error testing system", e);
//			fail("Error");
//		}
//
//	}

}
