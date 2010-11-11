package ca.polymtl.lttng.pwm;

public class Pulse implements Comparable<Pulse> {
	Long ts;
	Boolean state;
	public Pulse(Long ts, Boolean state) {
		this.setTs(ts);
		this.setState(state);
	}
	public Long getTs() {
		return ts;
	}
	public void setTs(Long ts) {
		this.ts = ts;
	}
	public Boolean getState() {
		return state;
	}
	public void setState(Boolean status) {
		this.state = status;
	}
	public String toString() {
		return "ts=" + this.ts + " state=" + this.state;
	}
	@Override
	public int hashCode() {
		return this.ts.hashCode() + this.state.hashCode();
	}
	@Override
	public boolean equals(Object other) {
		Pulse o;
		if (other instanceof Pulse) {
			o = (Pulse) other;
			if (o.getTs() == this.getTs() &&
				o.getState() == this.getState()) {
				return true;
			}
		}
		return false;
	}
	@Override
	public int compareTo(Pulse other) {
		return this.ts.compareTo(other.getTs());
	}
}
