package ca.polymtl.lttng.pwm.junit;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.polymtl.lttng.pwm.PulseAnalyzer;

public class TestPulseAnalyzer {
	@Test
	public void test1Interval() {
		// create a pulse of 10% of the interval
		PulseAnalyzer p = new PulseAnalyzer();
		p.update(0L, true);
		p.update(10000000L, false);
		p.finish(1000001L);
		assertEquals(1, p.getSamples().size());
		assertEquals(0.1, p.getSamples().getY(0), 0.001);
	}
	
	@Test
	public void test2Interval() {
		// create a pulse that traverse the interval boundary
		PulseAnalyzer p = new PulseAnalyzer(); 
		p.update(10000000L, true);
		p.update(180000000L, false);
		p.finish(180000001L);
		assertEquals(2, p.getSamples().size());
		assertEquals(0.9, p.getSamples().getY(0), 0.001);
		assertEquals(0.8, p.getSamples().getY(1), 0.001);
	}
	
	@Test
	public void test3Interval() {
		// create a pulse that traverse two intervals
		PulseAnalyzer p = new PulseAnalyzer();
		p.update( 10000000L, true);
		p.update(210000000L, false);
		p.finish(210000001L);
		assertEquals(3, p.getSamples().size());
		assertEquals(0.9, p.getSamples().getY(0), 0.001);
		assertEquals(1.0, p.getSamples().getY(1), 0.001);
		assertEquals(0.1, p.getSamples().getY(2), 0.001);
	}
	
	@Test
	public void testEmptyInterval() {
		PulseAnalyzer p = new PulseAnalyzer();
		p.finish(0L);
		assertEquals(1, p.getSamples().size());
		assertEquals(0.0, p.getSamples().getY(0), 0.001);
	}
	
	@Test
	public void testNoStop() {
		PulseAnalyzer p = new PulseAnalyzer();
		p.update(10000000L, true);
		p.finish(300000000L);
		assertEquals(3, p.getSamples().size());
		assertEquals(0.9, p.getSamples().getY(0), 0.001);
		assertEquals(1.0, p.getSamples().getY(1), 0.001);
		assertEquals(1.0, p.getSamples().getY(2), 0.001);
	}
	
	@Test
	public void testNoStart() {
		PulseAnalyzer p = new PulseAnalyzer();
		p.update(10000000L, false);
		p.finish(300000000L);
		assertEquals(3, p.getSamples().size());
		assertEquals(0.0, p.getSamples().getY(0), 0.001);
		assertEquals(0.0, p.getSamples().getY(1), 0.001);
		assertEquals(0.0, p.getSamples().getY(2), 0.001);
	}
}
