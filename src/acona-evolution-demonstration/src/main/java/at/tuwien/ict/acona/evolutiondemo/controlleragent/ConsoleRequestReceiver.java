package at.tuwien.ict.acona.evolutiondemo.controlleragent;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.cellfunction.ControlCommand;
import at.tuwien.ict.acona.cell.cellfunction.ServiceState;
import at.tuwien.ict.acona.cell.cellfunction.codelets.CellFunctionCodeletHandler;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.DatapointBuilder;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcError;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcRequest;
import at.tuwien.ict.acona.cell.datastructures.JsonRpcResponse;

public class ConsoleRequestReceiver extends CellFunctionThreadImpl {

	private final RequestReceiverUserConsole console = new RequestReceiverUserConsole(log, this);

	private static Logger log = LoggerFactory.getLogger(ConsoleRequestReceiver.class);
	// private Scanner scanner = new Scanner(System.in);

	public final static String ATTRIBUTECONTROLLERSERVICE = "controllerservice";

	private String codeletHandlerAddress = "";

	private int count = 1;
	private boolean runAllowed = true;

	@Override
	protected void cellFunctionThreadInit() throws Exception {
		codeletHandlerAddress = this.getFunctionConfig().getProperty(ATTRIBUTECONTROLLERSERVICE, "");

		this.setExecuteOnce(true);
		// this.setExecuteRate(1000);
		this.setCommand(ControlCommand.STOP);
		console.init();
	}

	@Override
	protected void executeFunction() throws Exception {
		try {

			for (int i = 1; i <= count; i++) {
				if (this.runAllowed == true) {
					log.info("run {}/{}", i, count);
					// Execute the codelet handler once
					JsonRpcRequest req = new JsonRpcRequest(CellFunctionCodeletHandler.EXECUTECODELETEHANDLER, 1);
					req.setParameterAsValue(0, false);
					Datapoint dp = DatapointBuilder.newDatapoint(this.codeletHandlerAddress);
					this.getCommunicator().executeServiceQueryDatapoints(dp.getAgent(), dp.getAddress(), req, dp.getAgent(), dp.getAddress() + ".state", new JsonPrimitive(ServiceState.FINISHED.toString()), this.getCommunicator().getDefaultTimeout());

//					//FIXME: No delays should be necessary, look at the codelet handler, sync problems.
//					synchronized (this) {
//						try {
//							this.wait(5);
//						} catch (InterruptedException e) {
//							
//						}
//							
//					}

				} else {
					log.warn("Running of simulator interrupted after {} runs", i);
					break;
				}

			}
		} catch (Exception e) {
			log.error("Cannot receive result", e.getMessage());
		}
	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		// log.debug("Command value: {}", this.readLocal(COMMANDADDRESS));
		log.debug("Processing finished after {} cycles", count);

	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		log.debug("Processing started. Will proceed with {} cycles", count);

	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub
	}

	protected void restart() {
		// TODO set a restart of the system
	}

//	protected void setExternalCommand(String command) {
//		log.debug("Set command {}", command);
//		this.command = command;
//		this.setStart();
//	}

	protected void startStockMarket(int count) throws Exception {
		log.debug("Run stock market for {} runs", count);

		this.runAllowed = true;
		this.count = count;

		this.setStart();
	}

	protected void interruptStockMarket() {
		this.runAllowed = false;
	}

	@Override
	protected void shutDownExecutor() {
		// TODO Auto-generated method stub

	}

	@Override
	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
		JsonRpcResponse result = null;

		try {
			switch (parameterdata.getMethod()) {
			case "startcontroller":
				this.count = parameterdata.getParameter(0, Integer.class);
				log.debug("Start stock market and run it {} times", this.count);
				this.startStockMarket(this.count);
				break;
			default:
				throw new UnsupportedOperationException();
			}

			// In the method, there should be parameter requestaddress and resultaddress. Methodname is any

			// this.resultAddress = parameterdata.getParameter(1, String.class);
			// No parameters necessary

			result = new JsonRpcResponse(parameterdata, new JsonPrimitive("OK"));

		} catch (Exception e) {
			result = new JsonRpcResponse(parameterdata, new JsonRpcError("ERROR", -1, e.getMessage(), e.getMessage()));
		}

		return result;
	}

}
