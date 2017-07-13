package at.tuwien.ict.acona.evolutiondemo.controlleragent;

import org.slf4j.Logger;

import at.tuwien.ict.commonutils.simpleuserconsole.UserConsoleFunction;


public class RequestReceiverUserConsole extends UserConsoleFunction {
	
	private ConsoleRequestReceiver client;
	
	/**
	 * Set the device, which shall be used 
	 */
	public RequestReceiverUserConsole(Logger log, ConsoleRequestReceiver client) {
		//Set the name of the class here
		super(log, RequestReceiverUserConsole.class);
		
		this.client = client;
	}
	
	/* (non-Javadoc)
	 * @see commonclasses.userconsole.UserConsoleFunction#registerFunctions()
	 */
	@Override
	protected void registerFunctions() {
		//Register all functions, which shall be used in the user console
		
		this.register("exit", "exit program", "exitProgram");
		this.register("restart", "restart tapchanger", "restart");
		this.register("set", "Set command", "setExternalCommand");
		this.register("start", "Set command", "startStockMarket");
		
	}
	
	//=== individual functions for the user console ===//
	
	public void exitProgram() {
		//client.shutdown();
		try {
			client.shutDown();
		} catch (Exception e) {
			log.error("Cannot correctly exit tapchanger", e);
		}
	}
	
	public void restart() {
		client.restart();
	}
	
	public void setExternalCommand(String[] command) {
		this.client.setExternalCommand(command[0]);
	}
	
	public void startStockMarket() throws Exception {
		this.client.startStockMarket();
	}

}
