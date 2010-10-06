///**
// * 
// */
//
//package org.eclipse.linuxtools.lttng.state.history;
//
///**
// * Helper class containing, well, helper methods to deal with arrays:
// * - xToByteArray and byteArrayToX, to easily convert
// *   standard types to and from byte[] arrays, which are used
// *   to write in files sequentially.
// * - arrayResize method, which pseudo-dynamically resizes an array,
// *   without the overhead and performance hit of using Collections.
// * 
// * @author alexmont
// *
// */
//abstract class ArrayHelper {
//	
//	/**
//	 * Resize an array (ie, create a new one of the same type and
//	 * copy the old elements to the new one)
//	 * @param oldArray
//	 * @param newSize
//	 * @return The new array
//	 */
//	public static Object resizeArray (Object oldArray, int newSize) {
//		int oldSize = java.lang.reflect.Array.getLength(oldArray);
//		Class elementType = oldArray.getClass().getComponentType();
//		Object newArray = java.lang.reflect.Array.newInstance(elementType, newSize);
//		int preserveLength = Math.min(oldSize, newSize);
//		if (preserveLength > 0) {
//			System.arraycopy (oldArray, 0, newArray, 0, preserveLength);
//		}
//		return newArray;
//	}
//	
//	
//	/**
//	 * Helper methods to convert TO byte arrays
//	 */
//	public static byte[] boolToByteArray(boolean value) {
//		if ( value == true ) {
//			return new byte[] {1};
//		} else {
//			return new byte[] {0};
//		}
//	}
//	
//	public static byte[] intToByteArray(int value) {
//        return new byte[] {
//                (byte)(value >>> 24),
//                (byte)(value >>> 16),
//                (byte)(value >>> 8),
//                (byte)(value)
//        		};
//	}
//	
//	public static byte[] longToByteArray(long value) {
//		return new byte[] {
//				(byte)(value >>> 56),
//				(byte)(value >>> 48),
//				(byte)(value >>> 40),
//				(byte)(value >>> 32),
//				(byte)(value >>> 24),
//				(byte)(value >>> 16),
//				(byte)(value >>> 8),
//				(byte)(value)
//				};
//	}
//	
//	public static byte[] charArrayToByteArray(char[] charArray) {
//		byte[] array = new byte[charArray.length * 2];
//		for(int i=0; i < charArray.length; i++) {
//			array[2*i] = (byte) ((charArray[i] & 0xFF00) >> 8); 
//			array[2*i+1] = (byte) (charArray[i] & 0x00FF); 
//		}
//		return array;
//	}
//
//	/**
//	 * Helper methods to convert FROM byte arrays
//	 */
//	
//	public static boolean byteArrayToBool(byte[] array) {
//		assert(array.length == 1);
//		if ( array[0] == 1) {
//			return true;
//		} else {
//			return false;
//		}
//	}
//	
//	public static int byteArrayToInt(byte[] array) {
//		assert(array.length == 4);
//		return 	(int)array[0] + 
//				(int)(array[1] << 8) + 
//				(int)(array[2] << 16) +
//				(int)(array[3] << 24);
//	}
//	
//	public static long byteArrayToLong(byte[] array) {
//		assert(array.length == 8);
//		return 	(int)array[0] + 
//				(int)(array[1] << 8) + 
//				(int)(array[2] << 16) +
//				(int)(array[3] << 24);
//	}
//	
//	public static char[] byteArraytoCharArray(byte[] array) {
//		assert( array.length % 2 == 0 );	//make sure the number of bytes is even
//		char[] charArray = new char[array.length / 2];
//		for (int i=0; i < charArray.length; i++) {
//			charArray[i] = (char) ( (array[2*i] << 8) + (array[2*i+1]) );
//		}
//		return charArray;
//	}
//	
//}











