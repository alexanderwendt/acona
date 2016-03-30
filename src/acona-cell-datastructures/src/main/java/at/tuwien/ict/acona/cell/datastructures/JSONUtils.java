package at.tuwien.ict.acona.cell.datastructures;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class JSONUtils {
	 private static final Gson gson = new Gson();

	  private JSONUtils(){}

	  public static boolean isJSONValid(String JSON_STRING) {
	      try {
	          gson.fromJson(JSON_STRING, JsonObject.class);
	          return true;
	      } catch (Exception ex) { 
	          return false;
	      }
	  }
}
