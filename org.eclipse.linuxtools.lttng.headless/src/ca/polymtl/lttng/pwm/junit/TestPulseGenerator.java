package ca.polymtl.lttng.pwm.junit;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.polymtl.lttng.pwm.Pulse;
import ca.polymtl.lttng.pwm.PulseGenerator;

public class TestPulseGenerator {

	@Test
	public void testPulseGenerator() {
		Double freq_hz = 100.0;
		Double duty = 0.1;
		PulseGenerator gen = new PulseGenerator(freq_hz, duty);
		Pulse curr = null;
		Pulse prev = null;
		for(int i=0; i<10; i++) {
			curr = gen.getNext();
			if (prev != null) {
				assertTrue(curr.getTs() > prev.getTs());
				assertTrue(curr.getState() != prev.getState());
			}
			prev = curr;
		}
	}
}
