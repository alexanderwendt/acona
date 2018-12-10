package at.tuwien.ict.acona.evolutiondemo.traderagent;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

public class RandomBuySellIndicator extends CellFunctionImpl {

	private final static Logger log = LoggerFactory.getLogger(RandomBuySellIndicator.class);

	private boolean swapSignal = true;
	private double threshold = 0.8;

	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		JsonObject signals = new JsonObject();

		double randomNumber = Math.random();

		if (randomNumber >= threshold) {
			if (swapSignal == true) {
				signals.addProperty("buy", true);
				signals.addProperty("sell", false);
			} else {
				signals.addProperty("sell", true);
				signals.addProperty("buy", false);
			}

			if (swapSignal == true) {
				swapSignal = false;
			} else {
				swapSignal = true;
			}

		} else {
			signals.addProperty("sell", false);
			signals.addProperty("buy", false);
		}

		JsonRpcResponse result = new JsonRpcResponse(parameterdata, signals);
		log.debug("Random indicator proposed the following trades={}", signals);

		return result;
	}

	@Override
	protected void cellFunctionInit() throws Exception {
		// TODO Auto-generated method stub

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
