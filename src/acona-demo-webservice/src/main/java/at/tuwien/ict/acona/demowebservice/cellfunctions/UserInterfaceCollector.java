package at.tuwien.ict.acona.demowebservice.cellfunctions;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;
import at.tuwien.ict.acona.demowebservice.launcher.GraphServer;



/**
 * The user interface collector will collect values from various datapoints in the system and represent them on a user interface.
 * 
 * @author wendt
 *
 */
public class UserInterfaceCollector extends CellFunctionThreadImpl {
	
	private final static Logger log = LoggerFactory.getLogger(UserInterfaceCollector.class);	
	private GraphServer gserver;
	
	public static final String SYSTEMSTATEADDRESSID = "state";
	
	@Override
	protected void cellFunctionThreadInit() throws Exception {
		// TODO Auto-generated method stub
		
		//If you poll, use this function in the init method and set
		//this.setExecuteOnce(false);
		//this.setExecuteRate(1000); //once 1s
		
		gserver = new GraphServer(8000);
		
		
	}
	
	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		// TODO Auto-generated method stub
		//Add your own service here and test it with
		//parameterdata.getMethod()
		
		
		
		return null;
	}
	
	@Override
	protected void executeCustomPreProcessing() throws Exception {
		// TODO Auto-generated method stub
		
		//@Lampros: If you poll a datapoints, put the datapoints to read in 
		//this.addManagedDatapoint(DatapointConfig.newConfig("testid", "test", "agenttest", SyncMode.READONLY));
		//and read like this
		//this.getValueMap().get("testid").getValue()
		
	}

	@Override
	protected void executeFunction() throws Exception {
		// TODO Auto-generated method stub
		
		//Here is the execution function, if you run the thread
		//gserver.put("JSON CONTENT");

	}
	
	@Override
	protected void executeCustomPostProcessing() throws Exception {
		
		// TODO Auto-generated method stub
		//Cleaning after the run
		
	}

	
		
	 private String  calculateColor(String inputState) {
		String tmpColor = "#000000";
		Map<String, String> palette = new HashMap<String, String>();
		// This custom palette has the colors that are more distinguishable with each other
		palette.put("black","#000000");
		palette.put("silver","#c0c0c0");
		palette.put("blue","#5DA5DA");
		palette.put("orange","#FAA43A");
		palette.put("green","#60BD68 ");
		palette.put("pink","#F17CB0");
		palette.put("brown","#B2912F");
		palette.put("purple","#B276B2");
		palette.put("yellow","#DECF3F");
		palette.put("red","#F15854");

		if (inputState.equals("INITIALIZING")) {
			tmpColor = palette.get("yellow");
		}
				
		else if (inputState.equals("RUNNING")) {
			tmpColor = palette.get("blue");			
		}
		else if (inputState.equals("ERROR")) {
			tmpColor = palette.get("red");			
		}
		else if (inputState.equals("FINISHED")) {
			tmpColor = palette.get("green");
		}
		else {
			
		}
		return tmpColor;
	}
	 
	 private String  calculateColor2(double inputState) {
			String tmpColor = "#000000";
			Map<String, String> palette = new HashMap<String, String>();
			// This custom palette has the colors that are more distinguishable with each other
			palette.put("black","#000000");
			palette.put("silver","#c0c0c0");
			palette.put("blue","#5DA5DA");
			palette.put("orange","#FAA43A");
			palette.put("green","#60BD68 ");
			palette.put("pink","#F17CB0");
			palette.put("brown","#B2912F");
			palette.put("purple","#B276B2");
			palette.put("yellow","#DECF3F");
			palette.put("red","#F15854");

			if (inputState>25) {
				tmpColor = palette.get("red");
			}
					
			else if (inputState<=35 && inputState>25) {
				tmpColor = palette.get("orange");			
			}
			else if (inputState<=25 && inputState>20) {
				tmpColor = palette.get("green");			
			}
			else if (inputState<=20) {
				tmpColor = palette.get("blue");
			}
			else {
				
			}
			return tmpColor;
		}
	 
	 
	Map<String, JsonArray> agentsList = new HashMap<String, JsonArray>();

	@Override
	protected synchronized void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
	
		
		// set default values
		int graphWidth = 1200;
		int graphHeight = 1000;
		int circleSize = 10;
		JsonArray  outputJSON = new JsonArray();
		
		if (data.containsKey(UserInterfaceCollector.SYSTEMSTATEADDRESSID)) {
			
			//log.info("Current state={}", data.get(UserInterfaceCollector.SYSTEMSTATEADDRESSID).getValue());
			//log.info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			
	        // Create Agent JsonObject and store the JSON
			JsonObject outAgent = new JsonObject();
			
			// Create JsonObject for the config 
			JsonObject graphConfig = new JsonObject();
			
			//Parse JSON Input
			JsonObject tmpAgent = data.get("state").getValue().getAsJsonObject();

			// getting Agent name
			String agentName = tmpAgent.get("agentname").getAsString();
        	//log.info("Agent Name: "+agentName);
        	outAgent.addProperty("NodeID", agentName);

        	// getting Agent Description
        	String agentDesc = tmpAgent.get("hasDescription").getAsString();
        	outAgent.addProperty("NodeText", agentDesc);
        	
        	// getting Agent state
        	String agentState = "";
        	if (tmpAgent.has("hasState")) {
        		agentState = tmpAgent.get("hasState").getAsString();
            	outAgent.addProperty("Color", calculateColor(agentState));
        	}
        	else {
            	//outAgent.addProperty("Color", calculateColor("DoesnotExist"));
        	}
    	
        	JsonArray all_nodes = new JsonArray();
        	JsonArray all_links = new JsonArray();
        	
        	//log.info("NODE info   :"+ outAgent.toString());
        	all_nodes.add(outAgent);

        	String tmpLinksource = tmpAgent.get("agentname").getAsString();

        	
			//get all the functions
	        JsonArray tmpFunctions = tmpAgent.getAsJsonArray("hasFunction");
	         	        	        		
	        //Iterate through functions and extract needed information
	        for (JsonElement currentFunct : tmpFunctions) {

	        	//Create an object to place the output function structure
		        JsonObject outFunction = new JsonObject();
	        	//Create an object for the temporary Json link object
		        JsonObject tmpLink = new JsonObject();

	        	JsonObject tmpFunct = currentFunct.getAsJsonObject();
	        	String functName = tmpFunct.get("hasName").getAsString();
	        	String functState = tmpFunct.get("hasState").getAsString();
	          	String functDesc = tmpFunct.get("hasDescription").getAsString();
	        	//log.info("Function Name: "+functName);
				//log.info("Function State: "+functState);
	        	outFunction.addProperty("NodeID", agentName+functName);
	        	outFunction.addProperty("NodeText", functDesc);
	        	outFunction.addProperty("Color", calculateColor(functState));				
	        	//log.info("NODE info   :"+ outFunction.toString());
	        	tmpLink.addProperty("source",tmpLinksource);
	        	tmpLink.addProperty("target",agentName+functName);
	        	all_links.add(tmpLink);
	        	all_nodes.add(outFunction);
	        	//tmpLinks.add(tmpLink);
	 	        //agentsList.put(agentName, tmpLinks);
	        }
	        
	        graphConfig.addProperty("graphWidth", graphWidth);
	        graphConfig.addProperty("graphHeight", graphHeight);
	        graphConfig.addProperty("circleSize", circleSize);
	       
	        
	       //log.info("CONFIG             :"+ graphConfig.toString());
	       //log.info("Nodes Json array   :"+  all_nodes.toString());
	       //log.info("Links Json array   :"+  all_links.toString());
	        
	        outputJSON.add(graphConfig);
	        outputJSON.add(all_nodes);
	        outputJSON.add(all_links);
	        //log.info("Final JSON: "+outputJSON.toString());
	        
	        //log.info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n");
	        //gserver.put(outputJSON.toString());
	       //gserver.setString(outputJSON.toString());
		} else if (data.containsKey("RESULT")) {
			log.info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			log.info("Current state={}", data.get("RESULT").getValue());
			//Datapoint y = data.get("RESULT").getValue();
			log.info("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			//gserver.setString(data.get("RESULT").getValue().toString());

			// Create Agent JsonObject and store the JSON

			// Create Agent JsonObject and store the JSON
			JsonObject outNode = new JsonObject();
			
			// Create JsonObject for the config  
//			JsonObject graphConfig = new JsonObject();
			
//			//Parse JSON Input - THIS IS THE ADDRESS CONTAINER
			JsonObject inDataJSON= data.get("RESULT").getValue().getAsJsonObject();

			// 1ST LEVEL OF ARCHITECTURE			
			
			// getting Agent name, 1st level
			String nodeID = inDataJSON.get("hasName").getAsString();
			
			// getting Agent Description, 1st level
        	String nodeDesc = inDataJSON.get("hasConclusio").getAsString();

        	// getting Agent Color, 1st level
        	String nodeColor = "";			
        	
        	// Add the 1st Level Node properties
			outNode.addProperty("NodeID", nodeID);
        	outNode.addProperty("NodeText", nodeDesc);
        	outNode.addProperty("NodeColor", nodeColor);
        	outNode.addProperty("NodeSize", Double.parseDouble("30"));

        	// Create the Ouput JSONArrays
        	JsonArray allNodes = new JsonArray();
        	JsonArray allLinks = new JsonArray();
        	
        	allNodes.add(outNode);
	        log.info("TEST Parent: "+ allNodes.toString());

        	//Set as source of the childNodes the Parent node ID
        	String childLinksSource = nodeID;

			//get all the child nodes string; It is under hasData
	        JsonArray childsData = inDataJSON.getAsJsonArray("hasData");
	         	        	        		
	        //Iterate through hasData and extract needed information
	        for (JsonElement currentData : childsData) {

	        	//Create an object to place the output child Node
		        JsonObject outChildNode = new JsonObject();
	        	//Create an object for the temporary Json link object
		        JsonObject childLinks = new JsonObject();

		        //Get the JSON object from the JSON element
	        	JsonObject childData = currentData.getAsJsonObject();
	        	
	        	//Custom structure Data 
	        	String childDataNodeID = childData.get("hasName").getAsString();
	        	String childDataCity = childData.get("City").getAsString();
	        	String childDataTemperature = childData.get("Temperature").getAsString();
	          	
	        	//childData Output structure
	        	outChildNode.addProperty("NodeID", childDataNodeID);
	        	outChildNode.addProperty("NodeText", childDataCity+":"+childDataTemperature);
	        	String a =Double.toString((Double.parseDouble(childDataTemperature)));
	        	log.info("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA: "+a);
	        	outChildNode.addProperty("NodeSize", a);
	        	outChildNode.addProperty("NodeColor", calculateColor2(Double.parseDouble(childDataTemperature)));
	        	childLinks.addProperty("source",childLinksSource);
	        	childLinks.addProperty("target",childDataNodeID);
	        	allLinks.add(childLinks);
	        	allNodes.add(outChildNode);
	        }
	        //log.info("TEST: "+ allNodes.toString());	      // gserver.setString(outputJSON.toString());
	        //log.info("TEST: "+ allLinks.toString());	      // gserver.setString(outputJSON.toString());
	        outputJSON.add(allNodes);
	        outputJSON.add(allLinks);
	        gserver.setString(outputJSON.toString());
	        
		}
		
	}

	@Override
	protected void shutDownExecutor() throws Exception {
		// TODO Auto-generated method stub
		
	}

}