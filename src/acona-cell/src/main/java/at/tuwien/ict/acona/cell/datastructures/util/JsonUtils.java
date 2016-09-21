package at.tuwien.ict.acona.cell.datastructures.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class JsonUtils {
	 private static final Gson gson = new Gson();

	  private JsonUtils(){}

	  public static boolean isJSONValid(String JSON_STRING) {
	      try {
	          gson.fromJson(JSON_STRING, JsonObject.class);
	          return true;
	      } catch (Exception ex) { 
	          return false;
	      }
	  }
}
