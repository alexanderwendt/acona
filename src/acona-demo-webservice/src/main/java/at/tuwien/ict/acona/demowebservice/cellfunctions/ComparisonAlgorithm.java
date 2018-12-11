package at.tuwien.ict.acona.demowebservice.cellfunctions;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.mq.datastructures.Chunk;
import at.tuwien.ict.acona.mq.datastructures.ChunkBuilder;
import at.tuwien.ict.acona.mq.datastructures.Datapoint;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

/**
 * @author wendt
 *
 * The function shall subscribe the values from the weather services, compare the temperatures and create a new result, which is published
 * on both a datapoint as well as in a service
 *
 */
public class ComparisonAlgorithm extends CellFunctionThreadImpl {
	
	private Chunk algorithmResult;
	private final static Logger log = LoggerFactory.getLogger(ComparisonAlgorithm.class);
	private DecimalFormat df = new DecimalFormat("#.#");
	
	public final static String GETDATASUFFIX = "getresult";
	
	private final static String DATAPREDICATE = "hasData";

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		this.setExecuteOnce(true); 	//Is deafult. This is just to show.

		algorithmResult = this.generateResponse(this.algorithmResult);
		
		// Add subfunctions
		this.addRequestHandlerFunction(GETDATASUFFIX, (Request input) -> getResult(input));
		
	}
	
	private Response getResult(Request req) {
		Response result = new Response(req);
		
		log.debug("Get result");
		try {
			result.setResult(algorithmResult.toJsonObject());
		} catch (Exception e) {
			log.error("Cannot get result", e);
			result = new Response(req);
			result.setError(e.getMessage());
		}
		
		return result;
	}
	
	private Chunk generateResponse(final Chunk currentResult) throws Exception {
		Chunk result = ChunkBuilder.newChunk("AlgorithmResponse", "Calculation");
		if (currentResult!=null && currentResult.getName()!=null && currentResult.getName().equals("AlgorithmResponse")) {
			result = ChunkBuilder.newChunk(currentResult);
		}
		
		List<Chunk> weatherdata = result.getAssociatedContent(DATAPREDICATE);
		
		String conclusio = "";
		
		if (weatherdata.isEmpty()) {
			conclusio = "No weather data is available";
		} else if (weatherdata.size() == 1) {
			conclusio = weatherdata.get(0).getValue("City") + ", temp=" + weatherdata.get(0).getValue("Temperature") + "°C.";
		} else {
			List<Chunk> sortedList = weatherdata.stream().sorted((o1, o2)->Double.valueOf(o1.getValue("Temperature")).compareTo(Double.valueOf(o2.getValue("Temperature")))).collect(Collectors.toList());
			//conclusio = "In ";
			for (int i=sortedList.size()-1;i>=0;i--) {
				conclusio += sortedList.get(i).getValue("City") + ", T=" + df.format(Double.valueOf(sortedList.get(i).getValue("Temperature"))) + "°C";
				if (i>0) {
					conclusio += ", warmer than ";
				} else {
					conclusio += ".";
				}
			}
		}
		
		result.setValue("hasConclusio", conclusio);
		//log.debug("Conclusio={}", conclusio);
		
		return result;
	}

	@Override
	protected void executeFunction() throws Exception {
		log.debug("Update algorithm result");
		//Create new conclusion
		synchronized (this.algorithmResult) {
			this.algorithmResult = this.generateResponse(this.algorithmResult);
		}		
		
		//log.debug("New algorithm result={}", this.algorithmResult);
	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		//write conclusio to datapoint
		
		Datapoint resultDatapoint = this.getDatapointBuilder().newDatapoint(this.enhanceWithRootAddress(RESULTSUFFIX)).setValue(this.algorithmResult.toJsonObject());
		//this.getCommunicator().read("agent1:hugo").getValue(Episode.class)
		//this.getCommunicator().execute(agentName, serviceName, methodParameters, timeout)
		synchronized (resultDatapoint) {
			//resultDatapoint = DatapointBuilder.newDatapoint(this.addServiceName(RESULTSUFFIX)).setValue(this.algorithmResult.toJsonObject());
			this.getCommunicator().write(resultDatapoint);
			log.debug("Written datapoint={}", resultDatapoint);
		}
		
	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateCustomDatapointsById(String id, JsonElement data) {
		//if (this.isSystemDatapoint(data)==false && data.isEmpty()==false) {
			//Every datapoint that is not a system datapoint must be a subscribed weather datapoint
			//data.values().forEach(dp->{
			Datapoint dp = this.getDatapointBuilder().toDatapoint(data.getAsJsonObject());	
				try {
					Chunk receivedChunk = ChunkBuilder.newChunk(dp.getValue().getAsJsonObject());
					
					//Update new values
					Chunk existingChunk = this.algorithmResult.getFirstAssociatedContentFromAttribute(DATAPREDICATE, "City", receivedChunk.getValue("City"));
					if (existingChunk==null) {
						this.algorithmResult.addAssociatedContent(DATAPREDICATE, receivedChunk);
						log.debug("Added weather data={}", receivedChunk);
					} else {
						this.algorithmResult.removeAssociatedContent(DATAPREDICATE, existingChunk);
						this.algorithmResult.addAssociatedContent(DATAPREDICATE, receivedChunk);
						log.debug("Replaced existing chunk={} with new chunk={}", existingChunk, receivedChunk);
					}
					
					//Execute the calculation as values arrive
					this.setStart();
					
					
				} catch (Exception e) {
					log.error("Cannot update datapoint: " + dp);
				}
			//});
		//}
	}

	@Override
	protected void shutDownThreadExecutor() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
