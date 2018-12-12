package at.tuwien.ict.acona.mq.datastructures;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

public class ChunkBuilder {
	public static final String NAMEPROPERTY = "hasName";
	public static final String TYPEPROPERTY = "hasType";

	private static Logger log = LoggerFactory.getLogger(ChunkBuilder.class);

	public synchronized static Chunk newChunk(String name, String type) throws Exception {
		return new Chunk(name, type);
	}

	public synchronized static Chunk newChunk(JsonObject object) throws Exception {
		Chunk result = null;

		if (ChunkBuilder.isChunk(object) == true) {
			result = new Chunk(object);
		} else {
			throw new ClassCastException("The object " + object + " is no chunk object");
		}

		return result;

	}

	public synchronized static Chunk newChunk(Chunk object) throws Exception {
		return new Chunk(object.toJsonObject());
	}

	public synchronized static boolean isChunk(JsonObject object) {
		boolean result = false;

		if (object.get(NAMEPROPERTY) != null && object.get(TYPEPROPERTY) != null) {
			result = true;
		}

		return result;
	}

	public synchronized static Chunk nullChunk() {
		Chunk result = null;

		try {
			result = new Chunk("null", Chunk.class.getSimpleName());
		} catch (Exception e) {
			log.error("Cannot create the null chunk. The value is NULL", e);
		}

		return result;
	}

	public synchronized static boolean isNullChunk(Chunk chunk) {
		boolean result = false;

		if (chunk.getName().equals("null")) {
			result = true;
		}

		return result;
	}
}
