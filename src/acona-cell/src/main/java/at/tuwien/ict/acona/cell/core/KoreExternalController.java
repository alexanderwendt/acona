package at.tuwien.ict.acona.cell.core;

import com.google.gson.JsonObject;

public interface KoreExternalController {
	public void executeUserInput(String command, String parameter);
	
	public KoreExternalController init(JsonObject config);
	public KoreExternalController init(String filePath);
	public void sendCommands();
}
