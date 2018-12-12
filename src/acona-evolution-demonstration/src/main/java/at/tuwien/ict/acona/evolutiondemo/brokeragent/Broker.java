package at.tuwien.ict.acona.evolutiondemo.brokeragent;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import at.tuwien.ict.acona.mq.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

public class Broker extends CellFunctionThreadImpl {

	// private Map<String, List<String>> agentTypeMap = new HashMap<String, List<String>>(); //Type, agentname
	private static final Logger log = LoggerFactory.getLogger(Broker.class);

	public final static String ATTRIBUTESTOCKNAME = "stockname";

	private String stockName = "";
	private final static String PREFIXDEPOTADDRESS = "depot";
	
	public final static String REGISTERDEPOT = "registerdepot";
	public final static String UNREGISTERDEPOT = "unregisterdepot";
	public final static String BUY = "buy";
	public final static String SELL = "sell";
	public final static String GETDEPOTINFO = "getdepotinfo";
	public final static String ADDMONEY = "addmoney";
	public final static String REMOVEMONEY = "removemoney";

	private final Gson gson = new Gson();

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		stockName = this.getFunctionConfig().getProperty(ATTRIBUTESTOCKNAME, "");

		// === GENERATE RESPONDER NECESSARY ===//
		this.getFunctionConfig().setGenerateReponder(true);

		// Add subfunctions
		this.addRequestHandlerFunction(REGISTERDEPOT, (Request input) -> registerDepot(input));
		this.addRequestHandlerFunction(UNREGISTERDEPOT, (Request input) -> unregisterDepot(input));
		this.addRequestHandlerFunction(BUY, (Request input) -> buy(input));
		this.addRequestHandlerFunction(SELL, (Request input) -> sell(input));
		this.addRequestHandlerFunction(GETDEPOTINFO, (Request input) -> getDepotAsJson(input));
		this.addRequestHandlerFunction(ADDMONEY, (Request input) -> addMoneyToDepot(input));
		this.addRequestHandlerFunction(REMOVEMONEY, (Request input) -> removeMoneyFromDepot(input));
		
		
		
		
		
		log.debug("Broker initialized");

	}
	
	private Response registerDepot(Request req) {
		Response result = new Response(req);
		
		try {
			String agentName = req.getParameter("name", String.class);
			String agentType = req.getParameter("type", String.class);
			
			Depot depot = new Depot();
			depot.setLiquid(0);
			depot.setOwner(agentName);
			depot.setOwnerType(agentType);

			// Add agent type
			// addAgentType(agentName, agentType);

			JsonElement jsonDepot = gson.toJsonTree(depot);
			this.getCommunicator().write(this.getDatapointBuilder().newDatapoint(this.createDepotAddress(agentName)).setValue(jsonDepot));
			
			result.setResult(jsonDepot);
			log.debug("Registered agent={}, type={}", agentName, agentType);
		} catch (Exception e) {
			log.error("Cannot register depot", e);
			result.setError(e.getMessage());
		}
		
		return result;
	}

//	private void addAgentType(String agentName, String agentType) {
//		//create depot
//		List<String> agentNameList = this.agentTypeMap.get(agentType);
//		if (agentNameList!=null && agentNameList.contains(agentName)==false) {
//			agentNameList.add(agentName);
//		} else {
//			this.agentTypeMap.put(agentType, new ArrayList<String>(Arrays.asList(agentName)));
//		}
//	}

	private String createDepotAddress(String agentName) {
		return PREFIXDEPOTADDRESS + "." + agentName;
	}

	private Response unregisterDepot(Request req) {
		Response result = new Response(req);
		
		try {
			String agentName = req.getParameter("name", String.class);
		
			this.getCommunicator().remove(this.createDepotAddress(agentName));
			log.debug("Unregistered agent={}", agentName);
		} catch (Exception e) {
			log.error("Cannot register depot", e);
			result.setError(e.getMessage());
		}
		
		return result;
	}

	private Depot getDepot(String agentName) throws JsonSyntaxException, Exception {
		return gson.fromJson(this.getCommunicator().read(this.getCellName() + ":" + this.createDepotAddress(agentName)).getValue(), Depot.class);
	}

	private Response getDepotAsJson(Request req) {
		Response result = new Response(req);
		
		try {
			String agentName = req.getParameter("name", String.class);
			result.setResult(this.getCommunicator().read(this.getCellName() + ":" + this.createDepotAddress(agentName)).getValue());
			
		} catch (Exception e) {
			log.error("Cannot get depot", e);
			result.setError(e.getMessage());
		}
		
		return result;
	}

	private Response buy(Request req) {
		Response result = new Response(req);
		
		try {
			String agentName = req.getParameter("agentname", String.class); 
			String stockName = req.getParameter("stockname", String.class);  
			double price = req.getParameter("price", Double.class);  
			int volume = req.getParameter("volume", Integer.class); 
		
			Depot depot = this.getDepot(agentName);
			if (depot == null) {
				throw new Exception("Depot " + agentName + " does not exist");
			}

			depot.buy(stockName, volume, price);

			JsonElement jsonDepot = gson.toJsonTree(depot);
			this.getCommunicator().write(this.getDatapointBuilder().newDatapoint(this.createDepotAddress(agentName)).setValue(jsonDepot));
			log.debug("Agent={}, Bought stock={}, volume={}, price={}. Depot={}", agentName, stockName, volume, price, jsonDepot);
			
			result.setResult(jsonDepot);

		} catch (Exception e) {
			log.error("Cannot buy", e);
			result.setError(e.getMessage());
		}
		
		return result;
	}

	private Response sell(Request req) {
		Response result = new Response(req);
		
		try {
			String agentName = req.getParameter("agentname", String.class); 
			String stockName = req.getParameter("stockname", String.class);  
			double price = req.getParameter("price", Double.class);  
			int volume = req.getParameter("volume", Integer.class);
		
			Depot depot = this.getDepot(agentName);
			if (depot == null) {
				throw new Exception("Depot " + agentName + " does not exist");
			}
	
			depot.sell(stockName, volume, price);
	
			JsonElement jsonDepot = gson.toJsonTree(depot);
			this.getCommunicator().write(this.getDatapointBuilder().newDatapoint(this.createDepotAddress(agentName)).setValue(jsonDepot));
			log.debug("Agent={}, Sold stock={}, volume={}, price={}. Depot={}", agentName, stockName, volume, price, jsonDepot);

		} catch (Exception e) {
			log.error("Cannot sell", e);
			result.setError(e.getMessage());
		}
		
		return result;
	}

	private Response addMoneyToDepot(Request req) {
		Response result = new Response(req);
		
		try {
			String agentName = req.getParameter("agentname", String.class);  
			double amount = req.getParameter("price", Double.class);  
		
			Depot depot = this.getDepot(agentName);
			if (depot == null) {
				throw new Exception("Depot " + agentName + " does not exist");
			}
	
			depot.addLiquid(amount);
	
			JsonElement jsonDepot = gson.toJsonTree(depot);
			this.getCommunicator().write(this.getDatapointBuilder().newDatapoint(this.createDepotAddress(agentName)).setValue(jsonDepot));
			log.debug("Agent={}>Added money={}. Total amount={}", agentName, amount, depot.getLiquid());
	
			result.setResult(jsonDepot);
		
		} catch (Exception e) {
			log.error("Cannot add money to depot", e);
			result.setError(e.getMessage());
		}
		
		return result;
	}

	private Response removeMoneyFromDepot(Request req) {
		Response result = new Response(req);
		
		try {
			String agentName = req.getParameter("agentname", String.class);  
			double amount = req.getParameter("price", Double.class);  
		
		
			Depot depot = this.getDepot(agentName);
			if (depot == null) {
				throw new Exception("Depot " + agentName + " does not exist");
			}
	
			double removedMoney = depot.removeLiquid(amount);
	
			JsonElement jsonDepot = gson.toJsonTree(depot);
			this.getCommunicator().write(this.getDatapointBuilder().newDatapoint(this.createDepotAddress(agentName)).setValue(jsonDepot));
			log.debug("Agent={}>Removed money={}. Total amount={}", agentName, amount, depot.getLiquid());
	
			result.setResult(jsonDepot);
			
		} catch (Exception e) {
			log.error("Cannot remove money to depot", e);
			result.setError(e.getMessage());
		}
		
		return result;
			
			
	}
	
	
//
//	@Override
//	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
//		JsonRpcResponse result = null;
//
//		log.debug("Got request={}", parameterdata);
//
//		try {
//			JsonElement depot;
//			switch (parameterdata.getMethod()) {
//			case "registerdepot":
//				// Parameterdata: 1) name, 2) type
//				depot = this.registerDepot(parameterdata.getParameter(0, String.class), parameterdata.getParameter(1, String.class));
//				result = new JsonRpcResponse(parameterdata, depot);
//				break;
//			case "unregisterdepot":
//				// Parameterdata: 1) name
//				this.unregisterDepot(parameterdata.getParameter(0, String.class));
//				result = new JsonRpcResponse(parameterdata, new JsonPrimitive("OK"));
//				break;
//			case "buy":
//				// Parameterdata: 1) name, 2) stock name, 3) price; 4) volume
//				depot = this.buy(
//						parameterdata.getParameter(0, String.class),
//						parameterdata.getParameter(1, String.class),
//						parameterdata.getParameter(2, Double.class),
//						parameterdata.getParameter(3, Integer.class));
//				result = new JsonRpcResponse(parameterdata, depot);
//				break;
//			case "sell":
//				// Parameterdata: 1) name, 2) stock name, 3) price; 4) volume
//				depot = this.sell(
//						parameterdata.getParameter(0, String.class),
//						parameterdata.getParameter(1, String.class),
//						parameterdata.getParameter(2, Double.class),
//						parameterdata.getParameter(3, Integer.class));
//				result = new JsonRpcResponse(parameterdata, depot);
//				break;
//			case "getdepotinfo":
//				// Parameterdata: 1) name
//				depot = this.getDepotAsJson(parameterdata.getParameter(0, String.class));
//				result = new JsonRpcResponse(parameterdata, depot);
//				break;
//			case "addmoney":
//				// Parameter: 1) name, 2) amount
//				depot = this.addMoneyToDepot(parameterdata.getParameter(0, String.class), parameterdata.getParameter(1, Double.class));
//				result = new JsonRpcResponse(parameterdata, depot);
//				break;
//			case "removemoney":
//				// Parameter: 1) name, 2) amount
//				depot = this.removeMoneyFromDepot(parameterdata.getParameter(0, String.class), parameterdata.getParameter(1, Double.class));
//				result = new JsonRpcResponse(parameterdata, depot);
//				break;
//			default:
//				throw new Exception("Method " + parameterdata.getMethod() + " does not exist");
//			}
//		} catch (Exception e) {
//			result = new JsonRpcResponse(parameterdata, new JsonRpcError("ERROR", -1, e.getMessage(), e.getMessage()));
//		}
//
//		return result;
//	}

	// ===provide services===
	// Get depotinfo
	// Register depot
	// Deregister depot
	// Buy
	// Sell
	// Pay into depot

	@Override
	protected void shutDownImplementation() throws Exception {
		// TODO Auto-generated method stub

	}

@Override
protected void executeCustomPreProcessing() throws Exception {
	// TODO Auto-generated method stub
	
}

@Override
protected void executeFunction() throws Exception {
	// TODO Auto-generated method stub
	
}

@Override
protected void executeCustomPostProcessing() throws Exception {
	// TODO Auto-generated method stub
	
}

@Override
protected void updateCustomDatapointsById(String id, JsonElement data) {
	// TODO Auto-generated method stub
	
}

@Override
protected void shutDownThreadExecutor() throws Exception {
	// TODO Auto-generated method stub
	
}

}
