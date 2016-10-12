package at.tuwien.ict.acona.cell.core.cellfunction.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.framework.modules.AconaFunctionService;

public class CFIncrementService extends AconaFunctionService {

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
			log.info("{}>Start execution. Local sync datapoints = {}", this.getFunctionName(), this.getSyncDatapoints());
			double value = this.readLocal(this.getSyncDatapoints().get(INCREMENTATIONDATAPOINTNAME).getAddress()).getValue().getAsDouble();
			log.info("Read value={}", value);
			value++;
			log.info("New value={}", value);
			// write new value back to the same datapoint
			this.writeLocal(Datapoint.newDatapoint(this.getSyncDatapoints().get(INCREMENTATIONDATAPOINTNAME).getAddress())
					.setValue(String.valueOf(value)));
			log.debug("Function execution finished");
		} catch (Exception e) {
			log.error("Cannot execute incrementation service", e);
			throw new Exception(e.getMessage());
		}
	}

}
