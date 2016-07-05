package at.tuwien.ict.acona.cell.temporarysubscriptions;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import at.tuwien.ict.acona.cell.core.Cell;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class TemporarySubscription {
	private final Cell cell;
	private final SynchronousQueue<Datapoint> queue = new SynchronousQueue<Datapoint>();
	private final String subscriptionAddress;
	
	public TemporarySubscription(Cell cell, String subscriptionAddress) {
		//Get variables
		this.cell = cell;
		this.subscriptionAddress =  subscriptionAddress;
		
		//Subscribe
		this.cell.getDataStorage().subscribeDatapoint(subscriptionAddress, this.cell.getName());		
	}	
	
//	public Datapoint read(int timeout) {
//		Datapoint result = queue.poll(timeout, TimeUnit.MILLISECONDS);
//	}
	
}
