package at.tuwien.ict.acona.cognitivearchitecture.frameworkcodelets;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;
import at.tuwien.ict.acona.cognitivearchitecture.CognitiveProcess;
import at.tuwien.ict.acona.cognitivearchitecture.datastructures.Option;

public class ActionExecutionCodelet extends CognitiveCodelet {

	private final static Logger log = LoggerFactory.getLogger(ActionExecutionCodelet.class);

	// private String selectionAddress="selection";
	// private String actionHistoryAddress = "state.history";

	@Override
	protected void cellFunctionCodeletInit() throws Exception {
		log.info("{}>Acion execution codelet initialized", this.getFunctionName());

	}

	@Override
	protected void executeFunction() throws Exception {
		// Execute the action of the selected option
		Datapoint dpOption = this.getCommunicator().read(CognitiveProcess.SELECTEDOPTIONADDRESS);

		String serviceName = "";

		if (dpOption.hasEmptyValue() == false) {
			Option option = dpOption.getValue(Option.class);
			log.debug("Execute action for the option={}", option);
			// Extract the action and the parameter
			serviceName = option.getActionServiceName();
			if (serviceName.isEmpty() == false) {
				String[] parameter = option.getActionParameter();
				List<String> parameterList = Arrays.asList(parameter);
				String method = option.getActionMethod();
				if (method.isEmpty()) {
					method = "any";
				}

				log.debug("Service name={}, method={}, parameters={}", serviceName, method, parameterList);

				JsonRpcRequest req = new JsonRpcRequest(method, false, parameter);
				JsonRpcResponse result = this.getCommunicator().execute(serviceName, req);

				if (result.hasError() == true) {
					log.error("ERROR: The execution of the action failed. The error message is={}", result);
					// FIXME: At the moment, errors are not handled by the system
				} else {
					log.info("Action successfully executed");
				}
			} else {
				log.info("Empty action in the option. No action will be executed");
			}

		} else {
			log.info("No action to perform");
		}

		log.debug("Update action history with action={}", serviceName);
		this.updateActionHistory(serviceName);

	}

	/**
	 * Update the action history with the last action
	 * 
	 * @throws Exception
	 */
	private void updateActionHistory(String action) throws Exception {
		// Update
		Datapoint history = this.getCommunicator().read(CognitiveProcess.ACTIONHISTORYADDRESS);
		log.debug("Old history={}", history);
		JsonArray historyData;
		@SuppressWarnings("unused")
		JsonObject actionHistoryObject = new JsonObject();

		if (history.hasEmptyValue() == false) {
			historyData = history.getValue().getAsJsonArray();
		} else {
			historyData = new JsonArray();
		}

		// actionHistoryObject.add("timestamp", );
		// actionHistoryObject.add("action", );

		historyData.add(action);

		while (historyData.size() >= 50) {
			historyData.remove(0);
		}

		history.setValue(historyData);
		this.getCommunicator().write(history);
		log.debug("Updated action history={}", this.getInternalStateMemoryAddress() + "." + history);
	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub

	}

}