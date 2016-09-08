package at.tuwien.ict.acona.cell.storage;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.core.CellImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.cell.storage.DataStorage;
import at.tuwien.ict.acona.cell.storage.DataStorageImpl;
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
	public void writerAndReadTest() {
		log.debug("Start write and read test");

		try {
			//write data
			this.data.write(Datapoint.newDatapoint(address).setValue(value), dataprovider);
			
			//read written data
			String actualResult = this.data.read(address).getValue().getAsString();
			
			//assert with proposed data
			log.debug("expected result={}, actual result={}", value, actualResult);
			assertEquals(value, actualResult);
			
			
			
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
			data.write(Datapoint.newDatapoint(address).setValue(value), dataprovider);
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
				data.write(Datapoint.newDatapoint(address).setValue(value), dataprovider);
				
				data.unsubscribeDatapoint(address, sub1.getName());
				
				data.write(Datapoint.newDatapoint(address).setValue(value2), dataprovider);
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
