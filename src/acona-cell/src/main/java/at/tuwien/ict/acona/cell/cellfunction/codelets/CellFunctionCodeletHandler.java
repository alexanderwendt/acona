package at.tuwien.ict.acona.cell.cellfunction.codelets;

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

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.CellFunctionType;
import at.tuwien.ict.acona.cell.cellfunction.CommVocabulary;
import at.tuwien.ict.acona.cell.cellfunction.ServiceState;
import at.tuwien.ict.acona.cell.datastructures.Chunk;
import at.tuwien.ict.acona.cell.datastructures.ChunkBuilder;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.DatapointBuilder;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcError;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

public class CellFunctionCodeletHandler extends CellFunctionThreadImpl implements CodeletHandler {

	private final static Logger log = LoggerFactory.getLogger(CellFunctionCodeletHandler.class);

	public final static String SETSTATESERVICENAME = "setstate";
	public final static String REGISTERCODELETSERVICENAME = "registercodelet";
	public final static String DEREGISTERCODELETSERVICENAME = "deregistercodelet";
	public final static String EXECUTECODELETMETHODNAME = "execute";
	public final static String RESET = "reset";
	public final static String EXECUTECODELETEHANDLER = "executecodelethandler";
	public final static String KEYMETHOD = "method";
	public final static String KEYCALLERADDRESS = "calleraddress";
	public final static String KEYEXECUTIONORDERNAME = "executionorder";
	public final static String KEYISBLOCKING = "blockingmethod";
	public final static String KEYSTATE = "state";

	public final static String ATTRIBUTEWORKINGMEMORYADDRESS = "workingmemoryaddress";
	public final static String ATTRIBUTEINTERNALMEMORYADDRESS = "internalmemoryaddress";

	// private String codeletStateDatapointAddress;
	private String workingMemoryAddress = "workingmemory";
	private String internalStateMemoryAddress = "internalmemoryaddress";

	// private String resultDatapointAddress = "";

	private final static int METHODTIMEOUT = 9000;
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

		this.setFinishedAfterSingleRun(false);
	}

	@Override
	public JsonRpcResponse performOperation(final JsonRpcRequest parameter, final String caller) {
		JsonRpcResponse result = null;

		try {
			String callerAddress = "";
			switch (parameter.getMethod()) {
			case EXECUTECODELETEHANDLER:
				// boolean isBlocking = parameter.getParameter(0, Boolean.class);
				log.debug("Execute the codelet handler");
				this.startCodeletHandler();
				result = new JsonRpcResponse(parameter, new JsonPrimitive(CommVocabulary.ACKNOWLEDGEVALUE));

				break;
			case REGISTERCODELETSERVICENAME:
				callerAddress = parameter.getParameter(0, String.class);
				int executionOrder = parameter.getParameter(1, Integer.class);
				log.debug("Execute the to register a codelet with parameter caller address={} and execution order={}", callerAddress, executionOrder);
				this.registerCodelet(callerAddress, executionOrder);

				JsonObject obj = new JsonObject();
				obj.addProperty(ATTRIBUTEWORKINGMEMORYADDRESS, this.workingMemoryAddress);
				obj.addProperty(ATTRIBUTEINTERNALMEMORYADDRESS, this.internalStateMemoryAddress);
				result = new JsonRpcResponse(parameter, obj);

				break;
			case DEREGISTERCODELETSERVICENAME:
				callerAddress = parameter.getParameter(0, String.class);
				log.debug("Deregister a codelet with parameter caller address={}.", callerAddress);
				this.deregisterCodelet(callerAddress);
				result = new JsonRpcResponse(parameter, new JsonPrimitive(CommVocabulary.ACKNOWLEDGEVALUE));

				break;
			case SETSTATESERVICENAME:
				callerAddress = parameter.getParameter(0, String.class);
				// final String callerX = callerAddress;
				ServiceState state = ServiceState.valueOf(parameter.getParameter(1, String.class));
				log.info("{}>Set new service caller address={}, state={}.", this.getFunctionName(), callerAddress, state);
				// Thread t = new Thread() {
				//
				// @Override
				// public void run() {
				// try {
				// log.info("Set state of function={}, caller={}, state={}", getFunctionName(), callerX, state);
				// setCodeletState(state, callerX);
				// log.info("Codelet states={}", codeletMap);
				// } catch (Exception e) {
				// log.error("Cannot execute the set state");
				// }
				// }
				// };

				// t.setName(getFunctionName() + "Worker" + callerX);
				// t.start();

				this.setCodeletState(state, callerAddress);
				// log.info("Codelet states={}", codeletMap);
				result = new JsonRpcResponse(parameter, new JsonPrimitive(CommVocabulary.ACKNOWLEDGEVALUE));

				break;
			case RESET:
				log.debug("Reset codelet handler from caller={}", caller);
				this.resetCodeletHandler();
				result = new JsonRpcResponse(parameter, new JsonPrimitive(CommVocabulary.ACKNOWLEDGEVALUE));
			default:
				throw new Exception("No such method: " + parameter.getMethod());

			}

		} catch (Exception e) {
			log.error("Cannot execute a method for parameterdata " + parameter, e);
			result = new JsonRpcResponse(parameter, new JsonRpcError("CodeletHandlerError", -1, e.getMessage(), e.getLocalizedMessage()));
		}

		return result;
	}

	@Override
	public void registerCodelet(String callerAddress, int executionOrder) throws Exception {
		this.getCodeletMap().put(callerAddress, ServiceState.INITIALIZING);
		this.putCodeletExecutionOrder(callerAddress, executionOrder);

		log.debug("Codelet={} registered", callerAddress);
	}

	@Override
	public void deregisterCodelet(String codeletName) {
		this.getCodeletMap().remove(codeletName);
		this.removeCodeletExecutionOrder(codeletName);
		log.debug("Codelet={} deregistered", codeletName);

	}

	private boolean updateCodeletHandlerState() {
		boolean executeCodeletHandler = false;

		try {
			// Update the state of the codelet handler
			// Check if all codelets have finished operation
			boolean emptyExecutionMap = this.getExecutionOrderMap().isEmpty();
			if (emptyExecutionMap == false) {
				int currentRunOrder = this.getCurrentRunOrder();
				boolean isRunOrderStateReady = this.isRunOrderStateReady(currentRunOrder);
				int nextRunOrder = this.getNextRunOrderState(currentRunOrder);

				log.debug("startCommandSet={}, runOrderStateReady={}, currentRunOrder={}, nextRunOrder={}, emptyEecutionMap={}", this.startCodeletHandlerCommandSet, isRunOrderStateReady, currentRunOrder, nextRunOrder, emptyExecutionMap);

				if (this.startCodeletHandlerCommandSet == true) {
					if (isRunOrderStateReady == true) {
						if (nextRunOrder == -1) { // All FINISH + next runorder=-1 -> all codelet runs finished
							// Increment runorder
							this.setCurrentRunOrder(nextRunOrder);
							// Set finish state
							this.setServiceState(ServiceState.FINISHED);
							this.startCodeletHandlerCommandSet = false;
							log.debug("Codelet handler finished and has written state finished to datapoint address={}", this.addServiceName(STATESUFFIX));
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
						log.debug("Current command={}, write handerstate={}, isActive={}, allowedToRun={}, startCommandSet={}, newCodeletStartCommand={}.", this.currentCommand, handlerState, this.isActive(), this.isAllowedToRun(), this.isStartCommandIsSet(), this.startCodeletHandlerCommandSet);
						this.writeLocal(handlerState);

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
			log.debug("Codelet={} updated its state to state={}. Run execution={}, codelet states={}", codeletID, state, this.getCurrentRunOrder(), this.getCodeletMap());
		} else {
			log.error("Codelet={} that reported state={} is not registered in this codelet handler", codeletID, state);
			throw new Exception("Codelet not registered");
		}

		// Trigger the codelethandler to start
		this.setStart();
	}

	private Datapoint getExtendedState() throws Exception {
		Datapoint result = DatapointBuilder.newDatapoint(this.addServiceName(this.addServiceName(EXTENDEDSTATESUFFIX)));

		Chunk systemState = null;
		try {
			systemState = ChunkBuilder.newChunk(this.getFunctionName() + "_EXTSTATE", "CODELETHANDLER");
			systemState.setValue("hasState", this.getCurrentState().toString());
			for (Entry<String, ServiceState> entry : this.getCodeletMap().entrySet()) {
				try {
					systemState.addAssociatedContent("hasCodelet", ChunkBuilder.newChunk(entry.getKey(), "CODELET").setValue("hasState", entry.getValue().toString()));
				} catch (Exception e) {
					log.error("Cannot set the associated codelet={}", entry, e);
				}
			}

			// TODO: Create datapoints that can take Chunks and Chunk arrays and Json arrays
			// TODO: The nullpointer chunk shall not need any try-catch

			result.setValue(systemState.toJsonObject());
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
			this.getExecutionOrderMap().get(this.currentRunOrder).forEach((k) -> {
				String agentName = k.split(":")[0];
				String functionName = k.split(":")[1];

				try {
					JsonRpcRequest request = new JsonRpcRequest(EXECUTECODELETMETHODNAME, 0);

					JsonRpcResponse result = this.getCommunicator().execute(agentName, functionName, request, METHODTIMEOUT);
					log.debug("Started codelet={}", agentName + ":" + functionName);
					if (result.hasError()) {
						throw new Exception("Error at the starting of the codelet. " + result.getError());
					}
				} catch (Exception e1) {
					log.error("Cannot execute the execute method in the codelets", e1);
				}
			});
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

	// /**
	// * Check if all codelets are ready
	// *
	// * @return
	// */
	// private boolean isRunOrderStateReady() {
	// boolean result = true;
	// for (ServiceState s : this.getCodeletMap().values()) {
	// if (s.equals(ServiceState.FINISHED) == false) {
	// result = false;
	// break;
	// }
	// }
	//
	// return result;
	// }

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
		this.codeletHandlerRunAllowed = this.updateCodeletHandlerState(); // If state is updated and allowed to run, the thread can run
	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void shutDownExecutor() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void startCodeletHandler() throws Exception {

		this.startCodeletHandlerCommandSet = true;

		this.setExecuteOnce(false);
		this.setExecuteRate(1000);
		this.setStart();
		//
		// // Check if the whole system is ready
		// try {
		// isAllowedToRun = this.isRunOrderStateReady() && this.getCurrentRunOrder() == -1 && this.getExecutionOrderMap().isEmpty() == false; // if all codelets are idle and runorder is reset
		//
		// if (isAllowedToRun == true) {
		// log.debug("All codelets in run {} are in state FINISHED. Start run.", this.getCurrentRunOrder());
		// // Clear the blocker
		//
		// // Increment to start with the first run order
		// int nextRunOrder = this.getNextRunOrderState(this.getCurrentRunOrder());
		// this.setCurrentRunOrder(nextRunOrder);
		//
		// this.setStart();
		//
		// } else {
		// this.getCodeletMap().entrySet().forEach(entry -> {
		// if (entry.getValue().equals(ServiceState.RUNNING)) {
		// log.warn("Codelet={} is still running", entry.getKey());
		// }
		// });
		// log.warn("{}>Not all codelets are ready or no codelets have been registered. Values: runOrderStateReady={}, currentRunOrder={}, Codelet execution orders={}, codelet states={}",
		// this.getFunctionName(), this.isRunOrderStateReady(), this.getCurrentRunOrder(), this.getExecutionOrderMap(), this.getCodeletMap());
		// // Write finish notification
		// this.setServiceState(ServiceState.FINISHED);
		//
		// }
		// } catch (NullPointerException e) {
		// log.error("Method timeout", e);
		// throw new Exception(e.getMessage());
		// } catch (Exception e) {
		// log.error("Cannot execute the codelet handler correctly", e);
		// throw new Exception(e.getMessage());
		// }

	}

	private Map<String, ServiceState> getCodeletMap() {
		return codeletMap;
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
			this.getExecutionOrderMap().get(order).add(name);
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
		Iterator<Entry<Integer, List<String>>> iter = this.getExecutionOrderMap().entrySet().iterator();

		while (iter.hasNext()) {
			Entry<Integer, List<String>> e = iter.next();
			e.getValue().remove(name);
			if (e.getValue().isEmpty()) {
				iter.remove();
				log.debug("Removed codelet={} with execution order={}", name, e.getKey());
			}
		}
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

		Chunk state = null;
		try {
			state = ChunkBuilder.newChunk(this.getFunctionName() + "_State", "CODELETHANDLERSTATE");
			state.setValue("RUNNINGCODELETS", runningCodelets);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return state.toJsonObject();
	}

	@Override
	public CellFunctionType getFunctionType() {
		return CellFunctionType.CODELET;
	}

}
