package at.tuwien.ict.acona.jadelauncher.util;

import com.google.gson.JsonObject;

public interface KoreExternalController {
	public void executeUserInput(String command, String parameter);
	public KoreExternalController init(JsonObject config);
	public KoreExternalController init(String filePath);
	public void sendCommands();
}
