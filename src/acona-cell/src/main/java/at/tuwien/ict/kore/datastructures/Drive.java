package at.tuwien.ict.kore.datastructures;

import com.google.gson.JsonObject;

/**
 * Kore drive is the mean of evaluation for the system. It also represents the
 * system goals.
 * 
 * @author wendt
 *
 */
public class Drive extends KoreDatastructure {

	public final static String DRIVE = "DRIVE";
	public final static String PREDICATEINTENSITY = "hasIntensity";

	public static Drive newDrive(String name, double intensity) throws Exception {
		return new Drive(name, intensity);
	}

	public static Drive newDrive(JsonObject object) throws Exception {
		return new Drive(object);
	}

	private Drive(String name, double intensity) throws Exception {
		super(name, DRIVE);
		this.setIntensity(intensity);
	}

	private Drive(JsonObject drive) throws Exception {
		super(drive);
		if (isDrive(drive) == false) {
			throw new Exception("This is no drive + " + drive);
		}
	}

	public static boolean isDrive(JsonObject object) {
		boolean result = false;

		if (object.has(PREDICATEINTENSITY) == true) {
			result = true;
		}

		return result;
	}

	@Override
	public Drive setName(String name) {
		super.setName(name);
		return this;

	}

	public Drive setIntensity(double intensity) {
		this.getChunk().setValue(PREDICATEINTENSITY, intensity);
		return this;
	}

	public double getIntensity() {
		return this.getChunk().getDoubleValue(PREDICATEINTENSITY);
	}

}
