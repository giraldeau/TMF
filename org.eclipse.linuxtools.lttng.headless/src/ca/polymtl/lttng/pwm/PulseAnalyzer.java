package ca.polymtl.lttng.pwm;

import java.util.ArrayList;

public class PulseAnalyzer {
	
	SampleSeries samples = null;
	Pulse prev_pulse;
	Pulse curr_pulse;
	Long interval_start;
	Long interval_end;
	Long period;
	Long sum;
	
	public PulseAnalyzer() {
		reset();
		period = 10000000L; // 100ms
	}
	
	public void update(Pulse p) {
		this.update(p.getTs(), p.getState());
	}
	
	public void update(Long ts, Boolean state) {
		// update states and timestamps
		prev_pulse = curr_pulse;
		curr_pulse = new Pulse(ts, state);
		
		//assert(curr_pulse.getTs() >= prev_pulse.getTs());
		
		// next event outside the interval boundary
		while (ts > interval_end) {
			if (prev_pulse.getState() == true) {
				sum += interval_end - prev_pulse.getTs();
			}
			prev_pulse.setTs(interval_end);
			samples.addPoint(interval_start.doubleValue(), sum.doubleValue()/period.doubleValue());
			interval_start += period;
			interval_end += period;
			sum = 0L;
		}
		
		// uncomplete period, add to total
		if (ts <= interval_end) {
			if (prev_pulse.getState() == true) {
				sum += curr_pulse.getTs() - prev_pulse.getTs();
			}
		}
	}

	public void finish(Long ts) {
		this.update(ts, curr_pulse.getState());
		// flush current sum 
		if (ts <= interval_end) {
			samples.addPoint(interval_start.doubleValue(), sum.doubleValue()/period.doubleValue());
		}
	}
	
	public SampleSeries analyze(SampleSeries pwm) {
		reset();
		for(int i=0; i<pwm.size(); i++) {
			update(pwm.getX(i).longValue(), pwm.isHigh(i));
		}
		return samples;
	}
	
	public void reset() {
		samples = new SampleSeries();
		sum = 0L;
		interval_start = 0L;
		interval_end = period;
		curr_pulse = prev_pulse = new Pulse(0L, false);
	}
	
	public SampleSeries getSamples() {
		return samples;
	}

	public void setSamples(SampleSeries samples) {
		this.samples = samples;
	}

	public Long getPeriod() {
		return period;
	}

	public void setPeriod(Long period) {
		this.period = period;
	}
	
}
