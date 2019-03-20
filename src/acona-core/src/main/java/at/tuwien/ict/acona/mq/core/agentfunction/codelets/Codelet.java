package at.tuwien.ict.acona.mq.core.agentfunction.codelets;

public interface Codelet {
	/**
	 * Public startExecution(): void; Nonblocking; Trigger start of codelet
	 */
	public void startCodelet();
	
	public void resetCodelet();
	
	public void shutDownCodelet() throws Exception;

}
