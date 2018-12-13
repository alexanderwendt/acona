package at.tuwien.ict.acona.evolutiondemo.traderagent;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import at.tuwien.ict.acona.mq.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

public class RandomBuySellIndicator extends CellFunctionThreadImpl {

	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private boolean swapSignal = true;
	private double threshold = 0.8;

	public static final String GENERATESIGNAL = "generatesignal";
	
	@Override
	protected void cellFunctionThreadInit() throws Exception {
		// Add subfunctions
		this.addRequestHandlerFunction(GENERATESIGNAL, (Request input) -> generateSignal(input));
	}
	
	private Response generateSignal(Request req) {
		Response result = new Response(req);
		
		try {
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

			result.setResult(signals);
			log.debug("Random indicator proposed the following trades={}", signals);
		
		} catch (Exception e) {
			log.error("Cannot send signal", e);
			result.setError(e.getMessage());
		}
		
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
	protected void updateCustomDatapointsById(String id, JsonElement data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void shutDownThreadExecutor() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
