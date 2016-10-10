package _OLD_at.tuwien.ict.acona.cell.testing;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class GlobalLogger {
	private static Queue<GlobalLogEntry> log = new ConcurrentLinkedQueue<>();
	
	public static void log(Agent agent, ACLMessage message) {
		log.add(new GlobalLogEntry(agent.getAID(), message));
	}
	
	public static Queue<GlobalLogEntry> getLog() {
		return log;
	}
}
