package at.tuwien.ict.kore.communicator.core;

import com.google.gson.JsonObject;

public interface ListenerModule {
	public void updateValue(JsonObject message);
}
