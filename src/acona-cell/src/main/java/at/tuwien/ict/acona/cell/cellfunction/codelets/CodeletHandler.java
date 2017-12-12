package at.tuwien.ict.acona.cell.cellfunction.codelets;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.cellfunction.ServiceState;

public interface CodeletHandler {

	/**
	 * Public registerCodelet(codeletname, executionOrder): void; blocking;
	 * register in the codelethandler, in order to be triggered at some point.
	 * ExecutionOrder: The codelet handler may also execute codelets in certain
	 * order and this is set by the parameter executionOrder. If 2 codelets have
	 * execution order 0 and a third execution order 1, then first the 2 first
	 * are executed and then the third.
	 * 
	 * @param callerAddress
	 * @param executionOrder
	 * @throws Exception
	 */
	public void registerCodelet(String callerAddress, int executionOrder) throws Exception;

	/**
	 * Public deregisterCodelet(codeletname): void; blocking; deregister codelet
	 * 
	 * @param codeletName
	 */
	public void deregisterCodelet(String codeletName);

	/**
	 * Public setCodeletState(Service state): void; blocking; a registered
	 * codelet sets ist state in a map. If the handler is in running mode and
	 * all codelets finish or give error, the handler execution is finished.
	 * Important: SetCodeletState can either be directly executed through the
	 * CF-Method performOperation or through subscription of a datapoint
	 * 
	 * @throws Exception
	 */
	public void setCodeletState(ServiceState state, String codeletID) throws Exception;

	/**
	 * Trigger to start the codelet handler as non blocking instance
	 * 
	 * @throws Exception
	 */
	public boolean startCodeletHandler(boolean isBlocking) throws Exception;

	public JsonObject getCodeletHandlerState();
}
