package at.tuwien.ict.acona.evolutiondemo.stockmarketagent;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.cellfunction.SyncMode;
import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionCodelet;
import at.tuwien.ict.acona.cell.config.DatapointConfig;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Datapoints;

/**
 * This function generates a highest, lowest and close price for the system and writes it into the working memory of itself. On trigger, 
 * it creates these 3 numbers, adds to the history and puts in the memory
 * 
 * @author wendt
 *
 */
public class DummyPriceGenerator extends CellFunctionCodelet {
	
	private final static Logger log = LoggerFactory.getLogger(DummyPriceGenerator.class);
	
	private double high, low, close;
	private int sinusperiod=20;
	private int currentPeriod=0;
	
	private final String dataAddress = "data";
	private JsonObject functionResult;
	
	public final static String ATTRIBUTESTOCKNAME = "stockname";
	public final static String ATTRIBUTEMODE = "mode";
	
	private String stockName = "";
	private int mode = 0;
	
	@Override
	protected void cellFunctionCodeletInit() throws Exception {
		stockName = this.getFunctionConfig().getProperty(ATTRIBUTESTOCKNAME, "");
		this.mode = this.getFunctionConfig().getProperty(ATTRIBUTEMODE, Integer.class);
		
		this.addManagedDatapoint(DatapointConfig.newConfig(dataAddress, dataAddress, SyncMode.WRITEONLY));
		
	}

	@Override
	protected void executeFunction() throws Exception {
		
		if (this.mode==0) {
			this.executeSinusFunction();
		} else if (this.mode==1) {
			this.executeConstantFunction();
		}
		
		functionResult = new JsonObject();
		this.functionResult.addProperty("name", stockName);
		this.functionResult.addProperty("run", this.currentPeriod);
		this.functionResult.addProperty("close", this.close);
		this.functionResult.addProperty("high", this.high);
		this.functionResult.addProperty("low", this.low);
		
		this.currentPeriod++;
		
		log.debug("Generated price={}. Put it on address={}", functionResult, this.getAgentName() + ":" + dataAddress);
		
		this.getValueMap().put(dataAddress, Datapoints.newDatapoint(dataAddress).setValue(functionResult));
	}
	
	private void executeSinusFunction() {
		this.close = 2 + Math.sin((double)currentPeriod/(double)sinusperiod);
		this.high = this.close + Math.random()*0.5;
		this.low = this.close - Math.random()*0.5;
	}
	
	private void executeConstantFunction() {
		this.close = 2;
		this.high = 2.1;
		this.low = 1.9;
	}

//	@Override
//	protected void executeCustomPostProcessing() throws Exception {
//		this.setServiceState(ServiceState.FINISHED);
//		
//	}
//
//	@Override
//	protected void executeCustomPreProcessing() throws Exception {
//		this.setServiceState(ServiceState.RUNNING);
//		
//	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		
	}

//	@Override
//	protected void shutDownExecutor() throws Exception {
//		// TODO Auto-generated method stub
//		
//	}



}
