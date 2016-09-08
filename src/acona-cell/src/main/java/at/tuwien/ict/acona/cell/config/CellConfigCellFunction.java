package at.tuwien.ict.acona.cell.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class CellConfigCellFunction {
	private static final String CELLNAME = "cellname";
	private static final String CELLTYPE = "celltype";
	private static final String CELLCLASS = "cellclass";
	private static final String CELLFUNCTIONS = "cellfunctions";
	
	private final JsonObject configObject = new JsonObject();
	
	public static CellConfigCellFunction newConfig(String name, String className) {
		return new CellConfigCellFunction(name, className);
	}
	
	private CellConfigCellFunction(String name, String className) {
		this.setName(name).setClassName(className);
		this.configObject.add(CELLFUNCTIONS, new JsonArray());
	}
	
	private CellConfigCellFunction setName(String name) {
		this.configObject.addProperty(CELLNAME, name);
		return this;
	}
	
	private CellConfigCellFunction setClassName(String className) {
		this.configObject.addProperty(CELLCLASS, className);
		return this;
	}
	
	public CellConfigCellFunction setClass(Class<?> clzz) {
		this.setClassName(clzz.getName());
		return this;
	}
	
	public Class<?> getClassToInvoke() throws Exception {
		return Class.forName(this.getClassName());
	}
	
	public CellConfigCellFunction addProperty(String name, String value) {
		this.configObject.addProperty(name, value);
		return this;
	}
	
	public CellConfigCellFunction addCellfunction(ActivatorConfigCellFunction config) {
		this.configObject.getAsJsonArray(CELLFUNCTIONS).add(config.toJsonObject());
		return this;
	}
	
	public JsonArray getCellfunction() {
		return this.configObject.getAsJsonArray(CELLFUNCTIONS);
	}
	
	public String getName() {
		return this.configObject.getAsJsonPrimitive(CELLNAME).getAsString();
	}
	
	public String getClassName() {
		return this.configObject.getAsJsonPrimitive(CELLCLASS).getAsString();
	}
	
	public String getProperty(String key) {
		return this.configObject.getAsJsonPrimitive(key).getAsString();
	}
	
	public JsonObject toJsonObject() {
		return this.configObject;
	}
}
