package at.tuwien.ict.acona.evolutiondemo.stockmarketagent;

import java.awt.BasicStroke;
import java.awt.Color;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickMarkPosition;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.HighLowRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.ui.ApplicationFrame;

public class OHLCGraph extends ApplicationFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private OHLCSeriesCollection dataset;
	private JPanel chartPanel;
	
	public OHLCGraph(String title) {
		super(title);
		
	    //OHLCDataset dataset = this.createDataset1();
	    //final JFreeChart chart = ChartFactory.createCandlestickChart(title, "Time", "Value", dataset, true);

		this.chartPanel = this.generateGraph(this.createDataset1());
		
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
     * @param dataset  a dataset.
     *
     * @return a sample chart.
     */
    private JFreeChart createChart(OHLCDataset dataset) {

        JFreeChart chart = ChartFactory.createHighLowChart(
            "Stock Market Charts",
            "Time",
            "Price",
            dataset,
            true
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        HighLowRenderer renderer = (HighLowRenderer) plot.getRenderer();
        renderer.setBaseStroke(new BasicStroke(2.0f));
        renderer.setSeriesPaint(0, Color.blue);

        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setTickMarkPosition(DateTickMarkPosition.MIDDLE);

        NumberAxis yAxis1 = (NumberAxis) plot.getRangeAxis();
        yAxis1.setAutoRangeIncludesZero(false);

        NumberAxis yAxis2 = new NumberAxis("Price 2");
        yAxis2.setAutoRangeIncludesZero(false);
        plot.setRangeAxis(1, yAxis2);
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
    public OHLCDataset createDataset1() {

        OHLCSeries s1 = new OHLCSeries("Series 1");
        s1.add(new Day(24, 9, 2007), 50.5, 53.2, 49.8, 50.1);
        s1.add(new Day(25, 9, 2007), 50.2, 51.2, 47.8, 48.1);
        s1.add(new Day(26, 9, 2007), 48.0, 49.2, 45.3, 47.4);
        s1.add(new Day(27, 9, 2007), 47.5, 48.3, 46.8, 46.8);
        s1.add(new Day(28, 9, 2007), 46.6, 47.0, 45.1, 46.0);
        s1.add(new Day(1, 10, 2007), 46.6, 47.0, 45.1, 46.0);
        s1.add(new Day(2, 10, 2007), 47.5, 48.3, 46.8, 46.8);
        s1.add(new Day(3, 10, 2007), 48.0, 49.2, 45.3, 47.4);
        s1.add(new Day(4, 10, 2007), 50.2, 51.2, 47.8, 48.1);
        s1.add(new Day(5, 10, 2007), 50.5, 53.2, 49.8, 50.1);
        OHLCSeriesCollection dataset = new OHLCSeriesCollection();
        dataset.addSeries(s1);
        return dataset;
    }
    
    public void updateDataset(Day time, double open, double high, double low, double close) {
    	//Create a new dataset
    	if (this.dataset==null) {
    		OHLCSeries s1 = new OHLCSeries("S1");
    		//s1.add(new Day(24, 9, 2007), 50.5, 53.2, 49.8, 50.1);
    		this.dataset = new OHLCSeriesCollection();
    		this.dataset.addSeries(s1);
    		this.dataset.getSeries(0).setMaximumItemCount(100);
    	}
    	
    	
    	
    	this.dataset.getSeries(0).add(time, open, high, low, close);
  
    	
    	//Remove old chart
    	if (this.chartPanel!=null) {
    	    this.chartPanel.removeAll();
    	    this.chartPanel.revalidate(); // This removes the old chart 
    	}

	    
	    //Create a new chart
	    JFreeChart updatedChart = this.createChart(dataset);
	    //updatedChart.removeLegend();
	    
	    this.chartPanel = new ChartPanel(updatedChart);
	    this.chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
	    if (this.chartPanel!=null) {
	    	this.chartPanel.repaint();
	    }
	    
		setContentPane(chartPanel);
    	
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
