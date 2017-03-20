package at.tuwien.ict.acona.cell.datastructures;

import java.util.List;

import com.google.gson.JsonObject;

public interface ChunkMethods {

	public void setName(String name);

	public void setType(String type);

	public void setId(String id);

	public void setValue(String key, String value);

	public void setValue(String key, double value);

	public void setValue(String key, int value);

	public void setValue(String key, boolean value);

	public void setValue(String key, Chunk value);

	public void setAssociatedContent(String association, Chunk content);

	public JsonObject toJsonObject();

	public String getName();

	public String getID();

	public String getType();

	public String getValue(String key);

	public double getDoubleValue(String key);

	public Chunk getValueAsChunk(String key) throws Exception;

	public List<Chunk> getAssociatedContent(String key);

}