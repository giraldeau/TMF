package ca.polymtl.lttng.pwm.junit;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.Range;

import ca.polymtl.lttng.pwm.PulseAnalyzer;
import ca.polymtl.lttng.pwm.PulseGenerator;
import ca.polymtl.lttng.pwm.SampleSeries;

/**
 * An example for line chart.
 */
public class TestPulseGraph {

	/*
	private static final double[] ySeries = { 0.0, 0.38, 0.71, 0.92, 1.0, 0.92,
			0.71, 0.38, 0.0, -0.38, -0.71, -0.92, -1.0, -0.92, -0.71, -0.38 };

	private static final double[] xSeries = { 0.0, 0.38, 0.71, 0.92, 1.0, 0.92,
		0.71, 0.38, 0.0, -0.38, -0.71, -0.92, -1.0, -0.92, -0.71, -0.38 };
	 */
	
	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments.
	 */
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("Line Chart Example");
		shell.setSize(1200, 800);
		shell.setLayout(new FillLayout());
		
		FillLayout layout = new FillLayout(SWT.VERTICAL);
		shell.setLayout(layout);
		
		final TabFolder tabFolder = new TabFolder(shell, SWT.BORDER);
		
		createOverviewTab(tabFolder);
		createSamplingRateTab(tabFolder);
		createErrorTab(tabFolder);
		
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		shell.dispose();

	}
	
	public static void createOverviewTab(TabFolder folder) {
		TabItem tab = new TabItem(folder, SWT.NULL);
		tab.setText("Overview");
		// Create the list of values
		Long duration = 6500000000L;
		PulseGenerator gen = new PulseGenerator();
		PulseAnalyzer pa = new PulseAnalyzer();
		SampleSeries pwm = gen.getPulseSeries(duration);
		SampleSeries fct = gen.getDutySeries(duration);
		pa.setPeriod(100000000L);
		SampleSeries avg100 = pa.analyze(pwm);

		Group group = new Group(folder, SWT.NONE);
		group.setLayout(new FillLayout(SWT.VERTICAL));
		createChart(group, fct, "Duty function");
		createChart(group, pwm, "PWM function");
		createChart(group, avg100, "Average function 100ms");
		
		tab.setControl(group);
	}
	
	public static void createSamplingRateTab(TabFolder folder) {
		TabItem tab = new TabItem(folder, SWT.NULL);
		tab.setText("Sampling");
		// Create the list of values
		Long duration = 6500000000L;
		PulseGenerator gen = new PulseGenerator();
		PulseAnalyzer pa = new PulseAnalyzer();
		SampleSeries pwm = gen.getPulseSeries(duration);
		SampleSeries fct = gen.getDutySeries(duration);
		//pa.setPeriod(1000000L);
		//SampleSeries avg1 = pa.analyze(pwm);
		pa.setPeriod(10000000L);
		SampleSeries avg10 = pa.analyze(pwm);
		pa.setPeriod(100000000L);
		SampleSeries avg100 = pa.analyze(pwm);
		pa.setPeriod(1000000000L);
		SampleSeries avg1000 = pa.analyze(pwm);

		Group group = new Group(folder, SWT.NONE);
		group.setLayout(new FillLayout(SWT.VERTICAL));
		createChart(group, fct, "Duty function");
		createChart(group, avg10, "Average function 10ms");
		createChart(group, avg100, "Average function 100ms");
		createChart(group, avg1000, "Average function 1000ms");
		
		tab.setControl(group);		
	}

	private static void createErrorTab(TabFolder folder) {
		TabItem tab = new TabItem(folder, SWT.NULL);
		tab.setText("Error");
		// Create the list of values
		Long duration = 6500000000L;
		PulseGenerator gen = new PulseGenerator();
		PulseAnalyzer pa = new PulseAnalyzer();
		SampleSeries pwm = gen.getPulseSeries(duration);
		SampleSeries fct = gen.getDutySeries(duration);
		pa.setPeriod(10000000L);
		SampleSeries avg10 = pa.analyze(pwm);
		pa.setPeriod(100000000L);
		SampleSeries avg100 = pa.analyze(pwm);
		pa.setPeriod(1000000000L);
		SampleSeries avg1000 = pa.analyze(pwm);
		
		SampleSeries err10 = computeError(gen, avg10);
		SampleSeries err100 = computeError(gen, avg100);
		SampleSeries err1000 = computeError(gen, avg1000);
		
		Group group = new Group(folder, SWT.NONE);
		group.setLayout(new FillLayout(SWT.VERTICAL));
		Chart chart = new Chart(group, SWT.NONE);
		chart.getTitle().setText("Error amplitude");
		chart.getAxisSet().getXAxis(0).getTitle().setText("Data Points");
		chart.getAxisSet().getYAxis(0).getTitle().setText("Amplitude");
		
		chart.getAxisSet().getYAxis(0).setRange(new Range(-1.0, 1.0));
		chart.getAxisSet().getXAxis(0).setRange(new Range(0, duration.doubleValue()));
		
		Color red = new Color(Display.getDefault(), 255, 0, 0);
		Color green = new Color(Display.getDefault(), 0, 255, 0);
		Color blue = new Color(Display.getDefault(), 0, 0, 255);
		
		addLineSeries(chart, err10, "err10", red);
		addLineSeries(chart, err100, "err100", green);
		addLineSeries(chart, err1000, "err1000", blue);
		
		tab.setControl(group);
	}
	
	public static void addLineSeries(Chart chart, SampleSeries series, String title, Color color) {
		ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet()
			.createSeries(SeriesType.LINE, title);
		lineSeries.setXSeries(series.getXSeries());
		lineSeries.setYSeries(series.getYSeries());
		lineSeries.setAntialias(SWT.ON);
		lineSeries.enableArea(true);
		lineSeries.setLineColor(color);
	}
	
	public static SampleSeries computeError(PulseGenerator gen, SampleSeries avg) {
		SampleSeries err = new SampleSeries();
		for(int i=0; i<avg.size();i++) {
			Long x = avg.getX(i).longValue();
			err.addPoint(avg.getX(i), (avg.getY(i) - gen.getDutyCycle(x)));
		}
		return err;
	}
	
	public static Chart createChart(Composite parent, SampleSeries series, String title) {
		// create a chart
		Chart chart = new Chart(parent, SWT.NONE);
		
		// set titles
		chart.getTitle().setText(title);
		chart.getAxisSet().getXAxis(0).getTitle().setText("Data Points");
		chart.getAxisSet().getYAxis(0).getTitle().setText("Amplitude");
		
		// create line series
		ILineSeries lineSeries = (ILineSeries) chart.getSeriesSet()
				.createSeries(SeriesType.LINE, "line series");
		lineSeries.setXSeries(series.getXSeries());
		lineSeries.setYSeries(series.getYSeries());
		lineSeries.setAntialias(SWT.ON);
		lineSeries.enableArea(true);
		// adjust the axis range
		chart.getAxisSet().adjustRange();
		return chart;
	}
	
}