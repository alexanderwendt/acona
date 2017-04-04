package at.tuwien.ict.acona.cell.cellfunction.codelets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.CommVocabulary;
import at.tuwien.ict.acona.cell.cellfunction.ServiceState;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public abstract class CellFunctionCodelet extends CellFunctionThreadImpl implements Codelet {

	private final static Logger log = LoggerFactory.getLogger(CellFunctionCodelet.class);

	private final static String SETSTATESERVICENAME = "setstate";
	private final static String REGISTERCODELETSERVICENAME = "registercodelet";
	private final static String DEREGISTERCODELETSERVICENAME = "deregistercodelet";
	private final static String EXECUTECODELETMETHODNAME = "execute";
	private final static String KEYMETHOD = "method";
	private final static String KEYCALLERADDRESS = "calleraddress";
	private final static String KEYEXECUTIONORDERNAME = "executionorder";
	private final static String KEYSTATE = "state";
	private final static int METHODTIMEOUT = 1000;

	public final static String ATTRIBUTECODELETHANDLERADDRESS = "handleraddress";
	public final static String ATTRIBUTEEXECUTIONORDER = "executionorder";

	private String codeletHandlerAgentName = "";
	private String codeletHandlerServiceName = "";
	private String callerAddress = "";
	private int exeutionOrder = 0;

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		try {
			//Set the caller address
			this.callerAddress = this.getCell().getLocalName() + ":" + this.getFunctionName();

			//Start internal init
			this.cellFunctionCodeletInit();

			//Get the codelethandler data
			this.codeletHandlerAgentName = this.getFunctionConfig().getProperty(ATTRIBUTECODELETHANDLERADDRESS).split(":")[0];
			this.codeletHandlerServiceName = this.getFunctionConfig().getProperty(ATTRIBUTECODELETHANDLERADDRESS).split(":")[1];
			this.exeutionOrder = Integer.valueOf(this.getFunctionConfig().getProperty(KEYEXECUTIONORDERNAME, "0"));

			//Register codelet in the codelethandler
			List<Datapoint> methodParameters = new ArrayList<>(Arrays.asList(
					Datapoint.newDatapoint(KEYMETHOD).setValue(REGISTERCODELETSERVICENAME),
					Datapoint.newDatapoint(KEYCALLERADDRESS).setValue(callerAddress),
					Datapoint.newDatapoint(ATTRIBUTEEXECUTIONORDER).setValue(new JsonPrimitive(this.exeutionOrder))));
			try {
				List<Datapoint> result = this.getCommunicator().execute(this.codeletHandlerAgentName, this.codeletHandlerServiceName, methodParameters, METHODTIMEOUT);
				String value = updateServiceStateInCodeletHandler(ServiceState.IDLE);
				if (result.get(0).getValueAsString().equals(CommVocabulary.ERRORVALUE) == true) {
					//log.error()
					throw new Exception("Cannot register the codelet. Maybe the codelet handler has not been started yet");
				}

			} catch (Exception e) {
				log.error("Cannot register codelet", e);
				throw new Exception(e.getMessage());
			}
		} catch (Exception e1) {
			log.error("Cannot initialize codelet", e1);
			throw new Exception(e1.getMessage());
		}

	}

	protected abstract void cellFunctionCodeletInit() throws Exception;

	@Override
	public List<Datapoint> performOperation(Map<String, Datapoint> parameterdata, String caller) {
		List<Datapoint> result = new ArrayList<>();
		//React on the following inputs
		//Attributes: method=startcodelet

		log.debug("{}>Received execute request={}", this.getFunctionName(), parameterdata);

		if (parameterdata.containsKey(KEYMETHOD) && parameterdata.get(KEYMETHOD).getValueAsString().equals(EXECUTECODELETMETHODNAME)) {
			log.debug("Execute the codelet");
			this.startCodelet();
			result.add(Datapoint.newDatapoint(CommVocabulary.PARAMETERRESULTADDRESS).setValue(CommVocabulary.ACKNOWLEDGEVALUE));
		} else {
			result.add(Datapoint.newDatapoint(CommVocabulary.PARAMETERRESULTADDRESS).setValue(CommVocabulary.ERRORVALUE));
		}

		return result;
	}

	@Override
	public void startCodelet() {
		//Run thread
		this.setStart();

	}

	//	@Override
	//	protected void executeFunction() throws Exception {
	//		// TODO Auto-generated method stub
	//
	//	}

	@Override
	protected void executePostProcessing() throws Exception {
		// TODO Auto-generated method stub
		//Set state of the codelet to finished
		//Register codelet in the codelethandler
		List<Datapoint> methodParameters = new ArrayList<>(Arrays.asList(
				Datapoint.newDatapoint(KEYMETHOD).setValue(SETSTATESERVICENAME),
				Datapoint.newDatapoint(KEYCALLERADDRESS).setValue(callerAddress),
				Datapoint.newDatapoint(KEYSTATE).setValue(ServiceState.IDLE.toString())));
		this.getCommunicator().execute(this.codeletHandlerAgentName, this.codeletHandlerServiceName, methodParameters, METHODTIMEOUT);

	}

	@Override
	protected void executePreProcessing() throws Exception {
		// TODO Auto-generated method stub
		//Set state to running
		List<Datapoint> methodParameters = new ArrayList<>(Arrays.asList(
				Datapoint.newDatapoint(KEYMETHOD).setValue(SETSTATESERVICENAME),
				Datapoint.newDatapoint(KEYCALLERADDRESS).setValue(callerAddress),
				Datapoint.newDatapoint(KEYSTATE).setValue(ServiceState.RUNNING.toString())));
		this.getCommunicator().execute(this.codeletHandlerAgentName, this.codeletHandlerServiceName, methodParameters, METHODTIMEOUT);

	}

	private String updateServiceStateInCodeletHandler(ServiceState state) throws Exception {
		List<Datapoint> methodParameters = new ArrayList<>(Arrays.asList(
				Datapoint.newDatapoint(KEYMETHOD).setValue(SETSTATESERVICENAME),
				Datapoint.newDatapoint(KEYCALLERADDRESS).setValue(callerAddress),
				Datapoint.newDatapoint(KEYSTATE).setValue(state.toString())));
		List<Datapoint> resultList = this.getCommunicator().execute(this.codeletHandlerAgentName, this.codeletHandlerServiceName, methodParameters, METHODTIMEOUT);

		String result = resultList.get(0).getValueAsString();

		return result;
	}

	//	@Override
	//	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
	//		// TODO Auto-generated method stub
	//
	//	}

	@Override
	protected void shutDownExecutor() throws Exception {
		//Deregister codelet
		List<Datapoint> methodParameters = new ArrayList<>(Arrays.asList(
				Datapoint.newDatapoint(KEYMETHOD).setValue(DEREGISTERCODELETSERVICENAME),
				Datapoint.newDatapoint(KEYCALLERADDRESS).setValue(callerAddress)));
		this.getCommunicator().execute(this.codeletHandlerAgentName, this.codeletHandlerServiceName, methodParameters, METHODTIMEOUT);
	}

}
