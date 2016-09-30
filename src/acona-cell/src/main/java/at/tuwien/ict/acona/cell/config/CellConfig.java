package at.tuwien.ict.acona.cell.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.core.CellImpl;

public class CellConfig {
	public static final String CELLNAME = "cellname";
	public static final String CELLCLASS = "cellclass";
	public static final String CELLFUNCTIONS = "cellfunctions";
	
	//Keep the jsonobject in order to be able to add more settings. If only a class is used, flexibility is lost for creating
	//new json
	
	private final JsonObject configObject = new JsonObject();	
	
	
	public static CellConfig newConfig(String name, String className) {
		return new CellConfig(name, className);
	}
	
	public static CellConfig newConfig(String name, Class<?> clzz) {
		return new CellConfig(name, clzz.getName());
	}
	
	public static CellConfig newConfig(String name) {
		return new CellConfig(name, CellImpl.class.getName());
	}
	
	private CellConfig(String name, String className) {
		this.setName(name).setClassName(className);
		this.configObject.add(CELLFUNCTIONS, new JsonArray());
	}
	
	private CellConfig setName(String name) {
		this.configObject.addProperty(CELLNAME, name);
		return this;
	}
	
	private CellConfig setClassName(String className) {
		this.configObject.addProperty(CELLCLASS, className);
		return this;
	}
	
	public CellConfig setClass(Class<?> clzz) {
		this.setClassName(clzz.getName());
		return this;
	}
	
	public Class<?> getClassToInvoke() throws Exception {
		return Class.forName(this.getClassName());
	}
	
	public CellConfig addProperty(String name, String value) {
		this.configObject.addProperty(name, value);
		return this;
	}
	
	public CellConfig addProperty(String name, JsonObject value) {
		this.configObject.add(name, value);
		return this;
	}
	
	public CellConfig addCellfunction(CellFunctionConfig config) {
		this.configObject.getAsJsonArray(CELLFUNCTIONS).add(config.toJsonObject());
		return this;
	}
	
	public JsonArray getCellfunctions() {
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
