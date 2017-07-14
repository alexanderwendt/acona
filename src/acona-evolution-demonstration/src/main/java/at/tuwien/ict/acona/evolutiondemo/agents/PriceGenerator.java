package at.tuwien.ict.acona.evolutiondemo.agents;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Datapoints;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;
import at.tuwien.ict.acona.evolutiondemo.traderagent.EMAIndicator;

/**
 * This function generates a highest, lowest and close price for the system and writes it into the working memory of itself. On trigger, 
 * it creates these 3 numbers, adds to the history and puts in the memory
 * 
 * @author wendt
 *
 */
public class PriceGenerator extends CellFunctionThreadImpl {
	
	private final static Logger log = LoggerFactory.getLogger(PriceGenerator.class);
	
	private double high, low, close;
	private int sinusperiod=20;
	private int currentPeriod=0;
	
	private final String dataAddress = "data";
	private JsonObject functionResult;
	
	private final static String ATTRIBUTESTOCKNAME = "stockname";
	
	private String stockName = "";

	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		
		return null;
	}

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		stockName = this.getFunctionConfig().getProperty(ATTRIBUTESTOCKNAME, "");
		
		this.addManagedDatapoint(DatapointConfig.newConfig(dataAddress, dataAddress, SyncMode.WRITEONLY));
		
	}

	@Override
	protected void executeFunction() throws Exception {
		this.close = 2 + Math.sin((double)currentPeriod/(double)sinusperiod);
		
		this.high = this.close + Math.random()*0.5;
		this.low = this.close - Math.random()*0.5;
		
		functionResult = new JsonObject();
		this.functionResult.addProperty("name", stockName);
		this.functionResult.addProperty("run", this.currentPeriod);
		this.functionResult.addProperty("close", this.close);
		this.functionResult.addProperty("high", this.high);
		this.functionResult.addProperty("low", this.low);
		
		this.currentPeriod++;
		
		log.debug("Generated price={}", functionResult);
		
		this.getValueMap().put(dataAddress, Datapoints.newDatapoint(dataAddress).setValue(functionResult));
	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		//Put the generated values in the memory
		
	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		
	}

	@Override
	protected void shutDownExecutor() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
