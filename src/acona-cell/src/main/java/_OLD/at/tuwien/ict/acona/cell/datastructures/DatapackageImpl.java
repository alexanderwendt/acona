package _OLD.at.tuwien.ict.acona.cell.datastructures;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DatapackageImpl implements Datapackage {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Link datapackage address with a concept
	 */
	private final Map<String, Chunk> data = new HashMap<String, Chunk>();
	/**
	 * Link concept id with concept. It is used to get connected concepts for a certain concept
	 */
	private final Map<String, Chunk> availableConcepts = new HashMap<String, Chunk>();
	
	private DatapackageImpl() {
		
	}
	
	private DatapackageImpl(Datapackage data) {
		this.setContent(data.getViewOfAllConcepts());
		this.setIdsForContent(data.getViewOfAllConceptsByID());
	}
	
	public static Datapackage newDatapackage() {
		return new DatapackageImpl();
	}

	public static Datapackage newDatapackage(String address, String content) {
		Datapackage result = new DatapackageImpl();
		result.setContent(address, content);
		return result;
	}
	
	public static Datapackage newDatapackage(Datapackage data) {
		return new DatapackageImpl(data);
	}
	
	public static Datapackage newDatapackage(Map<String, Chunk> data) {
		Datapackage dp = new DatapackageImpl();
		dp.setContent(data);
		return dp;
	}

	@Override
	public Chunk get(String address) {
		Chunk result = ChunkImpl.nullChunk();
		
		if (this.data.containsKey(address)==true) {
			result = this.data.get(address);
		}
		
		return result;
	}

	@Override
	public Map<String, Chunk> getViewOfAllConcepts() {
		return Collections.unmodifiableMap(this.data);
	}

	@Override
	public void setContent(String address, String content) {
		this.data.put(address, ChunkImpl.newChunk(address).newValue(address, content).build());
		this.availableConcepts.put(data.get(address).getId(), data.get(address));
	}

	@Override
	public void setContent(Map<String, Chunk> content) {
		this.data.putAll(content);
		this.setIdsForContent(content);
	}
	
	@Override
	public void setContent(Chunk concept) {
		this.data.put(concept.getName(), concept);
		this.availableConcepts.put(concept.getId(), concept);
		
	}
	
	@Override
	public Chunk getChunkByID(String id) {
		Chunk result = ChunkImpl.nullChunk();
		
		if (this.availableConcepts.containsKey(id)==true) {
			result = this.availableConcepts.get(id);
		}
		
		return result;
	}

	@Override
	public void setConceptByID(Chunk concept) {
		this.availableConcepts.put(concept.getId(), concept);
	}

	@Override
	public Map<String, Chunk> getViewOfAllConceptsByID() {
		return Collections.unmodifiableMap(this.availableConcepts);
	}

	@Override
	public void setConcent(Datapackage datapackage) {
		this.setContent(datapackage.getViewOfAllConcepts());
		this.setIdsForContent(datapackage.getViewOfAllConceptsByID());
		
	}

	@Override
	public void clear() {
		this.data.clear();
		this.availableConcepts.clear();
	}

	private void setIdsForContent(Map<String, Chunk> content) {
		//Put all added concepts in the ID-Map but with Ids as keys and not addresses
		content.values().forEach((Chunk concept)->this.availableConcepts.put(concept.getId(), concept));
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("data=");
		builder.append(this.data);
		builder.append(", concepts=");
		this.availableConcepts.values().forEach((Chunk concept)->builder.append(concept.getName()+ ", "));
		return builder.toString();
	}

	@Override
	public boolean isEmpty() {
		return this.availableConcepts.isEmpty();
	}

}
