package at.tuwien.ict.acona.visualization;

import java.util.List;
import java.util.Map;

public interface VisualizationData {
	public Map<String, List<Position>> getPositions();

	public int getSize();
}
