package at.tuwien.ict.acona.cell.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.tuwien.ict.acona.cell.datastructures.Message;
import at.tuwien.ict.acona.cell.testing.AconaSyncSequenceReceiver;
import at.tuwien.ict.acona.cell.testing.AconaSyncSequenceSender;
import at.tuwien.ict.acona.cell.testing.BaseCellTester;
import at.tuwien.ict.acona.cell.testing.GlobalLogEntry;
import at.tuwien.ict.acona.cell.testing.GlobalLogger;
import at.tuwien.ict.acona.cell.testing.O2AExecutionCellImpl;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class SyncCommunicationTest extends BaseCellTester {
	private static Logger log = LoggerFactory.getLogger(SyncCommunicationTest.class);
	AgentController sender = null;
	AgentController receiver = null;
	
	@Override
	protected void createAgents() throws StaleProxyException {
		
	}
	
	protected ACLMessage prepareMessage(String content) {
		ACLMessage message = null;
		message = new ACLMessage(ACLMessage.REQUEST);
		message.addReceiver(new AID("Receiver", false));
		message.setSender(new AID("Sender", false));
		message.setEncoding("DEBUG");
		message.setContent(content);

		return message;
	}
	
	protected List<ACLMessage> getMessageSequence1 () {
		List<ACLMessage> result = new ArrayList<>();

		for(int i = 0; i < 100; ++i) {
			Message message = Message.newMessage();
			message.setContent("message " + Integer.toString(i));
			result.add(prepareMessage(message.toString()));
		}
		
		return result;
	}
	
	private class GlobalLogEntryPattern {
		public GlobalLogEntryPattern(AID agent, MessageTemplate message) {
			this.agent = agent;
			this.message = message;
		}
		public final AID agent;
		public final MessageTemplate message;
		
		public String matches(GlobalLogEntry entry) {
			String error = "";
			
			if(!entry.getAgent().equals(agent)) {
				error = "Message was logged by wrong agent!\nExpected: " + message.toString() + "\nLogged: " + entry.getMessage().toString();
			}
			
			if(!message.match(entry.getMessage())) {
				error = "Message did not match expected format!\nExpected: " + message.toString() + "\nLogged: " + entry.getMessage().toString();
			}
			
			return error;
		}
		
		@Override
		public String toString() {
			return "Pattern: " + agent.getLocalName() + ": " + message.toString() + "\n";
		}
	}
	
	protected int getNextSuccessfullPerformative(int currentPerformative) {
		switch(currentPerformative) {
		case ACLMessage.REQUEST:
			return ACLMessage.INFORM;
		default:
			throw new RuntimeException("Unexpected performative with number " + currentPerformative + " encountered - can not predict next successfull performative - breaking test");
		}
	}
	
	protected MessageTemplate getSenderRequestTemplate(AID receiver, ACLMessage message) {
		MessageTemplate cond1 = MessageTemplate.MatchSender(receiver);
		MessageTemplate cond2 = MessageTemplate.MatchReceiver(Arrays.asList(message.getSender()).toArray(new AID[1]));
		MessageTemplate cond3 = MessageTemplate.MatchProtocol(message.getProtocol());
		MessageTemplate cond4 = MessageTemplate.MatchConversationId(message.getConversationId());
		MessageTemplate cond5 = MessageTemplate.MatchInReplyTo(message.getReplyWith());
		//MessageTemplate cond6 = MessageTemplate.MatchPerformative(message.getPerformative());
		MessageTemplate cond7 = MessageTemplate.MatchEncoding(message.getEncoding());
		
		return MessageTemplate.and(
				cond1, MessageTemplate.and(
				cond2, MessageTemplate.and(
				cond3, MessageTemplate.and(
				cond4, MessageTemplate.and(
				cond5, cond7)))));
	}
	
	protected MessageTemplate getReceiverRequestTemplate(AID receiver, ACLMessage message) {
		MessageTemplate cond1 = MessageTemplate.MatchSender(message.getSender());
		MessageTemplate cond2 = MessageTemplate.MatchReceiver(Arrays.asList(receiver).toArray(new AID[1]));
		MessageTemplate cond3 = MessageTemplate.MatchProtocol(message.getProtocol());
		MessageTemplate cond4 = MessageTemplate.MatchConversationId(message.getConversationId());
		MessageTemplate cond5 = MessageTemplate.MatchReplyWith(message.getReplyWith());
		//MessageTemplate cond6 = MessageTemplate.MatchPerformative(getNextSuccessfullPerformative(message.getPerformative()));
		MessageTemplate cond7 = MessageTemplate.MatchEncoding(message.getEncoding());
		
		return MessageTemplate.and(
				cond1, MessageTemplate.and(
				cond2, MessageTemplate.and(
				cond3, MessageTemplate.and(
				cond4, MessageTemplate.and(
				cond5, cond7)))));
	}
	
	protected AID getOnlyReceiver(ACLMessage message) {
		AID receiver = null;
		
		Iterator<?> iter = message.getAllReceiver();
		
		while(iter.hasNext()) {
			if(receiver == null) {
				receiver = (AID) iter.next();
			} else {
				throw new RuntimeException("Message was supposed to have only a single receptient, but contained at least 2. Message:\n" + message.toString());
			}
		}
		
		return receiver;
	}
	
	protected Queue<GlobalLogEntryPattern> generateExpectedSequence(ACLMessage message) {
		AID sender = message.getSender();
		AID receiver = null;
		
		Queue<GlobalLogEntryPattern> expected = new LinkedList<>();
		Iterator<?> iter = message.getAllReceiver();
		
		while(iter.hasNext()) {
			receiver = (AID) iter.next();
			expected.add(new GlobalLogEntryPattern(receiver, getReceiverRequestTemplate(receiver, message)));
			expected.add(new GlobalLogEntryPattern(message.getSender(), getSenderRequestTemplate(receiver, message)));
		}

		return expected;
	}
	
	protected Queue<GlobalLogEntryPattern> generateExpectedSequence(List<ACLMessage> sendSequence) {
		Queue<GlobalLogEntryPattern> expected = new LinkedList<>();
		
		for(ACLMessage message : sendSequence) {
			expected.addAll(generateExpectedSequence(message));
		}
		
		return expected;
	}
	
	protected List<String> checkMessageSequence(List<ACLMessage> sendSequence, Queue<GlobalLogEntry> loggedSequence) {
		Queue<GlobalLogEntryPattern> expected = generateExpectedSequence(sendSequence);
		Iterator<GlobalLogEntry> logIterator = loggedSequence.iterator();
		List<String> errors = new ArrayList<>();
		int messageCount = 0;
		
		for(GlobalLogEntryPattern pattern : expected) {
			if(logIterator.hasNext()) {
				String errorMessage = pattern.matches(logIterator.next());
				if(!errorMessage.isEmpty()) {
					errors.add(new String("Message " + Integer.toString(messageCount) + ": " + errorMessage.toString()));
				}
			} else {
				errors.add(new String("Message " + Integer.toString(messageCount) + ": no message looged!"));
			}
			messageCount++;
		}
		
		return errors;
	}
	
	/**
	 * Tests sync sending base functionality by sending a single message to a loopback agent that will return an empty INFORM message
	 */
	@Test
	public void syncSend1() {
		log.info("Starting test syncSend1");
		try {
			
			List<ACLMessage> messageSequence = getMessageSequence1();
			
			sender = newAgent("Sender", AconaSyncSequenceSender.class, messageSequence, 3000, false);
			receiver = newAgent("Receiver", AconaSyncSequenceReceiver.class);
			
			startAgent(receiver);
			startAgent(sender);
			
			System.out.println("Waiting for the sender to finish");
			try {
				synchronized (this) {
					wait(5000);					
					System.out.println("Waiting finished");
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			log.debug(GlobalLogger.getLog().toString());
			
			List<String> errors = checkMessageSequence(messageSequence, GlobalLogger.getLog());
			
			if(!errors.isEmpty()) {
				log.error("Errors: ");
				for(String error : errors) {
					log.error(error);
				}
			}
			
			assertTrue(errors.isEmpty());
		} catch (StaleProxyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.info("Finished test syncSend1");
	}
}
