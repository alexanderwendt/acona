package at.tuwien.ict.acona.distribution;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.core.agentfunction.AgentFunctionThreadImpl;
import at.tuwien.ict.acona.mq.datastructures.Request;

public class Client extends AgentFunctionThreadImpl {
	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private String sendToAddress = "Server/helloworldservice";
	
	@Override
	protected void cellFunctionThreadInit() throws Exception {
		//Initialize your agent here
		
		log.warn("No custom parameter set for sender address");
		
		
		//Execute the function in loop
		this.setExecuteOnce(false);
		
		//Execute every 2000ms
		this.setExecuteRate(2000);
		
		
		log.info("Init Senderservice");
	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		//Custom preprocessing of data like 
		
	}

	@Override
	protected void executeFunction() throws Exception {
		Request req;
		
		if (Math.random()>0.5) {
			req = (new Request()).setParameter("type", 1);
		} else {
			req = (new Request()).setParameter("type", 2);
		}
		
		
		log.debug("Send request for hello world to server. Message={}", req);
		JsonElement response = this.getCommunicator().execute(sendToAddress, req).getResult();
		log.debug("Response received {}", response);
		
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
