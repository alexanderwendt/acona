package at.tuwien.ict.acona.cell.cellfunction.codelets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.CellFunctionType;
import at.tuwien.ict.acona.cell.cellfunction.CommVocabulary;
import at.tuwien.ict.acona.cell.cellfunction.ServiceState;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcError;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

public abstract class CellFunctionCodelet extends CellFunctionThreadImpl implements Codelet {

	private final static Logger log = LoggerFactory.getLogger(CellFunctionCodelet.class);

	public final static String SETSTATESERVICENAME = "setstate";
	public final static String REGISTERCODELETSERVICENAME = "registercodelet";
	public final static String DEREGISTERCODELETSERVICENAME = "deregistercodelet";
	public final static String EXECUTECODELETMETHODNAME = "execute";
	public final static String KEYMETHOD = "method";
	public final static String KEYCALLERADDRESS = "calleraddress";
	public final static String KEYEXECUTIONORDERNAME = "executionorder";
	public final static String KEYSTATE = "state";
	private final static int DEFAULTTIMEOUT = 20000;

	public final static String ATTRIBUTECODELETHANDLERADDRESS = "handleraddress";
	public final static String ATTRIBUTEEXECUTIONORDER = "executionorder";
	public final static String ATTRIBUTETIMEOUT = "timeout";

	public final static String ATTRIBUTEWORKINGMEMORYADDRESS = "workingmemoryaddress";
	public final static String ATTRIBUTEINTERNALMEMORYADDRESS = "internalmemoryaddress";

	// private String codeletStateDatapointAddress;

	private String codeletHandlerAgentName = "";
	private String codeletHandlerServiceName = "";
	private String callerAddress = "";
	private int exeutionOrder = 0;
	private int timeout = DEFAULTTIMEOUT;

	private String workingMemoryAddress = "workingmemory";
	private String internalStateMemoryAddress = "internalstatememory";

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		try {
			// Set the caller address
			this.callerAddress = this.getCell().getLocalName() + ":" + this.getFunctionName();

			// Set the system state datapoint
			// codeletStateDatapointAddress = this.getFunctionName() + "." + "state";

			this.setFinishedAfterSingleRun(false); // The finish shall be set manually.

			// Start internal init
			this.cellFunctionCodeletInit();

			// Get the codelethandler data
			this.codeletHandlerAgentName = this.getFunctionConfig().getProperty(ATTRIBUTECODELETHANDLERADDRESS).split(":")[0];
			this.codeletHandlerServiceName = this.getFunctionConfig().getProperty(ATTRIBUTECODELETHANDLERADDRESS).split(":")[1];
			this.exeutionOrder = Integer.valueOf(this.getFunctionConfig().getProperty(KEYEXECUTIONORDERNAME, "0"));
			this.timeout = Integer.valueOf(this.getFunctionConfig().getProperty(ATTRIBUTETIMEOUT, String.valueOf(DEFAULTTIMEOUT)));

			// Register codelet in the codelethandler
			try {
				JsonRpcRequest request = new JsonRpcRequest(REGISTERCODELETSERVICENAME, 2);
				request.setParameterAsValue(0, callerAddress);
				request.setParameterAsValue(1, this.exeutionOrder);

				JsonRpcResponse response = this.getCommunicator().execute(this.codeletHandlerAgentName, this.codeletHandlerServiceName, request, this.timeout);
				updateServiceStateInCodeletHandler(ServiceState.FINISHED);

				// Check the result
				if (response.hasError()) {
					throw new Exception("Cannot register the codelet. Maybe the codelet handler has not been started yet");
				}

				// Get the working memory addresses
				if (response.getResult().getAsJsonObject().has(ATTRIBUTEWORKINGMEMORYADDRESS)) {
					this.setWorkingMemoryAddress(response.getResult().getAsJsonObject().get(ATTRIBUTEWORKINGMEMORYADDRESS).getAsString());
				}

				// Get the internal state memory address
				if (response.getResult().getAsJsonObject().has(ATTRIBUTEINTERNALMEMORYADDRESS)) {
					this.setInternalStateMemoryAddress(response.getResult().getAsJsonObject().get(ATTRIBUTEINTERNALMEMORYADDRESS).getAsString());
				}

			} catch (Exception e) {
				log.error("{}>Cannot register codelet", this.getFunctionName(), e);
				throw new Exception(e.getMessage());
			}
		} catch (Exception e1) {
			log.error("{}>Cannot initialize codelet", this.getFunctionName(), e1);
			throw new Exception(e1.getMessage());
		}

	}

	protected abstract void cellFunctionCodeletInit() throws Exception;

	@Override
	public synchronized JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		JsonRpcResponse result = null;
		// React on the following inputs
		// Attributes: method=startcodelet

		try {
			log.debug("{}>Received execute request={}", this.getFunctionName(), parameterdata);

			result = this.performCodeletOperation(parameterdata, caller);

			if (result == null) {
				switch (parameterdata.getMethod()) {
				case EXECUTECODELETMETHODNAME:
					log.debug("{}>Execute the codelet", this.getFunctionName());
					this.startCodelet();
					result = new JsonRpcResponse(parameterdata, new JsonPrimitive(CommVocabulary.ACKNOWLEDGEVALUE));
					break;
				default:
					throw new Exception(this.getFunctionName() + ">Method name " + parameterdata.getMethod() + " unknown");
				}
			}

		} catch (Exception e) {
			log.warn("Method cannot be found", e);
			result = new JsonRpcResponse(parameterdata, new JsonRpcError("CodeletError", -1, e.getMessage(), e.getLocalizedMessage()));
		}

		return result;
	}

	protected JsonRpcResponse performCodeletOperation(JsonRpcRequest parameterdata, String caller) {
		return null;
	}

	@Override
	public void startCodelet() {
		// Run thread
		this.setStart();

	}

	// @Override
	// protected void executeFunction() throws Exception {
	// // TODO Auto-generated method stub
	//
	// }

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		// Set state to running
		JsonRpcRequest request = new JsonRpcRequest(SETSTATESERVICENAME, 2);
		request.setParameterAsValue(0, callerAddress).setParameterAsValue(1, ServiceState.RUNNING.toString());
		log.debug("Set state running");
		this.getCommunicator().execute(this.codeletHandlerAgentName, this.codeletHandlerServiceName, request, this.timeout);
		this.setServiceState(ServiceState.RUNNING);
	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		// Set state of the codelet to finished
		JsonRpcRequest request = new JsonRpcRequest(SETSTATESERVICENAME, 2);
		log.debug("Set state finished");
		request.setParameterAsValue(0, callerAddress).setParameterAsValue(1, ServiceState.FINISHED.toString());
		this.setServiceState(ServiceState.FINISHED);

		this.getCommunicator().execute(this.codeletHandlerAgentName, this.codeletHandlerServiceName, request, this.timeout);

	}

	private void updateServiceStateInCodeletHandler(ServiceState state) throws Exception {
		JsonRpcRequest request = new JsonRpcRequest(SETSTATESERVICENAME, 2);
		request.setParameterAsValue(0, callerAddress).setParameterAsValue(1, state.toString());

		JsonRpcResponse response = this.getCommunicator().execute(this.codeletHandlerAgentName, this.codeletHandlerServiceName, request, this.timeout);

		if (response.hasError()) {
			throw new Exception("Communication error. Error: " + response.getError());
		}
	}

	@Override
	protected void shutDownExecutor() throws Exception {
		this.shutDownCodelet();

		JsonRpcRequest request = new JsonRpcRequest(DEREGISTERCODELETSERVICENAME, 1);
		request.setParameterAsValue(0, callerAddress);

		JsonRpcResponse response = this.getCommunicator().execute(this.codeletHandlerAgentName, this.codeletHandlerServiceName, request, this.timeout);
		if (response.hasError()) {
			throw new Exception("Communication error. Error: " + response.getError());
		}
	}

	protected void shutDownCodelet() throws Exception {

	}

	// protected String getCodeletStateDatapointAddress() {
	// return codeletStateDatapointAddress;
	// }

	protected String getWorkingMemoryAddress() {
		return workingMemoryAddress;
	}

	protected void setWorkingMemoryAddress(String workingMemoryAddress) {
		this.workingMemoryAddress = workingMemoryAddress;
	}

	protected String getInternalStateMemoryAddress() {
		return internalStateMemoryAddress;
	}

	protected void setInternalStateMemoryAddress(String internalStateMemoryAddress) {
		this.internalStateMemoryAddress = internalStateMemoryAddress;
	}

	@Override
	public CellFunctionType getFunctionType() {
		return CellFunctionType.CODELET;
	}

}
