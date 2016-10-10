package at.tuwien.ict.acona.cell.core.cellfunction.helpers;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.config.CellConfig;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.framework.modules.AconaFunctionService;

public class CFIncrementService extends AconaFunctionService {

	private static final String INCREMENTATIONDATAPOINTNAME = "increment";
	private static final String R = "rawdata";

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
		CellConfig config2 = this.getConfig().getProperty("test", CellConfig.class);

		JsonObject rawdata = this.readLocalAsJson(R).getAsJsonObject();

		log.debug("Local sync datapoints = {}", this.getSyncDatapoints());
		double value = this.readLocal(this.getSyncDatapoints().get(INCREMENTATIONDATAPOINTNAME).getAddress()).getValue()
				.getAsDouble();
		log.info("Received value={}", value);
		value++;
		// write new value back to the same datapoint
		this.writeLocal(Datapoint.newDatapoint(this.getSyncDatapoints().get(INCREMENTATIONDATAPOINTNAME).getAddress())
				.setValue(String.valueOf(value)));
		log.debug("Function execution finished");
	}

}
