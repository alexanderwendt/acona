package at.tuwien.ict.acona.evolutiondemo.traderagent;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionCodelet;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.DatapointBuilder;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.Depot;

public class Trader extends CellFunctionCodelet {

	private final static Logger log = LoggerFactory.getLogger(Trader.class);

	public final static String ATTRIBUTESTOCKMARKETADDRESS = "stockmarketaddress";
	public final static String ATTRIBUTEBROKERADDRESS = "brokeraddress";
	public final static String ATTRIBUTECONTROLLERADDRESS = "controlleraddress";
	public final static String ATTRIBUTESIGNALADDRESS = "signaladdress";

	public final static String ATTRIBUTESTARTSIZE = "startsize";
	public final static String ATTRIBUTEAGENTTYPE = "type";
	public final static String ATTRIBUTESTOCKNAME = "stockname";

	private final static String IDPRICE = "price";

	private String initBrokerAddress = "Broker:BrokerService";
	private String initStockmarketAddress = "Stockmarket:Price";
	private String initSignalAddress = "EmaSignalFunction";
	private String initControllerAddress = "controller";

	private String localDepotAddress = "localdepot";

	private double initStartSize = 1000;
	private String initType = "type1";
	private String initStockName = "Fingerprint";

	// === static values ===//
	private String brokerAddress = "";
	private String stockMarketAddress = "";
	private String signalAddress = "";
	private String controllerAddress = "";

	private String agentType = "";
	private String stockName = "";
	private double startSize = 0;
	private double multiplyLimit = 0;
	private double deathLimit = 0;

	// Dynamic values
	private Depot depot = null;
	private double highPrice = 0;
	private double lowPrice = 0;
	private double closePrice = 0;
	private boolean buySignal = false;
	private boolean sellSignal = false;
	private boolean killSignal = false;

	@Override
	protected void cellFunctionCodeletInit() throws Exception {
		stockMarketAddress = this.getFunctionConfig().getProperty(ATTRIBUTESTOCKMARKETADDRESS, initStockmarketAddress);
		brokerAddress = this.getFunctionConfig().getProperty(ATTRIBUTEBROKERADDRESS, initBrokerAddress);
		signalAddress = this.getFunctionConfig().getProperty(ATTRIBUTESIGNALADDRESS, initSignalAddress);
		controllerAddress = this.getFunctionConfig().getProperty(ATTRIBUTECONTROLLERADDRESS, initControllerAddress);

		this.startSize = Double.valueOf(this.getFunctionConfig().getProperty(ATTRIBUTESTARTSIZE, String.valueOf(initStartSize)));

		this.multiplyLimit = startSize * 2.0;
		this.deathLimit = startSize * 0.3;

		this.agentType = this.getFunctionConfig().getProperty(ATTRIBUTEAGENTTYPE, initType);
		this.stockName = this.getFunctionConfig().getProperty(ATTRIBUTEAGENTTYPE, initStockName);

		// Add subscription to the stock market price
		log.debug("Read from address={}", stockMarketAddress);
		this.addManagedDatapoint(DatapointConfig.newConfig(IDPRICE, DatapointBuilder.newDatapoint(stockMarketAddress).getAddress(), DatapointBuilder.newDatapoint(stockMarketAddress).getAgent(), SyncMode.READONLY));

		// Create a depot
		this.createDepot();

	}

//	@Override
//	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	protected JsonRpcResponse performCodeletOperation(JsonRpcRequest parameterdata, String caller) {

		if (parameterdata.getMethod().equals("getdepot")) {

		}

		return null;
	}

	@Override
	public void executeCodeletPreprocessing() throws Exception {
		// Read depot
		// Write to broker a new depot
		JsonRpcRequest req = new JsonRpcRequest("getdepotinfo", 1);
		req.setParameters(this.getCell().getLocalName());
		depot = this.getCommunicator().execute(this.brokerAddress, req).getResult(new TypeToken<Depot>() {});

		// Update prices
		log.debug("Value map={}", this.getValueMap());
		this.closePrice = this.getValueMap().get(IDPRICE).getValue().getAsJsonObject().getAsJsonPrimitive("close").getAsDouble();
		this.highPrice = this.getValueMap().get(IDPRICE).getValue().getAsJsonObject().getAsJsonPrimitive("high").getAsDouble();
		this.lowPrice = this.getValueMap().get(IDPRICE).getValue().getAsJsonObject().getAsJsonPrimitive("low").getAsDouble();
	}

	@Override
	protected void executeFunction() throws Exception {

		log.info("{}>Start agent caluclation", this.getAgentName());
		// Program logic
		// 2. Check depot death
		this.killSignal = this.killAgentOnDepotDeath();
		if (this.killSignal == false) {
			// 1. Split depot if necessary
			this.multiplyAgent();
			// 3. Calculate indicator
			// this.calculateIndicator();
			// 4. Calculate signal
			this.calculateSignal();
			// 5. Execute signal
			this.executeTrade();

			log.info("{}:{}>Finished. Assets in the depot: {}", this.getAgentName(), this.agentType, this.depot.getAssets());

		} else {
			// If the kill signal has been set, the system shall exit.
			this.setCommand(ControlCommand.EXIT);

			DelayedCellShutDown killSwitch = new DelayedCellShutDown();
			killSwitch.killSwitch(2000, this.getCell());
			log.info("Cell will also be shut down");
		}
	}

	@Override
	public void executeCodeletPostprocessing() throws Exception {
		// Reset signals
		this.buySignal = false;
		this.sellSignal = false;

		// Write the local depot
		Gson gson = new Gson();
		JsonElement jsonDepot = gson.toJsonTree(depot);
		this.writeLocal(DatapointBuilder.newDatapoint(this.localDepotAddress).setValue(jsonDepot));
	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
//		if (data.containsKey(IDPRICE)) {
//			this.setStart();
//		}
//		
	}

	@Override
	protected void shutDownCodelet() throws Exception {
		// Delete the depot
		this.deleteDepot();

		log.info("{}>Agent is killed", this.getCell().getLocalName());
		// Then, agent is killed
	}

//	@Override
//	protected void shutDownExecutor() throws Exception {
//		//Delete the depot
//		this.deleteDepot();
//		
//		log.info("{}>Agent is killed", this.getCell().getLocalName());
//		//Then, agent is killed
//		
//	}

	private void createDepot() throws Exception {
		// Write to broker a new depot
		JsonRpcRequest req = new JsonRpcRequest("registerdepot", 0);
		req.setParameters(this.getCell().getLocalName(), this.agentType);
		JsonRpcResponse result1 = this.getCommunicator().execute(this.brokerAddress, req);

		// Add money to broker
		if (result1.hasError() == true) {
			throw new Exception("Cannot create depot. " + result1.getError().getMessage());
		}

		JsonRpcRequest req2 = new JsonRpcRequest("addmoney", 0);
		req2.setParameters(this.getCell().getLocalName(), this.startSize);
		JsonRpcResponse result2 = this.getCommunicator().execute(this.brokerAddress, req2);

		if (result2.hasError() == true) {
			throw new Exception("Cannot add money= " + this.startSize + " to depot. " + result2.getError().getMessage());
		}

		this.depot = (new Gson()).fromJson(result2.getResult(), Depot.class);
		log.debug("Depot created={}", this.depot);
	}

	private void deleteDepot() throws Exception {
		// Sell everything
		this.depot.getAssets().forEach(a -> {
			JsonRpcRequest req = new JsonRpcRequest("sell", 0);
			req.setParameters(this.getCell().getLocalName(), a.getStockName(), this.closePrice, a.getVolume());
			JsonRpcResponse result1 = null;
			try {
				result1 = this.getCommunicator().execute(this.brokerAddress, req);
			} catch (Exception e) {
				log.error("Cannot sell stock={} due to error={}", a.getStockName(), result1);
			}
		});

		// Addregister
		JsonRpcRequest req = new JsonRpcRequest("unregisterdepot", 0);
		req.setParameters(this.getCell().getLocalName());
		JsonRpcResponse result1 = this.getCommunicator().execute(this.brokerAddress, req);

		// Check if unregister error
		if (result1.hasError() == true) {
			throw new Exception("Cannot delete depot. " + result1.getError().getMessage());
		}

		this.depot = null;
		log.debug("Depot deleted={}", depot);
	}

	private void multiplyAgent() {
		// If depot size > 2x start size
		if (this.depot.getTotalValue() >= this.multiplyLimit) {
			log.info("Time to split and create new cells");
		}
	}

	private boolean killAgentOnDepotDeath() throws Exception {
		boolean isKilled = false;

		if (this.depot.getTotalValue() < this.deathLimit) {
			log.info("Agent dies. Depot={}, deathlimit={}", this.depot.getTotalValue(), this.deathLimit);
			// this.shutDownCodelet();
			// this.getCell().takeDownCell();
			isKilled = true;
			// this.setAllowedToRun(false);
			// this.setActive(false);
			// log.debug("Agent is killed");
		}

		return isKilled;
	}

	private void calculateSignal() throws Exception {
		// Calculate buy or sell signal based on indicators
		JsonRpcRequest req = new JsonRpcRequest("any", 0);
		JsonRpcResponse result = this.getCommunicator().execute(this.signalAddress, req);

		this.buySignal = result.getResult().getAsJsonObject().getAsJsonPrimitive("buy").getAsBoolean();
		this.sellSignal = result.getResult().getAsJsonObject().getAsJsonPrimitive("sell").getAsBoolean();

	}

	private void executeTrade() throws Exception {
		log.info("Buy signal={}; sell signal={}", this.buySignal, this.sellSignal);
		if (this.buySignal == true) {
			if (depot.getLiquid() > this.closePrice * 1) {
				JsonRpcRequest request1 = new JsonRpcRequest("buy", 0);
				// request1.setParameterAsValue(0, traderAgentName);
				// request1.setParameterAsValue(1, traderType);
				request1.setParameters(this.getCell().getLocalName(), this.stockName, this.closePrice, 1);
				JsonRpcResponse result = this.getCommunicator().execute(this.brokerAddress, request1);
				if (result.hasError()) {
					throw new Exception("Cannot buy stock. " + result.getError().getMessage());
				}

				this.depot = (new Gson()).fromJson(result.getResult(), Depot.class);
				log.info("Stock bought={}. Depot change={}", request1.getParams(), this.depot);
			} else {
				log.debug("No enough money, no buy signal");
			}

		}

		if (this.sellSignal == true) {
			if (this.depot.getAssets().stream().filter(a -> a.getStockName().equals(this.stockName)).findFirst().isPresent()
					&& (this.depot.getAssets().stream().filter(a -> a.getVolume() >= 1)).findFirst().isPresent()) {

				JsonRpcRequest request1 = new JsonRpcRequest("sell", 0);
				request1.setParameters(this.getCell().getLocalName(), this.stockName, this.closePrice, 1);
				JsonRpcResponse result = this.getCommunicator().execute(this.brokerAddress, request1);
				if (result.hasError()) {
					throw new Exception("Cannot buy stock. " + result.getError().getMessage());
				}

				this.depot = (new Gson()).fromJson(result.getResult(), Depot.class);
				log.info("Stock sold={}. Depot change={}", request1.getParams(), this.depot);

			} else {
				log.debug("No sell signal as the volume of stock is not enough");
			}

		}
	}

}
