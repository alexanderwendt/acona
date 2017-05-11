package at.tuwien.ict.acona.cell.core.cellfunction.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.OndemandFunctionService;
import at.tuwien.ict.acona.cell.cellfunction.ServiceState;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class CFIncrementService extends OndemandFunctionService {

	private static Logger log = LoggerFactory.getLogger(CFIncrementService.class);

	//public final static String INCREMENTDATAPOINTATTRIBUTE = "incrementDatapointAddress";
	public final static String ATTRIBUTEINCREMENTDATAPOINT = "increment"; //This is the key for the actual address

	// private static final String R = "rawdata";

	@Override
	protected void serviceInit() {
		log.info("Init service={}", this.getFunctionName());

		//this.incrementDatapointAddress = this.getFunctionConfig().getProperty(INCREMENTDATAPOINTATTRIBUTE, incrementDatapointAddress);
		//this.addManagedDatapoint(DatapointConfig.newConfig(incrementDatapointAddress, incrementDatapointAddress, SyncMode.SUBSCRIBEWRITEBACK));

	}

	@Override
	protected void executeFunction() throws Exception {
		// Get the datapoint to increment with 1

		// Get settings from config
		// CellConfig config2 = this.getConfig().getProperty("test",
		// CellConfig.class);

		// JsonObject rawdata = this.readLocalAsJson(R).getAsJsonObject();
		String address = "";
		try {
			log.info("{}>Start execution. Local sync datapoints = {}", this.getFunctionName(), this.getSyncDatapoints().keySet());
			//address = this.getSyncDatapoints().get(ATTRIBUTEINCREMENTDATAPOINT).getAddress();
			double value = this.valueMap.get(ATTRIBUTEINCREMENTDATAPOINT).getValue().getAsDouble();
			log.info("Read value={}", value);
			value++;
			log.info("New value={}", value);
			// write new value back to the same datapoint
			this.valueMap.put(this.getSyncDatapoints().get(ATTRIBUTEINCREMENTDATAPOINT).getAddress(), Datapoint.newDatapoint(this.getSyncDatapoints().get(ATTRIBUTEINCREMENTDATAPOINT).getAddress()).setValue(String.valueOf(value)));
			log.debug("Function execution finished");
		} catch (Exception e) {
			log.error("Cannot execute incrementation service. Often the problem is that the value of the address {} has not been initialized yet", address, e);
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public List<Datapoint> performOperation(Map<String, Datapoint> parameterdata, String caller) {
		List<Datapoint> result = new ArrayList<>();
		//Syntax
		//address: command, value START, STOP, EXIT
		//get command
		//if (parameterdata.containsKey("command")) {
		log.debug("Execute method Setcommand with parameter {}", parameterdata);
		result.add(this.executeCommandStart());
		//}

		return result;
	}

	private Datapoint executeCommandStart() {
		String message = ServiceState.IDLE.toString();

		try {
			//Start the incrementor
			this.setCommand("START");

			//			//Get the blocker
			//			boolean blockState = false;
			//			try {
			//				blockState = this.getBlocker().poll(10000, TimeUnit.MICROSECONDS);
			//			} catch (InterruptedException e) {
			//				log.error("Queue interrupted");
			//			}
			//
			//			if (blockState == false) {
			//				throw new Exception("Timeout");
			//			}
		} catch (Exception e) {
			log.error("Error", e);
			message = ServiceState.ERROR.toString();
		}

		return Datapoint.newDatapoint("state").setValue(message);

	}

	@Override
	protected void shutDownExecutor() {
		// TODO Auto-generated method stub

	}

}
