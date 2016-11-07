package at.tuwien.ict.acona.framework.modules;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import at.tuwien.ict.acona.cell.cellfunction.CellFunctionThreadImpl;
import at.tuwien.ict.acona.cell.datastructures.Datapoint;
import at.tuwien.ict.acona.framework.modules.visualization.Position;
import at.tuwien.ict.acona.framework.modules.visualization.Visualization2D;
import at.tuwien.ict.acona.framework.modules.visualization.VisualizationData;

public class CFVisualizer extends CellFunctionThreadImpl implements VisualizationData {

	private Map<String, List<Position>> moAgentPositions = Collections
			.synchronizedMap(new HashMap<String, List<Position>>());
	private int size = 800;
	private JFrame frame;

	@Override
	protected void cellFunctionInternalInit() throws Exception {
		// Create the visualization frame

		frame = new Visualization2D(this);
		frame.setSize(size + 60, size + 80);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}

	@Override
	protected void executeFunction() throws Exception {
		// Update position in an arrayblockingqueue

		// repaint after update
		this.repaint();

	}

	@Override
	protected void executePostProcessing() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void executePreProcessing() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateDatapointsById(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, List<Position>> getPositions() {
		// TODO Auto-generated method stub
		return null;
	}

	public void repaint() {
		frame.repaint();
	}

	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void updateDatapointsByIdOnThread(Map<String, Datapoint> data) {
		// TODO Auto-generated method stub

	}

}
