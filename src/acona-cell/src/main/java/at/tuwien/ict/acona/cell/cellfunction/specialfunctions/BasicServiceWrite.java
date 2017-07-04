package at.tuwien.ict.acona.cell.cellfunction.specialfunctions;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionBasicService;
import at.tuwien.ict.acona.cell.cellfunction.CommVocabulary;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcError;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

public class BasicServiceWrite extends CellFunctionBasicService {

	private static Logger log = LoggerFactory.getLogger(BasicServiceWrite.class);

	@Override
	public JsonRpcResponse performOperation(final JsonRpcRequest parameter, String caller) {
		JsonRpcResponse result = null;

		try {
			switch (parameter.getMethod()) {
			case "write":
				//All datapoints are in the first parameter of the method call
				List<Datapoint> datapoints = parameter.getParameter(0, new TypeToken<List<Datapoint>>() {
				});
				//List<Datapoint> datapoints = Lists.newArrayList(parameter.values());
				this.write(datapoints, caller);
				result = new JsonRpcResponse(parameter, new JsonPrimitive(CommVocabulary.ACKNOWLEDGEVALUE));
				break;
			default:
				throw new Exception("Method " + parameter.getMethod() + " not available.");
			}
		} catch (Exception e) {
			log.error("Cannot perform operation of parameter={}", parameter, e);
			result = new JsonRpcResponse(parameter, new JsonRpcError("WriteError", -1, e.getMessage(), e.getLocalizedMessage()));
		}

		return result;
	}

	private void write(final List<Datapoint> datapointList, String caller) throws Exception {
		List<String> errorList = new ArrayList<>();

		datapointList.forEach(dp -> {
			try {
				this.getCell().getDataStorage().write(dp, caller);
			} catch (Exception e) {
				errorList.add(dp.getAddress());
				log.error("Cannot write data " + dp, e);
			}
		});

		if (errorList.isEmpty() == false) {
			throw new Exception("Error writing the following data: " + errorList);
		}
	}

}
