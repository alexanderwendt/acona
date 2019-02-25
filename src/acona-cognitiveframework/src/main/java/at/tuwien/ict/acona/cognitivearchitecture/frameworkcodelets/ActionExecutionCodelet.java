package at.tuwien.ict.acona.cognitivearchitecture.frameworkcodelets;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import at.tuwien.ict.acona.cognitivearchitecture.CognitiveProcess;
import at.tuwien.ict.acona.cognitivearchitecture.datastructures.Option;
import at.tuwien.ict.acona.mq.datastructures.Datapoint;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

public class ActionExecutionCodelet extends CognitiveCodelet {

	private final static Logger log = LoggerFactory.getLogger(ActionExecutionCodelet.class);

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss:SSS");

	@Override
	protected void cellFunctionCodeletInit() throws Exception {
		log.info("{}>Acion execution codelet initialized", this.getFunctionName());

	}

	@Override
	protected void executeFunction() throws Exception {
		// Execute the action of the selected option
		Datapoint dpOption = this.getCommunicator().read(CognitiveProcess.SELECTEDOPTIONADDRESS);

		String serviceName = "";
		String[] parameter = new String[0];

		if (dpOption.hasEmptyValue() == false) {
			Option option = dpOption.getValue(Option.class);
			log.debug("Execute action for the option={}", option);
			// Extract the action and the parameter
			serviceName = option.getActionServiceName();
			if (serviceName.isEmpty() == false) {
				parameter = option.getActionParameter();
				List<String> parameterList = Arrays.asList(parameter);
				String method = option.getActionMethod();
				if (method.isEmpty()) {
					method = "any";
				}

				log.debug("Service name={}, method={}, parameters={}", serviceName, method, parameterList);
				Request req = new Request(method, false, parameter);
				Response result = this.getCommunicator().execute(serviceName, req);

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
		this.updateActionHistory(serviceName, parameter);

	}

	/**
	 * Update the action history with the last action
	 * 
	 * @throws Exception
	 */
	private void updateActionHistory(String action, String[] parameter) throws Exception {
		Date date = new Date(System.currentTimeMillis());

		// Update
		Datapoint history = this.getCommunicator().read(CognitiveProcess.ACTIONHISTORYADDRESS);
		log.debug("Old history={}", history);
		List<ActionHistoryEntry> historyData = new ArrayList<ActionHistoryEntry>();
		// JsonArray historyData = new JsonArray();
		// @SuppressWarnings("unused")
		// JsonObject actionHistoryObject = new JsonObject();

		if (history.hasEmptyValue() == false) {
			historyData = history.getValue(new TypeToken<List<ActionHistoryEntry>>() {});
		}

		// actionHistoryObject.add("timestamp", );
		// actionHistoryObject.add("action", );

		historyData.add(new ActionHistoryEntry(this.sdf.format(date), action, parameter));

		while (historyData.size() >= 100) {
			historyData.remove(0);
		}

		history.setValue(historyData);
		this.getCommunicator().write(history);
		log.debug("Updated action history={}", this.getInternalStateMemoryAddress() + "." + history);
	}

	@Override
	protected void updateCustomDatapointsById(String id, JsonElement data) {
		// TODO Auto-generated method stub
		
	}

}