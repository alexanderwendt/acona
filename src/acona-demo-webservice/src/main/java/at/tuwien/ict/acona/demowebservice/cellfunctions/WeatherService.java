package at.tuwien.ict.acona.demowebservice.cellfunctions;

import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import at.tuwien.ict.acona.demowebservice.cellfunctions.weather.Weather;
import at.tuwien.ict.acona.demowebservice.helpers.WeatherServiceClientMock;
import at.tuwien.ict.acona.mq.core.agentfunction.AgentFunctionThreadImpl;
import at.tuwien.ict.acona.mq.datastructures.Chunk;
import at.tuwien.ict.acona.mq.datastructures.ChunkBuilder;

/**
 * This is a class that reads the weather from the internet and presents it as datapoints on a certain address
 * 
 * @author wendt
 *
 */
public class WeatherService extends AgentFunctionThreadImpl {
	
	private final static Logger log = LoggerFactory.getLogger(WeatherService.class);
	
	public final static String CITYNAME = "cityname";
	public final static String USERID = "userid";
	
	//Address to publish on
	public final static String WEATHERADDRESSID = "weatheraddress";
	
	private String cityName="";
	private String userid="";
	
	private static final String REST_URI = "http://api.openweathermap.org/data/2.5/weather?";
	private Client client = ClientBuilder.newClient();

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		this.setExecuteOnce(false);
		this.setExecuteRate(5000);
		
		this.cityName = this.getFunctionConfig().getProperty(CITYNAME);
		this.userid = this.getFunctionConfig().getProperty(USERID);
		
		log.info("Weather service initialized");
	}
	
	private Response createJsonRequest() {
		log.debug("Check weather at {}", this.cityName);
	    return client
	      .target(REST_URI).queryParam("q", this.cityName).queryParam("APPID" , this.userid)  
	      .request(MediaType.APPLICATION_JSON)
	      .get(); //post(Entity.entity(emp, MediaType.APPLICATION_JSON));
	}

	@Override
	protected void executeFunction() throws Exception {
		
		Response resp = this.createJsonRequest();
		
		if (resp.getStatus() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + resp.getStatus() + " for weather request for city: " + this.cityName);
		}
		
		log.info("Received weather data={}", resp);
		
		String s = resp.readEntity(String.class);
		
		Weather object = (new Gson()).fromJson(s, Weather.class);
		log.info("Got json= {}", object);
		
		Chunk result = ChunkBuilder.newChunk(this.getFunctionName() + "_result" + "_" + object.name, "WeatherData")
				.setValue("City", object.name)
				.setValue("Temperature", object.main.temp-273.15);
		
		this.getValueMap().get(WEATHERADDRESSID).setValue(result.toJsonObject());
		log.debug("Written value={}", result);
	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		// TODO Auto-generated method stub
		
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
