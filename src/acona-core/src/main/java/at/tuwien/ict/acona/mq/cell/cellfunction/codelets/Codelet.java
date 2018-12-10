package at.tuwien.ict.acona.mq.cell.cellfunction.codelets;

public interface Codelet {
	/**
	 * Public startExecution(): void; Nonblocking; Trigger start of codelet
	 */
	public void startCodelet();
	
	public void resetCodelet();
	
	public void shutDown();

}
