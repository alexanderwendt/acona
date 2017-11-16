package at.tuwien.ict.acona.cell.cellfunction.specialfunctions;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionBasicService;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcError;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

/**
 * @author wendt
 * 
 *         A list of datapoints is read. Automatically, wildcards are used. If a
 *         name is not complete, all completing names
 *
 */
public class BasicServiceRead extends CellFunctionBasicService {

	private static Logger log = LoggerFactory.getLogger(BasicServiceWrite.class);

	//private static Logger log = LoggerFactory.getLogger(BasicServiceRead.class);

	//	public static final String READMETHOD = "read";
	//
	//	private static final String ACKNOWLEDGE = "OK";
	//	private static final String ERROR = "ERROR";
	//	private static final String PARAMETERRESULT = "result";
	//	private static final String PARAMETERSENDER = "sender";
	//	private static final String METHOD = "method";
	//	private static final String PARAMETERDATAPOINTS = "datapoints";

	//private String currentCaller = this.getCell().getLocalName();

	// Parameter
	// SENDER: name,
	// Datapoints as JsonArray with datapoints as Json objects

	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameter, String caller) {
		JsonRpcResponse result = null;
		try {
			switch (parameter.getMethod()) {
			case "read":
				//All datapoints are in the first parameter of the method call
				List<String> addresses = parameter.getParameter(0, new TypeToken<List<String>>() {
				});
				//List<Datapoint> datapoints = Lists.newArrayList(parameter.values());
				List<Datapoint> readValues = this.read(addresses);

				result = new JsonRpcResponse(parameter, readValues);

				break;
			default:
				throw new Exception("Erroneous method name");
			}

		} catch (Exception e) {
			log.error("Cannot perform operation", e);
			result = new JsonRpcResponse(parameter, new JsonRpcError("ReadError", -1, e.getMessage(), e.getLocalizedMessage()));
		}

		return result;
	}

	private List<Datapoint> read(final List<String> datapointList) {
		List<Datapoint> result = new ArrayList<>();

		datapointList.forEach(dp -> {
			result.addAll(this.getCell().getDataStorage().read(dp));
		});

		return result;
	}

}
