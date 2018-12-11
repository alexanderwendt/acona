package at.tuwien.ict.acona.demowebservice.helpers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.mq.datastructures.Chunk;
import at.tuwien.ict.acona.mq.datastructures.ChunkBuilder;


/**
 * This is a class that reads the weather from the internet and presents it as datapoints on a certain address
 * 
 * @author wendt
 *
 */
public class WeatherServiceClientMock extends CellFunctionThreadImpl {
	
	private final static Logger log = LoggerFactory.getLogger(WeatherServiceClientMock.class);
	
	public final static String WEATHERADDRESSID = "weatheraddress";
	
	public final static String CITYNAME = "cityname";
	public final static String USERID = "userid";
	
	private boolean swap=false; 
	
	String cityName = "";
	String userid ="";

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		this.setExecuteOnce(false);
		this.setExecuteRate(10000);
		
		this.cityName = this.getFunctionConfig().getProperty(CITYNAME);
		this.userid = this.getFunctionConfig().getProperty(USERID);
		
	}

	@Override
	protected void executeFunction() throws Exception {
		try {
			//Generate weather data
			Chunk result = ChunkBuilder.newChunk(this.getFunctionName() + "_result_" + cityName, "WeatherData")
					.setValue("City", this.cityName + "_Mock");
			
			if (this.swap==false) {
				result.setValue("Temperature", 24.5 + Math.random()-0.5);
				this.swap = true;
			} else {
				result.setValue("Temperature", 2.5 + Math.random()-0.5);
				this.swap = false;
			}
					
			
			//write it to the public datapoint
			//Through the write map,
			this.getValueMap().get(WEATHERADDRESSID).setValue(result.toJsonObject());
			
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
	protected void shutDownThreadExecutor() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateCustomDatapointsById(String id, JsonElement data) {
		// TODO Auto-generated method stub
		
	}

}
