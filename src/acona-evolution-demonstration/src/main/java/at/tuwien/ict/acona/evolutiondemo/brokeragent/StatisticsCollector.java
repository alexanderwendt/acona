package at.tuwien.ict.acona.evolutiondemo.brokeragent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The statistics collector reads all depots in the broker, extracts the number for each type and returns a list of <TYPENAME, COUNT> to the user.
 * 
 * @author wendt
 *
 */
public class StatisticsCollector extends CellFunctionImpl {

	private static final Logger log = LoggerFactory.getLogger(StatisticsCollector.class);

	public final static String DATAADDRESS = "dataaddress";
	private final static String DEPOTPREFIX = "depot";

	private String dataaddress = "data";

	@Override
	protected void cellFunctionInit() throws Exception {
		// Service shall be reachable from abroad
		this.getFunctionConfig().setGenerateReponder(true);

		dataaddress = this.getFunctionConfig().getProperty(DATAADDRESS);

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
		JsonObject result = new JsonObject();

		Map<String, Integer> typeCount = new ConcurrentHashMap<String, Integer>();

		// Read whole address space
		List<Datapoint> agents = this.getCommunicator().readWildcard(DEPOTPREFIX + "." + "*");

		// Read the date
		String dateString = "";
		if (this.getCommunicator().read(dataaddress).hasEmptyValue() == false) {
			dateString = this.getCommunicator().read(dataaddress).getValue().getAsJsonObject().getAsJsonPrimitive("date").getAsString();
		}
		// Calendar cal = Calendar.getInstance();
		// SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
		// cal.setTime(dateFormat.parse(dateString));
		// Day currentDay = new Day(cal.getTime());

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

		JsonElement tree = (new Gson()).toJsonTree(types);
		result.add("types", tree);
		result.addProperty("date", dateString);

		// Object with a date and a tree of a map

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
