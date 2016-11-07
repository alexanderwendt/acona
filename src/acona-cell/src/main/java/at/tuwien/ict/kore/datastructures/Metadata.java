package at.tuwien.ict.kore.datastructures;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Metadata extends KoreDatastructure {
	public final static String PREDICATEREWARD = "hasReward";
	public final static String PREDICATEDRIVE = "hasDrive";
	public final static String DATATYPE = "metadata";

	public static Metadata newMetadata(Concept c) throws Exception {
		return new Metadata(c);
	}

	private Metadata(Concept c) throws Exception {
		super(c.getChunk().getName() + "Metadata", DATATYPE);
	}

	public static Metadata newMetadata(JsonObject object) throws Exception {
		return new Metadata(object);
	}

	private Metadata(JsonObject object) throws Exception {
		super(object);
		if (Metadata.isMetadata(object) == false) {
			throw new Exception("This structure is no metadata " + object);
		}
	}

	public static boolean isMetadata(JsonObject object) {
		boolean result = false;

		if (object.get(Chunk.TYPEPROPERTY).getAsString().equals(DATATYPE)) {
			result = true;
		}

		return result;
	}

	public Metadata addReward(Reward reward) {
		this.getChunk().setAssociatedContent(PREDICATEREWARD, reward.getChunk());
		return this;
	}

	public List<Reward> getReward() {
		JsonArray result = this.getChunk().getAssociatedContentAsArray(PREDICATEREWARD);

		List<Reward> convertedResult = new ArrayList<Reward>();

		result.forEach(e -> {
			try {
				convertedResult.add(Reward.newReward(e.getAsJsonObject()));
			} catch (Exception e1) {
				log.error("Cannot get as reward " + e);
			}
		});

		return convertedResult;
	}

	public Metadata addDrive(Drive reward) {
		this.getChunk().setAssociatedContent(PREDICATEDRIVE, reward.getChunk());
		return this;
	}

	public List<Drive> getDrive() {
		JsonArray result = this.getChunk().getAssociatedContentAsArray(PREDICATEDRIVE);

		List<Drive> convertedResult = new ArrayList<Drive>();

		result.forEach(e -> {
			try {
				convertedResult.add(Drive.newDrive(e.getAsJsonObject()));
			} catch (Exception e1) {
				log.error("Cannot get as reward " + e);
			}
		});

		return convertedResult;
	}

	public Drive getDrive(String driveName) {
		List<Drive> drives = this.getDrive();

		Drive result = null;

		for (Drive drive : drives) {
			if (drive.getName().equals(driveName)) {
				result = drive;
				break;
			}
		}

		return result;
	}

	public Metadata setValue(String key, String value) {
		this.getChunk().setValue(key, value);
		return this;
	}

	public Metadata setValue(String key, double value) {
		this.getChunk().setValue(key, value);
		return this;
	}

	public Metadata setValue(String key, int value) {
		this.getChunk().setValue(key, value);
		return this;
	}

	public Metadata setValue(String key, boolean value) {
		this.getChunk().setValue(key, value);
		return this;
	}

	public String getValue(String key) {
		return this.getChunk().getValue(key);
	}

	public double getDoubleValue(String key) {
		return this.getChunk().getDoubleValue(key);
	}

}
