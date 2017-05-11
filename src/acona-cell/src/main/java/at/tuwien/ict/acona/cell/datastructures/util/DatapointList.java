package at.tuwien.ict.acona.cell.datastructures.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import at.tuwien.ict.acona.cell.datastructures.Datapoint;

public class DatapointList extends ArrayList<Datapoint> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static DatapointList newDatapointList(List<Datapoint> list) {
		return new DatapointList(list);
	}

	public DatapointList(List<Datapoint> list) {
		this.addAll(list);
	}

	public Datapoint get(String address) {
		Datapoint result = null;

		Optional<Datapoint> datapoint = this.stream().filter(dp -> address.equals(dp.getAddress())).findFirst();

		if (datapoint.isPresent()) {
			result = datapoint.get();
		}

		return result;

	}

	public boolean has(String address) {
		boolean result = false;

		if (this.get(address) != null) {
			result = true;
		}

		return result;
	}

}
