package org.eclipse.linuxtools.lttng.jni.common;


/**
 * <b><u>Jni_C_Pointer</u></b>
 * <p>
 * Class pointer to handle properly "C pointer" <p>
 * 
 * Can transparently handle pointer of 32 or 64 bits.
 */
public class Jni_C_Pointer extends Jni_C_Constant {

    private long ptr = NULL;
    private boolean isLong = true;
    
    /**
     * Default constructor.<p>
     * 
     * Note : Pointer will be set to a 64bits "NULL".
     */
    public Jni_C_Pointer() {
        ptr  = NULL;
    }
    
    /**
     * Constructor with parameters for 64bits pointers.
     * 
     * @param newPtr    long-converted (64 bits) C pointer.
     */
    public Jni_C_Pointer(long newPtr) {
        ptr = newPtr;
        isLong = true; 
    }
    
    /**
     * Constructor with parameters for 32bits pointers.
     * 
     * @param newPtr    int-converted (32 bits) C pointer.
     */
    public Jni_C_Pointer(int newPtr) {
        ptr = (long)newPtr;
        isLong = false; 
    }
    
    /**
     * Get the current pointer.
     * 
     * @return  The current pointer, in long.
     */
    public long getPointer() {
        return ptr;
    }
    
    /**
     * Set the pointer, as a 64bits pointer.
     * 
     * @param newPtr    long-converted (64 bits) C pointer.
     */
    public void setPointer(long newPtr) {
        ptr = newPtr;
        isLong = true;
    }
    
    /**
     * Set the pointer, as a 64bits pointer.
     * 
     * @param newPtr    int-converted (32 bits) C pointer.
     */
    public void setPointer(int newPtr) {
        ptr = newPtr;
        isLong = false;
    }

    /**
     * toString() method. <p>
     * 
     * Convert the pointer to a nice looking int/long hexadecimal format. 
     * 
     * @return Attributes of the object concatenated in String
     */
    @Override
    public String toString() {
        String returnData = "0x";

        if (isLong == true) {
            returnData += Long.toHexString(ptr);
        }
        else {
            returnData += Integer.toHexString((int) ptr);
        }

        return returnData;
    }
}
