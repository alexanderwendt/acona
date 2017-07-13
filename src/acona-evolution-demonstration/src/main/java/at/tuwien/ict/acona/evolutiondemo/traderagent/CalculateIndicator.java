package at.tuwien.ict.acona.evolutiondemo.traderagent;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Datapoints;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;
import at.tuwien.ict.acona.evolutiondemo.brokeragent.Depot;

public class CalculateIndicator extends CellFunctionThreadImpl {
	
	private final static Logger log = LoggerFactory.getLogger(CalculateIndicator.class);
	
	public final static String ATTRIBUTESTOCKMARKETADDRESS = "stockmarketaddress";
	private final static String IDPRICE = "price";
	
	private double initStartSize = 1000;
	private String initBrokerFullName = "Broker";
	private String initType = "type1";
	private double initEmaLong = 10;
	private double initEmaShort = 5;
	
	private double startSize = 0;
	private double multiplyLimit = 0;
	private double deathLimit = 0;
	private String brokerFullServiceName = "";
	private String agentType = "";
	private double emaLongPeriod = 0;
	private double emaShortPeriod = 0;
	
	//Dynamic values
	private Depot depot = null;
	private double highPrice = 0;
	private double lowPrice = 0;
	private double closePrice = 0;
	private double emaLongPrevious = 0;
	private double emaLong = 0;
	private double emaShortPrevious = 0;
	private double emaShort = 0;
	private boolean buySignal = false;
	private boolean sellSignal = false;
	
	
	@Override
	protected void cellFunctionThreadInit() throws Exception {
		Datapoint stockMarket = Datapoints.newDatapoint(this.getFunctionConfig().getProperty(ATTRIBUTESTOCKMARKETADDRESS, ""));
		
		//Add subscription to the stock market price
		this.addManagedDatapoint(DatapointConfig.newConfig(IDPRICE, stockMarket.getAddress(), stockMarket.getAgent(), SyncMode.SUBSCRIBEONLY));
		
		this.startSize = initStartSize;
		this.multiplyLimit = startSize * 2.0;
		this.deathLimit = startSize * 0.3;
		this.brokerFullServiceName = initBrokerFullName;
		this.agentType = initType;
		this.emaLongPeriod = initEmaLong;
		this.emaShortPeriod = initEmaShort;
		
		//Create a depot
		this.createDepot();
	}

	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void executeFunction() throws Exception {
		log.trace("Start agent caluclation");
		//Program logic
		//1. Split depot if necessary
		this.multiplyAgent();
		//2. Check depot death
		this.killAgentOnDepotDeath();
		//3. Calculate indicator
		this.calculateIndicator();
		//4. Calculate signal
		this.calculateSignal();
		//5. Execute signal
		this.executeTrade();
		log.trace("All operations executed");
	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		//Update emas
		this.emaLongPrevious = this.emaLong;
		this.emaLongPrevious = this.emaShort;
		//Reset signals
		this.buySignal = false;
		this.sellSignal = false;
		
	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		//Update prices
		this.closePrice = this.getValueMap().get(IDPRICE).getValue().getAsJsonObject().getAsJsonPrimitive("close").getAsDouble();
		this.highPrice = this.getValueMap().get(IDPRICE).getValue().getAsJsonObject().getAsJsonPrimitive("high").getAsDouble();
		this.lowPrice = this.getValueMap().get(IDPRICE).getValue().getAsJsonObject().getAsJsonPrimitive("low").getAsDouble();
		
	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void shutDownExecutor() throws Exception {
		//Delete the depot
		this.deleteDepot();
		
		log.info("{}>Agent is killed", this.getCell().getLocalName());
		//Then, agent is killed
		
	}
	
	private void createDepot() throws Exception {
		//Write to broker a new depot
		JsonRpcRequest req = new JsonRpcRequest("registerdepot", 0);
		req.setParameters(this.getCell().getLocalName(), this.agentType);
		JsonRpcResponse result1 = this.getCommunicator().execute(this.brokerFullServiceName, req);

		//Add money to broker
		if (result1.hasError()==false) {
			throw new Exception("Cannot create depot. " + result1.getError().getMessage());
		}
		
		JsonRpcRequest req2 = new JsonRpcRequest("addmoney", 0);
		req2.setParameters(this.getCell().getLocalName(), this.startSize);
		JsonRpcResponse result2 = this.getCommunicator().execute(this.brokerFullServiceName, req2);
		
		if (result2.hasError()==false) {
			throw new Exception("Cannot add money= " + this.startSize + " to depot. " + result2.getError().getMessage());
		}
		
		this.depot = (new Gson()).fromJson(result2.getResult(), Depot.class);
		log.debug("Depot created={}", this.depot);
	}
	
	private void deleteDepot() throws Exception {
		//Sell everything
		this.depot.getAsset().forEach(a->{
			JsonRpcRequest req = new JsonRpcRequest("sell", 0);
			req.setParameters(this.getCell().getLocalName(), a.getStockName(), this.closePrice, a.getVolume());
			JsonRpcResponse result1 = null;
			try {
				result1 = this.getCommunicator().execute(this.brokerFullServiceName, req);
			} catch (Exception e) {
				log.error("Cannot sell stock={} due to error={}", a.getStockName(), result1);
			}
		});
		
		//Addregister
		JsonRpcRequest req = new JsonRpcRequest("unregisterdepot", 0);
		req.setParameters(this.getCell().getLocalName());
		JsonRpcResponse result1 = this.getCommunicator().execute(this.brokerFullServiceName, req);

		//Check if unregister error
		if (result1.hasError()==false) {
			throw new Exception("Cannot delete depot. " + result1.getError().getMessage());
		}
		
		this.depot = null;
		log.debug("Depot deleted={}");
	}
	
	private void multiplyAgent() {
		//If depot size > 2x start size
	}
	
	private void killAgentOnDepotDeath() throws Exception {
		if (this.depot.getTotalValue()<this.deathLimit) {
			this.shutDownExecutor();
		}
	}
	
	private void calculateIndicator() {
		//Create an EMA of 2 values
		this.emaLong = this.calculateEMA(this.emaLongPeriod, this.closePrice, this.emaLongPrevious);
		this.emaShort = this.calculateEMA(this.emaShortPeriod, this.closePrice, this.emaShortPrevious);
	}
	
	private double calculateEMA(double period, double price, double emaPrevious) {
		double result = 0;
		
		//Multiplier: (2 / (Time periods + 1) ) = (2 / (10 + 1) ) = 0.1818 (18.18%)
		//EMA: {Close - EMA(previous day)} x multiplier + EMA(previous day).
		
		result = (price - emaPrevious) * (2/(period + 1)) + emaPrevious;
		
		return result;
	}
	
	private void calculateSignal() {
		if (this.emaShortPrevious<this.emaLongPrevious && this.emaShort>this.emaLong) {
			this.buySignal = true;
			log.debug("Buy signal as short EMA crosses long EMA from beneath");
		}
		
		if (this.emaShortPrevious>this.emaLongPrevious && this.emaShortPeriod<this.emaLong) {
			this.sellSignal = true;
			log.debug("Sell signa as short EMA crosses long EMA from above");
			
		}
	}
	
	private void executeTrade() {
		if (this.buySignal==true) {
			
			
		}
		
		if (this.sellSignal==true) {
			
		}
	}

}
