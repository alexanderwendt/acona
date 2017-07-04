package at.tuwien.ict.acona.cell.cellfunction.specialfunctions;

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

public class BasicServiceRemove extends CellFunctionBasicService {

	private static Logger log = LoggerFactory.getLogger(BasicServiceRemove.class);

	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		JsonRpcResponse result = null;

		try {

			List<Datapoint> datapoints = parameterdata.getParameter(0, new TypeToken<List<Datapoint>>() {
			});

			datapoints.forEach(dp -> this.getCell().getDataStorage().remove(dp.getAddress(), caller));

			result = new JsonRpcResponse(parameterdata, new JsonPrimitive(CommVocabulary.ACKNOWLEDGEVALUE));
		} catch (Exception e) {
			log.error("Cannot perform operation", e);
			result = new JsonRpcResponse(parameterdata, new JsonRpcError("RemoveError", -1, e.getMessage(), e.getLocalizedMessage()));
		}

		return result;
	}

}
