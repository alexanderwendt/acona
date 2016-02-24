package at.tuwien.ict.kore.cell.datastructures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ChunkImpl implements Chunk {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Nullobject pattern, in order not to get any nullpointer errors
	 */
	private final static Chunk nullChunk = ChunkImpl.newChunk("null").build();
	/**
	 * Counter for generating concept ids
	 */
	private static long conceptCount;
	
	private final static String SUBCHUNKPREDICATE  = "hasSubChunk";
	private final static String SUPERCHUNKPREDICATE = "hasSuperChunk";
	
	private final String emptyString = "";
	private final String name;
	private final String id;
	private final Map<String, ArrayList<String>> values = new HashMap<String, ArrayList<String>>();
	//private final Map<String, Double> weights = new HashMap<String, Double>();
	private final Map<String, ArrayList<String>> associatedChunks = new HashMap<String, ArrayList<String>>();
	//private final ArrayList<ConceptImpl> subConcepts = new ArrayList<ConceptImpl>();
	//private ConceptImpl superConcept = null;
	
	private ChunkImpl(String name) {
		this.name = name;
		conceptCount++;
		this.id = this.name + String.valueOf(conceptCount);
		
	}
	
	/**
	 * Copy constructor
	 * 
	 * @param concept
	 */
	private ChunkImpl(Chunk concept) {
		this.id = concept.getId();
		this.name = concept.getName();
		this.values.putAll(concept.getValueMap());
		this.associatedChunks.putAll(concept.getAssociatedConceptsMap());
		//Java 1.8 parallelized copying of concepts
		//concept.subConcepts.forEach((ConceptImpl subconcept)->this.subConcepts.add(new ConceptImpl(subconcept)));
		
		
//		if (concept.superConceptExist()==true) {
//			this.superConcept = new ConceptImpl(concept.superConcept);
//		}
	}
	
	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public List<String> getValues(String type) {
		List<String> result = new ArrayList<String>();
		if (this.getValues().containsKey(type)==true && this.getValues().get(type).isEmpty()==false) {
			result = Collections.unmodifiableList(this.getValues().get(type));
		}
		
		return result;
	}
	
	public String getValue(String type) {
		String result = this.emptyString;
		
		List<String> list = this.getValues(type);
		if (list.isEmpty()==false) {
			result = list.get(0);
		}
		
		return result;
	}
	
	public String getDefaultValue() {
		return this.getValue(name);
	}
	
	public void setDefaultValue(String defaultValue) {
		this.addValue(name, defaultValue);
	}
	
	public void setValue(String type, String value) {
		this.getValues().put(type, new ArrayList<String>(Arrays.asList(value)));
	}
	
	public void addValue(String type, String value) {
		if (this.getValues().containsKey(type)==false) {
			this.setValue(type, value);
		} else {
			this.getValues().get(type).add(value);
		}
	}
	
	public void removeValue(String type) {
		this.getValues().remove(type);
	}

	private Map<String, ArrayList<String>> getValues() {
		return values;
	}

	@Override
	public Chunk getAssociatedChunk(String predicate, Datapackage container) {
		String id = "";
		
		//Get all ids of sub concepts
		List<String> list = this.getValues(predicate);
		if (list.isEmpty()==false) {
			id = list.get(0);
		}
		
		//Get those ids from the container if they can be found
		//JAVA 1.8. For each id in the list, get their correpsonding concepts
		Chunk result = container.getChunkByID(id);
		
		return result;
	}
	
	
	private void setAssociatedChunkID(String predicate, String id) {
		this.getAssociatedChunks().put(predicate, new ArrayList<String>(Arrays.asList(id)));
	}
	
	private void addAssociatedChunkID(String predicate, String id) {
		if (this.getAssociatedChunks().containsKey(predicate)==false) {
			this.setAssociatedChunkID(predicate, id);
		} else {
			this.getAssociatedChunks().get(predicate).add(id);
		}
	}
	
	private void removeAssociatedChunkID(String id) {
		this.getAssociatedChunks().remove(id);
	}

	@Override
	public List<Chunk> getAssociatedChunks(String predicate, Datapackage container) {
		List<Chunk> result = new ArrayList<Chunk>();
		
		//Get all ids of sub concepts
		List<String> subConceptIDs = this.getAssociatedChunks().get(predicate);
		
		//Get those ids from the container if they can be found
		//JAVA 1.8. For each id in the list, get their correpsonding concepts
		subConceptIDs.forEach((String id)->result.add(container.getChunkByID(id)));
		
		return result;
	}

	@Override
	public void setAssociatedChunk(String predicate, Chunk concept, Datapackage container) {
		this.getAssociatedChunks().put(predicate, new ArrayList<String>(Arrays.asList(concept.getId())));
		container.setConceptByID(concept);
	}

	@Override
	public void setAssociatedChunk(String predicate, Chunk concept, double weight, Datapackage container) {
		//TODO Implement
	}
	
	@Override
	public void addAssociatedChunk(String predicate, Chunk concept, Datapackage container) throws Exception {
		addAssociatedChunk(predicate, concept, 1.0, container);
		
	}

	@Override
	public void addAssociatedChunk(String predicate, Chunk concept, double weight, Datapackage container) throws Exception {
		if (this.getAssociatedChunks().containsKey(predicate)==false) {
			this.setAssociatedChunk(predicate, concept, container);
		} else {
			this.getAssociatedChunks().get(predicate).add(concept.getId());
			container.setConceptByID(concept);
		}
		
		//Add weight
		if (weight!=1.0) {
			//this.getWeights().put(concept.getId(), weight);
		} else if (weight==0.0) {
			throw new Exception("Cannot add a weight that is 0.0 because it is then no association");
		}
		
	}

	private Map<String, ArrayList<String>> getAssociatedChunks() {
		return associatedChunks;
	}

	public List<Chunk> getSubChunks(Datapackage container) {
		return this.getAssociatedChunks(SUBCHUNKPREDICATE, container);
	}

	public Chunk getSubChunk(String name, Datapackage container) {
		Chunk result = ChunkImpl.nullChunk();
		
		List<Chunk> subconcepts = this.getSubChunks(container);
		
		for (Chunk subConcept : subconcepts) {
			if (subConcept.getName().equals(name)) {
				result = subConcept;
				break;
			}
		}
		
		return result;
	}

	public void addSubChunk(Chunk concept, Datapackage container) {
		//Add id
		this.addAssociatedChunkID(SUBCHUNKPREDICATE, concept.getId());
		
		//Add concept to container
		container.setConceptByID(concept);
	}

	public Chunk getSuperChunk(Datapackage container) {
		//Get id and concept
		return this.getAssociatedChunk(SUPERCHUNKPREDICATE, container);
	}

	public void setSuperChunk(Chunk superConcept, Datapackage container) {
		this.setAssociatedChunk(SUPERCHUNKPREDICATE, superConcept, container);
	}

	public boolean superChunkExist() {
		boolean result = false;
		
		if (this.getAssociatedChunks().containsKey(SUPERCHUNKPREDICATE)==true) {
			result=true;
		}
		
		return result;
	}

	public boolean equals(Object obj) {
		boolean result = false;
		if (obj.getClass() == this.getClass()) {
			String objectId = ((ChunkImpl)obj).id;
			if (this.id.equals(objectId)==true) {
				result = true;
			}
		}
		
		return result;
		
	}
	
	private String printValues() {
		//String result = "";
		StringBuilder builder = new StringBuilder();
		this.values.entrySet().forEach((Entry<String, ArrayList<String>> e)->e.getValue().forEach((String value)->builder.append(e.getKey() + " " + value + ", ")));
		return builder.toString();
	}
	
	private String printAssociatedChunks() {
		//String result = "";
		StringBuilder builder = new StringBuilder();
		this.associatedChunks.entrySet().forEach((Entry<String, ArrayList<String>> e)->e.getValue().forEach((String value)->builder.append(e.getKey() + " <" + value + ">, ")));
		return builder.toString();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		//builder.append(name);
		//builder.append(", id=");
		builder.append(name + " ");
		//if (this.getValues().isEmpty()==false) {
		builder.append(printValues());
		//}
		builder.append(this.printAssociatedChunks());
		
//		if (this.getAssociatedConcepts().containsKey(SUBCONCEPTPREDICATE)==true) {
//			builder.append(", subConcepts=");
//			this.getAssociatedConcepts().get(ConceptImpl.SUBCONCEPTPREDICATE).forEach((String id)->builder.append(id + ", "));
////			for (String id :  this.getAssociatedConcepts().get(ConceptImpl.SUBCONCEPTPREDICATE)) {
////				builder.append(id + ", ");
////			}
//		}
//		
//		if (this.superConceptExist()==true) {
//			builder.append(", super=");
//			builder.append(this.getAssociatedConcepts().get(SUPERCONCEPTPREDICATE));
//		}
		
		return builder.toString();
	}
	
	public static Chunk nullChunk() {
		return ChunkImpl.nullChunk;
	}

	public static ChunkBuilder newChunk(String name) {
		return new ChunkBuilder(name);
	}

	public static ChunkBuilder newChunk(Chunk concept) {
		return new ChunkBuilder(concept);
	}

	public static ChunkBuilder newChunk(Chunk concept, String name) {
		return new ChunkBuilder(concept, name);
	}

	public interface Build {
		Build newValue(String name, String value);
		Build newDefaultValue(String value);
		Build addSubChunk(Chunk concept, Datapackage datapackage);
		Build setSuperChunk(Chunk concept, Datapackage datapackage);
		Build addAssociatedChunk(String predicate, Chunk concept, Datapackage datapackage) throws Exception;
		Chunk build();
	}
	
	public static class ChunkBuilder implements Build {
		private ChunkImpl instance;
		
		public ChunkBuilder(String name) {
			this.instance = new ChunkImpl(name);
		}
		
		public ChunkBuilder(Chunk concept) {
			this.instance = new ChunkImpl(concept);
		}
		
		public ChunkBuilder(Chunk concept, String name) {
			ChunkImpl copy = new ChunkImpl(concept);
			this.instance = new ChunkImpl(name);
			
			this.instance.values.putAll(copy.values);
			this.instance.associatedChunks.putAll(copy.associatedChunks);
			
			//Java 1.8 parallelized copying of concepts
			//copy.subConcepts.forEach((String subconcept)->this.instance.subConcepts.add(new ConceptImpl(subconcept)));
			
			//if (concept.superConceptExist()==true) {
			//	this.instance.superConcept = new ConceptImpl(copy.superConcept);
			//}
			
		}
		
		@Override
		public Build newValue(String name, String value) {
			this.instance.setValue(name, value);
			return this;
		}

		@Override
		public Build addSubChunk(Chunk concept, Datapackage datapackage) {
			this.instance.addSubChunk(concept, datapackage);
			return this;
		}
		
		@Override
		public Build setSuperChunk(Chunk concept, Datapackage datapackage) {
			this.instance.setSuperChunk(concept, datapackage);
			return this;
		}

		@Override
		public Chunk build() {
			return this.instance;
		}

		@Override
		public Build newDefaultValue(String value) {
			this.instance.setDefaultValue(value);
			return this;
		}

		@Override
		public Build addAssociatedChunk(String predicate, Chunk concept, Datapackage datapackage) throws Exception {
			this.instance.addAssociatedChunk(predicate, concept, datapackage);
			return this;
		}
		
	}

	@Override
	public Map<String, ArrayList<String>> getValueMap() {
		return Collections.unmodifiableMap(this.getValues());
	}

	@Override
	public Map<String, ArrayList<String>> getAssociatedConceptsMap() {
		return Collections.unmodifiableMap(this.getAssociatedChunks());
	}
	
}
