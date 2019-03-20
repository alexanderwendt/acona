package at.tuwien.ict.acona.mq.core.agentfunction.basicfunctions;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.core.agentfunction.AgentFunctionImpl;
import at.tuwien.ict.acona.mq.datastructures.Datapoint;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

/**
 * The basic service data access provides functions to access the data storage of an agent with the function methods read, write, remove, subscribe, unsubscribe
 * 
 * @author wendt
 *
 */
public class DataAccess extends AgentFunctionImpl {

	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public final static String BASICSERVICESUFFIX = "dataaccess";

	private final static String METHODNAMEREAD = "read";
	private final static String METHODNAMEWRITE = "write";
	private final static String METHODNAMESUBSCRIBE = "subscribe";
	private final static String METHODNAMEUNSUBSCRIBE = "unsubscribe";
	private final static String METHODNAMEREMOVE = "remove";

	@Override
	protected void agentFunctionInit() throws Exception {
		this.addRequestHandlerFunction(METHODNAMEREAD, (Request input) -> read(input));
		this.addRequestHandlerFunction(METHODNAMEWRITE, (Request input) -> write(input));
		this.addRequestHandlerFunction(METHODNAMESUBSCRIBE, (Request input) -> subscribe(input));
		this.addRequestHandlerFunction(METHODNAMEUNSUBSCRIBE, (Request input) -> unsubscribe(input));
		this.addRequestHandlerFunction(METHODNAMEREMOVE, (Request input) -> remove(input));

		log.info("{}>Initialized.", this.getFunctionName());
	}

	@Override
	protected void shutDownImplementation() throws Exception {
		log.info("Shutting down data access services");

	}

	@Override
	protected void updateDatapointsById(String id, String topic, JsonElement data) {
		// log.warn("This method shall not subscribe anything");
		log.debug("received id={}, value={}", id, data);

	}

	/**
	 * Read from the database and return to the caller
	 * 
	 * @param req
	 * @return
	 */
	private Response read(Request req) {
		Response result = new Response(req);

		try {
			String param = req.getParameter("param", String.class);

			// Remove the agent
			Datapoint dp = this.getDatapointBuilder().newDatapoint(param);

			List<Datapoint> readData = this.getAgent().getDataStorage().read(dp.getAddress());
			readData.forEach(d->d.setAgent(this.getAgentName()));

			result.setResult(readData);

			log.debug("Read request {}. Read data from address={}, value={}", req, dp.getAddress(), readData);
		} catch (Exception e) {
			log.error("Cannot read from the database", e);
			result.setError("Cannot read from database" + e);
		}

		return result;
	}

	/**
	 * Write from the database and return to the caller
	 * 
	 * @param req
	 * @return
	 */
	private Response write(Request req) {
		Response result = new Response(req);

		try {
			Datapoint param = req.getParameter("param", Datapoint.class);
			this.getAgent().getDataStorage().write(param);

			result.setResultOK();
		} catch (Exception e) {
			log.error("Cannot read from the database", e);
			result.setError("Cannot write to database" + e);
		}

		return result;
	}

	/**
	 * Subscribe from the database and return to the caller
	 * 
	 * @param req
	 * @return
	 */
	private Response subscribe(Request req) {
		Response result = new Response(req);

		try {
			String param = req.getParameter("param", String.class);
			this.getAgent().getDataStorage().subscribeDatapoint(param, req.getReplyTo());
			List<Datapoint> readData = this.getAgent().getDataStorage().read(param);
			
			readData.forEach(d->d.setAgent(this.getAgentName()));

			result.setResult(readData);
		} catch (Exception e) {
			log.error("Cannot subscribe {} from the database", req, e);
			result.setError("Cannot subscribe " + req + " from database" + e);
		}

		return result;
	}

	/**
	 * Unsubscribe from the database
	 * 
	 * @param req
	 * @return
	 */
	private Response unsubscribe(Request req) {
		Response result = new Response(req);

		try {
			String param = req.getParameter("param", String.class);
			this.getAgent().getDataStorage().unsubscribeDatapoint(param, req.getReplyTo());

			result.setResultOK();
		} catch (Exception e) {
			log.error("Cannot subscribe {} from the database", req, e);
			result.setError("Cannot unsubscribe " + req + " from database" + e);
		}

		return result;
	}
	
	/**
	 * Remove a datapoint
	 * 
	 * @param req
	 * @return
	 */
	private Response remove(Request req) {
		Response result = new Response(req);

		try {
			String param = req.getParameter("param", String.class);
			this.getAgent().getDataStorage().remove(param);

			result.setResultOK();
		} catch (Exception e) {
			log.error("Cannot remove {} from the database", req, e);
			result.setError("Cannot remove " + req + " from database" + e);
		}

		return result;
	}

}
