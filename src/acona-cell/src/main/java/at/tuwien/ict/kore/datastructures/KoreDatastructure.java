package at.tuwien.ict.kore.datastructures;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Kore drive is the mean of evaluation for the system. It also represents the
 * system goals.
 * 
 * @author wendt
 *
 */
public abstract class KoreDatastructure {

	protected final static Gson gson = new Gson();
	protected static Logger log = LoggerFactory.getLogger(KoreDatastructure.class);

	private Chunk chunk;

	public KoreDatastructure(String name, String type) throws Exception {
		try {
			chunk = new Chunk(name, type);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	public KoreDatastructure(JsonObject chunk) throws Exception {
		try {
			if (Chunk.isChunk(chunk)) {
				this.chunk = new Chunk(chunk);
			} else {
				throw new Exception("this is no chunk " + chunk);
			}
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	public KoreDatastructure setName(String name) {
		this.chunk.setName(name);
		return this;

	}

	public String getName() {
		return this.getName();
	}

	public KoreDatastructure setID(String id) {
		this.chunk.setId(id);
		return this;
	}

	public Chunk getChunk() {
		return this.chunk;
	}

	protected void setChunk(JsonObject object) throws Exception {
		try {
			this.chunk = new Chunk(object);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}

	}

	public JsonObject toJsonObject() {
		return this.getChunk().toJsonObject();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getChunk());
		return builder.toString();
	}
}
