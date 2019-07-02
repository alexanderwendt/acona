package at.tuwien.ict.acona.mq.core.agentfunction;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.core.communication.MqttCommunicator;
import at.tuwien.ict.acona.mq.core.config.DatapointConfig;
import at.tuwien.ict.acona.mq.core.config.FunctionConfig;
import at.tuwien.ict.acona.mq.core.core.Cell;
import at.tuwien.ict.acona.mq.datastructures.DPBuilder;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

public class AgentFunctionStub implements AgentFunction {
	
	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	/**
	 * Cell, which executes this function
	 */
	private Cell cell;
	/**
	 * Cell function configuration
	 */
	private FunctionConfig config;
	
	/**
	 * Name of the activator
	 */
	private String agentFunctionName;

	/**
	 * Function root address when a function has been added to an agent.
	 */
	private String functionRootAddress;
	
	/**
	 * Datapoint builder utility
	 */
	private final DPBuilder dpBuilder = new DPBuilder();

	@Override
	public void init(FunctionConfig config, Cell caller) throws Exception {
		// === Extract fundamental settings ===//
		// Extract settings
		this.config = config;
		this.cell = caller;

		// Get the settings but set also default values
		// Get name
		this.agentFunctionName = this.config.getName();

		this.functionRootAddress = this.dpBuilder.generateAgentTopic(this.getAgentName()) + "/" + this.agentFunctionName;
		log.debug("{}>Root address={}", this.agentFunctionName, this.functionRootAddress); 
		
		log.info("Agent function stub initialized as empty function. Re");
	}

	@Override
	public String getFunctionName() {
		return this.agentFunctionName;
	}

	@Override
	public String getAgentName() {
		return this.cell.getName();
	}

	@Override
	public String getFunctionRootAddress() {
		return this.functionRootAddress;
	}

	@Override
	public FunctionConfig getFunctionConfig() {
		return this.config;
	}

	@Override
	public Map<String, DatapointConfig> getSubscribedDatapoints() {
		throw new UnsupportedOperationException("Method not implemented in the stub");
	}

	@Override
	public Response performOperation(String topic, Request param) {
		throw new UnsupportedOperationException("Method not implemented in the stub");
	}

	@Override
	public void updateSubscribedData(String topic, JsonElement data) throws Exception {
		throw new UnsupportedOperationException("Method not implemented in the stub");
	}

	@Override
	public void shutDownFunction() {
		log.warn("FIXME: Create clean closing of the function");
		throw new UnsupportedOperationException("Method not implemented in the stub");
		
	}

	@Override
	public ServiceState getCurrentState() {
		throw new UnsupportedOperationException("Method not implemented in the stub");
	}

	@Override
	public MqttCommunicator getCommunicator() {
		throw new UnsupportedOperationException("No communicator instantiated in the stub. Method not implemented in the stub");
	}

}
