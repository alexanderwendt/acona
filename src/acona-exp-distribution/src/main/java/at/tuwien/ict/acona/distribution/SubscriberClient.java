package at.tuwien.ict.acona.distribution;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.core.agentfunction.AgentFunctionThreadImpl;

public class SubscriberClient extends AgentFunctionThreadImpl {
	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private String subscribeAddress = "<Agent1>/testaddress/test";
	
	@Override
	protected void cellFunctionThreadInit() throws Exception {
		//Subscribe address
		this.getCommunicator().subscribeTopic(subscribeAddress);
		
		log.info("Agent subscribed address = {}", this.subscribeAddress);
		log.info("Agent function {} initialized", this.getFunctionName());
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
		log.info("Received data {} from address {}", data, id);
		
	}

	@Override
	protected void shutDownThreadExecutor() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
