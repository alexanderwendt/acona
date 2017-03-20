package at.tuwien.ict.acona.framework.modules.visualization;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JFrame;

import at.tuwien.ict.acona.cell.cellfunction.ServiceState;

/**
 *
 * @author Privat
 */
public class Visualization2D extends JFrame {

	private VisualizationData mapAgent;
	private int xZero = 30;
	private int yZero = 50;
	private BufferedImage bufferedImage;

	public Visualization2D(VisualizationData a) {
		mapAgent = a;
		bufferedImage = new BufferedImage(mapAgent.getSize(), mapAgent.getSize(), BufferedImage.TYPE_INT_BGR);
	}

	@Override
	public void paint(Graphics graphics) {
		Graphics g = bufferedImage.getGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, mapAgent.getSize(), mapAgent.getSize());
		g.setColor(Color.black);
		for (String aid : mapAgent.getPositions().keySet()) {
			List<Position> list = mapAgent.getPositions().get(aid);
			if (list.get(0).getState() == ServiceState.RUNNING.toString()) {
				g.setColor(Color.red);
				for (int i = 0; i < list.size() - 1; i++) {
					g.drawLine(list.get(i).getX() * mapAgent.getSize(), list.get(i).getY() * mapAgent.getSize(),
							list.get(i + 1).getX() * mapAgent.getSize(), list.get(i + 1).getY() * mapAgent.getSize());
				}
				g.setColor(Color.black);
				g.fillOval(list.get(list.size() - 1).getX() * mapAgent.getSize() - 3,
						list.get(list.size() - 1).getY() * mapAgent.getSize() - 3, 7, 7);
			} else {
				g.setColor(Color.gray);
				g.fillOval(list.get(0).getX() * mapAgent.getSize() - 2, list.get(0).getY() * mapAgent.getSize() - 2, 5,
						5);
			}
		}
		graphics.drawRect(xZero - 1, yZero - 1, mapAgent.getSize() + 2, mapAgent.getSize() + 2);
		graphics.drawImage(bufferedImage, xZero, yZero, this);
	}

}
