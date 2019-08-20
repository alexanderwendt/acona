package at.tuwien.ict.acona.mq.core.agentfunction.codelets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.mq.core.agentfunction.AgentFunctionThreadImpl;
import at.tuwien.ict.acona.mq.core.agentfunction.ServiceState;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

public abstract class CodeletImpl extends AgentFunctionThreadImpl implements Codelet {

	private final static Logger log = LoggerFactory.getLogger(CodeletImpl.class);

	public final static String EXECUTECODELETNAME = "execute";
	public final static String KEYEXECUTIONORDERNAME = "executionorder";
	private final static int DEFAULTTIMEOUT = 20000;

	public final static String ATTRIBUTECODELETHANDLERADDRESS = "handleraddress";
	public final static String ATTRIBUTEEXECUTIONORDER = "executionorder";
	public final static String ATTRIBUTETIMEOUT = "timeout";
	public final static String ATTRIBUTEDOREGISTER = "doregister";

	private String codeletHandlerAddress = "";
	private String callerAddress = "";
	private int exeutionOrder = 0;
	private boolean doRegister = true;
	private int timeout = DEFAULTTIMEOUT;

	private String workingMemoryAddress = "workingmemory";
	private String internalStateMemoryAddress = "internalstatememory";

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		try {
			// Set the caller address
			this.callerAddress = this.getFunctionRootAddress();

			this.setFinishedAfterSingleRun(false); // The finish shall be set manually.

			// Start internal init
			this.cellFunctionCodeletInit();

			// Get the codelethandler data
			// Get codelet handler address. It shall be used if do register is true
			this.codeletHandlerAddress = this.getFunctionConfig().getProperty(ATTRIBUTECODELETHANDLERADDRESS, "");
			// Get execution order for the codelet.
			this.exeutionOrder = Integer.valueOf(this.getFunctionConfig().getProperty(KEYEXECUTIONORDERNAME, "0"));
			// Register the codelet in a codelet handler
			this.doRegister = Boolean.valueOf(this.getFunctionConfig().getProperty(ATTRIBUTEDOREGISTER, "true"));
			// Timeout for execution
			this.timeout = Integer.valueOf(this.getFunctionConfig().getProperty(ATTRIBUTETIMEOUT, String.valueOf(DEFAULTTIMEOUT)));
			
			// Add subfunctions
			this.addRequestHandlerFunction(EXECUTECODELETNAME, (Request input) -> executeCodelet(input));

			// Register codelet in the codelethandler
			if (this.doRegister==true) {
				try {
					Request request = new Request();
					request.setParameter("caller", callerAddress);
					request.setParameter("order", this.exeutionOrder);

					Response response = this.getCommunicator().execute(this.codeletHandlerAddress + "/" + CodeletHandlerImpl.REGISTERCODELETSERVICENAME, request, this.timeout);
					updateServiceStateInCodeletHandler(ServiceState.FINISHED);

					// Check the result
					if (response.hasError()) {
						throw new Exception("Cannot register the codelet. Maybe the codelet handler has not been started yet");
					}

					// Get the working memory addresses
					if (response.getResult().getAsJsonObject().has(CodeletHandlerImpl.PARAMWORKINGMEMORYADDRESS)) {
						this.setWorkingMemoryAddress(response.getResult().getAsJsonObject().get(CodeletHandlerImpl.PARAMWORKINGMEMORYADDRESS).getAsString());
					}

					// Get the internal state memory address
					if (response.getResult().getAsJsonObject().has(CodeletHandlerImpl.PARAMINTERNALMEMORYADDRESS)) {
						this.setInternalStateMemoryAddress(response.getResult().getAsJsonObject().get(CodeletHandlerImpl.PARAMINTERNALMEMORYADDRESS).getAsString());
					}
					
					log.debug("{}>Registered in codelet handler {}", this.getFunctionName(), this.codeletHandlerAddress);

				} catch (Exception e) {
					log.error("{}>Cannot register codelet", this.getFunctionName(), e);
					throw new Exception(e.getMessage());
				}
			} else {
				log.warn("Codelet not registered in any codelet handler. A custom codelet handler must trigger it.");
			}
		} catch (Exception e1) {
			log.error("{}>Cannot initialize codelet", this.getFunctionName(), e1);
			throw new Exception(e1.getMessage());
		}

	}

	protected abstract void cellFunctionCodeletInit() throws Exception;
	
	
	private Response executeCodelet(Request req) {
		Response result = new Response(req);
		
		log.debug("Execute the codelet handler");
		try {
			this.startCodelet();
			result.setResultOK(); 
		} catch (Exception e) {
			log.error("Cannot start codelet handler", e);
			result = new Response(req);
			result.setError(e.getMessage());
		}
		
		return result;
	}

	@Override
	public void startCodelet() {
		// Run thread
		this.setStart();
	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		updateServiceStateInCodeletHandler(ServiceState.RUNNING);
		
		// Set state to running
		log.debug("Set state running");

		// Execute the codelet specific preprocessing
		this.executeCodeletPreprocessing();

	}

	protected void executeCodeletPreprocessing() throws Exception {

	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		// Execute the codelet post processing
		this.executeCodeletPostprocessing();

		updateServiceStateInCodeletHandler(ServiceState.FINISHED);
		
		// Set state of the codelet to finished
		log.debug("Set state finished");
	}

	protected void executeCodeletPostprocessing() throws Exception {

	}

	private void updateServiceStateInCodeletHandler(ServiceState state) throws Exception {
		this.setServiceState(state);
		
		// Set state of the codelet to finished
		Request request = new Request();
		request.setParameter("caller", callerAddress);
		request.setParameter("state", state.toString());

		Response response = this.getCommunicator().execute(this.codeletHandlerAddress + "/" + CodeletHandlerImpl.SETSTATESERVICENAME, request, this.timeout);

		log.debug("Set state={}", state);
		if (response.hasError()) {
			throw new Exception("Communication error. Error: " + response.getError());
		}
	}

	@Override
	protected void shutDownThreadExecutor() throws Exception {
		this.shutDownCodelet();

		Request request = new Request();
		request.setParameter("caller", callerAddress);
		
		Response response = this.getCommunicator().execute(this.codeletHandlerAddress + "/" + CodeletHandlerImpl.UNREGISTERCODELETSERVICENAME, request, this.timeout);
		if (response.hasError()) {
			throw new Exception("Communication error. Error: " + response.getError());
		}
	}

	/**
	 * @throws Exception
	 * 
	 *             If the codelet is supposed to be shut down, put codelet specific shut-down code here. However, this function shall not be called from the codelet itself. The easiest way to shut down a
	 *             codelet is to set the command EXIT.
	 */
	public void shutDownCodelet() throws Exception {

	}
	
	public void resetCodelet() {
		
	}

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

}
