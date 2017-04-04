package at.tuwien.ict.acona.cell.cellfunction.codelets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.CommVocabulary;
import at.tuwien.ict.acona.cell.cellfunction.ServiceState;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class CellFunctionCodeletHandler extends CellFunctionThreadImpl implements CodeletHandler {

	private final static Logger log = LoggerFactory.getLogger(CellFunctionCodeletHandler.class);

	private final static String SETSTATESERVICENAME = "setstate";
	private final static String REGISTERCODELETSERVICENAME = "registercodelet";
	private final static String DEREGISTERCODELETSERVICENAME = "deregistercodelet";
	private final static String EXECUTECODELETMETHODNAME = "execute";
	private final static String EXECUTECODELETEHANDLER = "executecodelethandler";
	private final static String KEYMETHOD = "method";
	private final static String KEYCALLERADDRESS = "calleraddress";
	private final static String KEYEXECUTIONORDERNAME = "executionorder";
	private final static String KEYNOTIFICATIONADDRESS = "notificationaddress";
	private final static String KEYSTATE = "state";
	private final static int METHODTIMEOUT = 1000;

	private String notificationDatapointAddress = "";

	private final Map<String, ServiceState> codeletMap = new ConcurrentHashMap<>();
	private final Map<Integer, List<String>> executionOrderMap = new TreeMap<>();

	private int currentRunOrder = -1; //-1 for starting mode

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Datapoint> performOperation(Map<String, Datapoint> parameterdata, String caller) {
		List<Datapoint> result = new ArrayList<>();

		try {
			if (parameterdata.containsKey(KEYMETHOD) && parameterdata.get(KEYMETHOD).getValueAsString().equals(EXECUTECODELETEHANDLER)) {
				String notificationAddress = parameterdata.get(KEYNOTIFICATIONADDRESS).getValueAsString();
				log.debug("Execute the codelet handler");
				this.startCodeletHandlerBlocking(notificationAddress);
				result.add(Datapoint.newDatapoint(CommVocabulary.PARAMETERRESULTADDRESS).setValue(CommVocabulary.ACKNOWLEDGEVALUE));

			} else if (parameterdata.containsKey(KEYMETHOD) && parameterdata.get(KEYMETHOD).getValueAsString().equals(REGISTERCODELETSERVICENAME)) {
				String callerAddress = parameterdata.get(KEYCALLERADDRESS).getValueAsString();
				int executionOrder = parameterdata.get(KEYEXECUTIONORDERNAME).getValue().getAsInt();
				log.debug("Execute the to register a codelet with parameter caller address={} and execution order={}", callerAddress, executionOrder);
				this.registerCodelet(callerAddress, executionOrder);
				result.add(Datapoint.newDatapoint(CommVocabulary.PARAMETERRESULTADDRESS).setValue(CommVocabulary.ACKNOWLEDGEVALUE));

			} else if (parameterdata.containsKey(KEYMETHOD) && parameterdata.get(KEYMETHOD).getValueAsString().equals(DEREGISTERCODELETSERVICENAME)) {
				String callerAddress = parameterdata.get(KEYCALLERADDRESS).getValueAsString();
				log.debug("Deregister a codelet with parameter caller address={}.", callerAddress);
				this.deregisterCodelet(callerAddress);
				result.add(Datapoint.newDatapoint(CommVocabulary.PARAMETERRESULTADDRESS).setValue(CommVocabulary.ACKNOWLEDGEVALUE));

			} else if (parameterdata.containsKey(KEYMETHOD) && parameterdata.get(KEYMETHOD).getValueAsString().equals(SETSTATESERVICENAME)) {
				String callerAddress = parameterdata.get(KEYCALLERADDRESS).getValueAsString();
				ServiceState state = ServiceState.valueOf(parameterdata.get(KEYSTATE).getValueAsString());
				log.debug("Set new service caller address={}, state={}.", callerAddress, state);
				this.setCodeletState(state, callerAddress);
				result.add(Datapoint.newDatapoint(CommVocabulary.PARAMETERRESULTADDRESS).setValue(CommVocabulary.ACKNOWLEDGEVALUE));

			} else {
				log.warn("No valid method. Method sent={}", parameterdata.get(KEYMETHOD));
				result.add(Datapoint.newDatapoint(CommVocabulary.PARAMETERRESULTADDRESS).setValue(CommVocabulary.ERRORVALUE));
			}

		} catch (Exception e) {
			log.error("Cannot execute a method for parameterdata " + parameterdata, e);
			result.add(Datapoint.newDatapoint(CommVocabulary.PARAMETERRESULTADDRESS).setValue(CommVocabulary.ERRORVALUE));
		}

		return result;
	}

	@Override
	public void registerCodelet(String callerAddress, int executionOrder) {
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

	@Override
	public void setCodeletState(ServiceState state, String codeletID) throws Exception {
		if (this.getCodeletMap().containsKey(codeletID) == true) {
			this.getCodeletMap().put(codeletID, state);

			boolean isRunOrderStateReady = false;
			if (this.currentRunOrder >= 0) {
				isRunOrderStateReady = this.isRunOrderStateReady(this.currentRunOrder);
			}
			boolean isCurrentRunOrderLast = false;
			if (this.getNextRunOrderState(currentRunOrder) == -1) { //Run order has reached the end
				isCurrentRunOrderLast = true;
				this.currentRunOrder = this.getNextRunOrderState(currentRunOrder);

			}

			log.debug("Codelet={} updated its state to state={}", codeletID, state);
			if ((isRunOrderStateReady == true) && (isCurrentRunOrderLast == false)) {
				log.debug("The next codelet run can start");
				this.setStart();
			}

		} else {
			throw new Exception("Codelet not registered");
		}
	}

	@Override
	protected void executeFunction() throws Exception {
		//Load all codelets to execute, i.e. only the codelets that shall run in parallel, get the map
		//Get the next runOrder by incrementing the current one
		this.currentRunOrder = this.getNextRunOrderState(this.currentRunOrder);
		log.debug("Start running the run order={}", this.currentRunOrder);

		//Check that all codelets are ready (it shall always be true, else there is an error)
		boolean runAllowed = this.isRunOrderStateReady(this.currentRunOrder);

		if (runAllowed == false) {
			log.warn("Not all codelets are ready to run. States={}", this.getCodeletMap());
		}
		//Check their states. Only if all are idle, the process can start. It means that this process is still running and the
		//system has to wait
		//		for (Entry<String, ServiceState> e : this.codeletMap.entrySet()) {
		//			if (e.getValue().equals(ServiceState.IDLE) == false) {
		//				log.warn("All codelets are not finished yet. {} is still not idle", e);
		//				runAllowed = false;
		//			}
		//		}

		//Execute all codelets in a sequence.
		//All codelets of an execution order have to be finished for the next order to start
		//this.retrieveExecutionOrder()

		if (runAllowed == true) {
			//For each, send a message to start in parallel
			this.getExecutionOrderMap().get(this.currentRunOrder).forEach((k) -> {
				String agentName = k.split(":")[0];
				String functionName = k.split(":")[1];
				List<Datapoint> methodParameters = new ArrayList<>(Arrays.asList(
						Datapoint.newDatapoint(KEYMETHOD).setValue(EXECUTECODELETMETHODNAME)));
				try {
					List<Datapoint> result = this.getCommunicator().execute(agentName, functionName, methodParameters, METHODTIMEOUT);
					if (result.get(0).getValueAsString().equals(CommVocabulary.ERRORVALUE)) {
						throw new Exception("Error at the starting of the codelet. " + result);
					}
				} catch (Exception e1) {
					log.error("Cannot execute the execute method in the codelets", e1);
				}
			});
		}

		//Set function state to running. Use a timeout for how long the codelets may work. No new requests can be made

		//When the codelets are finished, they set the new state. The process is finished, when all codelets are in the state idle.

		//Notify the caller on the provided datapoint that the codelets are finished

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
				if (this.getCodeletMap().get(codelet).equals(ServiceState.IDLE) == false) {
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
			if (s.equals(ServiceState.IDLE) == false) {
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
	protected void executePostProcessing() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void executePreProcessing() throws Exception {
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
	public boolean startCodeletHandlerBlocking(String notificationDatapointAddress) {
		//Check if the whole system is ready
		boolean isAllowedToRun = this.isRunOrderStateReady() && this.currentRunOrder == -1; //if all codelets are idle and runorder is reset

		if (isAllowedToRun == true) {
			log.debug("All codelets are in state IDLE");
			this.setStart();

		} else {
			log.warn("Not all codelets are ready. Codelet states={}", this.getCodeletMap());

		}

		return isAllowedToRun;
	}

	private Map<String, ServiceState> getCodeletMap() {
		return codeletMap;
	}

	private void putCodeletExecutionOrder(String name, int order) {
		if (this.getExecutionOrderMap().containsKey(order)) {
			//Add to existing list
			synchronized (this.executionOrderMap) {
				this.executionOrderMap.get(order).add(name);
			}

			log.debug("Add codelet={} to existing order={}", name, order);
		} else {
			//Create new list
			synchronized (this.executionOrderMap) {
				this.executionOrderMap.put(order, new ArrayList<>(Arrays.asList(name)));
			}
			log.debug("Create new order for codelet={} with order={}", name, order);
		}
	}

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

	private List<Integer> retrieveExecutionOrder() {
		List<Integer> result = new ArrayList<>(this.getExecutionOrderMap().keySet());
		return result;
	}

	protected synchronized Map<Integer, List<String>> getExecutionOrderMap() {
		return executionOrderMap;
	}

	protected int getCurrentRunOrder() {
		return currentRunOrder;
	}

	protected void setCurrentRunOrder(int currentRunOrder) {
		this.currentRunOrder = currentRunOrder;
	}

}
