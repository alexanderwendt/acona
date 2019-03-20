package at.tuwien.ict.acona.mq.core.agentfunction.codelets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import at.tuwien.ict.acona.mq.core.agentfunction.AgentFunctionThreadImpl;
import at.tuwien.ict.acona.mq.core.agentfunction.ServiceState;
import at.tuwien.ict.acona.mq.datastructures.Datapoint;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

public class CodeletHandlerImpl extends AgentFunctionThreadImpl implements CodeletHandler {

	private final static Logger log = LoggerFactory.getLogger(CodeletHandlerImpl.class);

	//Method names, suffixes
	public final static String SETSTATESERVICENAME = "setstate";
	public final static String REGISTERCODELETSERVICENAME = "register";
	public final static String UNREGISTERCODELETSERVICENAME = "unregister";
	public final static String EXECUTECODELETMETHODNAME = "execute";
	public final static String RESET = "reset";
	//public final static String EXECUTECODELETEHANDLER = "executecodelethandler";
	
	
	//public final static String KEYMETHOD = "method";
	//public final static String KEYCALLERADDRESS = "calleraddress";
	//public final static String KEYEXECUTIONORDERNAME = "executionorder";
	//public final static String KEYISBLOCKING = "blockingmethod";
	//public final static String KEYSTATE = "state";

	public final static String ATTRIBUTEWORKINGMEMORYADDRESS = "workingmemoryaddress";
	public final static String ATTRIBUTEINTERNALMEMORYADDRESS = "internalmemoryaddress";

	// private String codeletStateDatapointAddress;
	private String workingMemoryAddress = "workingmemory";
	private String internalStateMemoryAddress = "internalmemoryaddress";

	// private String resultDatapointAddress = "";

	private final static int METHODTIMEOUT = 19000;
	// private final static int CODELETHANDLERTIMEOUT = 10000;

	// For your needs, use ConcurrentHashMap. It allows concurrent modification of the Map from several
	// threads without the need to block them. Collections.synchronizedMap(map) creates a blocking Map
	// which will degrade performance, albeit ensure consistency (if used properly).
	// Use the second option if you need to ensure data consistency, and each thread needs to have an
	// up-to-date view of the map. Use the first if performance is critical, and each thread only
	// inserts data to the map, with reads happening less frequently.

	private final Map<String, ServiceState> codeletMap = new ConcurrentHashMap<>();

	private final Map<Integer, List<String>> executionOrderMap = new ConcurrentSkipListMap<>(new Comparator<Integer>() {

		@Override
		public int compare(Integer o1, Integer o2) {
			return o1.compareTo(o2);
		}
	});

	private int currentRunOrder = -1; // -1 for starting mode

	private boolean codeletHandlerRunAllowed = false;
	private boolean startCodeletHandlerCommandSet = false;

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		// this.resultDatapointAddress = this.getFunctionName() + "." + "result";
		// this.codeletStateDatapointAddress = this.getFunctionName() + "." + "state";
		this.workingMemoryAddress = this.getFunctionConfig().getProperty(ATTRIBUTEWORKINGMEMORYADDRESS, workingMemoryAddress);
		this.internalStateMemoryAddress = this.getFunctionConfig().getProperty(ATTRIBUTEINTERNALMEMORYADDRESS, internalStateMemoryAddress);

		this.setExecuteOnce(true);
		this.setFinishedAfterSingleRun(false);
		
		
		// Add subfunctions
		this.addRequestHandlerFunction(EXECUTECODELETMETHODNAME, (Request input) -> executeCodeletHandler(input));
		this.addRequestHandlerFunction(SETSTATESERVICENAME, (Request input) -> setServiceState(input));
		this.addRequestHandlerFunction(REGISTERCODELETSERVICENAME, (Request input) -> registerCodelet(input));
		this.addRequestHandlerFunction(UNREGISTERCODELETSERVICENAME, (Request input) -> unregisterCodelet(input));
		this.addRequestHandlerFunction(RESET, (Request input) -> reset(input));
		
	}
	
	/**
	 * Start the execution of codelets
	 * 
	 * @param req
	 * @return null as the execution just started and does not finish until the codelets have finished
	 */
	private Response executeCodeletHandler(Request req) {
		Response result = null;
		
		log.debug("Execute the codelet handler");
		try {
			this.startCodeletHandler();
		} catch (Exception e) {
			log.error("Cannot start codelet handler", e);
			result = new Response(req);
			result.setError(e.getMessage());
		}
		
		return result;
	}
	
	/**
	 * Register a codelet
	 * 
	 * @param req
	 * @return
	 */
	private Response registerCodelet(Request req) {
		Response result = new Response(req);
		try {
			String callerAddress = req.getParameter("caller", String.class); //parameter.getParameter(0, String.class);
			int executionOrder = req.getParameter("order", Integer.class); //parameter.getParameter(1, Integer.class);
			log.debug("Execute the to register a codelet with parameter caller address={} and execution order={}", callerAddress, executionOrder);
		
			this.registerCodelet(callerAddress, executionOrder);
			
			JsonObject obj = new JsonObject();
			obj.addProperty(ATTRIBUTEWORKINGMEMORYADDRESS, this.workingMemoryAddress);
			obj.addProperty(ATTRIBUTEINTERNALMEMORYADDRESS, this.internalStateMemoryAddress);
			  //JsonRpcResponse(parameter, obj);
			result.setResult(obj);
		} catch (Exception e) {
			log.error("Cannot register codelet", e);
			result.setError(e.getMessage());
		}
		
		return result;
	}
	
	/**
	 * Unregister codelet
	 * 
	 * @param req
	 * @return
	 */
	private Response unregisterCodelet(Request req) {
		Response result = new Response(req); //JsonRpcResponse(parameter, new JsonPrimitive(CommVocabulary.ACKNOWLEDGEVALUE));
		
		try {
			String callerAddress = req.getParameter("caller", String.class);
			
			log.debug("Unregister a codelet with parameter caller address={}.", callerAddress);
			this.deregisterCodelet(callerAddress);
			
			result.setResultOK();
			
		} catch (Exception e) {
			log.error("Codelet could not unregister.", e);
			result.setError(e.getMessage());
		} //parameter.getParameter(0, String.class);
		
		
		return result;
	}
	
	private Response setServiceState(Request req) {
		Response result = new Response(req); //new JsonRpcResponse(parameter, new JsonPrimitive(CommVocabulary.ACKNOWLEDGEVALUE));
		
		try {
			String callerAddress = req.getParameter("caller", String.class); //parameter.getParameter(0, String.class);
			ServiceState state = req.getParameter("state", ServiceState.class);
			log.info("{}>Set new service caller address={}, state={}.", this.getFunctionName(), callerAddress, state);

			
		
		
			this.setCodeletState(state, callerAddress);
			
			result.setResultOK();
		} catch (Exception e) {
			log.error("Codelet state could not be set", e);
			result.setError(e.getMessage());
		}
		
		
		return result;
	}
	
	private Response reset(Request req) {
		String callerAddress;
		Response result = new Response(req);
		try {
			callerAddress = req.getParameter("caller", String.class);
			
			log.debug("Reset codelet handler from caller={}", callerAddress);
			this.resetCodeletHandler();
			result.setResultOK();
			
		} catch (Exception e) {
			log.error("Cannot reset", e);
			result.setError(e.getMessage());
		} //parameter.getParameter(0, String.class);
		
		
		return result;
	}

	@Override
	public void registerCodelet(String callerAddress, int executionOrder) throws Exception {
		synchronized (this) {
			this.getCodeletMap().put(callerAddress, ServiceState.INITIALIZING);
			this.putCodeletExecutionOrder(callerAddress, executionOrder);
			
			log.debug("Codelet={} registered", callerAddress);
		}
	}

	@Override
	public void deregisterCodelet(String codeletName) {
		synchronized (this) {
			this.removeCodeletExecutionOrder(codeletName);
			this.getCodeletMap().remove(codeletName);
			log.debug("Codelet={} deregistered", codeletName);
		}
	}

	private synchronized boolean updateCodeletHandlerState() {
		boolean executeCodeletHandler = false;

		try {
			// Update the state of the codelet handler
			// Check if all codelets have finished operation
			boolean emptyExecutionMap = this.getExecutionOrderMap().isEmpty();
			if (emptyExecutionMap == false) {
				int currentRunOrder = this.getCurrentRunOrder();
				boolean isRunOrderStateReady = this.isRunOrderStateReady(currentRunOrder);
				int nextRunOrder = this.getNextRunOrderState(currentRunOrder);

				log.debug("startCommandSet={}, runOrderStateReady={}, currentRunOrder={}, nextRunOrder={}, emptyEecutionMap={}, executeOnce={}", this.startCodeletHandlerCommandSet, isRunOrderStateReady, currentRunOrder, nextRunOrder, emptyExecutionMap, this.isExecuteOnce());

				if (this.startCodeletHandlerCommandSet == true) {
					if (isRunOrderStateReady == true) {
						if (nextRunOrder == -1) { // All FINISH + next runorder=-1 -> all codelet runs finished
							// Increment runorder
							this.setCurrentRunOrder(nextRunOrder);
							// Set finish state
							this.setServiceState(ServiceState.FINISHED);
							//Return to caller. DO NOT FORGET THIS. ElSE THE CALLER IS BLOCKED
							this.closeOpenRequestWithOK(true);
							
							this.startCodeletHandlerCommandSet = false;
							log.debug("Codelet handler finished and has written state finished to datapoint address={}", this.enhanceWithRootAddress(STATESUFFIX));
						} else if (nextRunOrder >= 0) { // All FINISH + runOrder>=0 -> run next order
							// Increment run order
							this.setCurrentRunOrder(nextRunOrder);
							// Start the codelet handler for the next run
							executeCodeletHandler = true;
							log.debug("{}>The codelet run={} can start. Current command={}. State of codelets={}", this.getFunctionName(), nextRunOrder, this.currentCommand, this.codeletMap);
						} else {
							throw new Exception("Illegal runorder " + nextRunOrder);
						}

						Datapoint handlerState = getExtendedState();
						log.debug("Current command={}, publish handerstate={}, isActive={}, allowedToRun={}, startCommandSet={}, newCodeletStartCommand={}.", this.currentCommand, handlerState, this.isActive(), this.isAllowedToRun(), this.isStartCommandIsSet(), this.startCodeletHandlerCommandSet);
						this.getCommunicator().write(handlerState);

					} else {
						StringBuilder runningCodelets = new StringBuilder();
						this.getCodeletMap().entrySet().forEach(entry -> {
							if (entry.getValue().equals(ServiceState.RUNNING)) {
								runningCodelets.append(entry.getKey() + ", ");
							}
						});

						log.debug("{}>codelets are running: {}. No change of state.", this.getFunctionName(), runningCodelets.toString());
					}
				}
			} else {
				this.startCodeletHandlerCommandSet = false;
				if (this.startCodeletHandlerCommandSet == true) {
					log.warn("{}>No codelets registered. No execution.", this.getFunctionName());
				}

				this.setServiceState(ServiceState.FINISHED);
				//Return to caller. DO NOT FORGET THIS. ElSE THE CALLER IS BLOCKED
				this.closeOpenRequestWithOK(true);
			}

			if (this.getCurrentState().equals(ServiceState.FINISHED)) {
				this.setExecuteOnce(true);
				this.setExecuteRate(1000);
			}

		} catch (Exception e) {
			log.error("Cannot update state", e);
		}

		return executeCodeletHandler;
	}

	@Override
	public void setCodeletState(ServiceState state, String codeletID) throws Exception {
		if (this.getCodeletMap().containsKey(codeletID) == true) {
			this.getCodeletMap().put(codeletID, state);
			log.debug("Codelet={} updated its state to state={}. Run execution={}, \ncodelet states={}\ncodelets per run execution: {}", codeletID, state, this.getCurrentRunOrder(), this.getCodeletMap(), executionOrderMap);
		} else {
			log.error("Codelet={} that reported state={} is not registered in this codelet handler", codeletID, state);
			throw new Exception("Codelet not registered");
		}

		// Trigger the codelethandler to start
		this.setStart();
	}

	private Datapoint getExtendedState() throws Exception {
		Datapoint result = this.getDatapointBuilder().newDatapoint(this.enhanceWithRootAddress(EXTENDEDSTATESUFFIX));

		JsonObject systemState = new JsonObject();
		try {
			systemState.addProperty("id", this.getFunctionName() + "_EXTSTATE");
			systemState.addProperty("state", this.getCurrentState().toString());
			systemState.add("hasCodelets", new JsonArray());
			
			for (Entry<String, ServiceState> entry : this.getCodeletMap().entrySet()) {
				try {
					JsonObject subcodeletObject = new JsonObject();
					subcodeletObject.addProperty("name", entry.getKey());
					subcodeletObject.addProperty("state", entry.getValue().toString());
					systemState.get("hasCodelets").getAsJsonArray().add(subcodeletObject);
					
					//systemState.add(property, value);.addAssociatedContent("hasCodelet", ChunkBuilder.newChunk(entry.getKey(), "CODELET").setValue("hasState", entry.getValue().toString()));
				} catch (Exception e) {
					log.error("Cannot set the associated codelet={}", entry, e);
				}
			}

			// TODO: Create datapoints that can take Chunks and Chunk arrays and Json arrays
			// TODO: The nullpointer chunk shall not need any try-catch

			result.setValue(systemState);
		} catch (Exception e) {
			log.error("Cannot create the system state", e);
			throw new Exception(e.getMessage());
		}

		return result;
	}

	@Override
	protected void executeFunction() throws Exception {
		// Load all codelets to execute, i.e. only the codelets that shall run in parallel, get the map

		if (this.codeletHandlerRunAllowed == false) {
			log.debug("Run not allowed. States={}", this.getCodeletMap());
		} else {
			log.debug("Current run order={} of total run orders={}. Run codelets.", this.getCurrentRunOrder(), this.retrieveExecutionOrder());
			// For each, send a message to start in parallel
			int runorder = this.getCurrentRunOrder();
			try {
				//synchronized (this.getExecutionOrderMap()) {
				List<String> runList = new ArrayList<String>(this.getRunList(runorder));
					runList.forEach((k) -> {
						//String agentName = k.split(":")[0];
						//String functionName = k.split(":")[1];

						try {
							Request request = (new Request());//new Request(EXECUTECODELETMETHODNAME, 0);

							Response result = this.getCommunicator().execute(k + "/" + EXECUTECODELETMETHODNAME, request, METHODTIMEOUT);
							log.debug("Started codelet={}", k);
							if (result.hasError()) {
								throw new Exception("Error at the starting of the codelet. " + result.getError());
							}
						} catch (Exception e1) {
							log.error("Cannot execute the execute method in the codelets", e1);
						}
					});
				//}
			} catch (Exception e) {
				log.error("Execution error. Execution order map={}. Runorder={}", this.getExecutionOrderMap(), runorder, e);
				throw new Exception(e.getMessage());
			}
		}
	}

	/**
	 * The if the codelets for a certain run order are ready to execute. Return true if all codelets are idle.
	 * 
	 * @param runOrder
	 * @return
	 * @throws Exception
	 */
	private boolean isRunOrderStateReady(int runOrder) throws Exception {
		boolean result = true;
		try {
			// log.debug("ExecutionOrders:{}", this.getExecutionOrderMap());
			if (runOrder >= 0) {
				List<String> names = this.getExecutionOrderMap().get(runOrder);
				if (names == null) {
					log.error("For runorder={}, names={}, there are no codelets registered. This is a bug. Registered codelets={}. ExecutionOrderMap={}", runOrder, names, this.codeletMap, this.getExecutionOrderMap());
					result = false;
				} else {
					for (String codelet : names) {
						if (this.getCodeletMap().get(codelet).equals(ServiceState.FINISHED) == false) {
							result = false;
							break;
						}
					}
				}
			}

		} catch (Exception e) {
			log.error("Cannot check if the states are ok for the run for runorder={}. Execution order map={}.", runOrder, this.getExecutionOrderMap(), e);
			throw new Exception(e.getMessage());
		}

		return result;
	}

	/**
	 * Get the next run order number
	 * 
	 * @param runOrder
	 * @return
	 */
	private int getNextRunOrderState(int runOrder) {
		int nextValue = -1;

		if (runOrder < 0) { // Beginning
			nextValue = this.retrieveExecutionOrder().get(0);
		} else {
			int currentIndex = this.retrieveExecutionOrder().indexOf(runOrder);
			if (currentIndex + 1 < this.retrieveExecutionOrder().size()) {
				nextValue = this.retrieveExecutionOrder().get(currentIndex + 1);
			} else {
				nextValue = -1;
			}
		}

		return nextValue;
	}

	private void resetCodeletHandler() {
		log.warn("Codelet handler reset. Current states of codelets={}", this.codeletMap);
	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		// Check the state of the codelet handler
		// Check that all codelets are ready (it shall always be true, else there is an error)
		try {
			this.codeletHandlerRunAllowed = this.updateCodeletHandlerState(); // If state is updated and allowed to run, the thread can run
		} catch (Exception e) {
			log.error("Cannot check if run is allowed", e);
		}
		
	}

	@Override
	protected void shutDownThreadExecutor() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void startCodeletHandler() throws Exception {

		this.startCodeletHandlerCommandSet = true;

		//Executeonce is false to check if the codelets are alive
		this.setExecuteOnce(false);
		this.setExecuteRate(1000);
		this.setStart();
	}

	private Map<String, ServiceState> getCodeletMap() {
		return codeletMap;
	}
	
	private List<String> getRunList(int runOrder) {
		return Collections.synchronizedList(this.executionOrderMap.get(runOrder));
	}

	/**
	 * Add codelet execution order
	 * 
	 * @param name
	 * @param order
	 * @throws Exception
	 */
	private void putCodeletExecutionOrder(String name, int order) throws Exception {
		if (order < 0) {
			throw new Exception("Execution order must be > 0. Current execution order=" + order);
		}

		if (this.getExecutionOrderMap().containsKey(order)) {
			// Add to existing list
			// synchronized (this.executionOrderMap) {
			//this.getExecutionOrderMap().get(order).add(name);
			this.getRunList(order).add(name);
			// }

			log.debug("Add codelet={} to existing order={}", name, order);
		} else {
			// Create new list
			// synchronized (this.executionOrderMap) {
			this.executionOrderMap.put(order, new ArrayList<>(Arrays.asList(name)));
			// }
			log.debug("Create new order for codelet={} with order={}", name, order);
		}
	}

	/**
	 * Remove codelet execution order
	 * 
	 * @param name
	 */
	private void removeCodeletExecutionOrder(String name) {
		//Search all places, where this codelet exists and remove it from there
		List<Integer> keyList = new ArrayList<Integer>();
		for (Entry<Integer, List<String>> e : this.executionOrderMap.entrySet()) {
			while (this.getRunList(e.getKey()).contains(name)) {
				this.getRunList(e.getKey()).remove(name);
			}
			
			if (e.getValue().isEmpty()) {
				keyList.add(e.getKey());
				if (this.getCurrentRunOrder()==e.getKey()) {
					int nextRunOrder = this.getNextRunOrderState(this.getCurrentRunOrder());
					this.setCurrentRunOrder(nextRunOrder);
				}
				
				log.debug("Removed codelet={} with execution order={}", name, e.getKey());
			}
		}
		
		for (Integer i : keyList) {
			this.executionOrderMap.remove(i);
		}
		
		//synchronized (this.executionOrderMap) {
//			Iterator<Entry<Integer, List<String>>> iter = this.executionOrderMap.entrySet().iterator();
//
//			while (iter.hasNext()) {
//				Entry<Integer, List<String>> e = iter.next();
//				e.getValue().remove(name);
//				// log.warn("Removeale={}", e);
//				if (e.getValue().isEmpty()) {
//					iter.remove();
//					this.executionOrderMap.remove(e.getKey());
//					int nextRunOrder = this.getNextRunOrderState(this.getCurrentRunOrder());
//					this.setCurrentRunOrder(nextRunOrder);
//					log.debug("Removed codelet={} with execution order={}", name, e.getKey());
//				}
//			}

		log.debug("Remaining codelets={}", this.executionOrderMap);
		//}
	}

	/**
	 * Retrieve a sorted list of execution orders
	 * 
	 * @return
	 */
	private List<Integer> retrieveExecutionOrder() {
		List<Integer> result = new ArrayList<>(this.getExecutionOrderMap().keySet());

		return result;
	}

	/**
	 * Get execution order map
	 * 
	 * @return
	 */
	protected Map<Integer, List<String>> getExecutionOrderMap() {
		return Collections.unmodifiableMap(executionOrderMap);
	}

	/**
	 * Get current run order
	 * 
	 * @return
	 */
	private int getCurrentRunOrder() {
		return this.currentRunOrder;
	}

	/**
	 * set current run order
	 * 
	 * @param currentRunOrder
	 */
	private void setCurrentRunOrder(int currentRunOrder) {
		this.currentRunOrder = currentRunOrder;
	}

	@Override
	public JsonObject getCodeletHandlerState() {
		String runningCodelets = this.getExecutionOrderMap().get(this.getCurrentRunOrder()).toString();

		JsonObject state = new JsonObject();
		try {
			state.addProperty("id", this.getFunctionName() + "_State");
			state.addProperty("runningcodelets", runningCodelets);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return state;
	}

	@Override
	protected void updateCustomDatapointsById(String id, JsonElement data) {
		// TODO Auto-generated method stub
		
	}

}
