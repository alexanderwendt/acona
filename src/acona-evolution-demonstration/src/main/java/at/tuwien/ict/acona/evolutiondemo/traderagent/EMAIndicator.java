package at.tuwien.ict.acona.evolutiondemo.traderagent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import at.tuwien.ict.acona.mq.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.mq.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.mq.cell.config.DatapointConfig;
import at.tuwien.ict.acona.mq.datastructures.Datapoint;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

public class EMAIndicator extends CellFunctionThreadImpl {

	private final static Logger log = LoggerFactory.getLogger(EMAIndicator.class);

	// === fixed variables ===//
	public final static String ATTRIBUTESTOCKMARKETADDRESS = "stockmarketaddress";
	private final static String IDPRICE = "price";
	public static final String GENERATESIGNAL = "generatesignal";
	public static final String ATTRIBUTEEMALONG = "emalong";
	public static final String ATTRIBUTEEMASHORT = "emashort";

	// === Get through config ===//
	//private double initEmaLong = 10;
	//private double initEmaShort = 5;

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
	protected void cellFunctionThreadInit() throws Exception {
		// Get price

		// Settings
		//this.emaLongPeriod = initEmaLong;
		//this.emaShortPeriod = initEmaShort;

		// Subscribe price address but without trigger to start
		Datapoint stockMarket = this.getDatapointBuilder().newDatapoint(this.getFunctionConfig().getProperty(ATTRIBUTESTOCKMARKETADDRESS));
		this.emaLong = Double.valueOf(this.getFunctionConfig().getProperty(ATTRIBUTEEMALONG));
		this.emaShort = Double.valueOf(this.getFunctionConfig().getProperty(ATTRIBUTEEMASHORT));

		// Add subscription to the stock market price
		this.addManagedDatapoint(DatapointConfig.newConfig(IDPRICE, stockMarket.getAgent() + ":" + stockMarket.getAddress(), SyncMode.SUBSCRIBEONLY));
		
		// Add subfunctions
		this.addRequestHandlerFunction(GENERATESIGNAL, (Request input) -> calculateSignal(input));

	}
	
	private Response calculateSignal(Request req) {
		Response result = new Response(req);
		
		try {
			JsonObject calc = new JsonObject();

			if (this.emaShortPrevious < this.emaLongPrevious && this.emaShort > this.emaLong) {
				log.debug("Buy signal as short EMA crosses long EMA from beneath");
				// if (depot.getLiquid()>this.closePrice * 1) {
				this.buySignal = true;
				// log.debug("Enough liquid there. Set buy signal");
				// } else {
				// log.debug("No enough money, no buy signal");
				// }

			} else {
				this.buySignal = false;
			}

			if (this.emaShortPrevious > this.emaLongPrevious && this.emaShortPeriod < this.emaLong) {
				// if (this.depot.getAsset().stream().filter(a->a.getStockName().equals(this.stockName)).findFirst().isPresent()
				// && (this.depot.getAsset().stream().filter(a->a.getVolume()>=1)).findFirst().isPresent()) {
				this.sellSignal = true;
				log.debug("Sell signa as short EMA crosses long EMA from above");
				// } else {
				// log.debug("No sell signal as the volume of stock is not enough");
				// }
			} else {
				this.sellSignal = false;
			}

			calc.addProperty("buy", this.buySignal);
			calc.addProperty("sell", this.sellSignal);

			result.setResult(calc);
		} catch (Exception e) {
			log.error("Cannot register depot", e);
			result.setError(e.getMessage());
		}
		
		return result;
	}

	@Override
	protected void shutDownImplementation() throws Exception {
		// TODO Auto-generated method stub

	}
	
	@Override
	protected void updateCustomDatapointsById(String id, JsonElement data) {
		try {
			if (id.equals(IDPRICE)) {
				// Update emas
				this.emaLongPrevious = this.emaLong;
				this.emaShortPrevious = this.emaShort;

				// Update prices
				this.closePrice = this.getValueFromJsonDatapoint(data).getAsJsonObject().getAsJsonPrimitive("close").getAsDouble();
				this.highPrice = this.getValueFromJsonDatapoint(data).getAsJsonObject().getAsJsonPrimitive("high").getAsDouble();
				this.lowPrice = this.getValueFromJsonDatapoint(data).getAsJsonObject().getAsJsonPrimitive("low").getAsDouble();
				
				//Update calculation
				this.calculateIndicator();

			}
		} catch (Exception e) {
			log.error("Cannot get data", e);
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
	protected void shutDownThreadExecutor() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
