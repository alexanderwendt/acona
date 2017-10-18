package at.tuwien.ict.acona.demowebservice.webservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.datastructures.Chunk;
import at.tuwien.ict.acona.cell.datastructures.ChunkBuilder;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.DatapointBuilder;

@Path("korecogsys")
public class JerseyRestService {
	private final static String SERVICENAME = "korecogsys";
	
	public static final String attributeCommandAddress = "commandaddress";
	public static final String attributeResultAddress =  "resultaddress";
	public static final String attributeSystemStateAddress = "systemstateaddress";
	public static final String attributeEpisodeLoaderStateAddress = "episodeloaderstate";
	public static final String attributerulegeneratorStateAddress = "rulegeneratorstate";
	
	private String commandAddress = "";
	private String resultAddress = "";
	private String stateAddress = "";
	private String episodestateAddress = "";
	private String rulegeneratorStateAddress = "";
	
	private final JerseyRestServer function;
	
	private final static Logger log = LoggerFactory.getLogger(JerseyRestService.class);
	
	public JerseyRestService() throws Exception {
		this.function = ServletSingleton.getFunction();
		
		//this.commandAddress = this.function.getFunctionConfig().getProperty(attributeCommandAddress, Variables.Request.COMMANDADDRESS);
		//this.resultAddress = this.function.getFunctionConfig().getProperty(attributeResultAddress, Variables.Request.RESULTADDRESS);
		this.stateAddress = this.function.getFunctionConfig().getProperty(attributeSystemStateAddress);
		this.episodestateAddress = this.function.getFunctionConfig().getProperty(attributeEpisodeLoaderStateAddress);
		this.rulegeneratorStateAddress = this.function.getFunctionConfig().getProperty(attributerulegeneratorStateAddress);
	}
	
	@GET
    @Path("test")
    @Produces(MediaType.APPLICATION_JSON)
    public Response readState() throws Exception {
		Response result = null;
		
		String description = "Description of the system state\n\n";
		
		try {
			String systemStateString = "State of all codelets in the system:\n";
			Chunk systemState = ChunkBuilder.newChunk(this.function.getCommunicatorFromFunction().read(this.stateAddress).getValue().getAsJsonObject());
			for (Chunk c: systemState.getAssociatedContent("hasCodelet")) {
				systemStateString += "Function name: " + c.getName() + ", state: " + c.getValue("State") + "\n";
			}
			
			systemState.getFirstAssociatedContentFromAttribute("hasCodelet", "hasName", "cogsysagent:loadercodelet").setValue("Description", "Episode Loader");
			systemState.getFirstAssociatedContentFromAttribute("hasCodelet", "hasName", "cogsysagent:loadercodelet").setValue("StateDetails", this.function.getCommunicatorFromFunction().read(this.episodestateAddress).getValueAsString());
			
			systemState.getFirstAssociatedContentFromAttribute("hasCodelet", "hasName", "cogsysagent:rulegenerationcodelet").setValue("Description", "Rule Structure Generator");
			systemState.getFirstAssociatedContentFromAttribute("hasCodelet", "hasName", "cogsysagent:rulegenerationcodelet").setValue("StateDetails", this.function.getCommunicatorFromFunction().read(this.rulegeneratorStateAddress).getValueAsString());
			
			systemStateString += "\n";
			String episodeLoaderString = "State of the episode loader:\n" + this.function.getCommunicatorFromFunction().read(this.episodestateAddress).getValueAsString() + "\n\n";
			String ruleEgeneratorString = "State of the rule structure generator:\n" + this.function.getCommunicatorFromFunction().read(this.rulegeneratorStateAddress).getValueAsString() + "\n\n";
			
			description = description +systemStateString + episodeLoaderString + ruleEgeneratorString;
			
			systemState.setValue("hasDescription", description);
			result = Response.ok().entity(systemState.toJsonObject().toString()).build();
		} catch (Exception e) {
			log.error("Cannot read address: " + this.stateAddress, e);
			result = Response.status(Response.Status.BAD_REQUEST).build();
			throw new Exception(e.getMessage());
		}
		
		return result;	
	}
	
	@GET
    @Path("tests")
    @Produces(MediaType.APPLICATION_JSON)
	public Response readResult() throws Exception {
		//Datapoint result = Datapoint.newNullDatapoint();
		Response result = null;
		
		
		try {
			Datapoint readResult = this.function.getCommunicatorFromFunction().read(this.resultAddress);
			
			result = Response.ok().entity(readResult.toJsonObject().toString()).build();
		} catch (Exception e) {
			log.error("Cannot read address: " + this.resultAddress);
			result = Response.status(Response.Status.BAD_REQUEST).build();
			throw new Exception(e.getMessage());
		}
		
		return result;
	}
	
	@POST
    @Path("test3")
    @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response requestOptimization(
			
			//simulationid=filename2&ontologyid=usecase1_variation3&breaksimulatorrun=101&breakevaluationco2=0.4&breakevaluationenergy=0.2&breakevaluationpenalty=0.8
			@FormParam("simulationid") String simulationId, 
			@FormParam("ontologyid") String ontologyId, 
			@FormParam("breaksimulatorrun") int breakSimulatorRun, 
			@FormParam("breakevaluationco2") double breakEvaluationCo2,
			@FormParam("breakevaluationenergy") double breakEvaluationEnergy,
			@FormParam("breakevaluationpenalty") double breakEvaluationPenalty) throws Exception {
		Response result = null;
		
		//http://localhost:8001/kore/?command=start&simulationid=textfile1&ontologyid=usecase1varition1&breaksimulatorrun=100&breakevaluationco2=0.5&breakevaluationenergy=0.2&breakevaluationpenalty=0.1
		//log.info("Received request {}. Now check the reaction of the system", Variables.Webrequest.GETSATISFACTORYRULESET);
		//RequestRuleset requestChunk;
		try {
		//	requestChunk = DatastructureUtil.generateRequestToOptimizeChunk(simulationId, ontologyId, breakSimulatorRun, breakEvaluationCo2, breakEvaluationEnergy, breakEvaluationPenalty);
		//	log.debug("Converted http-request into chunk:", requestChunk);
			
			//Write the answer to the working memory
		//	this.function.getCommunicatorFromFunction().write(Datapoints.newDatapoint(this.commandAddress).setValue(requestChunk));
		//	log.debug("request {} written to address {}", requestChunk, this.commandAddress);
			result = Response.ok().build();
		} catch (Exception e) {
			log.error("Cannot receive result", e.getMessage());
			result = Response.status(Response.Status.BAD_REQUEST).entity(new JsonPrimitive("Cannot receive result" + e.getMessage())).build();
			throw new Exception (e.getMessage());
		}
		
		return result;
	}
	
    @GET
    @Path("square")
    @Produces(MediaType.APPLICATION_JSON)
    public String square(@QueryParam("input") double input){
    	String result = "1.99";
        return result;
    }
}
