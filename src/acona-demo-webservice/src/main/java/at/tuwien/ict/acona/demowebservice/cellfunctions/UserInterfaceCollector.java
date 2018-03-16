package at.tuwien.ict.acona.demowebservice.cellfunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

			if (inputState>35) {
				tmpColor = palette.get("red");
			}
					
			else if (inputState<=35 && inputState>25) {
				tmpColor = palette.get("orange");			
			}
			else if (inputState<=25 && inputState>10) {
				tmpColor = palette.get("green");			
			}
			else if (inputState<=10) {
				tmpColor = palette.get("blue");
			}
			else {
				
			}
			return tmpColor;
		}
	 
	 
	Map<String, JsonArray> agentsList = new HashMap<String, JsonArray>();
	
	private JsonObject setNode(String nodeID, String nodeDesc, String nodeColor, String NodeSize) {
		JsonObject outNode = new JsonObject();
		outNode.addProperty("NodeID", nodeID);
    	outNode.addProperty("NodeText", nodeDesc);
    	outNode.addProperty("NodeColor", nodeColor);
    	outNode.addProperty("NodeSize", Double.parseDouble(NodeSize));		
		return outNode;
	}
	private JsonObject setLink(String NodeIDSource, String NodeIDTarget) {
		JsonObject outLink = new JsonObject();
		outLink.addProperty("source", NodeIDSource);
		outLink.addProperty("target", NodeIDTarget);
		return outLink;
	}
	
	private class KoreNode {
		JsonArray allNodes;
		JsonArray allLinks;
		public JsonArray getNodes(){
			return allNodes;
		}
		public JsonArray getLinks() {
			return allLinks;
		}
	
//	private KoreNode calcTree(KoreNode inputNode , JsonElement inputElement) {
//		
//		if (inputElement.isJsonObject()) {
//			
//		}
//		//goal is a JsonObject
//		JsonObject goalObject = goal.getAsJsonObject(); 
//		
//		//create unique nodeID
//		nodeID = rootNodeID+ goalObject.get("name").getAsString();
//		nodeDesc = goalObject.get("name").getAsString();
//		nodeColor = "blue";
//		nodeSize = "10";
//		
//		//add a node "goal" with the above properties
//		allNodes.add(setNode(nodeID, nodeDesc, nodeColor, nodeSize));
//		//create the "goal" node links
//		allLinks.add(setLink(rootNodeID, nodeID));
//		
//		//loop through goal children
//		
//		for (Map.Entry<String, JsonElement> goalProperty: goalObject.entrySet()) {
//			//if the name is "condition", it is a JsonArray
//			if (goalProperty.getKey().equals("condition")){
//					
//		return inputNode;
	}
	
	@Override
	protected synchronized void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
	
		
		log.debug("Received data={}", data);
		
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
	        graphConfig.addProperty("circleSize", 10+circleSize);
	       
	        
	       //log.info("CONFIG             :"+ graphConfig.toString());
	       //log.info("Nodes Json array   :"+  all_nodes.toString());
	       //log.info("Links Json array   :"+  all_links.toString());
	        
	        outputJSON.add(graphConfig);
	        outputJSON.add(all_nodes);
	        outputJSON.add(all_links);
	        log.info("Final JSON: "+outputJSON.toString());
	        
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
	        	Double childDataTemperature = Double.parseDouble(childData.get("Temperature").getAsString());
	        	childDataTemperature = Math.round(100*childDataTemperature)/100. ;
	         
	        	//childData Output structure
	        	outChildNode.addProperty("NodeID", childDataNodeID);
	        	outChildNode.addProperty("NodeText", childDataCity+":"+ childDataTemperature);
	        	log.info("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA: "+ childDataTemperature);
	        	outChildNode.addProperty("NodeSize", childDataTemperature);
	        	outChildNode.addProperty("NodeColor", calculateColor2(childDataTemperature));
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
	        
		} else if (data.containsKey("KORE")) {
			log.info("KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK");
			log.info("Current state={}", data.get("KORE").getValue());
	        gserver.setString(data.get("KORE").getValue().toString());
			log.info("KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK");

        	// Create the Ouput JSONArrays
        	JsonArray allNodes = new JsonArray();
        	JsonArray allLinks = new JsonArray();
        	
        	// Set the default Node Information
        	String nodesizeLVL1 = "100";
        	String nodesizeLVL2 = "50";
        	String nodesizeLVL3 = "5";
        	String defaultTextAlign = "middle";
        	
        	
        	// Create JsonObject for the config  
//			JsonObject graphConfig = new JsonObject();
			
//			//Parse JSON Input - THIS IS THE KORE CONTAINER
			JsonObject inDataJSON= data.get("KORE").getValue().getAsJsonObject();
			
			//Create an entrySet to loop through root values (without knowing member names);In this case we have 9 JsonArray values
			Set<Map.Entry<String, JsonElement>> entrySet = inDataJSON.entrySet();
			
			//Loop through entrySet 
			for (Map.Entry<String, JsonElement> entry:entrySet){
								
				// get the identification value
				String typeKORE = entry.getKey().toString();
				
				//KORE type is "Requests"
				if (typeKORE.equals("Requests")) {
					
					//Get the data included in this type, we know it is an JsonArray
					JsonArray dataKORE = entry.getValue().getAsJsonArray();
					
					//Prepare the output Node
					JsonObject outNode = new JsonObject();
					
					//set the parent node; the root node
					String rootNodeID = typeKORE;
					
					//root node
					String nodeID = rootNodeID;
					String nodeDesc = typeKORE;
					String nodeColor = "red";
					String nodeSize = nodesizeLVL1;

					//set the node Properties
					outNode = setNode(nodeID, nodeDesc, nodeColor, nodeSize);
					
					//add node
					allNodes.add(outNode);
		        	
					
					//loop through JsonArray "Requests"; each "request" is a JsonObject
					
					for (JsonElement request: dataKORE) {
						JsonObject requestObject = request.getAsJsonObject(); 
						
						//request is a JsonObject
						//create unique nodeID
						nodeID = rootNodeID+ requestObject.get("name").getAsString();
						nodeDesc = requestObject.get("name").getAsString();
						nodeColor = "blue";
						nodeSize = nodesizeLVL2;
						
						//add a node "request" with the above properties
						allNodes.add(setNode(nodeID, nodeDesc, nodeColor, nodeSize));
						
						//create the "request" node links
						allLinks.add(setLink(rootNodeID, nodeID));
			
						Set<Map.Entry<String, JsonElement>> requestProperties = requestObject.entrySet();

						//Create nodes for all properties
						for (Map.Entry<String, JsonElement> requestProperty :requestProperties){
							//Set property values
							String propertyID = nodeID+requestProperty.getKey().toString();
							String propertyDesc = requestProperty.getKey().toString()+" : " + requestProperty.getValue().toString();
							String propertyColor = "green";
							String propertySize = nodesizeLVL3;

							//add a node "property" with the above properties
							allNodes.add(setNode(propertyID, propertyDesc, propertyColor, propertySize));
							
							//create the "property" node links
							allLinks.add(setLink(nodeID,propertyID));										
							}
						}
					}//end if "requests
				//KORE type is "Episodes"
				else if (typeKORE.equals("Episodes")) {
					
					//Get the data included in this type, we know it is an JsonArray
					JsonArray dataKORE = entry.getValue().getAsJsonArray();
					
					//Prepare the output Node
					JsonObject outNode = new JsonObject();
					
					//set the parent node; the root node
					String rootNodeID = typeKORE;
					
					//root node
					String nodeID = rootNodeID;
					String nodeDesc = typeKORE;
					String nodeColor = "red";
					String nodeSize = nodesizeLVL1;
					
					//set the node Properties
					outNode = setNode(nodeID, nodeDesc, nodeColor, nodeSize);
					//add node
					allNodes.add(outNode);
       	
					//loop through JsonArray "Episodes"; each "episode" is a JsonObject
					for (JsonElement episode: dataKORE) {
						
						//episode is a JsonObject
						JsonObject episodeObject = episode.getAsJsonObject(); 
						
						//create unique nodeID
						nodeID = rootNodeID+ episodeObject.get("name").getAsString();
						nodeDesc = episodeObject.get("name").getAsString();
						nodeColor = "blue";
						nodeSize = nodesizeLVL2;
						
						//add a node "episode" with the above properties
						allNodes.add(setNode(nodeID, nodeDesc, nodeColor, nodeSize));
						//create the "episode" node links
						allLinks.add(setLink(rootNodeID, nodeID));
						
						//loop through episode children
						
						for (Map.Entry<String, JsonElement> episodeProperty: episodeObject.entrySet()) {
							//if the name is "evaluation, it is a nested object
							if (episodeProperty.getKey().equals("evaluation")){
								
								//get the object
								JsonObject evaluationsObject = episodeProperty.getValue().getAsJsonObject();
								
								//create unique nodeID2
								String node2ID =nodeID+ episodeProperty.getKey();
								String node2Desc = episodeProperty.getKey();
								String node2Color = "blue";
								String node2Size = nodesizeLVL2;
								//add a node "evaluation" with the above properties
								allNodes.add(setNode(node2ID, node2Desc, node2Color, node2Size));
								//create the "episode" node links
								allLinks.add(setLink(nodeID, node2ID));
								
								for (Map.Entry<String, JsonElement> evaluationProperty: evaluationsObject.entrySet()) {
									
									//Set property values
									String propertyID = node2ID+evaluationProperty.getKey().toString();
									String propertyDesc = evaluationProperty.getKey().toString()+" : " + evaluationProperty.getValue().toString();
									String propertyColor = "green";
									String propertySize = nodesizeLVL3;

									//add a node "property" with the above properties
									allNodes.add(setNode(propertyID, propertyDesc, propertyColor, propertySize));
									//create the "property" node links
									allLinks.add(setLink(node2ID,propertyID));
								}
							}
							else {
								//Set property values
								String propertyID = nodeID+episodeProperty.getKey().toString();
								String propertyDesc = episodeProperty.getKey().toString()+" : " + episodeProperty.getValue().toString();
								String propertyColor = "green";
								String propertySize = nodesizeLVL3;

								//add a node "property" with the above properties
								allNodes.add(setNode(propertyID, propertyDesc, propertyColor, propertySize));
								//create the "property" node links
								allLinks.add(setLink(nodeID,propertyID));

							}
						}
					}//End loop through episodes
													
				}//Endif episodes
				else if (typeKORE.equals("Goals")) {
					
					//Get the data included in this type, we know it is an JsonArray
					JsonArray dataKORE = entry.getValue().getAsJsonArray();
					
					//Prepare the output Node
					JsonObject outNode = new JsonObject();
					
					//set the parent node; the root node
					String rootNodeID = typeKORE;
					
					//root node
					String nodeID = rootNodeID;
					String nodeDesc = typeKORE;
					String nodeColor = "red";
					String nodeSize = nodesizeLVL1;
					
					//set the node Properties
					outNode = setNode(nodeID, nodeDesc, nodeColor, nodeSize);
					//add node
					allNodes.add(outNode);
       	
					//loop through JsonArray "Goals"; each "goal" is a JsonObject
					for (JsonElement goal: dataKORE) {
						
						//goal is a JsonObject
						JsonObject goalObject = goal.getAsJsonObject(); 
						
						//create unique nodeID
						nodeID = rootNodeID+ goalObject.get("name").getAsString();
						nodeDesc = goalObject.get("name").getAsString();
						nodeColor = "blue";
						nodeSize = nodesizeLVL2;
						
						//add a node "goal" with the above properties
						allNodes.add(setNode(nodeID, nodeDesc, nodeColor, nodeSize));
						//create the "goal" node links
						allLinks.add(setLink(rootNodeID, nodeID));
						
						//loop through goal children
						
						for (Map.Entry<String, JsonElement> goalProperty: goalObject.entrySet()) {
							//if the name is "condition", it is a JsonArray
							if (goalProperty.getKey().equals("condition")){
								
								//the goal-"condition" is a JsonArray
								JsonArray conditionArray = goalProperty.getValue().getAsJsonArray();
								
								//create unique nodeID2
								String node2ID =nodeID+ goalProperty.getKey();
								String node2Desc = goalProperty.getKey();
								String node2Color = "blue";
								String node2Size = nodesizeLVL2;
								//add a node "evaluation" with the above properties
								allNodes.add(setNode(node2ID, node2Desc, node2Color, node2Size));
								//create the "episode" node links
								allLinks.add(setLink(nodeID, node2ID));



								//loop through conditions of Goals
								for (JsonElement condition: conditionArray) {
									
									//create unique nodeID3
									String node3ID = node2ID+ condition.getAsJsonObject().get("name").getAsString();
									String node3Desc = condition.getAsJsonObject().get("name").getAsString();
									String node3Color = "green";
									String node3Size = nodesizeLVL3;
									
									//add a node "name of condition" with the above properties
									allNodes.add(setNode(node3ID, node3Desc, node3Color, node3Size));
									//create the "episode" node links
									allLinks.add(setLink(node2ID, node3ID));
									
									// loop through properties of GoalsConditions
									for (Map.Entry<String, JsonElement> goalConditionProperty: condition.getAsJsonObject().entrySet()) {
										String propertyID = node3ID+goalConditionProperty.getKey().toString();
										String propertyDesc = goalConditionProperty.getKey().toString()+" : " + goalConditionProperty.getValue().toString();
										String propertyColor = "green";
										String propertySize = nodesizeLVL3;
										
										//add a node "property" with the above properties
										allNodes.add(setNode(propertyID, propertyDesc, propertyColor, propertySize));
										//create the "property" node links
										allLinks.add(setLink(nodeID,propertyID));
									}
									
								}

							}
							else {
								//Set property values
								String propertyID = nodeID+goalProperty.getKey().toString();
								String propertyDesc = goalProperty.getKey().toString()+" : " + goalProperty.getValue().toString();
								String propertyColor = "green";
								String propertySize = nodesizeLVL3;

								//add a node "property" with the above properties
								allNodes.add(setNode(propertyID, propertyDesc, propertyColor, propertySize));
								//create the "property" node links
								allLinks.add(setLink(nodeID,propertyID));

							}
						}
					}//End loop through goals
													
				}//Endif goals
			}//End loop through all elements
			log.info("OUTPUT={}", allNodes.toString());
			log.info("OUTPUT2={}", allLinks.toString());
			outputJSON.add(allNodes);
			outputJSON.add(allLinks);
	        gserver.setString(outputJSON.toString());
		}//End if "KORE"
		
	}//End updateDatapointsbythread
	
	@Override
	protected void shutDownThreadExecutor() throws Exception {
		// TODO Auto-generated method stub
		
	}

}