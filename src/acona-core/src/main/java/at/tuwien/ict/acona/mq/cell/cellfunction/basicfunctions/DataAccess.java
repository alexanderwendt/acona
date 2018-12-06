package at.tuwien.ict.acona.mq.cell.cellfunction.basicfunctions;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.cell.cellfunction.CellFunctionImpl;
import at.tuwien.ict.acona.mq.datastructures.Datapoint;
import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

/**
 * The basic service data access provides functions to access the data storage of an agent with the function methods read, write, remove, subscribe, unsubscribe
 * 
 * @author wendt
 *
 */
public class DataAccess extends CellFunctionImpl {

	private final static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public final static String BASICSERVICESUFFIX = "dataaccess";

	private final String METHODNAMEREAD = "read";
	private final String METHODNAMEWRITE = "write";
	private final String METHODNAMESUBSCRIBE = "subscribe";
	private final String METHODNAMEUNSUBSCRIBE = "unsubscribe";

	@Override
	protected void cellFunctionInit() throws Exception {
		this.addRequestHandlerFunction(this.METHODNAMEREAD, (Request input) -> read(input));
		this.addRequestHandlerFunction(this.METHODNAMEWRITE, (Request input) -> write(input));
		this.addRequestHandlerFunction(this.METHODNAMESUBSCRIBE, (Request input) -> subscribe(input));
		this.addRequestHandlerFunction(this.METHODNAMEUNSUBSCRIBE, (Request input) -> unsubscribe(input));

		log.info("{}>Initialized.", this.getFunctionName());
	}

	@Override
	protected void shutDownImplementation() throws Exception {
		log.info("Shutting down data access services");

	}

	@Override
	protected void updateDatapointsById(String id, JsonElement data) {
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

			List<Datapoint> readData = this.getCell().getDataStorage().read(dp.getAddress());

			result.setResult(readData);

			log.debug("Read data from address={}", dp.getAddress());
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
			this.getCell().getDataStorage().write(param);

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
			this.getCell().getDataStorage().subscribeDatapoint(param, req.getReplyTo());
			List<Datapoint> readData = this.getCell().getDataStorage().read(param);

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
			this.getCell().getDataStorage().unsubscribeDatapoint(param, req.getReplyTo());

			result.setResultOK();
		} catch (Exception e) {
			log.error("Cannot subscribe {} from the database", req, e);
			result.setError("Cannot unsubscribe " + req + " from database" + e);
		}

		return result;
	}

}
