package at.tuwien.ict.acona.cell.datastructures;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Chunk implements ChunkMethods {

	private static Logger log = LoggerFactory.getLogger(Chunk.class);

	private JsonObject chunkObject;
	//private Gson gson = new Gson();

	public static final String NAMEPROPERTY = "hasName";
	public static final String TYPEPROPERTY = "hasType";
	public static final String IDPROPERTY = "hasID";

	public static Chunk newChunk(String name, String type) throws Exception {
		return new Chunk(name, type);
	}

	public static Chunk newChunk(JsonObject object) throws Exception {
		Chunk result = null;

		if (Chunk.isChunk(object) == true) {
			result = new Chunk(object);
		} else {
			throw new ClassCastException("The object " + object + " is no chunk object");
		}

		return result;

	}

	public static Chunk newChunk(Chunk object) throws Exception {
		return new Chunk(object.toJsonObject());
	}

	public Chunk(JsonObject object) throws Exception {
		if (isChunk(object) == true) {
			this.chunkObject = object;
		} else {
			throw new Exception("This object is no chunk, " + object);
		}
	}

	private Chunk(String name, String type) throws Exception {
		this.init(name, type);
	}

	public static boolean isChunk(JsonObject object) {
		boolean result = false;

		if (object.get(NAMEPROPERTY) != null && object.get(TYPEPROPERTY) != null && object.get(IDPROPERTY) != null) {
			result = true;
		}

		return result;
	}

	public static Chunk nullChunk() throws Exception {
		return new Chunk("null", Chunk.class.getSimpleName());
	}

	public static boolean isNullChunk(Chunk chunk) {
		boolean result = false;

		if (chunk.getName().equals("null")) {
			result = true;
		}

		return result;
	}

	private void init(String name, String type) {
		chunkObject = new JsonObject();
		this.setId(name + this.hashCode());
		this.setName(name);
		this.setType(type);
	}

	@Override
	public void setName(String name) {
		chunkObject.addProperty(NAMEPROPERTY, name);
	}

	@Override
	public void setId(String id) {
		chunkObject.addProperty(IDPROPERTY, id);
	}

	@Override
	public void setType(String type) {
		chunkObject.addProperty(TYPEPROPERTY, type);

	}

	@Override
	public void setValue(String key, String value) {
		this.chunkObject.addProperty(key, value);
	}

	@Override
	public void setValue(String key, double value) {
		this.chunkObject.addProperty(key, value);
	}

	@Override
	public void setValue(String key, int value) {
		this.chunkObject.addProperty(key, value);
	}

	@Override
	public void setValue(String key, boolean value) {
		this.chunkObject.addProperty(key, value);
	}

	@Override
	public void setAssociatedContent(String association, Chunk content) {
		if (this.chunkObject.getAsJsonArray(association) == null) {
			this.chunkObject.add(association, new JsonArray());
		}

		if (Chunk.isNullChunk(content) == false) {
			this.chunkObject.getAsJsonArray(association).add(content.toJsonObject());
		}

	}

	@Override
	public String getName() {
		return this.chunkObject.getAsJsonPrimitive(NAMEPROPERTY).getAsString();
	}

	@Override
	public String getID() {
		return this.chunkObject.getAsJsonPrimitive(IDPROPERTY).getAsString();
	}

	@Override
	public String getValue(String key) {
		return this.chunkObject.getAsJsonPrimitive(key).getAsString();
	}

	@Override
	public double getDoubleValue(String key) {
		return this.chunkObject.getAsJsonPrimitive(key).getAsDouble();
	}

	@Override
	public List<Chunk> getAssociatedContent(String key) {
		JsonArray array = this.chunkObject.get(key).getAsJsonArray();
		List<Chunk> result = new ArrayList<>();
		array.forEach(element -> {
			try {
				result.add(new Chunk(element.getAsJsonObject()));
			} catch (Exception e) {
				log.error("Corrupted jsonobject. Cannot be converted into a chunk", element);
			}
		});

		return result;
	}

	public JsonArray getAssociatedContentAsArray(String key) {
		JsonArray array = this.chunkObject.get(key).getAsJsonArray();

		return array;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.chunkObject);
		return builder.toString();
	}

	@Override
	public String getType() {
		return this.chunkObject.getAsJsonPrimitive(TYPEPROPERTY).getAsString();
	}

	@Override
	public JsonObject toJsonObject() {
		return this.chunkObject;
	}

	@Override
	public void setValue(String key, Chunk value) {
		this.chunkObject.add(key, value.toJsonObject());

	}

	@Override
	public Chunk getValueAsChunk(String key) throws Exception {
		return new Chunk(this.chunkObject.getAsJsonObject(key));
	}

}
