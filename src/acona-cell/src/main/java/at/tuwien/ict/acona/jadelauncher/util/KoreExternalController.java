package at.tuwien.ict.acona.jadelauncher.util;

import com.google.gson.JsonObject;

public interface KoreExternalController {
	public void executeUserInput(String command, String parameter);
	/**
	 * 
	 * 
	 * @param config
	 * @return
	 */
	public KoreExternalController init(JsonObject config);
	public KoreExternalController init(String filePath);
	public ControllerCellGateway getAgent(String localName);
	public ControllerCellGateway getTopController();
	
	
	public void sendCommands();
}
