package at.tuwien.ict.acona.cognitivearchitecture.frameworkcodelets;

import com.google.gson.JsonObject;

public class ActionHistoryEntry {

	private String timestamp = "";
	private String action = "";
	private String parameter = "";

	public ActionHistoryEntry(JsonObject actionHistoryEntry) {
		this.timestamp = actionHistoryEntry.get("timestamp").getAsString();
		this.action = actionHistoryEntry.get("action").getAsString();
		this.parameter = actionHistoryEntry.get("parameter").getAsString();
	}

	/**
	 * Constructor for action history
	 * 
	 * @param timestamp
	 * @param action
	 * @param parameter
	 */
	public ActionHistoryEntry(String timestamp, String action, String[] parameter) {
		super();
		this.timestamp = timestamp;
		this.action = action;
		for (int i = 0; i < parameter.length; i++) {
			if (parameter[i].isEmpty() == false) {
				if (i > 0) {
					this.parameter += ", ";
				}
				this.parameter += parameter[i];
			}
		}
		// this.parameter = parameter;
	}

	/**
	 * @return
	 */
	public String getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp
	 */
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @param action
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @return
	 */
	public String getParameter() {
		return parameter;
	}

	/**
	 * @param parameter
	 */
	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(timestamp);
		builder.append(" | ");
		builder.append(action);
		builder.append(" | ");
		builder.append(parameter);
		return builder.toString();
	}

}
