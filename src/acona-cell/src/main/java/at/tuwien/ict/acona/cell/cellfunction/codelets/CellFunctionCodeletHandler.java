package at.tuwien.ict.acona.cell.cellfunction.codelets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.CellFunctionType;
import at.tuwien.ict.acona.cell.cellfunction.CommVocabulary;
import at.tuwien.ict.acona.cell.cellfunction.ServiceState;
import at.tuwien.ict.acona.cell.datastructures.Chunk;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Datapoints;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcError;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

public class CellFunctionCodeletHandler extends CellFunctionThreadImpl implements CodeletHandler {

	private final static Logger log = LoggerFactory.getLogger(CellFunctionCodeletHandler.class);

	public final static String SETSTATESERVICENAME = "setstate";
	public final static String REGISTERCODELETSERVICENAME = "registercodelet";
	public final static String DEREGISTERCODELETSERVICENAME = "deregistercodelet";
	public final static String EXECUTECODELETMETHODNAME = "execute";
	public final static String EXECUTECODELETEHANDLER = "executecodelethandler";
	public final static String KEYMETHOD = "method";
	public final static String KEYCALLERADDRESS = "calleraddress";
	public final static String KEYEXECUTIONORDERNAME = "executionorder";
	public final static String KEYISBLOCKING = "blockingmethod";
	public final static String KEYSTATE = "state";

	public final static String ATTRIBUTEWORKINGMEMORYADDRESS = "workingmemoryaddress";
	public final static String ATTRIBUTEINTERNALMEMORYADDRESS = "internalmemoryaddress";

	//private String codeletStateDatapointAddress;
	private String workingMemoryAddress = "workingmemory";
	private String internalStateMemoryAddress = "internalmemoryaddress";

	private String resultDatapointAddress = "";

	private final static int METHODTIMEOUT = 20000;
	//private final static int CODELETHANDLERTIMEOUT = 10000;

	private final Map<String, ServiceState> codeletMap = new ConcurrentHashMap<>();
	private final Map<Integer, List<String>> executionOrderMap = new ConcurrentHashMap<>();

	private int currentRunOrder = -1; //-1 for starting mode

	@Override
	protected synchronized void cellFunctionThreadInit() throws Exception {
		//this.resultDatapointAddress = this.getFunctionName() + "." + "result";
		//this.codeletStateDatapointAddress = this.getFunctionName() + "." + "state";
		this.workingMemoryAddress = this.getFunctionConfig().getProperty(ATTRIBUTEWORKINGMEMORYADDRESS, workingMemoryAddress);
		this.internalStateMemoryAddress = this.getFunctionConfig().getProperty(ATTRIBUTEINTERNALMEMORYADDRESS, internalStateMemoryAddress);
	}

	@Override
	public synchronized JsonRpcResponse performOperation(JsonRpcRequest parameter, String caller) {
		JsonRpcResponse result = null;

		try {
			String callerAddress = "";
			switch (parameter.getMethod()) {
			case EXECUTECODELETEHANDLER:
				boolean isBlocking = parameter.getParameter(0, Boolean.class);
				log.debug("Execute the codelet handler");
				this.startCodeletHandler(isBlocking);
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
				ServiceState state = ServiceState.valueOf(parameter.getParameter(1, String.class));
				log.debug("Set new service caller address={}, state={}.", callerAddress, state);
				this.setCodeletState(state, callerAddress);
				result = new JsonRpcResponse(parameter, new JsonPrimitive(CommVocabulary.ACKNOWLEDGEVALUE));

				break;
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
	public synchronized void registerCodelet(String callerAddress, int executionOrder) {
		this.getCodeletMap().put(callerAddress, ServiceState.INITIALIZING);
		this.putCodeletExecutionOrder(callerAddress, executionOrder);

		log.debug("Codelet={} registered", callerAddress);
	}

	@Override
	public synchronized void deregisterCodelet(String codeletName) {
		this.getCodeletMap().remove(codeletName);
		this.removeCodeletExecutionOrder(codeletName);
		log.debug("Codelet={} deregistered", codeletName);

	}

	@Override
	public synchronized void setCodeletState(ServiceState state, String codeletID) throws Exception {
		if (this.getCodeletMap().containsKey(codeletID) == true) {
			this.getCodeletMap().put(codeletID, state);

			//Check if all codelets for a certain run order state are ready
			boolean isRunOrderStateReady = false;
			if (this.getCurrentRunOrder() >= 0) {
				isRunOrderStateReady = this.isRunOrderStateReady(this.getCurrentRunOrder());
			}

			boolean isCurrentRunOrderLast = false;
			//			if (this.getNextRunOrderState(this.getCurrentRunOrder()) == -1) { //Run order has reached the end
			//				isCurrentRunOrderLast = true;
			//			}

			log.debug("Codelet={} updated its state to state={}", codeletID, state);
			log.debug("Current states of codelets={}. Current run execution={}", this.getCodeletMap(), this.getCurrentRunOrder());

			//If the current run order state is ready, increment it
			if (isRunOrderStateReady == true) {
				int nextRunOrder = this.getNextRunOrderState(this.getCurrentRunOrder());
				this.setCurrentRunOrder(nextRunOrder);
				if (this.getCurrentRunOrder() == -1) { //Run order has reached the end
					isCurrentRunOrderLast = true;

					//Write finish notification
					//log.debug("write finished codelet");
					//this.writeLocal(Datapoints.newDatapoint(this.resultDatapointAddress).setValue(CommVocabulary.ACKNOWLEDGEVALUE));
					this.setServiceState(ServiceState.FINISHED);
					log.debug("Codelet handler finished and has written state finished to datapoint address={}", this.addServiceName(STATESUFFIX));
					//this.setServiceState(ServiceState.IDLE);
				} else {
					log.debug("The next codelet run can start");
					this.setStart();
				}
			}

			//Write the current state of the system
			Datapoint handlerState = getExtendedState();
			log.debug("write handerstate={}", handlerState);
			this.writeLocal(handlerState);

		} else {
			throw new Exception("Codelet not registered");
		}
	}

	private Datapoint getExtendedState() throws Exception {
		Datapoint result = Datapoints.newDatapoint(this.addServiceName(this.addServiceName(EXTENDEDSTATESUFFIX)));

		Chunk systemState = null;
		try {
			systemState = Chunk.newChunk(this.getFunctionName() + "_EXTSTATE", "EXTEDNEDSTATE");
			systemState.setValue("hasState", this.getCurrentState().toString());
			for (Entry<String, ServiceState> entry : this.getCodeletMap().entrySet()) {
				try {
					systemState.addAssociatedContent("hasCodelet", Chunk.newChunk(entry.getKey(), "CODELETSTATE").setValue("State", entry.getValue().toString()));
				} catch (Exception e) {
					log.error("Cannot set the associated codelet={}", entry, e);
				}
			}

			//TODO: Create datapoints that can take Chunks and Chunk arrays and Json arrays
			//TODO: The nullpointer chunk shall not need any try-catch

			result.setValue(systemState.toJsonObject());
		} catch (Exception e) {
			log.error("Cannot create the system state", e);
			throw new Exception(e.getMessage());
		}

		return result;
	}

	@Override
	protected void executeFunction() throws Exception {
		//Load all codelets to execute, i.e. only the codelets that shall run in parallel, get the map
		log.debug("Start running the run order={} of total run orders={}", this.getCurrentRunOrder(), this.retrieveExecutionOrder());

		//Check that all codelets are ready (it shall always be true, else there is an error)
		boolean runAllowed = this.isRunOrderStateReady(this.getCurrentRunOrder());

		if (runAllowed == false) {
			log.warn("Not all codelets are ready to run. States={}", this.getCodeletMap());
		} else {
			//For each, send a message to start in parallel
			this.getExecutionOrderMap().get(this.currentRunOrder).forEach((k) -> {
				String agentName = k.split(":")[0];
				String functionName = k.split(":")[1];
				//				List<Datapoint> methodParameters = new ArrayList<>(Arrays.asList(
				//						Datapoints.newDatapoint(KEYMETHOD).setValue(EXECUTECODELETMETHODNAME)));
				try {
					JsonRpcRequest request = new JsonRpcRequest(EXECUTECODELETMETHODNAME, 0);

					JsonRpcResponse result = this.getCommunicator().execute(agentName, functionName, request, METHODTIMEOUT);
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
	 * The if the codelets for a certain run order are ready to execute. Return
	 * true if all codelets are idle.
	 * 
	 * @param runOrder
	 * @return
	 * @throws Exception
	 */
	private boolean isRunOrderStateReady(int runOrder) throws Exception {
		boolean result = true;
		try {
			log.debug("Map:{}", this.getExecutionOrderMap());
			List<String> names = this.getExecutionOrderMap().get(runOrder);
			for (String codelet : names) {
				if (this.getCodeletMap().get(codelet).equals(ServiceState.FINISHED) == false) {
					result = false;
					break;
				}
			}
		} catch (Exception e) {
			log.error("Cannot check if the states are ok for the run for runorder={}", runOrder, e);
			throw new Exception(e.getMessage());
		}

		return result;
	}

	/**
	 * Check if all codelets are ready
	 * 
	 * @return
	 */
	private boolean isRunOrderStateReady() {
		boolean result = true;
		for (ServiceState s : this.getCodeletMap().values()) {
			if (s.equals(ServiceState.FINISHED) == false) {
				result = false;
				break;
			}
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

		if (runOrder < 0) { //Beginning
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

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		// TODO Auto-generated method stub

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
	public synchronized boolean startCodeletHandler(boolean isBlocking) throws Exception {
		boolean isAllowedToRun = false;

		//Check if the whole system is ready
		try {
			isAllowedToRun = this.isRunOrderStateReady() && this.getCurrentRunOrder() == -1 && this.executionOrderMap.isEmpty() == false; //if all codelets are idle and runorder is reset

			if (isAllowedToRun == true) {
				log.debug("All codelets are in state FINISHED");
				//Clear the blocker

				//Increment to start with the first run order
				int nextRunOrder = this.getNextRunOrderState(this.getCurrentRunOrder());
				this.setCurrentRunOrder(nextRunOrder);

				this.setStart();

				//				if (isBlocking) {
				//					try {
				//						this.getBlocker().clear();
				//						boolean runComplete = this.getBlocker().poll(CODELETHANDLERTIMEOUT, TimeUnit.MILLISECONDS);
				//					} catch (InterruptedException e) {
				//
				//					}
				//				}
			} else {
				this.getCodeletMap().entrySet().forEach(entry -> {
					if (entry.getValue().equals(ServiceState.RUNNING)) {
						log.warn("Codelet={} is still running", entry.getKey());
					}
				});
				log.warn("{}>Not all codelets are ready or no codelets have been registered. Codelet states={}", this.getFunctionName(), this.getCodeletMap());
				//Write finish notification
				this.setServiceState(ServiceState.FINISHED);

			}
		} catch (NullPointerException e) {
			log.error("Method timeout", e);
			throw new Exception(e.getMessage());
		} catch (Exception e) {
			log.error("Cannot execute the codelet handler correctly", e);
			throw new Exception(e.getMessage());
		}

		return isAllowedToRun;
	}

	private Map<String, ServiceState> getCodeletMap() {
		return codeletMap;
	}

	/**
	 * Add codelet execution order
	 * 
	 * @param name
	 * @param order
	 */
	private void putCodeletExecutionOrder(String name, int order) {
		if (this.getExecutionOrderMap().containsKey(order)) {
			//Add to existing list
			//synchronized (this.executionOrderMap) {
			this.executionOrderMap.get(order).add(name);
			//}

			log.debug("Add codelet={} to existing order={}", name, order);
		} else {
			//Create new list
			//synchronized (this.executionOrderMap) {
			this.executionOrderMap.put(order, new ArrayList<>(Arrays.asList(name)));
			//}
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
	protected synchronized Map<Integer, List<String>> getExecutionOrderMap() {
		return executionOrderMap;
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
			state = Chunk.newChunk(this.getFunctionName() + "_State", "CODELETHANDLERSTATE");
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
