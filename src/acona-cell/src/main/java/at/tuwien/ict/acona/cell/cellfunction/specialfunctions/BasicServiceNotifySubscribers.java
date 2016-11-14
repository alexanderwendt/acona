package at.tuwien.ict.acona.cell.cellfunction.specialfunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class BasicServiceNotifySubscribers extends BasicService {

	private static Logger log = LoggerFactory.getLogger(BasicServiceWrite.class);

	@Override
	public List<Datapoint> performOperation(Map<String, Datapoint> parameterdata, String caller) {
		List<Datapoint> result = new ArrayList<Datapoint>();
		log.trace("Notify subscribers service for caller={}, addresses={}", caller, parameterdata.keySet());

		parameterdata.values().forEach(dp -> {
			this.getCell().getFunctionHandler().activateNotifySubscribers(caller, dp);
		});
		try {
			result.add(Datapoint.newDatapoint(PARAMETERRESULT).setValue(ACKNOWLEDGE));

		} catch (Exception e) {
			log.error("Cannot perform notify on parameter={}", parameterdata, e);
			result.add(Datapoint.newDatapoint(PARAMETERRESULT).setValue(ERROR));
		}

		return result;
	}

}
