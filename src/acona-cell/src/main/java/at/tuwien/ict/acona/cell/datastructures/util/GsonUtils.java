package at.tuwien.ict.acona.cell.datastructures.util;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class GsonUtils {

	private static Gson gson = new Gson();
	private static Gson gsonExtended = new GsonBuilder().setPrettyPrinting().create();

	public static enum ConflictStrategy {
		THROW_EXCEPTION, PREFER_FIRST_OBJ, PREFER_SECOND_OBJ, PREFER_NON_NULL;
	}

	public static class JsonObjectExtensionConflictException extends Exception {

		/**
		* 
		*/
		private static final long serialVersionUID = 1L;

		public JsonObjectExtensionConflictException(String message) {
			super(message);
		}

	}

	/**
	 * Json merger for Gson from
	 * http://stackoverflow.com/questions/34092373/merge-extend-json-objects-
	 * using-gson-in-java
	 * 
	 * @param destinationObject
	 * @param conflictResolutionStrategy
	 * @param objs
	 * @throws JsonObjectExtensionConflictException
	 */
	public static void extendJsonObject(JsonObject destinationObject, ConflictStrategy conflictResolutionStrategy,
			JsonObject... objs) throws JsonObjectExtensionConflictException {
		for (JsonObject obj : objs) {
			extendJsonObject(destinationObject, obj, conflictResolutionStrategy);
		}
	}

	private static void extendJsonObject(JsonObject leftObj, JsonObject rightObj, ConflictStrategy conflictStrategy)
			throws JsonObjectExtensionConflictException {
		for (Map.Entry<String, JsonElement> rightEntry : rightObj.entrySet()) {
			String rightKey = rightEntry.getKey();
			JsonElement rightVal = rightEntry.getValue();
			if (leftObj.has(rightKey)) {
				// conflict
				JsonElement leftVal = leftObj.get(rightKey);
				if (leftVal.isJsonArray() && rightVal.isJsonArray()) {
					JsonArray leftArr = leftVal.getAsJsonArray();
					JsonArray rightArr = rightVal.getAsJsonArray();
					// concat the arrays -- there cannot be a conflict in an
					// array, it's just a collection of stuff
					for (int i = 0; i < rightArr.size(); i++) {
						leftArr.add(rightArr.get(i));
					}
				} else if (leftVal.isJsonObject() && rightVal.isJsonObject()) {
					// recursive merging
					extendJsonObject(leftVal.getAsJsonObject(), rightVal.getAsJsonObject(), conflictStrategy);
				} else {// not both arrays or objects, normal merge with
						// conflict resolution
					handleMergeConflict(rightKey, leftObj, leftVal, rightVal, conflictStrategy);
				}
			} else {// no conflict, add to the object
				leftObj.add(rightKey, rightVal);
			}
		}
	}

	private static void handleMergeConflict(String key, JsonObject leftObj, JsonElement leftVal, JsonElement rightVal,
			ConflictStrategy conflictStrategy) throws JsonObjectExtensionConflictException {

		switch (conflictStrategy) {
		case PREFER_FIRST_OBJ:
			break;// do nothing, the right val gets thrown out
		case PREFER_SECOND_OBJ:
			leftObj.add(key, rightVal);// right side auto-wins, replace left
										// val with its val
			break;
		case PREFER_NON_NULL:
			// check if right side is not null, and left side is null, in
			// which case we use the right val
			if (leftVal.isJsonNull() && !rightVal.isJsonNull()) {
				leftObj.add(key, rightVal);
			} // else do nothing since either the left value is non-null or
				// the right value is null
			break;
		case THROW_EXCEPTION:
			throw new JsonObjectExtensionConflictException("Key " + key
					+ " exists in both objects and the conflict resolution strategy is " + conflictStrategy);
		default:
			throw new UnsupportedOperationException(
					"The conflict strategy " + conflictStrategy + " is unknown and cannot be processed");
		}
	}

	/**
	 * Convert any jsonarray with convertable objects to a List
	 * 
	 * @param jsonArray
	 * @param clzz
	 * @return
	 */
	public static List<Datapoint> convertJsonArrayToDatapointList(JsonArray jsonArray) {
		List<Datapoint> result = gson.fromJson(jsonArray.toString(), new TypeToken<List<Datapoint>>() {
		}.getType());
		return result;
	}

	/**
	 * Convert any jsonarray with convertable objects to a List
	 * 
	 * @param jsonArray
	 * @param clzz
	 * @return
	 */
	public static <T> List<T> convertJsonArrayToList(JsonArray jsonArray, TypeToken<T> token) {
		List<T> result = gson.fromJson(jsonArray.toString(), token.getType());
		return result;
	}

	/**
	 * Convert any list with jsonconvertable objects to jsonarray
	 * 
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public static JsonArray convertListToJsonArray(List<?> list) throws Exception {
		Gson gson = new Gson();
		JsonElement element = gson.toJsonTree(list, new TypeToken<List<?>>() {
		}.getType());

		if (!element.isJsonArray()) {
			// fail appropriately
			throw new Exception("Element is no JsonArray");
		}

		JsonArray jsonArray = element.getAsJsonArray();
		return jsonArray;
	}

	//	public String jsonPrettyPrint(Object obj) {
	//		return gsonExtended.toJson(obj);
	//	}
}
