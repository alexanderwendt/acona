package at.tuwien.ict.acona.evolutiondemo.traderagent;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

public class EMAIndicator extends CellFunctionThreadImpl {

	private final static Logger log = LoggerFactory.getLogger(EMAIndicator.class);

	// === fixed variables ===//
	public final static String ATTRIBUTESTOCKMARKETADDRESS = "stockmarketaddress";
	private final static String IDPRICE = "price";

	// === Get through config ===//
	private double initEmaLong = 10;
	private double initEmaShort = 5;

	// === static ===//
	private double emaLongPeriod = 0;
	private double emaShortPeriod = 0;

	// === dynamic ===//
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
	protected void cellFunctionInit() throws Exception {
		// Get price

		// Settings
		this.emaLongPeriod = initEmaLong;
		this.emaShortPeriod = initEmaShort;

		// Subscribe price address but without trigger to start
		Datapoint stockMarket = DatapointBuilder.newDatapoint(this.getFunctionConfig().getProperty(ATTRIBUTESTOCKMARKETADDRESS, ""));

		// Add subscription to the stock market price
		this.addManagedDatapoint(DatapointConfig.newConfig(IDPRICE, stockMarket.getAddress(), stockMarket.getAgent(), SyncMode.SUBSCRIBEONLY));

	}

	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		JsonRpcResponse result = null;

		// Calculate signals
		result = new JsonRpcResponse(parameterdata, this.calculateSignal());

		return result;
	}

	@Override
	protected void shutDownImplementation() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateDatapointsById(Map<String, Datapoint> data) {
		// Update day values
		if (data.containsKey(IDPRICE)) {
			// Update emas
			this.emaLongPrevious = this.emaLong;
			this.emaShortPrevious = this.emaShort;

			this.calculateIndicator();

			// Update prices
			this.closePrice = this.getDatapointFromId(data, IDPRICE).getValue().getAsJsonObject().getAsJsonPrimitive("close").getAsDouble();
			this.highPrice = this.getDatapointFromId(data, IDPRICE).getValue().getAsJsonObject().getAsJsonPrimitive("high").getAsDouble();
			this.lowPrice = this.getDatapointFromId(data, IDPRICE).getValue().getAsJsonObject().getAsJsonPrimitive("low").getAsDouble();

		}
	}

	private void calculateIndicator() {
		// Create an EMA of 2 values
		this.emaLong = this.calculateEMA(this.emaLongPeriod, this.closePrice, this.emaLongPrevious);
		this.emaShort = this.calculateEMA(this.emaShortPeriod, this.closePrice, this.emaShortPrevious);
		log.debug("Price={}; EMA long={}, previous={}); EMA short={}, previous={}", this.closePrice, this.emaLong, this.emaLongPrevious, this.emaShort, this.emaShortPrevious);
	}

	private double calculateEMA(double period, double price, double emaPrevious) {
		double result = 0;

		// Multiplier: (2 / (Time periods + 1) ) = (2 / (10 + 1) ) = 0.1818 (18.18%)
		// EMA: {Close - EMA(previous day)} x multiplier + EMA(previous day).

		result = (price - emaPrevious) * (2 / (period + 1)) + emaPrevious;

		return result;
	}

	private JsonObject calculateSignal() {
		JsonObject result = new JsonObject();

		if (this.emaShortPrevious < this.emaLongPrevious && this.emaShort > this.emaLong) {
			log.debug("Buy signal as short EMA crosses long EMA from beneath");
			// if (depot.getLiquid()>this.closePrice * 1) {
			this.buySignal = true;
			// log.debug("Enough liquid there. Set buy signal");
			// } else {
			// log.debug("No enough money, no buy signal");
			// }

		}

		if (this.emaShortPrevious > this.emaLongPrevious && this.emaShortPeriod < this.emaLong) {
			// if (this.depot.getAsset().stream().filter(a->a.getStockName().equals(this.stockName)).findFirst().isPresent()
			// && (this.depot.getAsset().stream().filter(a->a.getVolume()>=1)).findFirst().isPresent()) {
			this.sellSignal = true;
			log.debug("Sell signa as short EMA crosses long EMA from above");
			// } else {
			// log.debug("No sell signal as the volume of stock is not enough");
			// }
		}

		result.addProperty("buy", this.buySignal);
		result.addProperty("sell", this.sellSignal);

		return result;
	}

}
