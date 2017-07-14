package at.tuwien.ict.acona.evolutiondemo.traderagent;

import java.util.Map;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

public class PermanentBuySellIndicator extends CellFunctionImpl {
	
	private boolean swapSignal = true;

	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		JsonObject signals = new JsonObject();
		
		if (swapSignal==true) {
			signals.addProperty("buy", true);
			signals.addProperty("sell", false);
		} else {
			signals.addProperty("sell", true);
			signals.addProperty("buy", false);
		}

		JsonRpcResponse result = new JsonRpcResponse(parameterdata, signals);
		
		if (swapSignal==true) {
			swapSignal = false;
		} else {
			swapSignal = true;
		}
		
		
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
