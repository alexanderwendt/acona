package at.tuwien.ict.acona.cell.datastructures;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Chunk {

	private static Logger log = LoggerFactory.getLogger(Chunk.class);

	//TODO: The nullpointer chunk shall not need any try-catch

	private JsonObject chunkObject;

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

	public static Chunk nullChunk() {
		Chunk result = null;

		try {
			result = new Chunk("null", Chunk.class.getSimpleName());
		} catch (Exception e) {
			log.error("Cannot create the null chunk. The value is NULL", e);
		}

		return result;
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

	public Chunk setName(String name) {
		chunkObject.addProperty(NAMEPROPERTY, name);
		return this;
	}

	public Chunk setId(String id) {
		chunkObject.addProperty(IDPROPERTY, id);
		return this;
	}

	public Chunk setType(String type) {
		chunkObject.addProperty(TYPEPROPERTY, type);
		return this;

	}

	public Chunk setValue(String key, String value) {
		this.chunkObject.addProperty(key, value);
		return this;
	}

	public Chunk setValue(String key, double value) {
		this.chunkObject.addProperty(key, value);
		return this;
	}

	public Chunk setValue(String key, int value) {
		this.chunkObject.addProperty(key, value);
		return this;
	}

	public Chunk setValue(String key, boolean value) {
		this.chunkObject.addProperty(key, value);
		return this;
	}

	public Chunk addAssociatedContent(String association, List<Chunk> content) {
		content.forEach(c -> {
			this.addAssociatedContent(association, c);
		});
		return this;
	}

	public Chunk addAssociatedContent(String association, Chunk content) {
		if (this.chunkObject.getAsJsonArray(association) == null) {
			this.chunkObject.add(association, new JsonArray());
		}

		if (Chunk.isNullChunk(content) == false) {
			this.chunkObject.getAsJsonArray(association).add(content.toJsonObject());
		}

		return this;
	}

	public String getName() {
		return this.chunkObject.getAsJsonPrimitive(NAMEPROPERTY).getAsString();
	}

	public String getID() {
		return this.chunkObject.getAsJsonPrimitive(IDPROPERTY).getAsString();
	}

	public String getValue(String key) {
		return this.chunkObject.getAsJsonPrimitive(key).getAsString();
	}

	public double getDoubleValue(String key) {
		return this.chunkObject.getAsJsonPrimitive(key).getAsDouble();
	}

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

	public List<Chunk> getAssociatedContentFromAttribute(String content, String subchunkAttributeName, String subChunkValue) {
		List<Chunk> result = new ArrayList<>();

		this.getAssociatedContent(content).forEach((Chunk c) -> {
			if (c.getValue(subchunkAttributeName).equals(subChunkValue)) {
				result.add(c);
			}
		});

		return result;
	}

	public Chunk getFirstAssociatedContentFromAttribute(String content, String subchunkAttributeName, String subChunkValue) {
		Chunk result = null;

		List<Chunk> allMatches = this.getAssociatedContentFromAttribute(content, subchunkAttributeName, subChunkValue);
		if (allMatches.isEmpty() == false) {
			result = allMatches.get(0);
		}

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

	public String getType() {
		return this.chunkObject.getAsJsonPrimitive(TYPEPROPERTY).getAsString();
	}

	public JsonObject toJsonObject() {
		return this.chunkObject;
	}

	public Chunk setValue(String key, Chunk value) {
		this.chunkObject.add(key, value.toJsonObject());
		return this;

	}

	public Chunk getValueAsChunk(String key) throws Exception {
		return new Chunk(this.chunkObject.getAsJsonObject(key));
	}

}
