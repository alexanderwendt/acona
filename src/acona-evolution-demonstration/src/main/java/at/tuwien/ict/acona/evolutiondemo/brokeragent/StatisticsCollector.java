package at.tuwien.ict.acona.evolutiondemo.brokeragent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcError;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

/**
 * The statistics collector reads all depots in the broker, extracts the number for each type and returns a list of <TYPENAME, COUNT> to the user.
 * 
 * @author wendt
 *
 */
public class StatisticsCollector extends CellFunctionImpl {

	private static final Logger log = LoggerFactory.getLogger(StatisticsCollector.class);

	private final static String DEPOTPREFIX = "depot";

	@Override
	protected void cellFunctionInit() throws Exception {
		// Service shall be reachable from abroad
		this.getFunctionConfig().setGenerateReponder(true);

	}

	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {

		JsonRpcResponse result = null;

		log.debug("Got request={}", parameterdata);

		try {
			JsonElement resultObject = this.generateTypeStatistics();
			result = new JsonRpcResponse(parameterdata, resultObject);
		} catch (Exception e) {
			result = new JsonRpcResponse(parameterdata, new JsonRpcError("Statistics caluclation error", -1, e.getMessage(), e.getMessage()));
			log.error("Error in calulating the distribution of types.", e);
		}

		return result;
	}

	private JsonElement generateTypeStatistics() throws Exception {
		JsonElement result = null;

		Map<String, Integer> typeCount = new ConcurrentHashMap<String, Integer>();

		// Read whole address space
		List<Datapoint> agents = this.getCommunicator().readWildcard(DEPOTPREFIX + "." + "*");

		// Count how many of each type are there
		agents.forEach(a -> {
			String ownerType = a.getValue(Depot.class).getOwnerType();
			if (typeCount.containsKey(ownerType)) {
				int number = typeCount.get(ownerType);
				number++;
				typeCount.put(ownerType, number);
			} else {
				typeCount.put(ownerType, 1);
			}
		});

		List<Types> types = new ArrayList<Types>();
		typeCount.forEach((k, v) -> {
			types.add(new Types(k, v));
		});

		result = (new Gson()).toJsonTree(types);

		return result;
	}

	@Override
	protected void shutDownImplementation() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateDatapointsById(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub

	}

}
