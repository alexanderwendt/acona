package at.tuwien.ict.acona.mq.datastructures;

import com.google.gson.JsonObject;

public class Command {
	public final static String CALLER = "caller";
	public final static String COMMAND = "command";

	private final String caller;
	private final ControlCommand command;

	public Command(String caller, ControlCommand command) {
		this.caller = caller;
		this.command = command;
	}

	public String getCaller() {
		return caller;
	}

	public ControlCommand getCommand() {
		return command;
	}

	public static boolean isRequest(JsonObject obj) {
		boolean result = false;

		if (obj.has(CALLER) && obj.has(COMMAND)) {
			result = true;
		}

		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Command [caller=");
		builder.append(caller);
		builder.append(", command=");
		builder.append(command);
		builder.append("]");
		return builder.toString();
	}
}
