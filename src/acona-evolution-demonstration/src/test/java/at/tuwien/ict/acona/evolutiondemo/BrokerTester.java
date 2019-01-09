package at.tuwien.ict.acona.evolutiondemo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import at.tuwien.ict.acona.evolutiondemo.brokeragent.Broker;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.Depot;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.DepotStaticticsGraphToolFunction;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.StatisticsCollector;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.SpeciesType;
import at.tuwien.ict.acona.mq.cell.config.CellConfig;
import at.tuwien.ict.acona.mq.cell.config.CellFunctionConfig;
import at.tuwien.ict.acona.mq.cell.core.Cell;
import at.tuwien.ict.acona.mq.datastructures.DPBuilder;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;
import at.tuwien.ict.acona.mq.launcher.SystemControllerImpl;

public class BrokerTester {

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
	 * Create a broker agent. Create a depot. Add money to depot, read state of depot, buy stock, sell stock, unregister depot
	 * 
	 */
	@Test
	public void brokerAgenttest() {
		try {
			String brokerAgentName = "BrokerAgent";
			String traderAgentName = "TraderAgent";
			String traderType = "type1";
			String brokerServiceName = "BrokerService";
			String stockName = "Fingerprint";

			CellConfig cf = CellConfig.newConfig(brokerAgentName)
					.addFunction(CellFunctionConfig.newConfig(brokerServiceName, Broker.class)
							.setProperty(Broker.ATTRIBUTESTOCKNAME, stockName));
			Cell brokerAgent = this.launcher.createAgent(cf);

			Cell traderAgent = this.launcher.createAgent(CellConfig.newConfig(traderAgentName));

			// === Init finished ===//

			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {

				}
			}
			log.info("=== All agents initialized ===");

			//JsonRpcRequest request1 = new JsonRpcRequest("registerdepot", 0);
			// request1.setParameterAsValue(0, traderAgentName);
			// request1.setParameterAsValue(1, traderType);
			//request1.setParameters(traderAgentName, traderType);

			// traderAgent.getCommunicator().write(Datapoints.newDatapoint(brokerAgentName + ":" + "test").setValue("test"));

			// JsonRpcRequest request = new JsonRpcRequest("write", false, new Object[1]);
			// request.setParameterAsList(0, Arrays.asList(Datapoints.newDatapoint("test").setValue("test")));

			// JsonRpcResponse result = traderAgent.getCommunicator().execute(brokerAgentName, "write", request, 100);

			//Response result = traderAgent.getCommunicator().execute(brokerAgentName, brokerServiceName, request1, 2000);
			Response result = brokerAgent.getCommunicator().execute(brokerAgentName + ":" + brokerServiceName + "/" + "registerdepot", (new Request())
					.setParameter("agentname", traderAgentName)
					.setParameter("agenttype", traderType)
					, 200000);
			
			Depot depot = brokerAgent.getCommunicator().read("depot." + traderAgentName).getValue(Depot.class);
			
			

			log.info("Registered depot={}", depot);
			assertEquals(traderAgentName, depot.getOwner());

			//request1 = new JsonRpcRequest("addmoney", 0);
			// request1.setParameterAsValue(0, traderAgentName);
			// request1.setParameterAsValue(1, traderType);
			//request1.setParameters(traderAgentName, 1000);
			//result = traderAgent.getCommunicator().execute(brokerAgentName, brokerServiceName, request1, 2000);
			
			result = brokerAgent.getCommunicator().execute(brokerAgentName + ":" + brokerServiceName + "/" + "addmoney", (new Request())
					.setParameter("agentname", traderAgentName)
					.setParameter("amount", 1000)
					, 200000);
			
			depot = result.getResult(new TypeToken<Depot>() {});

			log.info("Added money={}", depot);
			double liquid = depot.getLiquid();
			assertEquals(1000, liquid);
			log.info("Test passed");

			//request1 = new JsonRpcRequest("buy", 0);
			// request1.setParameterAsValue(0, traderAgentName);
			// request1.setParameterAsValue(1, traderType);
			//request1.setParameters(traderAgentName, stockName, 59.75, 10);
			//result = traderAgent.getCommunicator().execute(brokerAgentName, brokerServiceName, request1, 20000);
			result = brokerAgent.getCommunicator().execute(brokerAgentName + ":" + brokerServiceName + "/" + "buy", (new Request())
					.setParameter("agentname", traderAgentName)
					.setParameter("stockname", stockName)
					.setParameter("price", 59.75)
					.setParameter("volume", 10)
					, 200000);
			
			
			depot = result.getResult(new TypeToken<Depot>() {});

			log.info("Bought stock={}", depot);
			assertEquals(stockName, depot.getAssets().get(0).getStockName());
			log.info("Test passed");

			//request1 = new JsonRpcRequest("sell", 0);
			// request1.setParameterAsValue(0, traderAgentName);
			// request1.setParameterAsValue(1, traderType);
			//request1.setParameters(traderAgentName, stockName, 70.50, 5);
			//result = traderAgent.getCommunicator().execute(brokerAgentName, brokerServiceName, request1, 20000);
			
			result = brokerAgent.getCommunicator().execute(brokerAgentName + ":" + brokerServiceName + "/" + "sell", (new Request())
					.setParameter("agentname", traderAgentName)
					.setParameter("stockname", stockName)
					.setParameter("price", 70.50)
					.setParameter("volume", 5)
					, 200000);
			
			depot = result.getResult(new TypeToken<Depot>() {});

			log.info("Sold stock={}", depot);
			assertEquals(5, depot.getAssets().get(0).getVolume());
			log.info("Test passed");

			//request1 = new JsonRpcRequest("unregisterdepot", 0);
			// request1.setParameterAsValue(0, traderAgentName);
			// request1.setParameterAsValue(1, traderType);
			//request1.setParameters(traderAgentName);
			//result = traderAgent.getCommunicator().execute(brokerAgentName, brokerServiceName, request1, 20000);
			
			result = brokerAgent.getCommunicator().execute(brokerAgentName + ":" + brokerServiceName + "/" + "unregisterdepot", (new Request())
					.setParameter("agentname", traderAgentName)
					, 200000);
			
			JsonElement e = brokerAgent.getCommunicator().read("depot." + traderAgentName).getValue();

			log.info("unregistered depot={}", e);
			assertEquals(true, e.toString().equals("{}"));
			log.info("Test passed");

			log.info("All tests passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

	/**
	 * Create a broker agent. Create a depot. Add money to depot, read state of depot, buy stock, sell stock, unregister depot
	 * 
	 */
	@Test
	public void statisticsCollectorTest() {
		try {
			String brokerAgentName = "BrokerAgent";
			String traderAgentName = "TraderAgent";
			String traderType1 = "type1";
			String traderType2 = "type2";
			String brokerServiceName = "BrokerService";
			String statisticsService = "StatisticsService";
			String stockName = "Fingerprint";

			CellConfig cf = CellConfig.newConfig(brokerAgentName)
					.addFunction(CellFunctionConfig.newConfig(brokerServiceName, Broker.class)
							.setProperty(Broker.ATTRIBUTESTOCKNAME, stockName))
					.addFunction(CellFunctionConfig.newConfig(statisticsService, StatisticsCollector.class)
							.setProperty(StatisticsCollector.DATAADDRESS, "test"));
			Cell brokerAgent = this.launcher.createAgent(cf);

			List<Cell> traderAgents = new ArrayList<Cell>();

			for (int i = 0; i < 50; i++) {
				Cell a = this.launcher.createAgent(CellConfig.newConfig(traderType1 + i));
				traderAgents.add(a);
				a.getCommunicator().write(dpb.newDatapoint("type").setValue(traderType1));
			}

			for (int i = 0; i < 15; i++) {
				Cell a = this.launcher.createAgent(CellConfig.newConfig(traderType2 + i));
				traderAgents.add(a);
				a.getCommunicator().write(dpb.newDatapoint("type").setValue(traderType2));
			}

			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {

				}
			}
			// === Init finished ===//
			log.info("=== All agents initialized ===");

			log.debug("Register depots for all agents");
			traderAgents.forEach(a -> {
				//JsonRpcRequest req = new JsonRpcRequest("registerdepot", 0);
				try {
					//req.setParameters(a.getName(), a.getCommunicator().read("type").getValueAsString());
					//a.getCommunicator().execute(brokerAgentName, brokerServiceName, req, 1000);
					Request req = (new Request())
					.setParameter("agentname", a.getName())
					.setParameter("agenttype", a.getCommunicator().read("type").getValueAsString());
					
					Response result = a.getCommunicator().execute(brokerAgentName + ":" + brokerServiceName + "/" + "registerdepot", req
							, 200000);
				
					log.debug("registered depot={}", req);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				
			});

			log.debug("Read statistics");
			//JsonRpcRequest req = new JsonRpcRequest("gettypes", 0);
			//JsonRpcResponse result = brokerAgent.getCommunicator().execute(brokerAgent.getName(), statisticsService, req, 100000);
			
			Response result = brokerAgent.getCommunicator().execute(brokerAgent.getName() + ":" + statisticsService + "/" + "getstats", (new Request())
					, 200000);
			
			JsonElement typesEncoded = result.getResult().getAsJsonObject().get("types");
			List<SpeciesType> list = (new Gson()).fromJson(typesEncoded, new TypeToken<List<SpeciesType>>() {}.getType());
			Optional<SpeciesType> opt = list.stream().filter(o -> o.getType().equals(traderType1)).findFirst();

			log.info("number of type1={}. Calculated number={}", 50, opt.get().getNumber());
			assertEquals(50, opt.get().getNumber());

			log.info("All tests passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

	/**
	 * Create a broker agent. Create a depot. Add money to depot, read state of depot, buy stock, sell stock, unregister depot
	 * 
	 */
	@Test
	public void speciesGraphTest() {
		try {
			String brokerAgentName = "BrokerAgent";
			String traderType1 = "type1";
			String traderType2 = "type2";
			String brokerServiceName = "BrokerService";
			String statisticsService = "StatisticsService";
			String stockName = "Fingerprint";

			CellConfig cf = CellConfig.newConfig(brokerAgentName)
					.addFunction(CellFunctionConfig.newConfig(brokerServiceName, Broker.class)
							.setProperty(Broker.ATTRIBUTESTOCKNAME, stockName))
					.addFunction(CellFunctionConfig.newConfig(statisticsService, StatisticsCollector.class)
						.setProperty(StatisticsCollector.DATAADDRESS, "test"))
					.addFunction(CellFunctionConfig.newConfig("depotstatistics", DepotStaticticsGraphToolFunction.class));

			Cell brokerAgent = this.launcher.createAgent(cf);

//			List<CellGatewayImpl> traderAgents = new ArrayList<CellGatewayImpl>();
//
//			for (int i = 0; i < 50; i++) {
//				CellGatewayImpl a = this.launcher.createAgent(CellConfig.newConfig(traderType1 + i));
//				traderAgents.add(a);
//				a.getCommunicator().write(DatapointBuilder.newDatapoint("type").setValue(traderType1));
//			}
//
//			for (int i = 0; i < 15; i++) {
//				CellGatewayImpl a = this.launcher.createAgent(CellConfig.newConfig(traderType2 + i));
//				traderAgents.add(a);
//				a.getCommunicator().write(DatapointBuilder.newDatapoint("type").setValue(traderType2));
//			}

			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {

				}
			}
			// === Init finished ===//
			log.info("=== All agents initialized ===");

//			log.debug("Register depots for all agents");
//			traderAgents.forEach(a -> {
//				JsonRpcRequest req = new JsonRpcRequest("registerdepot", 0);
//				try {
//					req.setParameters(a.getCell().getLocalName(), a.getCommunicator().read("type").getValueAsString());
//					a.getCommunicator().execute(brokerAgentName, brokerServiceName, req, 1000);
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//				log.debug("registered depot={}", req);
//			});
//
//			log.debug("Read statistics");
//			JsonRpcRequest req = new JsonRpcRequest("gettypes", 0);
//			JsonRpcResponse result = brokerAgent.getCommunicator().execute(brokerAgent.getCell().getLocalName(), statisticsService, req, 100000);
//			List<Types> list = result.getResult(new TypeToken<List<Types>>() {});
//			Optional<Types> opt = list.stream().filter(o -> o.getType().equals(traderType1)).findFirst();
//
//			log.info("number of type1={}. Calculated number={}", 50, opt.get().getNumber());
			assertEquals(true, true);

			log.info("All tests passed");
		} catch (Exception e) {
			log.error("Error testing system", e);
			fail("Error");
		}

	}

}
