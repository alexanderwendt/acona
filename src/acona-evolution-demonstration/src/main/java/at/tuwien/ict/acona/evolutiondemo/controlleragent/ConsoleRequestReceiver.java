package at.tuwien.ict.acona.evolutiondemo.controlleragent;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Datapoints;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

public class ConsoleRequestReceiver extends CellFunctionThreadImpl {
	
	private final static String COMMANDADDRESS = "command";
	private final static String RESULTADDRESS = "result";
	
	private final RequestReceiverUserConsole console = new RequestReceiverUserConsole(log, this);
	private String command = "";

	private static Logger log = LoggerFactory.getLogger(ConsoleRequestReceiver.class);
	//private Scanner scanner = new Scanner(System.in);
	
	public final static String ATTRIBUTESTOCKMARKETADDRESS = "stockmarketaddress";
	//public final static String ATTRIBUTEGOALADDRESSSUFFIX  = "goaladdress";
	
	private String address = "";


	@Override
	protected void cellFunctionThreadInit() throws Exception {
		address = this.getFunctionConfig().getProperty(ATTRIBUTESTOCKMARKETADDRESS, "");
		
		this.setExecuteOnce(true);
		//this.setExecuteRate(1000);
		this.setCommand(ControlCommand.STOP);
		console.init();
		
	}

	@Override
	protected void executeFunction() throws Exception {
		try {
			System.out.println("Provide a request: ");
			//String command = scanner.next();
			log.info("Received command {}. Now check the reaction of the system", command);
			
			//Write the answer to the working memory
			//this.getCommunicator().write(Datapoint.newDatapoint(COMMANDADDRESS).setValue(command));
			
			//Execute a function that waits
			this.getCommunicator().queryDatapoints(COMMANDADDRESS, command, RESULTADDRESS, null, 2000);
			
			//Execute 
			//this.getCommunicator().execute(this.getCell().getLocalName(), , methodParameters, timeout)
		} catch (Exception e) {
			log.error("Cannot receive result", e.getMessage());
		}
		
		
	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		log.debug("Command value: {}", this.readLocal(COMMANDADDRESS));
		log.debug("Processing finished");
		
	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		log.debug("Processing started");
		
	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub
	}
	
	protected void restart() {
		//TODO set a restart of the system
	}
	
	protected void setExternalCommand(String command) {
		log.debug("Set command {}", command);
		this.command = command;
		this.setStart();
	}
	
	protected void startStockMarket() throws Exception {
		log.debug("Start stock market");
		this.getCommunicator().write(Datapoints.newDatapoint(address + "." + "command").setValue(ControlCommand.START));
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
