package at.tuwien.ict.acona.cell.core.cellfunctionthread.helpers;

import java.util.Map;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class CellFunctionTestInstance extends CellFunctionThreadImpl {
	
	private String commandDatapoint = "COMMAND";
	private String queryDatapoint = "QUERY";
	private String executeonceDatapoint = "EXECUTEONCE";
	
	private String query = "";

	@Override
	protected void executeFunction() throws Exception {
		//Execute some sort of query that takes a lot of time
		log.info("The query={} was received. Execute it", query);
		
		if (this.query.equals("")==false) {
			for (int i=1;i<=10;i++) {
				try {
					//Block profile controller
					synchronized (this) {
						this.wait(100);
					}
					
				} catch (InterruptedException e) {
					log.warn("Wait interrupted client");
				}
				
				log.debug("waited {}ms", i*100);
			}
			
			this.writeLocal(Datapoint.newDatapoint("datapoint.result").setValue("FINISHED"));
			log.info("Something was proceeded. Give back to tester");
		
			
		} else {
			throw new Exception("Query is empty");
		}
		
	}

	
	@Override
	protected void updateDatapointsById(Map<String, Datapoint> data) {
		if (data.containsKey(commandDatapoint)) {
			//Set command
			String command = data.get(commandDatapoint).getValueAsString();
			try {
				this.setCommand(command);
				log.debug("Command {} set", command);
			} catch (Exception e) {
				log.error("Cannot execute command {}", command, e);
			}
		} else if (data.containsKey(this.queryDatapoint)) {
			//Extract query and execute system
			this.query = data.get(queryDatapoint).getValueAsString();
			log.debug("Query {} received", this.query);
			this.setCommand(ControlCommand.START);
		} else if (data.containsKey(executeonceDatapoint)) {
			//Set mode execute once or periodically
			this.setExecuteOnce(data.get(executeonceDatapoint).getValue().getAsBoolean());
			log.debug("ExecuteOnce={}", this.isExecuteOnce());
		}
		
	}


	@Override
	protected void cellFunctionInternalInit() throws Exception {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void executePostProcessing() {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected void executePreProcessing() {
		// TODO Auto-generated method stub
		
	}

}
