package at.tuwien.ict.acona.evolutiondemo;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import at.tuwien.ict.acona.cell.cellfunction.ServiceState;
import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionCodeletHandler;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.core.CellGatewayImpl;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.Broker;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.Depot;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.StatisticsCollector;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.Types;
import at.tuwien.ict.acona.evolutiondemo.stockmarketagent.DummyPriceGenerator;
import at.tuwien.ict.acona.evolutiondemo.traderagent.PermanentBuySellIndicator;
import at.tuwien.ict.acona.evolutiondemo.traderagent.Trader;
import at.tuwien.ict.acona.jadelauncher.util.KoreExternalControllerImpl;
import jade.core.Runtime;

public class TraderTester {

	private static final Logger log = LoggerFactory.getLogger(TraderTester.class);
	private KoreExternalControllerImpl launcher = KoreExternalControllerImpl.getLauncher();

	@Before
	public void setUp() throws Exception {
		try {
			// Create container
			log.debug("Create or get main container");
			this.launcher.createMainContainer("localhost", 1099, "MainContainer");

			log.debug("Create subcontainer");
			this.launcher.createSubContainer("localhost", 1099, "Subcontainer");

			// log.debug("Create gui");
			// this.commUtil.createDebugUserInterface();

			// Create gateway
			// commUtil.initJadeGateway();

		} catch (Exception e) {
			log.error("Cannot initialize test environment", e);
		}
	}

	@After
	public void tearDown() throws Exception {
		synchronized (this) {
			try {
				this.wait(2000);
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

	/**
	 * Create a trader agent with a mock function that buys if nothing has been bought yet and sells if something has been sold. Trigger the
	 * trader agent to execute 2 times. The first time, the amount of the stock and the price shall be defined. In the second run, everything
	 * shall be sold.
	 * 
	 * Create the system with a controller that triggers the price generator. At new data from the price generator, the traders are triggered by
	 * the subscription of the new values from the price generator. After calculation, each trader reports finished to the controller.
	 * 
	 */
	@Test
	public void buyAndSellFunctionTestAgenttest() {
		try {
			String brokerAgentName = "BrokerAgent"; 
			String traderAgentName = "TraderAgent";
			String stockmarketAgentName = "StockMarketAgent";
			String controllerAgentName = "ControllerAgent";
			//String traderType = "type1";
			String brokerServiceName = "BrokerService";
			String statisticsService = "statisticsService";
			String stockmarketServiceName = "StockMarketService";
			String controllerService = "controllerservice";
			String signalService = "signal";
			
			String stockName = "Fingerprint";		
			
			CellGatewayImpl controllerAgent = this.launcher.createAgent(CellConfig.newConfig(controllerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(controllerService, CellFunctionCodeletHandler.class)
							.setGenerateReponder(true)));
			
			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}
			
			CellGatewayImpl brokerAgent = this.launcher.createAgent(CellConfig.newConfig(brokerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(brokerServiceName, Broker.class)
							.setProperty(Broker.ATTRIBUTESTOCKNAME, stockName))
					.addCellfunction(CellFunctionConfig.newConfig(statisticsService, StatisticsCollector.class)));
			
			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}
			
			CellGatewayImpl stockMarketAgent = this.launcher.createAgent(CellConfig.newConfig(stockmarketAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(stockmarketServiceName, DummyPriceGenerator.class)
							.setProperty(DummyPriceGenerator.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
							.setProperty(DummyPriceGenerator.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(DummyPriceGenerator.ATTRIBUTEMODE, 1)
							.setProperty(DummyPriceGenerator.ATTRIBUTESTOCKNAME, stockName)
							.setGenerateReponder(true)));	//Puts data on datapoint StockMarketAgent:data
			
			//Create 100 trading agents that first buy a stock, then sell it
			for (int i=1;i<=500;i++) {
				String traderType = "type";
				if (i%2==0) {
					traderType += "_even";
				} else {
					traderType += "_odd";
				}
				
				CellGatewayImpl traderAgent = this.launcher.createAgent(CellConfig.newConfig(traderAgentName + "_" + i)
						.addCellfunction(CellFunctionConfig.newConfig("trader_" + i, Trader.class)
								.setProperty(Trader.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
								.setProperty(Trader.ATTRIBUTESTOCKMARKETADDRESS, stockmarketAgentName + ":" + "data")
								.setProperty(Trader.ATTRIBUTEAGENTTYPE, traderType)
								.setProperty(Trader.ATTRIBUTESIGNALADDRESS, signalService)
								.setProperty(Trader.ATTRIBUTEEXECUTIONORDER, 1)
								.setProperty(Trader.ATTRIBUTEBROKERADDRESS, brokerAgentName + ":" + brokerServiceName)
								.setGenerateReponder(true))
						.addCellfunction(CellFunctionConfig.newConfig(signalService, PermanentBuySellIndicator.class)));
			}

			synchronized (this) {
				try {
					this.wait(10000);
				} catch (InterruptedException e) {

				}
			}
			
			log.info("=== All agents initialized ===");
			
			//Start the agents by starting the codelet handler
			JsonRpcRequest req = new JsonRpcRequest(CellFunctionCodeletHandler.EXECUTECODELETEHANDLER, 1);
			req.setParameterAsValue(0, false);
			controllerAgent.getCommunicator().executeServiceQueryDatapoints(controllerAgent.getCell().getLocalName(), controllerService, req, controllerAgent.getCell().getLocalName(), controllerService + ".state", new JsonPrimitive(ServiceState.FINISHED.toString()), 20000);
			
			req = new JsonRpcRequest(CellFunctionCodeletHandler.EXECUTECODELETEHANDLER, 1);
			req.setParameterAsValue(0, false);
			controllerAgent.getCommunicator().executeServiceQueryDatapoints(controllerAgent.getCell().getLocalName(), controllerService, req, controllerAgent.getCell().getLocalName(), controllerService + ".state", new JsonPrimitive(ServiceState.FINISHED.toString()), 20000);
			
			req = new JsonRpcRequest("gettypes", 0);
			JsonRpcResponse result = brokerAgent.getCommunicator().execute(brokerAgent.getCell().getLocalName(), statisticsService, req, 100000);
			List<Types> list = result.getResult(new TypeToken<List<Types>>(){});
			
			log.info("Got types={}", list);
			assertEquals(list.get(0).getNumber(), list.get(1).getNumber());
			
			//The test is to read the depot of agent 69, which shall be empty
			Depot depot = controllerAgent.getCommunicator().read(traderAgentName + "_" + "69", "localdepot").getValue(Depot.class);
			double money = depot.getLiquid();
			
			log.info("Got money from agent 69={}. Correct answer={}", money, 1000);
			assertEquals(1000, money, 0);
			log.info("All tests passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}
	}
	
}
