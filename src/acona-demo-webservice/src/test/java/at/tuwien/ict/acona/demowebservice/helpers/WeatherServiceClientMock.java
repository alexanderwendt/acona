package at.tuwien.ict.acona.demowebservice.helpers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.datastructures.Chunk;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Datapoints;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

/**
 * This is a class that reads the weather from the internet and presents it as datapoints on a certain address
 * 
 * @author wendt
 *
 */
public class WeatherServiceClientMock extends CellFunctionThreadImpl {
	
	private final static Logger log = LoggerFactory.getLogger(WeatherServiceClientMock.class);
	
	public final static String WEATHERADDRESSID = "weatheraddress";

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		this.setExecuteOnce(false);
		this.setExecuteRate(1000);
		
	}
	
	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void executeFunction() throws Exception {
		try {
			//Generate weather data
			JsonElement result = Chunk.newChunk("Testresult", "tester").setValue("Anyvalue", 2.0).toJsonObject();
			
			//write it to the public datapoint
			//Through the write map,
			this.getValueMap().get(WEATHERADDRESSID).setValue(result);
			
			//this.getCommunicator().write(Datapoints.newDatapoint("blabla").setValue(result));
			
			log.debug("wrote weather data={} to={}", result, this.getValueMap().get(WEATHERADDRESSID).getCompleteAddress());
			
		} catch (Exception e) {
			log.error("Cannot return weather data", e);
		}
	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {

		
	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void shutDownExecutor() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
