package ca.polymtl.lttng.pwm;

import java.lang.Math;

public class PulseGeneratorSine extends PulseGenerator {

	public Double getDutyCycle(Long x) {
		Long l = x / 1000000000L;
		return Math.sin(l.doubleValue());
	}
}
