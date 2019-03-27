package at.tuwien.ict.acona.mq.core.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import at.tuwien.ict.acona.mq.core.agentfunction.AgentFunction;
import at.tuwien.ict.acona.mq.core.config.AgentConfig;
import at.tuwien.ict.acona.mq.core.config.FunctionConfig;

public class AgentBuilder {

	private static Logger log = LoggerFactory.getLogger(AgentBuilder.class);

	private Cell caller;

	public AgentBuilder(Cell cell) {
		this.caller = cell;
	}

	/**
	 * Initialize the cell config. Create cell functions from Json configs.
	 * 
	 * @param config
	 * @param caller
	 * @throws Exception
	 */
	public void initializeCellConfig(AgentConfig config) throws Exception {
		try {
			// === Instantiate the conditions ===

			// Instantiate the activators
			config.getCellfunctions().forEach(activatorConfig -> {
				try {
					// Instantiate and init function
					AgentFunction cellfunction = this.createCellFunctionFromConfig(activatorConfig.getAsJsonObject());
					if (cellfunction == null) {
						throw new NullPointerException("activator does not exist");
					}

					log.debug("Cell function {} created.", cellfunction.getFunctionName());

				} catch (Exception e) {
					log.error("Cannot create activator from {}", activatorConfig, e);
				}
			});

		} catch (Exception e) {
			log.error("Cannot create cell", e);
			throw new Exception(e.getMessage());
		}

	}

	protected synchronized AgentFunction createCellFunctionFromConfig(JsonObject config) throws Exception {
		AgentFunction result = null;

		// Get all values
		FunctionConfig cellconfig = FunctionConfig.newConfig(config);
		String className = cellconfig.getClassName();

		// Generate the class
		try {

			Class<?> clazz = Class.forName(className);
			Constructor<?> constructor = clazz.getConstructor();
			Object obj = constructor.newInstance();
			if (obj instanceof AgentFunction) {
				result = (AgentFunction) obj;
				// Init the class, create the list of subscriptions
				result.init(cellconfig, caller);
			} else {
				throw new InstantiationException(
						"Cannot convert object to cellfunction class. The instantiated object is no cell function. "
								+ obj);
			}

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
}
