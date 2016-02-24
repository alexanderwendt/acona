package at.tuwien.ict.kore.cell.datastructures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface Chunk extends Serializable {
	//Metadata
	
	/**
	 * Get concept ID that is unique for a concept
	 * 
	 * @return
	 */
	public String getId();
	
	/**
	 * Get the name of a concept that can be present multiple times
	 * 
	 * @return
	 */
	public String getName();
	
	//String Values
	/**
	 * Get all values
	 * 
	 * @return
	 */
	public Map<String, ArrayList<String>> getValueMap();
	/**
	 * Get all values of the concept
	 * 
	 * @param type
	 * @return
	 */
	public List<String> getValues(String type);
	
	/**
	 * Get a value of a concept
	 * 
	 * @param type
	 * @return
	 */
	public String getValue(String type);
	
	
	/**
	 * Set a value and replace an existing one if there is one
	 * 
	 * @param type
	 * @param value
	 */
	public void setValue(String type, String value);
	
	/**
	 * Set or add a value to an existing value
	 * 
	 * @param type
	 * @param value
	 */
	public void addValue(String type, String value);
	
	/**
	 * Remove a value
	 * 
	 * @param type
	 */
	public void removeValue(String type);
	
	/**
	 * Get the default value. The key for this value is the name of the concept. In that way, simple 
	 * string can be transformed into concepts
	 * 
	 * @return
	 */
	public String getDefaultValue();
	
	/**
	 * Set the default value. The key for this value is the name of the concept. In that way, simple 
	 * string can be transformed into concepts
	 * 
	 * @return
	 */
	public void setDefaultValue(String defaultValue);
	
	//Sub concepts
	public List<Chunk> getSubChunks(Datapackage container);
	public Chunk getSubChunk(String name, Datapackage container);
	public void addSubChunk(Chunk concept, Datapackage container);
	
	//Super concepts
	public void setSuperChunk(Chunk superConcept, Datapackage container);
	public Chunk getSuperChunk(Datapackage container);
	public boolean superChunkExist();
	
	//Associated concepts
	public Map<String, ArrayList<String>> getAssociatedConceptsMap();
	public Chunk getAssociatedChunk(String predicate, Datapackage container);
	public List<Chunk> getAssociatedChunks(String predicate, Datapackage container);
	public void setAssociatedChunk(String predicate, Chunk concept, Datapackage container);
	public void setAssociatedChunk(String predicate, Chunk concept, double weight, Datapackage container);
	public void addAssociatedChunk(String predicate, Chunk concept, Datapackage container) throws Exception;
	public void addAssociatedChunk(String predicate, Chunk concept, double weight, Datapackage container) throws Exception;
	
}
