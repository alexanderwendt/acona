package at.tuwien.ict.acona.mq.cell.cellfunction;

import com.google.gson.JsonElement;

import at.tuwien.ict.acona.mq.datastructures.Request;
import at.tuwien.ict.acona.mq.datastructures.Response;

/**
 * The basic service data access provides functions to access the data storage of an agent with the function methods read, write, remove, subscribe, unsubscribe
 * 
 * @author wendt
 *
 */
public class DataAccess extends CellFunctionImpl {

	public final static String BASICSERVICESUFFIX = "dataaccess";

	@Override
	public Response performOperation(String topic, Request param) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void cellFunctionInit() throws Exception {

	}

	@Override
	protected void shutDownImplementation() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateDatapointsById(String id, JsonElement data) {
		// TODO Auto-generated method stub

	}

}
