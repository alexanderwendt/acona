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

public class BasicServiceWrite extends CellFunctionBasicService {

	private static Logger log = LoggerFactory.getLogger(BasicServiceWrite.class);

	@Override
	public List<Datapoint> performOperation(final Map<String, Datapoint> parameter, String caller) {
		List<Datapoint> result = new ArrayList<>();
		try {
			// Convert parameter to datapoint
			//String sender = parameter.get(PARAMETERSENDER).getValueAsString();
			//String method = parameter.get(METHOD).getValueAsString();

			//JsonArray array = parameter.get(PARAMETER).getValue().getAsJsonArray();
			List<Datapoint> datapoints = Lists.newArrayList(parameter.values());//GsonUtils.convertJsonArrayToDatapointList(array);

			//switch (method) {
			//case WRITEMETHOD:
			this.write(datapoints, caller);
			//	break;
			//default:
			//	throw new Exception("Method " + method + " not available.");
			//}

			result.add(Datapoint.newDatapoint(CommVocabulary.PARAMETERRESULTADDRESS).setValue(CommVocabulary.ACKNOWLEDGEVALUE));

		} catch (Exception e) {
			log.error("Cannot perform operation of parameter={}", parameter, e);
			result.add(Datapoint.newDatapoint(CommVocabulary.PARAMETERRESULTADDRESS).setValue(CommVocabulary.ERRORVALUE));
		}

		return result;
	}

	private void write(final List<Datapoint> datapointList, String caller) {
		datapointList.forEach(dp -> {
			this.getCell().getDataStorage().write(dp, caller);
		});
	}

}
