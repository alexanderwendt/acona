package at.tuwien.ict.acona.evolutiondemo.brokeragent;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.ui.ApplicationFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LineGraph extends ApplicationFrame {

	private final static Logger log = LoggerFactory.getLogger(LineGraph.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private OHLCSeriesCollection seriesCollection;
	// private Dataset dataset;
	private JPanel chartPanel;

	public LineGraph(String title) {
		super(title);

		seriesCollection = this.initSeriesCollection();
		this.chartPanel = this.generateGraph(seriesCollection);

	}

	private JPanel generateGraph(OHLCDataset dataset) {
		JFreeChart chart = createChart(dataset);
		JPanel panel = new ChartPanel(chart);
		panel.setPreferredSize(new java.awt.Dimension(1000, 600));
		setContentPane(panel);

		return panel;
	}

	/**
	 * Creates a sample chart.
	 *
	 * @param dataset
	 *            a dataset.
	 *
	 * @return a sample chart.
	 */
	private JFreeChart createChart(DefaultCategoryDataset dataset) {

		JFreeChart chart = ChartFactory.createLineChart("Spieces trends",
				"Spieces count",
				"Time",
				dataset,
				PlotOrientation.VERTICAL,
				true, true, false);
		// XYPlot plot = (XYPlot) chart.getPlot();
		// HighLowRenderer renderer = (HighLowRenderer) plot.getRenderer();
		// renderer.setBaseStroke(new BasicStroke(2.0f));
		// renderer.setSeriesPaint(0, Color.blue);

		// DateAxis axis = (DateAxis) plot.getDomainAxis();
		// axis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);

		// NumberAxis yAxis1 = (NumberAxis) plot.getRangeAxis();
		// yAxis1.setAutoRangeIncludesZero(false);

		// NumberAxis yAxis2 = new NumberAxis("Price 2");
		// yAxis2.setAutoRangeIncludesZero(false);
		// plot.setRangeAxis(1, yAxis2);
		plot.setDataset(1, dataset);
		plot.setRenderer(1, new CandlestickRenderer(10.0));
		plot.mapDatasetToRangeAxis(1, 1);
		ChartUtilities.applyCurrentTheme(chart);
		return chart;
	}

	/**
	 * Creates a sample high low dataset.
	 *
	 * @return a sample high low dataset.
	 */
	private OHLCSeriesCollection initSeriesCollection() {

		OHLCSeries s1 = new OHLCSeries("S1");
//        s1.add(new Day(24, 9, 2007), 50.5, 53.2, 49.8, 50.1);
//        s1.add(new Day(25, 9, 2007), 50.2, 51.2, 47.8, 48.1);
//        s1.add(new Day(26, 9, 2007), 48.0, 49.2, 45.3, 47.4);
//        s1.add(new Day(27, 9, 2007), 47.5, 48.3, 46.8, 46.8);
//        s1.add(new Day(28, 9, 2007), 46.6, 47.0, 45.1, 46.0);
//        s1.add(new Day(1, 10, 2007), 46.6, 47.0, 45.1, 46.0);
//        s1.add(new Day(2, 10, 2007), 47.5, 48.3, 46.8, 46.8);
//        s1.add(new Day(3, 10, 2007), 48.0, 49.2, 45.3, 47.4);
//        s1.add(new Day(4, 10, 2007), 50.2, 51.2, 47.8, 48.1);
		// s1.add(new Day(5, 10, 2007), 50.5, 53.2, 49.8, 50.1);
		OHLCSeriesCollection seriesCollection = new OHLCSeriesCollection();

		return seriesCollection;
	}

	private OHLCSeries addOrGetSeries(String name) {
		OHLCSeries result;

		// Check if series exists
		int numberOfSeries = seriesCollection.getSeriesCount();
		int index = -1;
		// Find in series
		for (int i = 0; i < numberOfSeries; i++) {
			Comparable<String> comparable = seriesCollection.getSeriesKey(i);
			if (comparable.compareTo(name) == 0) {
				index = i;
				break;
			}
		}

		if (index == -1) {
			OHLCSeries s1 = new OHLCSeries(name);
			s1.setMaximumItemCount(200);
			seriesCollection.addSeries(s1);
			int newNumberOfSeries = seriesCollection.getSeriesCount();
			index = newNumberOfSeries - 1;
			log.debug("Number of series={}, name={}", newNumberOfSeries, name);
		}

		result = seriesCollection.getSeries(index);

		return result;
	}

	public void updateDataset(String timerowid, Day time, double open, double high, double low, double close) {

		OHLCSeries serie = this.addOrGetSeries(timerowid);

		serie.add(time, open, high, low, close);
		serie.fireSeriesChanged();

		// Remove old chart
		// if (this.chartPanel!=null) {
		// this.chartPanel.removeAll();
		// this.chartPanel.revalidate(); // This removes the old chart
		// }

		// Create a new chart
		// JFreeChart updatedChart = this.createChart(dataset);
		// updatedChart.removeLegend();

		this.chartPanel.repaint();

		// this.chartPanel = new ChartPanel(updatedChart);
		// this.chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		// if (this.chartPanel!=null) {
		// this.chartPanel.repaint();
		// }

		// setContentPane(chartPanel);

	}

//	private void refreshChart() {
//	    this.chartPanel.removeAll();
//	    this.chartPanel.revalidate(); // This removes the old chart 
//	    aChart = createChart(); 
//	    aChart.removeLegend(); 
//	    ChartPanel chartPanel = new ChartPanel(aChart); 
//	    this.chartPanel.setLayout(new BorderLayout()); 
//	    this.chartPanel.add(chartPanel); 
//	    this.chartPanel.repaint(); // This method makes the new chart appear
//	}

}
