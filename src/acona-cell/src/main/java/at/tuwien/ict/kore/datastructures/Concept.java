package at.tuwien.ict.kore.datastructures;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Concept extends KoreDatastructure {

	public final static String DATATYPE = "CONCEPT";

	public final static String PROPERTYMETADATA = "hasMetadata";
	public final static String PROPERTYSUBCONEPT = "hasSubConcept";

	public static Concept newConcept(String name) throws Exception {
		return new Concept(name);
	}

	public static Concept newConcept(JsonObject object) throws Exception {
		return new Concept(object);
	}

	public static boolean isConcept(JsonObject object) {
		boolean result = false;

		if (object.has(PROPERTYMETADATA) && object.has(PROPERTYSUBCONEPT)
				&& object.get(PROPERTYSUBCONEPT).isJsonArray()) {
			result = true;
		}

		return result;
	}

	private Concept(String name) throws Exception {
		super(name, DATATYPE);
		this.getChunk().setValue(PROPERTYMETADATA, Chunk.nullChunk());
		this.getChunk().setAssociatedContent(PROPERTYSUBCONEPT, Chunk.nullChunk());
	}

	private Concept(JsonObject object) throws Exception {
		super(object);
		if (Concept.isConcept(object) == false) {
			throw new Exception("This structure is no concept " + object);
		}
	}

	public Concept addSubconcept(Concept concept) {
		this.getChunk().setAssociatedContent(PROPERTYSUBCONEPT, concept.getChunk());
		return this;
	}

	public List<Concept> getSubConcept(String id) {
		List<Concept> result = new ArrayList<Concept>();

		JsonArray array = this.getChunk().getAssociatedContentAsArray(PROPERTYSUBCONEPT);

		array.forEach(element -> {

			try {
				result.add(Concept.newConcept(element.getAsJsonObject()));
			} catch (Exception e) {
				log.error("Cannot create a concept from " + element);
			}
		});

		return result;
	}

	public Concept setMetadata(Metadata metadata) {
		this.getChunk().setValue(PROPERTYMETADATA, metadata.getChunk());
		return this;
	}

	public Concept setValue(String key, String value) {
		this.getChunk().setValue(key, value);
		return this;
	}

	public Concept setValue(String key, double value) {
		this.getChunk().setValue(key, value);
		return this;
	}

	public Concept setValue(String key, int value) {
		this.getChunk().setValue(key, value);
		return this;
	}

	public Concept setValue(String key, boolean value) {
		this.getChunk().setValue(key, value);
		return this;
	}

	public Concept setValue(String key, Chunk chunk) {
		this.getChunk().setValue(key, chunk);
		return this;
	}

	public String getValue(String key) {
		return this.getChunk().getValue(key);
	}

	public double getDoubleValue(String key) {
		return this.getChunk().getDoubleValue(key);
	}

	public Metadata getMetadata() throws Exception {
		return Metadata.newMetadata(this.getChunk().getValueAsChunk(PROPERTYMETADATA).toJsonObject());
	}

}
