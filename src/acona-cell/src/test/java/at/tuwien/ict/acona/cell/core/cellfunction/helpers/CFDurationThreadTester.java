package at.tuwien.ict.acona.cell.core.cellfunction.helpers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

public class CFDurationThreadTester extends CellFunctionThreadImpl {

	private static Logger log = LoggerFactory.getLogger(CFDurationThreadTester.class);

	public static String commandDatapointID = "command"; // agtne1.servicehalloworld.command,
	// description
	public static String queryDatapointID = "query";
	public static String executeonceDatapointID = "executeonce";
	public static String resultDatapointID = "result";

	private String query = "";

	@Override
	protected void executeFunction() throws Exception {
		// Execute some sort of query that takes a lot of time
		log.info("The query={} was received. Execute it", query);

		if (this.query.equals("") == false) {
			for (int i = 1; i <= 10; i++) {
				try {
					// Block profile controller
					synchronized (this) {
						this.wait(100);
					}

				} catch (InterruptedException e) {
					log.warn("Wait interrupted client");
				}

				log.debug("waited {}ms", i * 100);
			}

			//this.writeLocal(Datapoints.newDatapoint(this.getFunctionConfig().getManagedDatapointsAsMap().get(resultDatapointID).getAddress()).setValue("FINISHED"));
			this.getValueMap().get(resultDatapointID).setValue("FINISHED");
			log.info("Something was proceeded. Give back to tester");

		} else {
			log.warn("Query is empty");
			//this.writeLocal(Datapoints.newDatapoint(this.getFunctionConfig().getManagedDatapointsAsMap().get(resultDatapointID).getAddress()).setValue("NOT STARTED"));
			this.getValueMap().get(resultDatapointID).setValue("NOT STARTED");
		}

	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		// data.get(this.commandDatapoint).getValue().
		if (data.containsKey(commandDatapointID)
				&& data.get(commandDatapointID).getValue().toString().equals("{}") == false) {
			// Set command
			String command = data.get(commandDatapointID).getValueAsString();
			try {
				this.setCommand(command);
				log.debug("Command {} set", command);
			} catch (Exception e) {
				log.error("Cannot execute command {}", command, e);
			}
		} else if (data.containsKey(queryDatapointID)
				&& data.get(queryDatapointID).getValue().toString().equals("{}") == false) {
			// Extract query and execute system
			// JsonElement x=
			// data.get(this.queryDatapoint).getValue().getAsJsonObject();
			// boolean tester = x.isJsonNull();
			this.query = data.get(queryDatapointID).getValueAsString();
			log.debug("Query {} received", this.query);
			this.setCommand(ControlCommand.START);
		} else if (data.containsKey(executeonceDatapointID)
				&& data.get(executeonceDatapointID).getValue().toString().equals("{}") == false) {
			// Set mode execute once or periodically
			this.setExecuteOnce(data.get(executeonceDatapointID).getValue().getAsBoolean());
			log.debug("ExecuteOnce={}", this.isExecuteOnce());
		}

	}

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		log.info("Command datapoint={}, query datapoint={}, result datapoint={}", commandDatapointID, queryDatapointID, resultDatapointID);

	}

	@Override
	protected void executeCustomPostProcessing() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void executeCustomPreProcessing() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void shutDownExecutor() {
		// TODO Auto-generated method stub

	}

	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		// TODO Auto-generated method stub
		return null;
	}

}
