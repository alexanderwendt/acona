package at.tuwien.ict.acona.evolutiondemo.controlleragent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.core.agentfunction.AgentFunctionThreadImpl;
import at.tuwien.ict.acona.mq.core.agentfunction.ControlCommand;
import at.tuwien.ict.acona.mq.core.agentfunction.codelets.CodeletHandlerImpl;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

public class ConsoleRequestReceiver extends AgentFunctionThreadImpl {

	public static final String METHODSTARTCONTROLLER = "start";
	public static final String METHODINTERRUPTCONTROLLER = "interrupt";
	
	//private final RequestReceiverUserConsole console = new RequestReceiverUserConsole(log, this);

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
		//console.init();
		
		this.addRequestHandlerFunction(METHODSTARTCONTROLLER, (Request input) -> startController(input));
		this.addRequestHandlerFunction(METHODINTERRUPTCONTROLLER, (Request input) -> interrupt(input));
	}
	
	
	private Response startController(Request req) {
		Response result = new Response(req);
		
		try {
			
			this.count = req.getParameter("count", Integer.class);
			log.debug("Start stock market and run it {} times", this.count);
			this.startStockMarket(this.count);
			
		} catch (Exception e) {
			log.error("Cannot start controller", e);
			result.setError(e.getMessage());
		}
		
		return result;
	}
	
	private Response interrupt(Request req) {
		this.runAllowed = false;
		return (new Response(req)).setResultOK();
	}

	@Override
	protected void executeFunction() throws Exception {
		try {

			for (int i = 1; i <= count; i++) {
				if (this.runAllowed == true) {
					log.info("run {}/{}", i, count);
					// Execute the codelet handler once
					this.getCommunicator().execute(this.codeletHandlerAddress + "/" + CodeletHandlerImpl.EXECUTECODELETMETHODNAME, new Request(), 200000);

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

	protected void restart() {
		// TODO set a restart of the system
	}

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
	protected void shutDownThreadExecutor() {
		// TODO Auto-generated method stub

	}

//	@Override
//	public JsonRpcResponse performOperation(JsonRpcRequest parameterdata, String caller) {
//		JsonRpcResponse result = null;
//
//		try {
//			switch (parameterdata.getMethod()) {
//			case "startcontroller":
//				this.count = parameterdata.getParameter(0, Integer.class);
//				log.debug("Start stock market and run it {} times", this.count);
//				this.startStockMarket(this.count);
//				break;
//			default:
//				throw new UnsupportedOperationException();
//			}
//
//			// In the method, there should be parameter requestaddress and resultaddress. Methodname is any
//
//			// this.resultAddress = parameterdata.getParameter(1, String.class);
//			// No parameters necessary
//
//			result = new JsonRpcResponse(parameterdata, new JsonPrimitive("OK"));
//
//		} catch (Exception e) {
//			result = new JsonRpcResponse(parameterdata, new JsonRpcError("ERROR", -1, e.getMessage(), e.getMessage()));
//		}
//
//		return result;
//	}

	@Override
	protected void updateCustomDatapointsById(String id, JsonElement data) {
		// TODO Auto-generated method stub
		
	}

}
