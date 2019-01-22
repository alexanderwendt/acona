package at.tuwien.ict.acona.evolutiondemo.brokeragent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

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
	private static final Logger logcsvBody = LoggerFactory.getLogger("csvbody");
	private static final Logger logcsvHeader = LoggerFactory.getLogger("csvheader");

	public final static String DATAADDRESS = "dataaddress";
	public final static String GETSTATISTICSSUFFIX = "getstats";
	private final static String DEPOTPREFIX = "depot";
	
	private int previousNumberOfAgents = 0;
	private int currentNumberOfAgents = 0;

	private String dataaddress = "data";
	private long time = System.currentTimeMillis();
	
	//Make statistics string
	private final Map<String, Integer> typeCountMap = new HashMap<>();
	private final List<String> typeNames = new ArrayList<>();
	private final int maxAgentTypes = 3000;

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		// Service shall be reachable from abroad
		//this.getFunctionConfig().setGenerateReponder(true);

		dataaddress = this.getFunctionConfig().getProperty(DATAADDRESS, "data");

		// Add subfunctions
		this.addRequestHandlerFunction(GETSTATISTICSSUFFIX, (Request input) -> getStatistics(input));
	}
	
	private Response getStatistics(Request req) {
		Response result = null;
		
		log.debug("Get result");
		try {
			//JsonElement res = this.generateTypeStatistics();
			//result.setResult(res);
			this.setStart();
		} catch (Exception e) {
			log.error("Statistics caluclation error", e);
			result = new Response(req);
			result.setError(e.getMessage());
		}
		
		return result;
	}
	
	private JsonElement getAgentValues() throws Exception {
		JsonObject result = new JsonObject();
		
		// Read the date
		String dateString = "";
		double price = 0;
		Datapoint value = this.getCommunicator().read(dataaddress);
		if (value.hasEmptyValue() == false) {
			dateString = value.getValue().getAsJsonObject().getAsJsonPrimitive("date").getAsString();
			price = value.getValue().getAsJsonObject().getAsJsonPrimitive("close").getAsDouble();
		} else {
			throw new Exception("No price value available");
		}
		
		// Read whole address space
		List<Datapoint> agents = this.getCommunicator().readWildcard(DEPOTPREFIX + "." + "*").stream().filter(d->(DEPOTPREFIX + "." + "*").equals(d.getAddress())==false).collect(Collectors.toList());
		
		List<JsonObject> depots = new ArrayList<JsonObject>();
		
		for (Datapoint dp: agents) {
			Depot d = dp.getValue(Depot.class);
			
			//FIXME: This is a simplified solution for just one stock
			double totalAssetValue = 0;
			if (d.getAssets().isEmpty()==false) {
				totalAssetValue += d.getAssets().get(0).getVolume() * price;
			}
			
			double totalValue = totalAssetValue + d.getLiquid();
			
			
			JsonObject o = new JsonObject();
			o.addProperty("name", d.getOwner() + ":" + d.getOwnerType());
			o.addProperty("value", totalValue);
			depots.add(o);
		}
		
		//Get replication statistics
		this.currentNumberOfAgents = depots.size();
		log.info("Netto change of cells={}. Number of cells={}", this.currentNumberOfAgents - this.previousNumberOfAgents, this.currentNumberOfAgents);
		this.previousNumberOfAgents = this.currentNumberOfAgents;
		
		//Create a list of agentname:type, total value
		JsonElement depotsJson = (new Gson()).toJsonTree(depots);
		result.add("depots", depotsJson);
		result.addProperty("date", dateString);
		
		return result;
	}

	/**
	 * Generate statistics.
	 * 
	 * Note: This method has to run in an own thread as it reads from other methods
	 * 
	 * @return
	 * @throws Exception
	 */
	private JsonElement generateTypeStatistics() throws Exception {
		JsonObject result = new JsonObject();

		Map<String, Integer> typeCount = new ConcurrentHashMap<String, Integer>();

		// Read whole address space
		List<Datapoint> agents = this.getCommunicator().readWildcard(DEPOTPREFIX + "." + "*").stream().filter(d->(DEPOTPREFIX + "." + "*").equals(d.getAddress())==false).collect(Collectors.toList());

		// Read the date
		String dateString = "";
		Datapoint value = this.getCommunicator().read(dataaddress);
		if (value.hasEmptyValue() == false) {
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

		List<SpeciesType> types = new ArrayList<SpeciesType>();
		typeCount.forEach((k, v) -> {
			types.add(new SpeciesType(k, v));
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
		JsonElement res = this.generateTypeStatistics();
		
		JsonElement resValues = this.getAgentValues();
		res.getAsJsonObject().add("values", resValues.getAsJsonObject().get("depots"));
		
		//Get type statistics for csv
		String message = "";
		
		//[{"type":"L33S11","number":1}]
		//Get timestamp as string
		long newTime = System.currentTimeMillis();
		String timeStamp = res.getAsJsonObject().get("date").getAsString();
		message += newTime - this.time + ";" + timeStamp + ";";
		String header = "Timedifference;Timestamp;";
		this.time = newTime;
		
		
		ArrayList<SpeciesType> types = (new Gson()).fromJson(res.getAsJsonObject().get("types").getAsJsonArray(), new TypeToken<ArrayList<SpeciesType>>() {}.getType());
		//Add types to map
		types.forEach(t->this.typeCountMap.put(t.getType(), t.getNumber()));
		for (SpeciesType t : types) {
			if (this.typeNames.contains(t.getType())==false) {
				typeNames.add(t.getType());
			}
		}
		
		//Delete types that don't exist
		//if type exists in typecountmap but not in types, then set number=0
		this.typeCountMap.keySet().forEach(k->{
			boolean isPresent = types.stream().filter(t->k.equals(t.getType())).findFirst().isPresent();
			if (isPresent==false) {
				this.typeCountMap.put(k, 0);
			}
		});
		
		
		
		//Generate statistics
		for (int i=0;i<this.maxAgentTypes;i++) {
			if (i<this.typeNames.size()) {
				message += this.typeCountMap.get(this.typeNames.get(i));
				header += this.typeNames.get(i);
			}
			message += ";";
			header += ";";
		}
		
		logcsvBody.debug("" + message);
		logcsvHeader.debug("" + header);
		
		//Response result = new Response(this.getOpenRequest());
		//result.setResult(res);
		this.closeOpenRequestWithResponse(res);
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
