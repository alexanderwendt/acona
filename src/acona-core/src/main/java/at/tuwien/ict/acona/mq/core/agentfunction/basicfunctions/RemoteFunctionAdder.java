package at.tuwien.ict.acona.mq.core.agentfunction.basicfunctions;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.core.agentfunction.AgentFunction;
import at.tuwien.ict.acona.mq.core.agentfunction.AgentFunctionImpl;
import at.tuwien.ict.acona.mq.core.agentfunction.AgentFunctionStub;
import at.tuwien.ict.acona.mq.core.agentfunction.SyncMode;
import at.tuwien.ict.acona.mq.core.config.FunctionConfig;

public class RemoteFunctionAdder extends AgentFunctionImpl {
	
	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public final static String DATAPOINTSUFFIX = "addremotefunction";

	@Override
	protected void agentFunctionInit() throws Exception {
		this.addManagedDatapoint("AddFunctionTrigger", this.enhanceWithRootAddress(DATAPOINTSUFFIX), SyncMode.SUBSCRIBEONLY);
		
		log.info("Remote function adder initialized. Ready to receive function as Json on {}", this.enhanceWithRootAddress(DATAPOINTSUFFIX));
	}

	@Override
	protected void shutDownImplementation() throws Exception {
		log.info("Shut down remote function adder");
	}

	@Override
	protected void updateDatapointsById(String id, String address, JsonElement data) {
		
		if (this.enhanceWithRootAddress(DATAPOINTSUFFIX).equals(address)) {
			try {
				//Create a new stub function
				AgentFunction stubFunction = new AgentFunctionStub();
				stubFunction.init(FunctionConfig.newConfig(data.getAsJsonObject()), this.getAgent());
				
				this.getAgent().getFunctionHandler().registerCellFunctionInstance(stubFunction);
				log.info("Remote function {} registered.", stubFunction.getFunctionRootAddress());
			} catch (Exception e) {
				log.error("Cannot add remote function from JSON data: {}", data);
			}
		} else {
			log.warn("Wrong address triggered the function {}", address);
		}
	}

}
