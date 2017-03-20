package at.tuwien.ict.acona.cell.core.cellfunction.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.OndemandFunctionService;
import at.tuwien.ict.acona.cell.cellfunction.ServiceState;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class CFIncrementService extends OndemandFunctionService {

	private static Logger log = LoggerFactory.getLogger(CFIncrementService.class);

	private static final String INCREMENTATIONDATAPOINTNAME = "increment";
	// private static final String R = "rawdata";

	@Override
	protected void serviceInit() {
		log.trace("Init service={}", this.getFunctionName());
		// MainController c = this.getConfig().getProperty("xxx",
		// MainController);

	}

	@Override
	protected void executeFunction() throws Exception {
		// Get the datapoint to increment with 1

		// Get settings from config
		// CellConfig config2 = this.getConfig().getProperty("test",
		// CellConfig.class);

		// JsonObject rawdata = this.readLocalAsJson(R).getAsJsonObject();
		try {
			log.info("{}>Start execution. Local sync datapoints = {}", this.getFunctionName(), this.getSyncDatapoints().keySet());
			String address = this.getSyncDatapoints().get(INCREMENTATIONDATAPOINTNAME).getAddress();
			double value = this.valueMap.get(INCREMENTATIONDATAPOINTNAME).getValue().getAsDouble();
			log.info("Read value={}", value);
			value++;
			log.info("New value={}", value);
			// write new value back to the same datapoint
			this.valueMap.put(this.getSyncDatapoints().get(INCREMENTATIONDATAPOINTNAME).getAddress(), Datapoint.newDatapoint(this.getSyncDatapoints().get(INCREMENTATIONDATAPOINTNAME).getAddress()).setValue(String.valueOf(value)));
			log.debug("Function execution finished");
		} catch (Exception e) {
			log.error("Cannot execute incrementation service", e);
			throw new Exception(e.getMessage());
		}
	}

	@Override
	public List<Datapoint> performOperation(Map<String, Datapoint> parameterdata, String caller) {
		List<Datapoint> result = new ArrayList<Datapoint>();
		//Syntax
		//address: command, value START, STOP, EXIT
		//get command
		if (parameterdata.containsKey("command")) {
			log.debug("Execute method Setcommand with parameter {}", parameterdata.get("command").getValueAsString());
			result.add(this.executeCommandStart());
		}

		return result;
	}

	private Datapoint executeCommandStart() {
		String message = ServiceState.IDLE.toString();

		try {
			//Start the incrementor
			this.setCommand("START");

			//Get the blocker
			boolean blockState = false;
			try {
				blockState = this.getBlocker().poll(10000, TimeUnit.MICROSECONDS);
			} catch (InterruptedException e) {
				log.error("Queue interrupted");
			}

			if (blockState == false) {
				throw new Exception("Timeout");
			}
		} catch (Exception e) {
			log.error("Error", e);
			message = ServiceState.ERROR.toString();
		}

		return Datapoint.newDatapoint("state").setValue(message);

	}

}
