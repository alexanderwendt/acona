package at.tuwien.ict.acona.distribution;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.mq.core.agentfunction.AgentFunctionThreadImpl;
import at.tuwien.ict.acona.mq.datastructures.Request;

public class Publisher extends AgentFunctionThreadImpl {
	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static final String PUBLISHADDRESS = "publishaddress";
	
	private String publishToAddress;
	
	@Override
	protected void cellFunctionThreadInit() throws Exception {
		//Initialize your agent here
		
		//Execute the function in loop
		this.setExecuteOnce(false);
		
		//Execute every 2000ms
		this.setExecuteRate(2000);
		
		publishToAddress = this.getFunctionConfig().getProperty(PUBLISHADDRESS);
		
		log.info("Init Senderservice");
	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		//Custom preprocessing of data like 
		
	}

	@Override
	protected void executeFunction() throws Exception {
		String message = "Hello_" + Math.random();
		
		log.debug("Publish a message. Message={}", message);
		this.write(publishToAddress, new JsonPrimitive(message));
		log.debug("message {} poste to address", message, publishToAddress);
		
	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		
		
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
