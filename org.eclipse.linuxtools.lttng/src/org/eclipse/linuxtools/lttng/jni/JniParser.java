/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   William Bourque (wbourque@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.jni;

import java.util.HashMap;

/**
 * <b><u>JniParser</u></b>
 * <p>
 * JniParser class.
 * All methods are static, the parser shouldn't be instantiated.
 */
public class JniParser extends Jni_C_Common
{
    private static native void ltt_getParsedData(ParsedObjectContent parseddata, long eventPtr, long markerFieldPtr);

    static {
        System.loadLibrary("lttvtraceread");
    }

    /**
     * Default constructor is forbidden
     */
    private JniParser() {
    }
    
    
    
    /**
     * Method to parse a single field identified by its id<br>
     * All parsing will be done on C side as we need Ltt function
     * 
     * @param   eventToParse    The jni event we want to parse. 
     * @param   fieldPosition   The position (or id) of the field we want to parse
     * 
     * @return                  An Object that contain the JniEvent payload parsed by the C, or null, if if was impossible to parse (i.e., wrong position)
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniEvent
     */
    static public Object parseField(JniEvent eventToParse, int fieldPosition) {
        
        // Sanity check
        if ( (fieldPosition < 0) || ( fieldPosition >= eventToParse.requestEventMarker().getMarkerFieldsArrayList().size() ) ){
            return null;
        }
        
        // *** HACK ***
        // We cannot use "Object" directly as java does not support swapping primitive value
        //  We either need to create a new object type or to use a "non-primitive" type that have "Setter()" functions
        // ***
        ParsedObjectContent parsedData = new ParsedObjectContent();
        
        // Call the parsing function in C. The result will be put in parsedData object
        ltt_getParsedData(parsedData, eventToParse.getEventPtr().getPointer(), eventToParse.requestEventMarker().getMarkerFieldsArrayList().get(fieldPosition).getMarkerFieldPtr().getPointer() );
        
        return parsedData.getData();
    }
    
    
    /**
     * Method to parse a single field identified by its name<br>
     * All parsing will be done on C side as we need Ltt function
     * 
     * @param   eventToParse    The jni event we want to parse. 
     * @param   fieldPosition   The position (or id) of the field we want to parse
     * 
     * @return                  An Object that contain the JniEvent payload parsed by the C, or null, if if was impossible to parse (i.e., wrong position)
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniEvent
     */
    static public Object parseField(JniEvent eventToParse, String fieldName) {
        
        JniMarkerField tmpField = eventToParse.requestEventMarker().getMarkerFieldsHashMap().get(fieldName);
        
        // return immediately if there is no field by that name
        if ( tmpField == null ) {
            return null;
        }
        
        // *** HACK ***
        // We cannot use "Object" directly as java does not support swapping on primitive value
        //  We either need to create a new object type or to use a "non-primitive" type that have "Setter()" functions
        // ***
        ParsedObjectContent parsedData = new ParsedObjectContent();
        
        ltt_getParsedData(parsedData, eventToParse.getEventPtr().getPointer(), tmpField.getMarkerFieldPtr().getPointer() );
        
        return parsedData.getData();
    }
    
    
    
    /**
     * Method to parse all field at once<br>
     * All parsing will be done on C side as we need Ltt function
     * 
     * @param   eventToParse    The jni event we want to parse.  
     * @return                  An HashMap of Object that contain the is the JniEvent's payload parsed by the C
     * 
     * @see org.eclipse.linuxtools.lttng.jni.JniEvent
     */
    static public HashMap<String, Object> parseAllFields(JniEvent eventToParse) {
        JniMarker markerData = eventToParse.requestEventMarker();
        int nbMarkerField = markerData.getMarkerFieldsArrayList().size();
        
         // This hashmap will contain the parsed content.
         // ParsedContent is defined at the end of this file
         HashMap<String, Object> parsedDataArray = new HashMap<String, Object>(nbMarkerField);
        
         // *** HACK ***
         // We cannot use "Object" directly as java does not support swapping on primitive value
         //  We either need to create a new object type or to use a "non-primitive" type that have "Setter()" functions
         // ***
         ParsedObjectContent parsedData = new ParsedObjectContent();
         
        // Loop on markerfield, as we need to parse each field in the event data
        for (int pos = 0; pos < nbMarkerField; pos++) {
            // Call the C to parse the data
            ltt_getParsedData(parsedData, eventToParse.getEventPtr().getPointer(), markerData.getMarkerFieldsArrayList().get(pos).getMarkerFieldPtr().getPointer() );
            // Save the result into the HashMap
            parsedDataArray.put(markerData.getMarkerFieldsArrayList().get(pos).getField(), parsedData.getData() );
        }
        
         return parsedDataArray;
    }
    
    /* 
     * Add a parsed String value to the Array<br>
     * <br>
     * Note : this function will be called from the C side.
     * Note2: contentHolder is of type "Object" instead of "ParsedObjectContent" to be able to use the most generic function signature on the C side.
     *          its goal is to give a generic interface to people that would like to use the JNI library
     * 
     * @param parsedArray   Array where to store the value
     * @param fieldName     The name of the parsed field
     * @param stringToAdd   The parsed data to add
     * @param formatToAdd   The format of the raw data
     */
    @SuppressWarnings("unused")
    static private void addStringToParsingFromC(Object contentHolder, String fieldName, String stringToAdd) {
        ((ParsedObjectContent)contentHolder).setData( stringToAdd);
    }

    /* 
     * Add a parsed 64 bits Pointer value to the Array<br>
     * <br>
     * Note : this function will be called from the C side.
     * Note2: contentHolder is of type "Object" instead of "ParsedObjectContent" to be able to use the most generic function signature on the C side.
     *          its goal is to give a generic interface to people that would like to use the JNI library
     * 
     * @param contentHolder Object where to store the parsed value
     * @param fieldName     The name of the parsed field
     * @param pointerToAdd  The parsed data to add (in 64 bits long!)
     * @param formatToAdd   The format of the raw data
     */
    @SuppressWarnings("unused")
    static private void addLongPointerToParsingFromC(Object contentHolder, String fieldName, long pointerToAdd) {
        ((ParsedObjectContent)contentHolder).setData( new C_Pointer((long) pointerToAdd));
    }

    /* 
     * Add a parsed 32 bits Pointer value to the Array<br>
     * <br>
     * Note : this function will be called from the C side.
     * Note2: contentHolder is of type "Object" instead of "ParsedObjectContent" to be able to use the most generic function signature on the C side.
     *          its goal is to give a generic interface to people that would like to use the JNI library
     * 
     * @param contentHolder Object where to store the parsed value
     * @param fieldName     The name of the parsed field
     * @param pointerToAdd  The parsed data to add (converted in 64 bits long!)
     * @param formatToAdd   The format of the raw data
     */
    @SuppressWarnings("unused")
    static private void addIntPointerToParsingFromC(Object contentHolder, String fieldName, long pointerToAdd) {
        ((ParsedObjectContent)contentHolder).setData( new C_Pointer((int) pointerToAdd));
    }

    /* 
     * Add a parsed short value to the Array<br>
     * <br>
     * Note : this function will be called from the C side.
     * Note2: contentHolder is of type "Object" instead of "ParsedObjectContent" to be able to use the most generic function signature on the C side.
     *          its goal is to give a generic interface to people that would like to use the JNI library
     * 
     * @param contentHolder Object where to store the parsed value
     * @param fieldName     The name of the parsed field
     * @param shortToAdd    The parsed data to add
     * @param formatToAdd   The format of the raw data
     */
    @SuppressWarnings("unused")
    static private void addShortToParsingFromC(Object contentHolder, String fieldName, short shortToAdd) {
        ((ParsedObjectContent)contentHolder).setData( new Short(shortToAdd));
    }

    /* 
     * Add a parsed integer value to the Array<br>
     * <br>
     * Note : this function will be called from the C side.
     * Note2: contentHolder is of type "Object" instead of "ParsedObjectContent" to be able to use the most generic function signature on the C side.
     *          its goal is to give a generic interface to people that would like to use the JNI library
     * 
     * @param contentHolder Object where to store the parsed value
     * @param fieldName     The name of the parsed field
     * @param intToAdd      The parsed data to add
     * @param formatToAdd   The format of the raw data
     */
    @SuppressWarnings("unused")
    static private void addIntegerToParsingFromC(Object contentHolder, String fieldName, int intToAdd) {
        ((ParsedObjectContent)contentHolder).setData( new Integer(intToAdd));
    }

    /* 
     * Add a parsed long value to the Array<br>
     * <br>
     * Note : this function will be called from the C side.
     * Note2: contentHolder is of type "Object" instead of "ParsedObjectContent" to be able to use the most generic function signature on the C side.
     *          its goal is to give a generic interface to people that would like to use the JNI library
     * 
     * @param contentHolder Object where to store the parsed value
     * @param fieldName     The name of the parsed field
     * @param longToAdd     The parsed data to add
     * @param formatToAdd   The format of the raw data
     */
    @SuppressWarnings("unused")
    static private void addLongToParsingFromC(Object contentHolder, String fieldName, long longToAdd) {
        ((ParsedObjectContent)contentHolder).setData( new Long(longToAdd));
    }

    /* 
     * Add a parsed float value to the Array<br>
     * <br>
     * Note : this function will be called from the C side.
     * Note2: contentHolder is of type "Object" instead of "ParsedObjectContent" to be able to use the most generic function signature on the C side.
     *          its goal is to give a generic interface to people that would like to use the JNI library
     * 
     * @param contentHolder Object where to store the parsed value
     * @param fieldName     The name of the parsed field
     * @param floatToAdd    The parsed data to add
     * @param formatToAdd   The format of the raw data
     */
    @SuppressWarnings("unused")
    static private void addFloatToParsingFromC(Object contentHolder, String fieldName, float floatToAdd) {
        ((ParsedObjectContent)contentHolder).setData( new Float(floatToAdd));
    }

    /* 
     * Add a parsed double value to the Array<br>
     * <br>
     * Note : this function will be called from the C side.
     * Note2: contentHolder is of type "Object" instead of "ParsedObjectContent" to be able to use the most generic function signature on the C side.
     *          its goal is to give a generic interface to people that would like to use the JNI library
     * 
     * 
     * @param contentHolder Object where to store the parsed value
     * @param fieldName     The name of the parsed field
     * @param doubleToAdd   The parsed data to add
     * @param formatToAdd   The format of the raw data
     */
    @SuppressWarnings("unused")
    static private void addDoubleToParsingFromC(Object contentHolder, String fieldName, double doubleToAdd) {
        ((ParsedObjectContent)contentHolder).setData( new Double(doubleToAdd));
    }
    
}

/**
 * <b><u>ParsedObjectContent</u></b>
 * <p>
 * ParsedObjectContent class.
 * This class will only be used locally in this object to parse event data more efficiently in the C
 */
class ParsedObjectContent {
    private Object parsedData = null;
    
    public Object getData() {
        return parsedData;
    }
    
    public void setData(Object newData) {
        parsedData = newData;
    }
}