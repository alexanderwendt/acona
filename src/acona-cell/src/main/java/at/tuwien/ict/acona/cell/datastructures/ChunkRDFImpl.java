package at.tuwien.ict.acona.cell.datastructures;

import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import com.google.gson.JsonObject;

public class ChunkRDFImpl implements ChunkMethods {

	private Model model;
	private Resource baseResource;
	private String baseResourceUri;

	public static final String NAMEPROPERTY = "hasName";
	public static final String TYPEPROPERTY = "hasType";

	public ChunkRDFImpl(String name, String type) {
		this.init(name, type);
	}

	private void init(String name, String type) {
		model = ModelFactory.createDefaultModel();
		this.baseResourceUri = name + this.hashCode();
		this.baseResource = model.createResource(this.baseResourceUri);
		model.createProperty(NAMEPROPERTY);
		model.createProperty(TYPEPROPERTY);
		this.setName(name);
		this.setType(type);
	}

	@Override
	public void setName(String name) {
		model.add(this.baseResource, model.createProperty(NAMEPROPERTY), name);
	}

	@Override
	public void setId(String id) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void setType(String type) {
		model.add(this.baseResource, model.createProperty(TYPEPROPERTY), type);

	}

	@Override
	public void setValue(String key, String value) {
		model.createProperty(key);
		Statement statement = model.createStatement(this.baseResource, model.getProperty(key), value);
		model.add(statement);
	}

	@Override
	public void setValue(String key, double value) {
		model.createProperty(key);
		Statement statement = model.createLiteralStatement(this.baseResource, model.getProperty(key), value);
		model.add(statement);

	}

	@Override
	public void setValue(String key, int value) {
		model.createProperty(key);
		Statement statement = model.createLiteralStatement(this.baseResource, model.getProperty(key), value);
		model.add(statement);

	}

	@Override
	public void setValue(String key, boolean value) {
		model.createProperty(key);
		Statement statement = model.createLiteralStatement(this.baseResource, model.getProperty(key), value);
		model.add(statement);

	}

	public void setAssociatedContent(String association, ChunkRDFImpl content) {
		model.createProperty(association);
		Statement statement = model.createStatement(this.baseResource, model.getProperty(association),
				content.baseResource);
		model.add(statement);
	}

	@Override
	public JsonObject toJsonObject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return this.baseResource.getProperty(model.getProperty(NAMEPROPERTY)).getString();
	}

	@Override
	public String getID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getValue(String key) {
		return this.baseResource.getProperty(model.getProperty(key)).getString();
	}

	@Override
	public double getDoubleValue(String key) {
		return this.baseResource.getProperty(model.getProperty(key)).getDouble();
	}

	public List<ChunkRDFImpl> getAssociatedContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(model);
		return builder.toString();
	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAssociatedContent(String association, Chunk content) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Chunk> getAssociatedContent(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setValue(String key, Chunk value) {
		// TODO Auto-generated method stub

	}

	@Override
	public Chunk getValueAsChunk(String key) {
		// TODO Auto-generated method stub
		return null;
	}

}
