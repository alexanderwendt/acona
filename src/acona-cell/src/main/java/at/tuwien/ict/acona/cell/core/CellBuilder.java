package at.tuwien.ict.acona.cell.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import at.tuwien.ict.acona.cell.activator.Activator;
import at.tuwien.ict.acona.cell.activator.ActivatorImpl;
import at.tuwien.ict.acona.cell.activator.Condition;

public class CellBuilder {
	private static final String CELLNAME = "cellname";
	private static final String CELLCLASS = "cellclass";
	private static final String CELLCONDITIONS = "conditions";
	private static final String CELLBEHAVIOURS = "cellbehaviours";
	private static final String CELLACTIVATORS = "activators";
	
	private static final String CONDITIONNAME = "conditionname";
	private static final String CONDITIONCLASS = "conditionclass";
	//private static final String CONDITIONCONFIG = "conditionconfig";
	
	
	private static final String BEHAVIOURNAME = "behaviourname";
	private static final String BEHAVIOURCLASS = "behaviourclass";
	//private static final String BEHAVIOURCONFIG = "behaviourconfig";
	
	private static final String ACTIVATORNAME = "activatorname";
	private static final String ACTIVATORMAP = "activatormap";
	private static final String ACTIVATORBEHAVIOUR = "activatorbehaviour";
	private static final String ACTIVATORLOGIC = "activatorlogic";
	
	private static Logger log = LoggerFactory.getLogger(CellBuilder.class);
	
	private final Map<String, Condition> conditionMap = new HashMap<String, Condition>();
	private final Map<String, CellFunctionBehaviour> cellFunctionBehaviourMap = new HashMap<String, CellFunctionBehaviour>();
	private final Map<String, Activator> activatorMap = new HashMap<String, Activator>();
	
	public void initializeCellConfig(JsonObject config, CellInitialization caller) throws Exception {
		//Instantiate the cell itself
		
		try {
			//=== Instantiate the conditions ===
			//Get all condition configs
			JsonArray cellConditions = config.getAsJsonArray(CELLCONDITIONS);
			JsonArray cellBehaviours = config.getAsJsonArray(CELLBEHAVIOURS);
			JsonArray cellActivators = config.getAsJsonArray(CELLACTIVATORS);
			
			//Check if it is an empty config
			//If there is an activator, then there must be conditions and behaviour. If there is no activator, then it is an empty configuration
			
			if (cellActivators==null) {
				log.debug("There are no activators for this cell. Therefore, it is empty");
			} else {
				if (cellActivators!=null && (cellBehaviours==null || cellConditions==null)) {
					throw new Exception("There is an activator, but either no conditions or behaviours. Both are needed to create an activator");
				}
				
				
				cellConditions.forEach(conditionConfig->{
					try {
						Condition c = this.createConditionFromConfig(conditionConfig.getAsJsonObject());
						this.conditionMap.put(c.getName(), c);
					} catch (Exception e) {
						log.info("Cannot create condition from {}", conditionConfig);
					}			
				});
				
				//Instantiate the behaviours
				
				cellBehaviours.forEach(behaviourConfig->{
					try {
						CellFunctionBehaviour behaviour = this.createCellFunctionBehaviourFromConfig(behaviourConfig.getAsJsonObject(), caller);
						this.cellFunctionBehaviourMap.put(behaviour.getName(), behaviour);
					} catch (Exception e) {
						log.info("Cannot create behaviour from {}", behaviourConfig);
					}	
					
				});
				
				//Instantiate the activators
				cellActivators.forEach(activatorConfig->{
					try {
						Activator activator = this.createActivatorFromConfig(activatorConfig.getAsJsonObject(), this.conditionMap, this.cellFunctionBehaviourMap, caller);
						this.activatorMap.put(activator.getName(), activator);
					} catch (Exception e) {
						log.info("Cannot create activator from {}", activatorConfig);
					}	
					
				});
				
				//Set all activators
				caller.setActivatorMap(activatorMap);
				caller.setCellFunctionBehaviourMap(cellFunctionBehaviourMap);
				caller.setConditionMap(conditionMap);
				
				//Register activators
				caller.getActivatorMap().entrySet().forEach(a->{
					caller.getActivationHandler().registerActivatorInstance(a.getValue());
				});
				
			}
		} catch (Exception e) {
			log.error("Cannot create cell");
			throw new Exception(e.getMessage());
		}
		
	}
	
	private Condition createConditionFromConfig(JsonObject config) throws Exception {
		Condition result = null;
		
		//Get all values
		String name = config.get(CONDITIONNAME).getAsString();
		String className = config.get(CONDITIONCLASS).getAsString();
		//JsonObject conditionConfig = config.get(CONDITIONCONFIG).getAsJsonObject();
		
		//Generate the class
		try {
		
			Class<?> clazz = Class.forName(className);
			Constructor<?> constructor = clazz.getConstructor();
			Object obj = constructor.newInstance();
			if (obj instanceof Condition) {
				result = (Condition) obj;
			}
			
			//Init the class
			result.init(name, config);
			
		} catch (ClassNotFoundException e) {
			log.error("Cannot find a class with the name {}", className, e);
			throw new ClassNotFoundException(e.getMessage());
		} catch (InstantiationException e) {
			log.error("Cannot instantiate class with name {}", className, e);
			throw new InstantiationException(e.getMessage());
		} catch (IllegalAccessException e) {
			log.error("Cannot access class {}", className, e);
			throw new IllegalAccessException(e.getMessage());
		} catch (NoSuchMethodException e) {
			log.error("Cannot initialize constructor for class {}", className, e);
			throw new NoSuchMethodException(e.getMessage());
		} catch (SecurityException e) {
			log.error("Security exception for constructor for class {}", className, e);
			throw new SecurityException(e.getMessage());
		} catch (IllegalArgumentException e) {
			log.error("Wrong arguments in constrctor of class {}", className, e);
			throw new IllegalArgumentException(e.getMessage());
		} catch (InvocationTargetException e) {
			log.error("Cannot invoke constructor of class {}", className, e);
			throw new InvocationTargetException(e, e.getMessage());
		}
		
		return result;
	}
	
	private CellFunctionBehaviour createCellFunctionBehaviourFromConfig(JsonObject config, Cell caller) throws Exception {
		CellFunctionBehaviour result = null;
		
		//Get all values
		String name = config.get(BEHAVIOURNAME).getAsString();
		String className = config.get(BEHAVIOURCLASS).getAsString();
		//JsonObject behaviourConfig = config.get(BEHAVIOURCONFIG).getAsJsonObject();
		
		//Generate the class
		
		try {
			Class<?> clazz = Class.forName(className);
			Constructor<?> constructor = clazz.getConstructor();
			Object obj = constructor.newInstance();
			if (obj instanceof CellFunctionBehaviour) {
				result = (CellFunctionBehaviour) obj;
			}
			
			//Init the class
			result.init(name, config, caller);
			
		} catch (ClassNotFoundException e) {
			log.error("Cannot find a class with the name {}", className, e);
			throw new ClassNotFoundException(e.getMessage());
		} catch (InstantiationException e) {
			log.error("Cannot instantiate class with name {}", className, e);
			throw new InstantiationException(e.getMessage());
		} catch (IllegalAccessException e) {
			log.error("Cannot access class {}", className, e);
			throw new IllegalAccessException(e.getMessage());
		} catch (NoSuchMethodException e) {
			log.error("Cannot initialize constructor for class {}", className, e);
			throw new NoSuchMethodException(e.getMessage());
		} catch (SecurityException e) {
			log.error("Security exception for constructor for class {}", className, e);
			throw new SecurityException(e.getMessage());
		} catch (IllegalArgumentException e) {
			log.error("Wrong arguments in constrctor of class {}", className, e);
			throw new IllegalArgumentException(e.getMessage());
		} catch (InvocationTargetException e) {
			log.error("Cannot invoke constructor of class {}", className, e);
			throw new InvocationTargetException(e, e.getMessage());
		}
		
		return result;
	}
	
	private Activator createActivatorFromConfig(JsonObject config, Map<String, Condition> conditionMap, Map<String, CellFunctionBehaviour> cellFunctionBehaviourMap, Cell caller) throws Exception {
		Activator result = null;
		
		//Get all values
		String name = config.get(ACTIVATORNAME).getAsString();
		String behaviourName = config.get(ACTIVATORBEHAVIOUR).getAsString();
		JsonArray conditionMapping = config.get(ACTIVATORMAP).getAsJsonArray();	//"key : "[value, value, value]"
		String activatorLogic = config.get(ACTIVATORLOGIC).getAsString();
		
		try {
			//Get behaviour
			CellFunctionBehaviour cellFunctionBehaviour = cellFunctionBehaviourMap.get(behaviourName);
		
			if (cellFunctionBehaviour==null) {
				throw new NullPointerException("No behaviour with name " + behaviourName + " found in map " + cellFunctionBehaviourMap + ".");
			}
			
			//Create condition address map
			final Map<String, List<Condition>> datapointConditionMap = new ConcurrentHashMap<String, List<Condition>>();
			conditionMapping.forEach(mapping->{
				final Set<Entry<String, JsonElement>> set = mapping.getAsJsonObject().entrySet();
				set.forEach(e->{
					//Get datapointname
					String datapoint = e.getKey();
					
					//Get condition name
					String conditionName = e.getValue().getAsString();
					Condition condition = conditionMap.get(conditionName);
					
					//Set in table as new list or if it exists just add to list
					if (datapointConditionMap.containsKey(datapoint)==true) {
						datapointConditionMap.get(datapoint).add(condition);
					} else {
						datapointConditionMap.put(datapoint, Arrays.asList(condition));
					}
				});
			});
			
			if (datapointConditionMap.isEmpty()==true) {
				throw new Exception("No conditions available");
			}
			
			result = new ActivatorImpl().init(name, datapointConditionMap, activatorLogic, cellFunctionBehaviour, caller);
		
		} catch (Exception e) {
			log.error("Cannot create activation", e);
			throw new Exception(e.getMessage());
		}
		
		return result;
	}
}
