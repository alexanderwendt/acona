package at.tuwien.ict.acona.cell.cellfunction.specialfunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionBasicService;
import at.tuwien.ict.acona.cell.cellfunction.CommVocabulary;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class BasicServiceRemove extends CellFunctionBasicService {

	private static Logger log = LoggerFactory.getLogger(BasicServiceRemove.class);

	@Override
	public List<Datapoint> performOperation(Map<String, Datapoint> parameterdata, String caller) {
		List<Datapoint> result = new ArrayList<>();
		try {

			List<Datapoint> datapoints = Lists.newArrayList(parameterdata.values()); //GsonUtils.convertJsonArrayToDatapointList(array);

			datapoints.forEach(dp -> this.getCell().getDataStorage().remove(dp.getAddress(), caller));

			result.add(Datapoint.newDatapoint(CommVocabulary.PARAMETERRESULTADDRESS).setValue(CommVocabulary.ACKNOWLEDGEVALUE));

		} catch (Exception e) {
			log.error("Cannot perform operation", e);
			result.add(Datapoint.newDatapoint(CommVocabulary.PARAMETERRESULTADDRESS).setValue(CommVocabulary.ERRORVALUE));
		}

		return result;
	}

}
