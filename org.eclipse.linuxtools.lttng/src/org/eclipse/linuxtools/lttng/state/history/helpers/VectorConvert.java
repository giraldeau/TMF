/**
 * 
 */

package org.eclipse.linuxtools.lttng.state.history.helpers;

import java.util.Vector;

/**
 * The insertion methods in the State History use Vectors of Strings.
 * Instead of new'ing new Vectors for every request, we can use this little
 * helper class which keeps the objects in memory.
 * 
 * @author alexmont
 *
 */
public class VectorConvert<T> {
	
	/* Pre-defined widths, add more if you need a deeper tree! */
	private Vector<T> vector1, vector2, vector3, vector4, vector5, vector6;
	
	public VectorConvert() {
		vector1 = new Vector<T>(1);
		vector2 = new Vector<T>(2);
		vector3 = new Vector<T>(3);
		vector4 = new Vector<T>(4);
		vector5 = new Vector<T>(5);
		vector6 = new Vector<T>(6);
	}
	
	
	public Vector<T> parse(T entry0) {
		vector1.set(0, entry0);
		return vector1;
	}
	
	public Vector<T> parse(T entry0, T entry1) {
		vector2.set(0, entry0);
		vector2.set(1, entry1);
		return vector2;
	}
	
	public Vector<T> parse(T entry0, T entry1, T entry2) {
		vector3.set(0, entry0);
		vector3.set(1, entry1);
		vector3.set(2, entry2);
		return vector3;
	}
	
	public Vector<T> parse(T entry0, T entry1, T entry2,
									T entry3) {
		vector4.set(0, entry0);
		vector4.set(1, entry1);
		vector4.set(2, entry2);
		vector4.set(3, entry3);
		return vector4;
	}
	public Vector<T> parse(T entry0, T entry1, T entry2,
									T entry3, T entry4) {
		vector5.set(0, entry0);
		vector5.set(1, entry1);
		vector5.set(2, entry2);
		vector5.set(3, entry3);
		vector5.set(4, entry4);
		return vector5;
	}
	
	public Vector<T> parse(T entry0, T entry1, T entry2,
									T entry3, T entry4, T entry5) {
		vector6.set(0, entry0);
		vector6.set(1, entry1);
		vector6.set(2, entry2);
		vector6.set(3, entry3);
		vector6.set(4, entry4);
		vector6.set(5, entry5);
		return vector6;
	}
}
