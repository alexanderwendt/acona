package at.tuwien.ict.kore.datastructures;

import com.google.gson.JsonObject;

/**
 * Kore drive is the mean of evaluation for the system. It also represents the
 * system goals.
 * 
 * @author wendt
 *
 */
public class Reward extends KoreDatastructure {

	public final static String DATATYPE = "Reward";
	public final static String PREDICATEVALUE = "hasValue";

	public static Reward newReward(String name, double value) throws Exception {
		return new Reward(name).setReward(value);
	}

	private Reward(String name) throws Exception {
		super(name, DATATYPE);
	}

	public static Reward newReward(JsonObject object) throws Exception {
		return new Reward(object);
	}

	public static boolean isReward(JsonObject object) {
		boolean result = false;

		if (object.has(PREDICATEVALUE) == true) {
			result = true;
		}

		return result;
	}

	private Reward(JsonObject drive) throws Exception {
		super(drive);
		if (isReward(drive) == false) {
			throw new Exception("This is no drive + " + drive);
		}
	}

	@Override
	public Reward setName(String name) {
		super.setName(name);
		return this;

	}

	public Reward setReward(double intensity) {
		this.getChunk().setValue(PREDICATEVALUE, intensity);
		return this;
	}

	public double getReward() {
		return this.getChunk().getDoubleValue(PREDICATEVALUE);
	}
}
