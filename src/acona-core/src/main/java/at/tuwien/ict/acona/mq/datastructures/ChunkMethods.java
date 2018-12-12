package at.tuwien.ict.acona.mq.datastructures;

import java.util.List;

import com.google.gson.JsonObject;

public interface ChunkMethods {

	public ChunkMethods setName(String name);

	public ChunkMethods setType(String type);

	public ChunkMethods setId(String id);

	public ChunkMethods setValue(String key, String value);

	public ChunkMethods setValue(String key, double value);

	public ChunkMethods setValue(String key, int value);

	public ChunkMethods setValue(String key, boolean value);

	public ChunkMethods setValue(String key, ChunkMethods value);

	public ChunkMethods setAssociatedContent(String association, ChunkMethods content);

	public JsonObject toJsonObject();

	public String getName();

	public String getID();

	public String getType();

	public String getValue(String key);

	public double getDoubleValue(String key);

	public Chunk getValueAsChunk(String key) throws Exception;

	public List<Chunk> getAssociatedContent(String key);

}