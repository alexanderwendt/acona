package at.tuwien.ict.acona.cell.datastructures;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import at.tuwien.ict.acona.cell.datastructures.util.GsonUtils;

public class JsonConversionTester {

	private static Logger log = LoggerFactory.getLogger(JsonConversionTester.class);
	private GsonUtils util = new GsonUtils();
	private Gson gson = new Gson();

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void jsonrpcConversionTest() {
		log.info("Start datapoint tester");
		try {

			List<String> stringlist = Arrays.asList("test1", "test2", "test3");

			JsonArray array = util.convertListToJsonArray(stringlist);
			Object[] input = new Object[1];
			input[0] = array;

			JsonRpcRequest request = new JsonRpcRequest("testmethod", false, input);

			//Get first parameter and convert it
			List<String> result = gson.fromJson(request.getParams()[0].toString(), new TypeToken<List<String>>() {
			}.getType());

			List<String> resultList = new ArrayList<>();

			assertEquals(stringlist.get(1), result.get(1));
			log.info("Test passed. Result={}", result.get(1));
		} catch (Exception e) {
			log.error("Cannot test system", e);
			fail("Error");
		}
	}

	@Test
	public void jsonrpcRequestDatapointConversionTest() {
		log.info("Start datapoint tester");
		try {

			List<Datapoint> stringlist = Arrays.asList(Datapoints.newDatapoint("test1").setValue(new JsonPrimitive("SELECT * SERVER")), Datapoints.newDatapoint("test2").setValue("test2"), Datapoints.newDatapoint("test3"));

			//JsonArray array = util.convertListToJsonArray(stringlist);
			//Object[] input = new Object[1];
			//input[0] = array;

			JsonRpcRequest request = new JsonRpcRequest("testmethod", false, new Object[1]);
			request.setParameterAsList(0, stringlist);

			String transport = request.toJson().toString();

			JsonRpcRequest req2 = new JsonRpcRequest(transport);

			//Get first parameter and convert it
			List<Datapoint> result = req2.getParameter(0, new TypeToken<List<Datapoint>>() {
			});//gson.fromJson(request.getParams()[0].toString(), new TypeToken<List<Datapoint>>() {}.getType());

			log.info("Assert.");
			assertEquals(stringlist.get(1).getAddress(), result.get(1).getAddress());
			log.info("Test passed. Result={}", result.get(1));
		} catch (Exception e) {
			log.error("Cannot test system", e);
			fail("Error");
		}
	}

	@Test
	public void jsonrpcResponseDatapointConversionTest() {
		log.info("Start datapoint tester");
		try {

			List<Datapoint> stringlist = Arrays.asList(Datapoints.newDatapoint("test1"), Datapoints.newDatapoint("test2"), Datapoints.newDatapoint("test3"));

			//Convert to JsonArray
			//JsonArray array = util.convertListToJsonArray(stringlist);

			//JsonArray array = util.convertListToJsonArray(stringlist);
			//Object[] input = new Object[1];
			//input[0] = array;

			JsonRpcResponse repsonse = new JsonRpcResponse(new JsonRpcRequest("test", false, new Object[0]), stringlist);
			//repsonse.setResult(stringlist);

			//Get first parameter and convert it
			List<Datapoint> result = repsonse.getResult(new TypeToken<List<Datapoint>>() {
			});//gson.fromJson(request.getParams()[0].toString(), new TypeToken<List<Datapoint>>() {}.getType());

			log.info("Assert.");
			assertEquals(stringlist.get(1).getAddress(), result.get(1).getAddress());
			log.info("Test passed. Result={}", result.get(1));
		} catch (Exception e) {
			log.error("Cannot test system", e);
			fail("Error");
		}
	}

}
