package at.tuwien.ict.acona.cell.core.cellfunction.helpers;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.framework.modules.AconaService;

public class CFFullControlIncrementService extends AconaService {
	
	private static final String INCREMENTATIONDATAPOINTNAME = "increment";

	@Override
	protected void serviceInit() {
		log.trace("Init service={}", this.getFunctionName());
		
	}

	@Override
	protected void executeFunction() throws Exception {
		//Get the datapoint to increment with 1
		double value = this.readLocal(this.getSubscribedDatapoints().get(INCREMENTATIONDATAPOINTNAME).getAddress()).getValue().getAsDouble();
		log.info("Received value={}", value);
		value++;
		//write new value back to the same datapoint
		this.writeLocal(Datapoint.newDatapoint(this.getSubscribedDatapoints().get(INCREMENTATIONDATAPOINTNAME).getAddress()).setValue(String.valueOf(value)));
		log.debug("Function execution finished");
	}

}
