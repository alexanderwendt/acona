package at.tuwien.ict.acona.evolutiondemo;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionCodeletHandler;
import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.cell.core.CellGatewayImpl;
import at.tuwien.ict.acona.cell.core.cellfunction.codelets.Codelettester;
import at.tuwien.ict.acona.cell.core.cellfunction.codelets.helpers.IncrementOnConditionCodelet;
import at.tuwien.ict.acona.cell.datastructures.DatapointBuilder;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.Broker;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.Depot;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.StatisticsCollector;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.Types;
import at.tuwien.ict.acona.evolutiondemo.controlleragent.ConsoleRequestReceiver;
import at.tuwien.ict.acona.evolutiondemo.stockmarketagent.DummyPriceGenerator;
import at.tuwien.ict.acona.evolutiondemo.stockmarketagent.GraphToolFunction;
import at.tuwien.ict.acona.evolutiondemo.stockmarketagent.OHLCGraph;
import at.tuwien.ict.acona.jadelauncher.util.KoreExternalControllerImpl;
import jade.core.Runtime;

public class StockMarketTester {

	private static final Logger log = LoggerFactory.getLogger(StockMarketTester.class);
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
	 * Create a broker agent. Create a depot. Add money to depot, read state of depot, buy stock, sell stock, unregister depot
	 * 
	 */
	@Test
	public void stockmarketGenerationTester() {
		try {
			String brokerAgentName = "BrokerAgent"; 
			String traderAgentName = "TraderAgent";
			String traderType = "type1";
			String brokerServiceName = "BrokerService";
			String stockName = "Fingerprint";

			//=== Controller ===//
			String controllerAgentName = "ControllerAgent";
			String controllerService = "controllerservice";
			
			CellGatewayImpl controllerAgent = this.launcher.createAgent(CellConfig.newConfig(controllerAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(controllerService, CellFunctionCodeletHandler.class)
							.setGenerateReponder(true))
					.addCellfunction(CellFunctionConfig.newConfig("userconsole", ConsoleRequestReceiver.class)
							.setProperty(ConsoleRequestReceiver.ATTRIBUTECONTROLLERSERVICE, controllerService)));
			
			synchronized (this) {
				try {
					this.wait(200);
				} catch (InterruptedException e) {

				}
			}
			
			//=== Stock market ===//
			String stockmarketAgentName = "StockMarketAgent";
			String stockmarketServiceName = "StockMarketService";
			
			CellGatewayImpl stockMarketAgent = this.launcher.createAgent(CellConfig.newConfig(stockmarketAgentName)
					.addCellfunction(CellFunctionConfig.newConfig(stockmarketServiceName, DummyPriceGenerator.class)
							.setProperty(DummyPriceGenerator.ATTRIBUTECODELETHANDLERADDRESS, controllerAgentName + ":" + controllerService)
							.setProperty(DummyPriceGenerator.ATTRIBUTEEXECUTIONORDER, 0)
							.setProperty(DummyPriceGenerator.ATTRIBUTEMODE, 0)
							.setProperty(DummyPriceGenerator.ATTRIBUTESTOCKNAME, stockName)
							.setGenerateReponder(true))
					.addCellfunction(CellFunctionConfig.newConfig("OHLCGraph", GraphToolFunction.class)
							.addManagedDatapoint("Fingdata", "data", SyncMode.SUBSCRIBEONLY)));	//Puts data on datapoint StockMarketAgent:data
			
			//=== Init finished ===//

			synchronized (this) {
				try {
					this.wait(200000);
				} catch (InterruptedException e) {

				}
			}
			log.info("=== All agents initialized ===");
			
//			JsonRpcRequest request1 = new JsonRpcRequest("registerdepot", 0);
//			//request1.setParameterAsValue(0, traderAgentName);
//			//request1.setParameterAsValue(1, traderType);
//			request1.setParameters(traderAgentName, traderType);
//			
//			//traderAgent.getCommunicator().write(Datapoints.newDatapoint(brokerAgentName + ":" + "test").setValue("test"));
//			
//			//JsonRpcRequest request = new JsonRpcRequest("write", false, new Object[1]);
//			//request.setParameterAsList(0, Arrays.asList(Datapoints.newDatapoint("test").setValue("test")));
//
//			//JsonRpcResponse result = traderAgent.getCommunicator().execute(brokerAgentName, "write", request, 100);
//
//			JsonRpcResponse result = traderAgent.getCommunicator().execute(brokerAgentName, brokerServiceName, request1, 2000);
//			Depot depot = brokerAgent.readLocalDatapoint("depot." + traderAgentName).getValue(Depot.class);
//			
//			log.info("Registered depot={}", depot);
//			assertEquals(traderAgentName, depot.getOwner());
//			
//			request1 = new JsonRpcRequest("addmoney", 0);
//			//request1.setParameterAsValue(0, traderAgentName);
//			//request1.setParameterAsValue(1, traderType);
//			request1.setParameters(traderAgentName, 1000);
//			result = traderAgent.getCommunicator().execute(brokerAgentName, brokerServiceName, request1, 2000);
//			depot = result.getResult(new TypeToken<Depot>(){});
//			
//			log.info("Added money={}", depot);
//			assertEquals(1000, depot.getLiquid(), 0.0);
//			
//			request1 = new JsonRpcRequest("buy", 0);
//			//request1.setParameterAsValue(0, traderAgentName);
//			//request1.setParameterAsValue(1, traderType);
//			request1.setParameters(traderAgentName, stockName, 59.75, 10);
//			result = traderAgent.getCommunicator().execute(brokerAgentName, brokerServiceName, request1, 20000);
//			depot = result.getResult(new TypeToken<Depot>(){});
//			
//			log.info("Bought stock={}", depot);
//			assertEquals(stockName, depot.getAssets().get(0).getStockName());
//			
//			request1 = new JsonRpcRequest("sell", 0);
//			//request1.setParameterAsValue(0, traderAgentName);
//			//request1.setParameterAsValue(1, traderType);
//			request1.setParameters(traderAgentName, stockName, 70.50, 5);
//			result = traderAgent.getCommunicator().execute(brokerAgentName, brokerServiceName, request1, 20000);
//			depot = result.getResult(new TypeToken<Depot>(){});
//			
//			log.info("Sold stock={}", depot);
//			assertEquals(5, depot.getAssets().get(0).getVolume(), 0.0);
//			
//			request1 = new JsonRpcRequest("unregisterdepot", 0);
//			//request1.setParameterAsValue(0, traderAgentName);
//			//request1.setParameterAsValue(1, traderType);
//			request1.setParameters(traderAgentName);
//			result = traderAgent.getCommunicator().execute(brokerAgentName, brokerServiceName, request1, 20000);
//			JsonElement e = brokerAgent.readLocalDatapoint("depot." + traderAgentName).getValue();
			
			//log.info("unregistered depot={}", e);
			assertEquals(true, false);
			
			log.info("All tests passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

}
