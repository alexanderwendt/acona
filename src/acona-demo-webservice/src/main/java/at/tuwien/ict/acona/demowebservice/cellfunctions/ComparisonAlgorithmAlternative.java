package at.tuwien.ict.acona.demowebservice.cellfunctions;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.datastructures.Chunk;
import at.tuwien.ict.acona.cell.datastructures.ChunkBuilder;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.DatapointBuilder;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcError;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

/**
 * @author wendt
 *
 * The function shall subscribe the values from the weather services, compare the temperatures and create a new result, which is published
 * on both a datapoint as well as in a service
 *
 */
public class ComparisonAlgorithmAlternative extends CellFunctionThreadImpl {
	
	private Chunk algorithmResult;
	private final static Logger log = LoggerFactory.getLogger(ComparisonAlgorithmAlternative.class);
	private DecimalFormat df = new DecimalFormat("#.#");
	
	private final static String DATAPREDICATE = "hasData";

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		this.setExecuteOnce(true); 	//Is deafult. This is just to show.

		algorithmResult = this.generateResponse(this.algorithmResult);
		
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
			conclusio = "In " + weatherdata.get(0).getValue("City") + " the temperature is " + weatherdata.get(0).getValue("Temperature") + "°C.";
		} else {
			List<Chunk> sortedList = weatherdata.stream().sorted((o1, o2)->Double.valueOf(o1.getValue("Temperature")).compareTo(Double.valueOf(o2.getValue("Temperature")))).collect(Collectors.toList());
			//conclusio = "In ";
			for (int i=0;i<sortedList.size();i++) {
				conclusio += sortedList.get(i).getValue("City") + ", T=" + df.format(Double.valueOf(sortedList.get(i).getValue("Temperature"))) + "°C";
				if (i<sortedList.size()) {
					conclusio += ", cooler than ";
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
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		JsonRpcResponse result;
		try {
			switch (parameterdata.getMethod()) {
				case "getresult":
					result = new JsonRpcResponse(parameterdata, algorithmResult.toJsonObject());
					break;
				default:
					throw new Exception("No such method");
			}
		} catch (Exception e) {
			result = new JsonRpcResponse(parameterdata, new JsonRpcError("Error", -1, e.getMessage(), e.getMessage()));
		}
		
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
		
		Datapoint resultDatapoint = DatapointBuilder.newDatapoint(this.addServiceName(RESULTSUFFIX)).setValue(this.algorithmResult.toJsonObject());;
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
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		if (this.isSystemDatapoint(data)==false && data.isEmpty()==false) {
			//Every datapoint that is not a system datapoint must be a subscribed weather datapoint
			data.values().forEach(dp->{
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
			});
		}
	}

	@Override
	protected void shutDownExecutor() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
