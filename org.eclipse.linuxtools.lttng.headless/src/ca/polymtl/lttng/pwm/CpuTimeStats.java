package ca.polymtl.lttng.pwm;

import java.util.*;

public class CpuTimeStats {

	public enum Type {
		USER, IRQ, SYSCALL, TRAP
	}
	
	Integer id;
	EnumMap<Type, Double> stats;
	EnumMap<Type, Double> t1;
	EnumMap<Type, Double> t2;
	
	public CpuTimeStats () {
	}
	
	public void begin(Long ts, Type type) {
	}
	
	public void end(Long ts, Type type) {
	}
	
}
