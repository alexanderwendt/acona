package at.tuwien.ict.acona.cell.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.datastructures.Datapoints;
import at.tuwien.ict.acona.cell.storage.helpers.DataStorageSubscriberNotificatorMock;
import at.tuwien.ict.acona.cell.storage.helpers.SubscriberMock;

public class DataStorageTester {

	protected static Logger log = LoggerFactory.getLogger(DataStorageTester.class);

	private DataStorageSubscriberNotificatorMock notificator;
	private DataStorage data;
	private SubscriberMock sub1;
	private SubscriberMock sub2;
	private String subscriber1 = "SUBSCRIBER1";
	private String subscriber2 = "SUBSCRIBER2";
	private String dataprovider = "PROVIDER";
	private String address = "a1";
	private String value = "test";
	private String value2 = "test2";

	@Before
	public void setUp() {
		log.debug("Setup test");
		notificator = new DataStorageSubscriberNotificatorMock();
		data = new DataStorageImpl().init(notificator);
		sub1 = new SubscriberMock();
		sub2 = new SubscriberMock();
		sub1.setName(subscriber1);
		sub2.setName(subscriber2);
		notificator.addSubscriber(sub1);
		notificator.addSubscriber(sub2);
	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void appendAndReadTest() {
		log.debug("Start write and read test");

		try {
			JsonObject obj1 = new JsonObject();
			obj1.add("property1", new JsonPrimitive("value1"));

			JsonObject obj2 = new JsonObject();
			obj1.add("property2", new JsonPrimitive("value2"));

			JsonObject expectation = new JsonObject();
			expectation.add("property1", new JsonPrimitive("value1"));
			expectation.add("property2", new JsonPrimitive("value2"));

			//write data
			this.data.write(Datapoints.newDatapoint(address).setValue(obj1), dataprovider);

			//Append second data
			this.data.append(Datapoints.newDatapoint(address).setValue(obj2), dataprovider);

			//read written data
			JsonObject actualResult = this.data.readFirst(address).getValue().getAsJsonObject();

			//assert with proposed data
			log.debug("expected result={}, actual result={}", expectation, actualResult);
			assertEquals(expectation.toString(), actualResult.toString());

		} catch (Exception e) {
			log.error("Failed test due to error", e);
			fail("Error");
		}
	}

	@Test
	public void writeAndReadTest() {
		log.debug("Start write and read test");

		try {
			//write data
			this.data.write(Datapoints.newDatapoint(address).setValue(value), dataprovider);

			//read written data
			String actualResult = this.data.readFirst(address).getValue().getAsString();

			//assert with proposed data
			log.debug("expected result={}, actual result={}", value, actualResult);
			assertEquals(value, actualResult);

		} catch (Exception e) {
			log.error("Failed test due to error", e);
			fail("Error");
		}
	}

	@Test
	public void readwildcardsTest() {
		log.debug("Start Read with wildcards test");

		try {
			//write data
			String address1 = "datapoint.xxx.sss.ttt";
			String address2 = "datapoint.sss.ddd.rrr";
			String address3 = "datapoint.sss.xxx.aaa";

			String wildcardaddress = "datapoint.ss*";

			this.data.write(Datapoints.newDatapoint(address1).setValue(value), dataprovider);
			this.data.write(Datapoints.newDatapoint(address2).setValue(value), dataprovider);
			this.data.write(Datapoints.newDatapoint(address3).setValue(value), dataprovider);

			//read written data
			List<Datapoint> actualResult = this.data.read(wildcardaddress);

			//assert with proposed data
			log.debug("expected result={}, actual result={}", 2, actualResult);
			assertEquals(2, actualResult.size());

		} catch (Exception e) {
			log.error("Failed test due to error", e);
			fail("Error");
		}
	}

	@Test
	public void subscribeAndNotifyTest() {
		try {
			//REgister subscribers
			data.subscribeDatapoint(this.address, sub1.getName());
			data.subscribeDatapoint(this.address, sub2.getName());
			//Write data
			data.write(Datapoints.newDatapoint(address).setValue(value), dataprovider);
			//Get data from subscriberMock
			String actualValue = sub2.getValue();

			log.debug("expected result={}, actual result={}", value, actualValue);
			assertEquals(value, actualValue);

		} catch (Exception e) {
			log.error("Failed test due to error", e);
			fail("Error");
		}
	}

	@Test
	public void unSubscribeTest() {
		try {
			try {
				//REgister subscribers
				data.subscribeDatapoint(this.address, sub1.getName());
				//Write data
				data.write(Datapoints.newDatapoint(address).setValue(value), dataprovider);

				data.unsubscribeDatapoint(address, sub1.getName());

				data.write(Datapoints.newDatapoint(address).setValue(value2), dataprovider);
				//Get data from subscriberMock
				String actualValue = sub1.getValue();

				log.debug("expected result={}, actual result={}", value, actualValue);
				assertEquals(value, actualValue);

			} catch (Exception e) {
				log.error("Failed test due to error", e);
				fail("Error");
			}

		} catch (Exception e) {
			log.error("Failed test due to error", e);
			fail("Error");
		}
	}

}
