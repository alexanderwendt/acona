package at.tuwien.ict.acona.cell.core.cellfunctionthread.helpers;

import at.tuwien.ict.acona.cell.activator.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.activator.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class CellFunctionTestInstance extends CellFunctionThreadImpl {
	
	private String commandDatapoint = "datapoint.command";
	private String queryDatapoint = "datapoint.query";
	private String executeonceDatapoint = "datapoint.executeonce";
	
	private String query = "";

	@Override
	protected void updateDatapoint(Datapoint subscribedData) {
		if (subscribedData.getAddress().equals(commandDatapoint)) {
			//Set command
			String command = subscribedData.getValue().getAsString();
			try {
				this.setCommand(command);
				log.debug("Command {} set", command);
			} catch (Exception e) {
				log.error("Cannot execute command {}", command, e);
			}
		} else if (subscribedData.getAddress().equals(this.queryDatapoint)) {
			//Extract query and execute system
			this.query = subscribedData.getValue().getAsString();
			log.debug("Query {} received", this.query);
			this.setCommand(ControlCommand.START);
		} else if (subscribedData.getAddress().equals(this.executeonceDatapoint)) {
			//Set mode execute once or periodically
			this.setExecuteOnce(subscribedData.getValue().getAsBoolean());
			log.debug("ExecuteOnce={}", this.isExecuteOnceSet());
		}
		
	}

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
			
			this.cell.getDataStorage().write(Datapoint.newDatapoint("datapoint.result").setValue("FINISHED"), this.cell.getLocalName());
			log.info("Something was proceeded. Give back to tester");
		
			
		} else {
			throw new Exception("Query is empty");
		}
		
	}

	@Override
	protected void cellFunctionInit() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
