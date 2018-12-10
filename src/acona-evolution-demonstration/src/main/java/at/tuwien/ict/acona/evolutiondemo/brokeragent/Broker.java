package at.tuwien.ict.acona.evolutiondemo.brokeragent;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

public class Broker extends CellFunctionImpl {

	// private Map<String, List<String>> agentTypeMap = new HashMap<String, List<String>>(); //Type, agentname
	private static final Logger log = LoggerFactory.getLogger(Broker.class);

	public final static String ATTRIBUTESTOCKNAME = "stockname";

	private String stockName = "";
	private final static String PREFIXDEPOTADDRESS = "depot";

	private final Gson gson = new Gson();

	@Override
	protected void cellFunctionInit() throws Exception {
		stockName = this.getFunctionConfig().getProperty(ATTRIBUTESTOCKNAME, "");

		// === GENERATE RESPONDER NECESSARY ===//
		this.getFunctionConfig().setGenerateReponder(true);

		log.debug("Broker initialized");

	}

	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		JsonRpcResponse result = null;

		log.debug("Got request={}", parameterdata);

		try {
			JsonElement depot;
			switch (parameterdata.getMethod()) {
			case "registerdepot":
				// Parameterdata: 1) name, 2) type
				depot = this.registerDepot(parameterdata.getParameter(0, String.class), parameterdata.getParameter(1, String.class));
				result = new JsonRpcResponse(parameterdata, depot);
				break;
			case "unregisterdepot":
				// Parameterdata: 1) name
				this.unregisterDepot(parameterdata.getParameter(0, String.class));
				result = new JsonRpcResponse(parameterdata, new JsonPrimitive("OK"));
				break;
			case "buy":
				// Parameterdata: 1) name, 2) stock name, 3) price; 4) volume
				depot = this.buy(
						parameterdata.getParameter(0, String.class),
						parameterdata.getParameter(1, String.class),
						parameterdata.getParameter(2, Double.class),
						parameterdata.getParameter(3, Integer.class));
				result = new JsonRpcResponse(parameterdata, depot);
				break;
			case "sell":
				// Parameterdata: 1) name, 2) stock name, 3) price; 4) volume
				depot = this.sell(
						parameterdata.getParameter(0, String.class),
						parameterdata.getParameter(1, String.class),
						parameterdata.getParameter(2, Double.class),
						parameterdata.getParameter(3, Integer.class));
				result = new JsonRpcResponse(parameterdata, depot);
				break;
			case "getdepotinfo":
				// Parameterdata: 1) name
				depot = this.getDepotAsJson(parameterdata.getParameter(0, String.class));
				result = new JsonRpcResponse(parameterdata, depot);
				break;
			case "addmoney":
				// Parameter: 1) name, 2) amount
				depot = this.addMoneyToDepot(parameterdata.getParameter(0, String.class), parameterdata.getParameter(1, Double.class));
				result = new JsonRpcResponse(parameterdata, depot);
				break;
			case "removemoney":
				// Parameter: 1) name, 2) amount
				depot = this.removeMoneyFromDepot(parameterdata.getParameter(0, String.class), parameterdata.getParameter(1, Double.class));
				result = new JsonRpcResponse(parameterdata, depot);
				break;
			default:
				throw new Exception("Method " + parameterdata.getMethod() + " does not exist");
			}
		} catch (Exception e) {
			result = new JsonRpcResponse(parameterdata, new JsonRpcError("ERROR", -1, e.getMessage(), e.getMessage()));
		}

		return result;
	}

	// ===provide services===
	// Get depotinfo
	// Register depot
	// Deregister depot
	// Buy
	// Sell
	// Pay into depot

	private JsonElement registerDepot(String agentName, String agentType) throws Exception {
		Depot depot = new Depot();
		depot.setLiquid(0);
		depot.setOwner(agentName);
		depot.setOwnerType(agentType);

		// Add agent type
		// addAgentType(agentName, agentType);

		JsonElement jsonDepot = gson.toJsonTree(depot);
		this.writeLocal(DatapointBuilder.newDatapoint(this.createDepotAddress(agentName)).setValue(jsonDepot));
		log.debug("Registered agent={}, type={}", agentName, agentType);
		return jsonDepot;
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

	private void unregisterDepot(String agentName) throws Exception {
		this.getCommunicator().remove(this.createDepotAddress(agentName));
		log.debug("Unregistered agent={}", agentName);
	}

	private synchronized Depot getDepot(String agentName) throws JsonSyntaxException, Exception {
		return gson.fromJson(this.readLocal(this.createDepotAddress(agentName)).getValue(), Depot.class);
	}

	private JsonElement getDepotAsJson(String agentName) throws Exception {
		return this.readLocal(this.createDepotAddress(agentName)).getValue();
	}

	private JsonElement buy(String agentName, String stockName, double price, int volume) throws Exception {
		Depot depot = this.getDepot(agentName);
		if (depot == null) {
			throw new Exception("Depot " + agentName + " does not exist");
		}

		depot.buy(stockName, volume, price);

		JsonElement jsonDepot = gson.toJsonTree(depot);
		this.writeLocal(DatapointBuilder.newDatapoint(this.createDepotAddress(agentName)).setValue(jsonDepot));
		log.debug("Agent={}, Bought stock={}, volume={}, price={}. Depot={}", agentName, stockName, volume, price, jsonDepot);

		return jsonDepot;
	}

	private JsonElement sell(String agentName, String stockName, double price, int volume) throws Exception {
		Depot depot = this.getDepot(agentName);
		if (depot == null) {
			throw new Exception("Depot " + agentName + " does not exist");
		}

		depot.sell(stockName, volume, price);

		JsonElement jsonDepot = gson.toJsonTree(depot);
		this.writeLocal(DatapointBuilder.newDatapoint(this.createDepotAddress(agentName)).setValue(jsonDepot));
		log.debug("Agent={}, Sold stock={}, volume={}, price={}. Depot={}", agentName, stockName, volume, price, jsonDepot);

		return jsonDepot;
	}

	private JsonElement addMoneyToDepot(String agentName, double amount) throws Exception {
		Depot depot = this.getDepot(agentName);
		if (depot == null) {
			throw new Exception("Depot " + agentName + " does not exist");
		}

		depot.addLiquid(amount);

		JsonElement jsonDepot = gson.toJsonTree(depot);
		this.writeLocal(DatapointBuilder.newDatapoint(this.createDepotAddress(agentName)).setValue(jsonDepot));
		log.debug("Agent={}>Added money={}. Total amount={}", agentName, amount, depot.getLiquid());

		return jsonDepot;
	}

	private JsonElement removeMoneyFromDepot(String agentName, double amount) throws Exception {
		Depot depot = this.getDepot(agentName);
		if (depot == null) {
			throw new Exception("Depot " + agentName + " does not exist");
		}

		double removedMoney = depot.removeLiquid(amount);

		JsonElement jsonDepot = gson.toJsonTree(depot);
		this.writeLocal(DatapointBuilder.newDatapoint(this.createDepotAddress(agentName)).setValue(jsonDepot));
		log.debug("Agent={}>Added money={}. Total amount={}", agentName, amount, depot.getLiquid());

		return jsonDepot;
	}

	@Override
	protected void shutDownImplementation() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateDatapointsById(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub

	}

}
