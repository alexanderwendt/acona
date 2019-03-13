package at.tuwien.ict.acona.distribution;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.mq.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

public class Server extends CellFunctionThreadImpl {
	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final String serviceName = "helloworldservice";
	
	
	@Override
	protected void cellFunctionThreadInit() throws Exception {
		this.addRequestHandlerFunction(serviceName, (Request input) -> helloWorldService(input));
		
		log.info("Server initialized and offer service {}", serviceName);
		
	}
	
	/**
	 * Returns a string with hello world or bye world
	 * 
	 * @param req
	 * @return
	 */
	private Response helloWorldService(Request req) {
		Response result = new Response(req);
		
		try {
			log.info("Hello world service activated");
			int type = req.getParameter("type", Integer.class);
			
			String resultString = "";
			
			
			if (type==1) {
				resultString = "Hello world";
				result.setResult(new JsonPrimitive(resultString));
			} else if (type==2) {
				resultString = "Bye world";
				result.setResult(new JsonPrimitive(resultString));
			} else {
				log.error("Wrong type {}", type);
				result.setError("Wrong type input");
			}
			
		} catch (Exception e) {
			log.error("Cannot change confidence", e);
			result.setError(e.getMessage());
		}
		
		return result;
	}

	@Override
	protected void executeCustomPreProcessing() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void executeFunction() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void executeCustomPostProcessing() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void updateCustomDatapointsById(String id, JsonElement data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void shutDownThreadExecutor() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
