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

import at.tuwien.ict.acona.mq.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.mq.datastructures.Datapoint;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

/**
 * The statistics collector reads all depots in the broker, extracts the number for each type and returns a list of <TYPENAME, COUNT> to the user.
 * 
 * @author wendt
 *
 */
public class StatisticsCollector extends CellFunctionThreadImpl {

	private static final Logger log = LoggerFactory.getLogger(StatisticsCollector.class);

	public final static String DATAADDRESS = "dataaddress";
	public final static String GETSTATISTICSSUFFIX = "getstats";
	private final static String DEPOTPREFIX = "depot";

	private String dataaddress = "data";

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		// Service shall be reachable from abroad
		//this.getFunctionConfig().setGenerateReponder(true);

		dataaddress = this.getFunctionConfig().getProperty(DATAADDRESS, "data");

		// Add subfunctions
		this.addRequestHandlerFunction(GETSTATISTICSSUFFIX, (Request input) -> getStatistics(input));
	}
	
	private Response getStatistics(Request req) {
		Response result = new Response(req);
		
		log.debug("Get result");
		try {
			JsonElement res = this.generateTypeStatistics();
			result.setResult(res);
		} catch (Exception e) {
			log.error("Statistics caluclation error", e);
			result = new Response(req);
			result.setError(e.getMessage());
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
	protected void executeCustomPreProcessing() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void executeFunction() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateCustomDatapointsById(String id, JsonElement data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void shutDownThreadExecutor() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
