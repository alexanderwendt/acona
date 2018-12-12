package at.tuwien.ict.acona.mq.datastructures;

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
	//public static final String IDPROPERTY = "hasID";

	public Chunk(JsonObject object) throws Exception {
		if (ChunkBuilder.isChunk(object) == true) {
			this.chunkObject = object;
		} else {
			throw new Exception("This object is no chunk, " + object);
		}
	}

	protected Chunk(String name, String type) throws Exception {
		this.init(name, type);
	}

	private void init(String name, String type) {
		chunkObject = new JsonObject();
		//this.setId(name + this.hashCode());
		this.setName(name);
		this.setType(type);
	}

	public Chunk setName(String name) {
		chunkObject.addProperty(NAMEPROPERTY, name);
		return this;
	}

	//	public Chunk setId(String id) {
	//		chunkObject.addProperty(IDPROPERTY, id);
	//		return this;
	//	}

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

	/**
	 * @param association
	 * @param content
	 * @return
	 */
	public Chunk addAssociatedContent(String association, List<Chunk> content) {
		content.forEach(c -> {
			this.addAssociatedContent(association, c);
		});
		return this;
	}

	/**
	 * @param association
	 * @param content
	 * @return
	 */
	public Chunk addAssociatedContent(String association, Chunk content) {
		if (this.chunkObject.getAsJsonArray(association) == null) {
			this.chunkObject.add(association, new JsonArray());
		}

		if (ChunkBuilder.isNullChunk(content) == false) {
			this.chunkObject.getAsJsonArray(association).add(content.toJsonObject());
		}

		return this;
	}

	public void removeAssociatedContent(String association, Chunk content) {
		if (this.chunkObject.getAsJsonArray(association) != null) {
			this.chunkObject.getAsJsonArray(association).remove(content.toJsonObject());
		}
	}

	public String getName() {
		String name = "";
		if (this.chunkObject.has(NAMEPROPERTY)) {
			name = this.chunkObject.getAsJsonPrimitive(NAMEPROPERTY).getAsString();
		}

		return name;
	}

	//	public String getID() {
	//		return this.chunkObject.getAsJsonPrimitive(IDPROPERTY).getAsString();
	//	}

	public String getValue(String key) {
		String result = "";

		if (this.chunkObject.has(key)) {
			result = this.chunkObject.getAsJsonPrimitive(key).getAsString();
		}
		return result;
	}

	/**
	 * @param key
	 * @return
	 */
	public double getDoubleValue(String key) {
		return this.chunkObject.getAsJsonPrimitive(key).getAsDouble();
	}

	/**
	 * @param key
	 * @return
	 */
	public List<Chunk> getAssociatedContent(String key) {
		List<Chunk> result = new ArrayList<>();

		if (this.chunkObject.has(key)) {
			JsonArray array = this.chunkObject.get(key).getAsJsonArray();

			array.forEach(element -> {
				try {
					result.add(new Chunk(element.getAsJsonObject()));
				} catch (Exception e) {
					log.error("Corrupted jsonobject. Cannot be converted into a chunk", element);
				}
			});
		}

		return result;
	}

	/**
	 * From a chunk with associated other chunks, search for all subs chunks
	 * with a certain predicate in the association and a certain attribute like
	 * name and if the value of the name matches a reference value, the sub
	 * chunk is added to the list
	 * 
	 * @param predicate:
	 *            Set the predicate e.g. hasCondition
	 * @param subchunkAttributeName:
	 *            Define the name of the attribute that shall be received
	 * @param subChunkCompareValue:
	 *            Define the value of the received attribute
	 * @return
	 */
	public List<Chunk> getAssociatedContentFromAttribute(String predicate, String subchunkAttributeName, String subChunkCompareValue) {
		List<Chunk> result = new ArrayList<>();

		this.getAssociatedContent(predicate).forEach((Chunk c) -> {
			if (c.getValue(subchunkAttributeName).equals(subChunkCompareValue)) {
				result.add(c);
			}
		});

		return result;
	}

	/**
	 * @param predicate
	 * @param subchunkAttributeName
	 * @param subChunkValue
	 * @return
	 */
	public Chunk getFirstAssociatedContentFromAttribute(String predicate, String subchunkAttributeName, String subChunkValue) {
		Chunk result = ChunkBuilder.nullChunk();

		List<Chunk> allMatches = this.getAssociatedContentFromAttribute(predicate, subchunkAttributeName, subChunkValue);
		if (allMatches.isEmpty() == false) {
			result = allMatches.get(0);
		}

		return result;
	}

	/**
	 * @param key
	 * @return
	 */
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

	/**
	 * @return
	 */
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
