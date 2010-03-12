/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * <b><u>CreateTestFiles</u></b>
 * <p>
 * Create a number of event test files of various lengths.
 * <p>
 * Events have the following format:
 * <ul>
 * <li> [timestamp] [source] [type] [ref] [field]*
 * <li> There are NB_SOURCES sources and NB_TYPES types.
 * <li> The number of fields (0 .. NB_TYPES-1) depends on the event type.
 * </ul>
 */
public class CreateTestFiles {

    // ========================================================================
    // Constants
    // ========================================================================

	private static final String DIRECTORY = "testfiles";
//	private static final String FILE_NAMES[] = { "Test-10", "Test-1K", "Test-10K", "Test-100K" };
//    private static final int    FILE_SIZES[] = {       10 ,     1000 ,     10000 ,     100000  };
	private static final String FILE_NAMES[] = { "Test-10K" };
    private static final int    FILE_SIZES[] = {     10000  };

    private static final int NB_SOURCES = 15;  
    private static final int NB_TYPES   =  7;  

    // ========================================================================
    // Constructors
    // ========================================================================

   /**
     * @param args
     */
    public static void main(String[] args) {
        
        try {
            System.out.println("Creating test files in directory: " + new File(".").getCanonicalPath() + File.separator + DIRECTORY);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < FILE_SIZES.length; i++) {
            try {
            	createTestFile("testfiles" + File.separator + "O-" + FILE_NAMES[i], FILE_SIZES[i], true,  true);
            	createTestFile("testfiles" + File.separator + "E-" + FILE_NAMES[i], FILE_SIZES[i], true,  false);
                createTestFile("testfiles" + File.separator + "R-" + FILE_NAMES[i], FILE_SIZES[i], false, false);
            } catch (Exception e) {
            }
        }

		System.out.println("Done.");
    }

    // ========================================================================
    // Operators
    // ========================================================================

    /**
     * @param file
     * @param size
     * @param monotonic
     * @throws FileNotFoundException
     * @throws IOException
     */
    private static void createTestFile(String file, int size, boolean monotonic, boolean odd) throws FileNotFoundException, IOException {
        DataOutputStream out;
        System.out.println("Creating " + file);
        out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));

        Random generator = new Random(19580427 + size);
        long ts = (monotonic && odd) ? -1 : 0;
        for (int i = 0; i < size; i++) {
            ts += monotonic ? 2 : generator.nextInt(10);
            int sourceIndex = i % NB_SOURCES;
            int typeIndex   = i % NB_TYPES;
            out.writeLong(ts);                      // Timestamp
            out.writeUTF("Source-" + sourceIndex);  // Source
            out.writeUTF("Type-"   + typeIndex);    // Type
            out.writeInt(i + 1);                    // Reference (event #)
            for (int j = 0; j < typeIndex; j++) {
                out.writeUTF("Field-" + sourceIndex + "-" + j);
            }
        }
        out.flush();
        out.close();
    }

}
