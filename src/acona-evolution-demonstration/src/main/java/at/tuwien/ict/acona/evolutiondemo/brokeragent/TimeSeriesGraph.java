package at.tuwien.ict.acona.evolutiondemo.brokeragent;

import java.awt.Color;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeSeriesGraph extends ApplicationFrame {

	private final static Logger log = LoggerFactory.getLogger(TimeSeriesGraph.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private TimeSeriesCollection seriesCollection;
	// private Dataset dataset;
	private JPanel chartPanel;

	public TimeSeriesGraph(String title) {
		super(title);

		seriesCollection = this.initSeriesCollection();
		this.chartPanel = this.generateGraph(seriesCollection);
		RefineryUtilities.centerFrameOnScreen(this);
		// chartPanel.setVisible(true);

	}

	private JPanel generateGraph(XYDataset dataset) {

		JFreeChart chart = ChartFactory.createTimeSeriesChart("Spieces trends",
				"Time days", "Type count",
				dataset, true, true, true);
		chart.setBackgroundPaint(Color.WHITE);

		final XYPlot plot = chart.getXYPlot();
		// plot.setDomainCrosshairVisible(true);
		// plot.setRangeCrosshairVisible(false);
		// final XYItemRenderer renderer = plot.getRenderer();
		// if (renderer instanceof StandardXYItemRenderer) {
		// final StandardXYItemRenderer rr = (StandardXYItemRenderer) renderer;
		// rr.setSeriesStroke(0, new BasicStroke(2.0f));
		// rr.setSeriesStroke(1, new BasicStroke(2.0f));
		// }

		// final DateAxis axis = (DateAxis) plot.getDomainAxis();
		// axis.setDateFormatOverride(new SimpleDateFormat("hh:mma"));

		final JPanel panel = new ChartPanel(chart);
		panel.setPreferredSize(new java.awt.Dimension(1000, 600));
		setContentPane(panel);

		return panel;
	}

	/**
	 * Creates a sample high low dataset.
	 *
	 * @return a sample high low dataset.
	 */
	private TimeSeriesCollection initSeriesCollection() {

		final TimeSeries series = new TimeSeries("Testdata");
		series.add(new TimeSeriesDataItem(new Day(24, 9, 2007), 10));
		series.add(new TimeSeriesDataItem(new Day(25, 9, 2007), 11));
		series.add(new TimeSeriesDataItem(new Day(26, 9, 2007), 13));

		final TimeSeries series2 = new TimeSeries("Testdata2");
		series2.add(new TimeSeriesDataItem(new Day(24, 9, 2007), 14));
		series2.add(new TimeSeriesDataItem(new Day(25, 9, 2007), 15));
		series2.add(new TimeSeriesDataItem(new Day(26, 9, 2007), 16));

		final TimeSeries series3 = new TimeSeries("Testdata2");
		series3.add(new TimeSeriesDataItem(new Day(24, 9, 2007), 3));
		series3.add(new TimeSeriesDataItem(new Day(25, 9, 2007), 10));
		series3.add(new TimeSeriesDataItem(new Day(26, 9, 2007), 19));

		TimeSeriesCollection s1 = new TimeSeriesCollection();
		// s1.addSeries(series);
		// s1.addSeries(series2);
		// s1.addSeries(series3);
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
		// OHLCSeriesCollection seriesCollection = new OHLCSeriesCollection();

		return s1;
	}

	private TimeSeries addOrGetSeries(String name) {
		TimeSeries result;

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

		// Add series if it does not exists
		if (index == -1) {
			TimeSeries s1 = new TimeSeries(name);
			s1.setMaximumItemCount(200);
			seriesCollection.addSeries(s1);
			int newNumberOfSeries = seriesCollection.getSeriesCount();
			index = newNumberOfSeries - 1;
			log.debug("Number of series={}, name={}", newNumberOfSeries, name);
		}

		result = seriesCollection.getSeries(name);

		return result;
	}

	public void updateDataset(String id, Day time, double value) {

		TimeSeries serie = this.addOrGetSeries(id);

		serie.add(time, value);
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
