package ca.polymtl.lttng.pwm;

import java.math.*;

public class PulseGenerator {
	
	Double dc;
	Double freq;
	Double period; // period is in nano sec
	Long time;
	Boolean state;
	
	public PulseGenerator(Double freq, Double duty) {
		this.freq = freq;
		this.period = 1000000000 / freq; // 1s / freq / 1E9ns/s
		this.dc = duty;
		this.time = 0L;
		this.state = true;
	}
	
	public PulseGenerator() {
		this(10.0, 0.1); // 1000Hz, 10%
	}
	
	public Pulse getNext() {
		Double duty = getDutyCycle(time);
		Double val;
		if (state == true) {
			val = (period * duty);
		} else {
			val = (period * (1 - duty));
		}
		time += val.longValue();
		Pulse p = new Pulse(time, state);
		
		//toggle current state for next event
		state = !state;
		
		return p;
	}
	
	// base class returns a fixed duty cycle
	public Double getDutyCycle(Long x) {
		return (Math.sin(x.doubleValue()/1000000000.0) * 0.5) + 0.5;
	}
	
	public SampleSeries getPulseSeries(Long duration) {
		SampleSeries pseries = new SampleSeries();
		Long t = 0L;
		Double duty = 0.0;
		Double y1;
		Double y2; 
		Double val; 
		Double nb_samples = duration / period;
		for(int i=0; i<nb_samples.intValue(); i++) {
			duty = getDutyCycle(t);
			Double on = (period * duty);
			pseries.addPoint(t.doubleValue(), 0.0);
			pseries.addPoint(t.doubleValue(), 1.0);
			pseries.addPoint(t.doubleValue() + on, 1.0);
			pseries.addPoint(t.doubleValue() + on, 0.0);
			t += period.longValue();
		}
		return pseries;
	}
	
	public SampleSeries getDutySeries(Long duration) {
		SampleSeries pseries = new SampleSeries();
		Long t = 0L;
		Double duty = 0.0;
		while (t < duration) {
			duty = getDutyCycle(t);
			pseries.addPoint(t.doubleValue(), duty);
			t += period.longValue();
		}
		return pseries;
	}
}
