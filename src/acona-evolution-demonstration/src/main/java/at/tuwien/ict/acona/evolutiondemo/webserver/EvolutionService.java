package at.tuwien.ict.acona.evolutiondemo.webserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.evolutiondemo.controlleragent.ConsoleRequestReceiver;
import at.tuwien.ict.acona.mq.core.agentfunction.specialfunctions.StateMonitor;
import at.tuwien.ict.acona.mq.datastructures.Request;

@Path("evolution")
public class EvolutionService {

	//public static final String PARAMREQUESTPREFIXADDRESS = "requestprefixaddress";
	//public static final String PARAMUSERRESULTADDRESS = "userresultaddress";
	/**
	 * Name of the function that collects the state of the system. It is used to respond to a request to read the working memory state
	 */
	//public static final String PARAMSTATESERVICENAME = "stateservicename";
	//public final static String PARAMCOGSYSTRIGGER = "cogsystrigger";
	public static final String PARAMAGENTNAMES = "agentnames";	//Provide as string list
	public static final String PARAMCONTROLLERADDRESS = "controlleraddress";
	private static final String SYSTEMSTATESUFFIX = StateMonitor.SYSTEMSTATEADDRESS;

	//private String requestAddress = "";
	//private String userresultAddress = "";
	//private String stateCollectorName = "";
	//private String cogsysTriggerAddress = "";
	private String agentNames = "";
	private String controllerAddress = "";

	// private String optimizationRequestAddress = "";

	private final JerseyRestServer function;

	private final static Logger log = LoggerFactory.getLogger(EvolutionService.class);

	public EvolutionService() throws Exception {
		this.function = ServerSingleton.getFunction();

		this.agentNames = this.function.getFunctionConfig().getProperty(PARAMAGENTNAMES, "");
		this.controllerAddress = this.function.getFunctionConfig().getProperty(PARAMCONTROLLERADDRESS);

		log.info("Initialized Jersey service");
	}
	
	/**
	 * Read the current system state, what is currently running
	 * 
	 * Thing to provide: System state of each agent in the simulator
	 * 
	 * @return
	 * @throws Exception
	 */
	@GET
	@Path("state")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	public Response readSystemState(@DefaultValue("") @QueryParam("test") String test) throws Exception {
		Response result = null;

		try {
			//Split string
			List<String> agents = new ArrayList<String>();
			List<JsonElement> agentStates = new ArrayList<JsonElement>();
			if (agentNames.isEmpty()==false) {
				agents = Arrays.asList(agentNames.split(","));
			}
			
			//For each agent, read its system state
			for (String s : agents) {
				agentStates.add(this.function.getCommunicator().read(s + ":" + SYSTEMSTATESUFFIX).getValue());
			}
			
			result = Response.ok().entity(agentStates.toString()).build();
		} catch (Exception e) {
			log.error("Cannot read address: " + this.agentNames, e);
			result = Response.status(Response.Status.BAD_REQUEST).build();
			throw new Exception(e.getMessage());
		}

		return result;
	}
	
	/**
	 * Start the system
	 * 
	 * @return
	 * @throws Exception
	 */
	@POST
	@Path("start")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response startSystem(@Context HttpServletRequest context, String payload) throws Exception {
		Response result = null;

		try {
			//
			JsonObject obj = (new Gson()).fromJson(payload, JsonObject.class);
			int simulatorRuns = obj.get("count").getAsInt();
			log.info("Received request {}. Start the system", obj);
			
			this.function.getCommunicator().execute(controllerAddress + "/" + ConsoleRequestReceiver.METHODSTARTCONTROLLER, (new Request()).setParameter("count", simulatorRuns));
			
			//String callerIP = responseUri; // context.getRemoteAddr();
			log.info("Simulator started");;
			result = Response.ok().build();
		} catch (Exception e) {
			log.error("Cannot receive result", e.getMessage());
			result = Response.status(Response.Status.BAD_REQUEST).entity(new JsonPrimitive("Cannot start simulator" + e.getMessage())).build();
			throw new Exception(e.getMessage());
		}

		return result;
	}
	
	@POST
	@Path("interrupt")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response interrupt() throws Exception {
		Response result = null;

		try {
			log.info("Received interrupt request.");
			
			this.function.getCommunicator().execute(controllerAddress + "/" + ConsoleRequestReceiver.METHODINTERRUPTCONTROLLER, (new Request()));
			
			//String callerIP = responseUri; // context.getRemoteAddr();
			log.info("Simulator started");;
			result = Response.ok().build();
		} catch (Exception e) {
			log.error("Cannot receive result", e.getMessage());
			result = Response.status(Response.Status.BAD_REQUEST).entity(new JsonPrimitive("Cannot start simulator" + e.getMessage())).build();
			throw new Exception(e.getMessage());
		}

		return result;
	}

	

//	/**
//	 * Read the current system state, what is currently running
//	 * 
//	 * @return
//	 * @throws Exception
//	 */
//	@GET
//	@Path(Variables.Webrequest.READSYSTEMSTATE)
//	@Consumes(MediaType.TEXT_PLAIN)
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response readSystemState(@DefaultValue("") @QueryParam("agent") String agentName) throws Exception {
//		Response result = null;
//
//		try {
//			// System state
//			String checkAddress = this.systemstateAddress;
//			if (agentName.isEmpty() == false) {
//				checkAddress = agentName + ":" + CFStateGenerator.SYSTEMSTATEADDRESS;
//			}
//
//			JsonObject systemState = this.function.getCommunicatorFromFunction().read(checkAddress).getValue().getAsJsonObject();
//			result = Response.ok().entity(systemState.toString()).build();
//		} catch (Exception e) {
//			log.error("Cannot read address: " + this.systemstateAddress, e);
//			result = Response.status(Response.Status.BAD_REQUEST).build();
//			throw new Exception(e.getMessage());
//		}
//
//		return result;
//	}

//	@GET
//	@Path(Variables.Webrequest.READMEMORY)
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response readMemory() throws Exception {
//		Response result = null;
//
//		try {
//			JsonRpcRequest req = new JsonRpcRequest("readAll", 0);
//			// Execute RPC request to read the state of the cogsys memories
//			JsonRpcResponse memory = this.function.getCommunicatorFromFunction().execute(this.stateCollectorName, req);
//			JsonElement element = memory.getResult();
//
//			result = Response.ok().entity(element.toString()).build();
//		} catch (Exception e) {
//			log.error("Cannot read address: " + this.stateCollectorName, e);
//			result = Response.status(Response.Status.BAD_REQUEST).build();
//			throw new Exception(e.getMessage());
//		}
//
//		return result;
//	}

//	/**
//	 * Read the result of the cogsys, the returned episode
//	 * 
//	 * @return
//	 * @throws Exception
//	 */
//	@GET
//	@Path(Variables.Webrequest.READWORKINGMEMORYRESULT)
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response readResult() throws Exception {
//		// Datapoint result = Datapoint.newNullDatapoint();
//		Response result = null;
//
//		try {
//			Datapoint readResult = this.function.getCommunicatorFromFunction().read(this.userresultAddress);
//
//			result = Response.ok().entity(readResult.toJsonObject().toString()).build();
//		} catch (Exception e) {
//			log.error("Cannot read address: " + this.userresultAddress);
//			result = Response.status(Response.Status.BAD_REQUEST).build();
//			throw new Exception(e.getMessage());
//		}
//
//		return result;
//	}
//
//	/**
//	 * Read the result of the cogsys, the returned episode
//	 * 
//	 * @return
//	 * @throws Exception
//	 */
//	@GET
//	@Path(Variables.Webrequest.READWORKINGMEMORYSHORTRESULT)
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response readWorkingMemoryShortResult() throws Exception {
//		Response result = null;
//
//		try {
//			JsonRpcRequest req = new JsonRpcRequest("readSummary", 0);
//			// Execute RPC request to read the state of the cogsys memories
//			JsonRpcResponse memory = this.function.getCommunicatorFromFunction().execute(this.stateCollectorName, req);
//			JsonElement element = memory.getResult();
//
////			resultObject.add("Timestamp", element.getAsJsonObject().get("Timestamp"));
////
////			// Extract necessary data from the result
////			// Requests
////			JsonArray requestOriginalArray = element.getAsJsonObject().get("Requests").getAsJsonArray();
////			JsonArray requestShortArray = new JsonArray();
////			requestOriginalArray.forEach(o -> {
////				Request episode = gson.fromJson(o, Request.class);
////				// JsonObject goalObject = new JsonObject();
////				// 7goalObject.addProperty("name", episode.getName());
////				// goalObject.addProperty("value", episode.getImportance());
////				requestShortArray.add(episode.getName());
////			});
////			resultObject.add("Requests", requestShortArray);
////
////			// Goals
////			JsonArray goalOriginalArray = element.getAsJsonObject().get("Goals").getAsJsonArray();
////			JsonArray goalShortArray = new JsonArray();
////			goalOriginalArray.forEach(o -> {
////				Goal episode = gson.fromJson(o, Goal.class);
////				JsonObject goalObject = new JsonObject();
////				goalObject.addProperty("name", episode.getName());
////				goalObject.addProperty("value", episode.getImportance());
////				goalShortArray.add(goalObject);
////			});
////			resultObject.add("Goals", goalShortArray);
////
////			// Extract episodes
////			JsonArray episodeOriginalArray = element.getAsJsonObject().get("Episodes").getAsJsonArray();
////			JsonArray episodeShortArray = new JsonArray();
////			episodeOriginalArray.forEach(o -> {
////				Episode episode = gson.fromJson(o, Episode.class);
////				JsonObject episodeObject = new JsonObject();
////				episodeObject.addProperty("name", episode.getName());
////				episodeObject.addProperty("value", episode.getEvaluation().getTotalEvaluation());
////				episodeShortArray.add(episodeObject);
////			});
////			resultObject.add("Episodes", episodeShortArray);
////
////			JsonArray previousepisodeOriginalArray = element.getAsJsonObject().get("Previous episode(s)").getAsJsonArray();
////			JsonArray previousepisodeShortArray = new JsonArray();
////			previousepisodeOriginalArray.forEach(o -> {
////				Episode episode = gson.fromJson(o, Episode.class);
////				JsonObject episodeObject = new JsonObject();
////				episodeObject.addProperty("name", episode.getName());
////				episodeObject.addProperty("value", episode.getEvaluation().getTotalEvaluation());
////				previousepisodeShortArray.add(episodeObject);
////			});
////			resultObject.add("Unanalyzed Episodes", previousepisodeShortArray);
////
////			// Options
////			JsonArray optionOriginalArray = element.getAsJsonObject().get("Options").getAsJsonArray();
////			JsonArray optionShortArray = new JsonArray();
////			optionOriginalArray.forEach(o -> {
////				Option option = gson.fromJson(o, Option.class);
////				JsonObject optionObject = new JsonObject();
////				optionObject.addProperty("name", option.getName());
////				optionObject.addProperty("value", option.getEvaluation().getEvaluation());
////				optionShortArray.add(optionObject);
////			});
////			resultObject.add("Options", optionShortArray);
////
////			// Selected Option
////			JsonElement selectedOptionElement = element.getAsJsonObject().get("SelectedOption");
////			if (selectedOptionElement.isJsonArray() == true && selectedOptionElement.getAsJsonArray().size() > 0) {
////				Option selectedOption = gson.fromJson(selectedOptionElement.getAsJsonArray().get(0), Option.class);
////				JsonObject optionObject = new JsonObject();
////				optionObject.addProperty("name", selectedOption.getName());
////				optionObject.addProperty("value", selectedOption.getEvaluation().getEvaluation());
////				resultObject.add("SelectedOption", optionObject);
////			} else {
////				resultObject.add("SelectedOption", new JsonObject());
////			}
////
////			element.getAsJsonObject().entrySet().forEach(e -> {
////				// Internal state
////				if (e.getKey().contains("internalstatememory.state.") == true) {
////					resultObject.add(e.getKey(), e.getValue());
////				}
////
////				// Working memory result
////				if (e.getKey().contains("workingmemory.result.") == true) {
////					resultObject.add(e.getKey(), e.getValue());
////				}
////			});
//
//			result = Response.ok().entity(element.toString()).build();
//		} catch (Exception e) {
//			log.error("Cannot read from service: " + this.stateCollectorName, e);
//			result = Response.status(Response.Status.BAD_REQUEST).build();
//			throw new Exception(e.getMessage());
//		}
//
//		return result;
//	}

//	/**
//	 * Start the optimization of the system, including break conditions. This request is used for debug purposes.
//	 * 
//	 * @param simulationId
//	 * @param ontologyId
//	 * @param breakSimulatorRun
//	 * @param breakEvaluationCo2
//	 * @param breakEvaluationEnergy
//	 * @param breakEvaluationPenalty
//	 * @return
//	 * @throws Exception
//	 */
//	@POST
//	@Path(Variables.Webrequest.GETSATISFACTORYRULESET)
//	@Produces(MediaType.APPLICATION_JSON)
//	@Consumes(MediaType.APPLICATION_JSON)
//	public Response requestOptimization(@Context HttpServletRequest context, String payload) throws Exception {
//		Response result = null;
//
//		// simulationid=filename2&ontologyid=usecase1_variation3&breaksimulatorrun=101&breakevaluationco2=0.4&breakevaluationenergy=0.2&breakevaluationpenalty=0.8
//		// @FormParam("simulationid") String simulationId,
//		// @FormParam("ontologyid") String ontologyId,
//		// @FormParam("breaksimulatorrun") int breakSimulatorRun,
//		// @FormParam("breakevaluationco2") double breakEvaluationCo2,
//		// @FormParam("breakevaluationenergy") double breakEvaluationEnergy,
//		// @FormParam("breakevaluationpenalty") double breakEvaluationPenalty
//
//		JsonObject obj = (new Gson()).fromJson(payload, JsonObject.class);
//		String scenarioUri = obj.get("ScenarioURI").getAsString();
//		String usecaseUri = obj.get("UseCaseURI").getAsString();
//		String responseUri = (obj.has("ResponseURI") ? obj.get("ResponseURI").getAsString() : "");
//		int breakSimulatorRun = obj.get("breaksimulatorrun").getAsInt();
//		double breakEvaluationCo2 = obj.get("breakevaluationco2").getAsDouble();
//		double breakEvaluationEnergy = obj.get("breakevaluationenergy").getAsDouble();
//		double breakEvaluationPenalty = obj.get("breakevaluationpenalty").getAsDouble();
//		double breakEvaluationTotal = obj.get("breakevaluationtotal").getAsDouble();
//
//		// http://localhost:8001/kore/?command=start&simulationid=textfile1&ontologyid=usecase1varition1&breaksimulatorrun=100&breakevaluationco2=0.5&breakevaluationenergy=0.2&breakevaluationpenalty=0.1
//		log.info("Received request {}. Now check the reaction of the system", this.requestAddress + "." + Variables.Webrequest.GETSATISFACTORYRULESET);
//		RequestStartOptimization requestChunk;
//
//		try {
//			String callerIP = responseUri; // context.getRemoteAddr();
//			log.info("Received request call from={}", callerIP);
//
//			requestChunk = DatastructureUtil.generateRequestToOptimizeChunk(scenarioUri, usecaseUri, callerIP, breakSimulatorRun, breakEvaluationCo2, breakEvaluationEnergy, breakEvaluationPenalty, breakEvaluationTotal);
//			log.debug("Converted http-request into chunk:", requestChunk);
//
//			// Write the answer to the working memory
//			this.function.getCommunicatorFromFunction().write(DatapointBuilder.newDatapoint(this.requestAddress + "." + Variables.Webrequest.STARTOPTIMIZATIONTASK).setValue(requestChunk)); // Write to the start optimization task because it is the same
//			log.debug("request {} written to address {}", requestChunk, this.requestAddress + "." + Variables.Webrequest.STARTOPTIMIZATIONTASK);
//
//			// Trigger the cogsys
//			this.function.getCommunicatorFromFunction().write(DatapointBuilder.newDatapoint(this.cogsysTriggerAddress).setValue("Trigger by new request from user"));
//
//			JsonElement response = new JsonPrimitive("Request executed");
//			result = Response.ok(response.toString()).build();
//		} catch (Exception e) {
//			log.error("Cannot receive result", e.getMessage());
//			result = Response.status(Response.Status.BAD_REQUEST).entity(new JsonPrimitive("Cannot receive result" + e.getMessage())).build();
//			throw new Exception(e.getMessage());
//		}
//
//		return result;
//	}
//
//	/**
//	 * Start optimization by request from the director.
//	 * 
//	 * @param payload
//	 * @return
//	 * @throws Exception
//	 */
//	@POST
//	@Path(Variables.Webrequest.NEWSOLUTIONEVALUATION)
//	@Produces(MediaType.APPLICATION_JSON)
//	@Consumes(MediaType.APPLICATION_JSON)
//	public Response requestNewEvaluationReady(String payload) throws Exception {
//		Response result = null;
//
//		JsonObject obj = (new Gson()).fromJson(payload, JsonObject.class);
//
//		// http://localhost:8001/kore/?command=start&simulationid=textfile1&ontologyid=usecase1varition1&breaksimulatorrun=100&breakevaluationco2=0.5&breakevaluationenergy=0.2&breakevaluationpenalty=0.1
//		log.info("Received request {}. Payload={}", Variables.Webrequest.NEWSOLUTIONEVALUATION, obj);
//		String simulationrunURI = obj.get("SimulationRunURI").getAsString();
//		String useCaseUri = obj.get("UseCaseURI").getAsString();
//		String solutionUri = obj.get("SolutionURI").getAsString();
//		String variantUri = obj.get("VariantURI").getAsString();
//		// String scenarioUri = obj.get("ScenarioURI").getAsString();
//
//		RequestNewSolutionEvaluation request;
//		try {
//			request = new RequestNewSolutionEvaluation();
//			request.setSimulationRunUri(simulationrunURI);
//			request.setSolutionUri(solutionUri);
//			request.setUseCaseUri(useCaseUri);
//			request.setVariantUri(variantUri);
//			log.debug("Converted Json request into chunk:", request);
//
//			// Write the answer to the working memory
//			this.function.getCommunicatorFromFunction().write(DatapointBuilder.newDatapoint(this.requestAddress + "." + Variables.Webrequest.NEWSOLUTIONEVALUATION).setValue(request));
//			log.debug("request {} written to address {}", request, this.requestAddress + "." + Variables.Webrequest.NEWSOLUTIONEVALUATION);
//
//			// Trigger the cogsys
//			this.function.getCommunicatorFromFunction().write(DatapointBuilder.newDatapoint(this.cogsysTriggerAddress).setValue("Trigger by new simulation solution"));
//
//			JsonElement response = new JsonPrimitive("Request executed");
//			result = Response.ok(response.toString()).build();
//		} catch (Exception e) {
//			log.error("Cannot receive result", e.getMessage());
//			result = Response.status(Response.Status.BAD_REQUEST).entity(new JsonPrimitive("Cannot receive result" + e.getMessage())).build();
//			throw new Exception(e.getMessage());
//		}
//
//		return result;
//	}
//
//	/**
//	 * Start optimization by request from the director.
//	 * 
//	 * @param payload
//	 * @return
//	 * @throws Exception
//	 */
//	@POST
//	@Path(Variables.Webrequest.STARTOPTIMIZATIONTASK)
//	@Produces(MediaType.APPLICATION_JSON)
//	@Consumes(MediaType.APPLICATION_JSON)
//	public Response requestStartOptimizationTask(@Context HttpServletRequest context, String payload) throws Exception {
//		Response result = null;
//
//		final String usecaseid = "UseCaseURI";
//		final String scenarioid = "ScenarioURI";
//
//		JsonObject obj = (new Gson()).fromJson(payload, JsonObject.class);
//		String responseUri = (obj.has("ResponseURI") ? obj.get("ResponseURI").getAsString() : "");
//		String callerIP = responseUri;// context.getRemoteAddr();
//
//		log.info("Received request call from={}", callerIP);
//
//		// http://localhost:8001/kore/?command=start&simulationid=textfile1&ontologyid=usecase1varition1&breaksimulatorrun=100&breakevaluationco2=0.5&breakevaluationenergy=0.2&breakevaluationpenalty=0.1
//		log.info("Received request {}. UsecaseUri={}, scenarioUri={}", this.requestAddress + "." + Variables.Webrequest.STARTOPTIMIZATIONTASK, obj.get(usecaseid), obj.get(scenarioid));
//		RequestStartOptimization requestChunk;
//		try {
//			requestChunk = DatastructureUtil.generateIncompleteRequestToOptimizeChunk(obj.get(usecaseid).getAsString(), obj.get(scenarioid).getAsString(), callerIP);
//			log.debug("Converted Json request into chunk:", requestChunk);
//
//			// Write the answer to the working memory
//			this.function.getCommunicatorFromFunction().write(DatapointBuilder.newDatapoint(this.requestAddress + "." + Variables.Webrequest.STARTOPTIMIZATIONTASK).setValue(requestChunk));
//			log.debug("request {} written to address {}", requestChunk, this.requestAddress + "." + Variables.Webrequest.STARTOPTIMIZATIONTASK);
//
//			// Add delay as the notification of the new request may be slower than the update message.
////			try {
////				this.wait(200);
////			} catch (InterruptedException e1) {
////
////			}
//
//			// Trigger the cogsys
//			this.function.getCommunicatorFromFunction().write(DatapointBuilder.newDatapoint(this.cogsysTriggerAddress).setValue("Trigger by new request from user"));
//
//			JsonElement response = new JsonPrimitive("Request executed");
//			result = Response.ok(response.toString()).build();
//		} catch (Exception e) {
//			log.error("Cannot receive result", e.getMessage());
//			result = Response.status(Response.Status.BAD_REQUEST).entity(new JsonPrimitive("Cannot receive result" + e.getMessage())).build();
//			throw new Exception(e.getMessage());
//		}
//
//		return result;
//	}
//
//	@POST
//	@Path(Variables.Webrequest.INTERRUPTOPTIMIZATION)
//	@Produces(MediaType.APPLICATION_JSON)
//	@Consumes(MediaType.APPLICATION_JSON)
//	public Response interruptOptimization() throws Exception {
//		Response result = null;
//
//		// http://localhost:8001/kore/?command=start&simulationid=textfile1&ontologyid=usecase1varition1&breaksimulatorrun=100&breakevaluationco2=0.5&breakevaluationenergy=0.2&breakevaluationpenalty=0.1
//		log.info("Received request {}. Now check the reaction of the system", Variables.Webrequest.INTERRUPTOPTIMIZATION);
//		Request requestChunk;
//		try {
//			requestChunk = new Request(Variables.Webrequest.INTERRUPTOPTIMIZATION);
//			log.debug("Converted http-request into chunk:", requestChunk);
//
//			// Write the answer to the working memory
//			this.function.getCommunicatorFromFunction().write(DatapointBuilder.newDatapoint(CognitiveArchitecture.REQUESTSPREFIXADDRESS + "." + Variables.Webrequest.INTERRUPTOPTIMIZATION).setValue(requestChunk));
//			log.debug("request {} written to address {}", requestChunk, this.requestAddress + ".interruptionrequest");
//			JsonElement response = new JsonPrimitive("Request executed");
//			result = Response.ok(response.toString()).build();
//
//			// Trigger the cogsys
//			this.function.getCommunicatorFromFunction().write(DatapointBuilder.newDatapoint(this.cogsysTriggerAddress).setValue("Trigger by interruption by the user"));
//		} catch (Exception e) {
//			log.error("Cannot receive result", e.getMessage());
//			result = Response.status(Response.Status.BAD_REQUEST).entity(new JsonPrimitive("Cannot receive result" + e.getMessage())).build();
//			throw new Exception(e.getMessage());
//		}
//
//		return result;
//	}
//
//	@POST
//	@Path(Variables.Webrequest.SETDEBUGMODE)
//	@Produces(MediaType.APPLICATION_JSON)
//	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//	public Response setDebugMode(@FormParam("mode") String mode) throws Exception {
//		Response result = null;
//
//		// Modes:
//		// activate: activate debugging, i.e. pause between the runs
//		// deactivate: deactivate debugging, i.e. no pause between the runs
//		// start: if activated, continue
//
//		// http://localhost:8001/kore/?command=start&simulationid=textfile1&ontologyid=usecase1varition1&breaksimulatorrun=100&breakevaluationco2=0.5&breakevaluationenergy=0.2&breakevaluationpenalty=0.1
//		log.info("Received request {}. Now check the reaction of the system", Variables.Webrequest.SETDEBUGMODE);
//		// Request requestChunk;
//		try {
//			log.debug("Got debug request with mode=", mode);
//
//			if (mode.equals("activate")) {
//				log.debug("Activate debugging");
//
//			} else if (mode.equals("deactivate")) {
//				log.debug("Deactivate debugging");
//
//			} else if (mode.equals("start")) {
//				log.debug("Start next cycle");
//
//			}
//
//			// Write the answer to the working memory
//			// this.function.getCommunicatorFromFunction().write(Datapoints.newDatapoint(this.requestAddress +
//			// ".requestInterrupt").setValue(requestChunk));
//			// log.debug("request {} written to address {}", requestChunk, this.requestAddress +
//			// ".interruptionrequest");
//			result = Response.ok().build();
//		} catch (Exception e) {
//			log.error("Cannot receive result", e.getMessage());
//			result = Response.status(Response.Status.BAD_REQUEST).entity(new JsonPrimitive("Cannot receive result" + e.getMessage())).build();
//			throw new Exception(e.getMessage());
//		}
//
//		return result;
//	}
}
