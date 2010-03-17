/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.views.project.dialogs;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.lttng.LttngException;
import org.eclipse.linuxtools.lttng.trace.LTTngTraceVersion;
import org.eclipse.linuxtools.lttng.ui.views.project.handlers.TraceErrorHandler;
import org.eclipse.linuxtools.lttng.ui.views.project.model.LTTngProjectNode;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceImportPage1;

/**
 * <b><u>ImportTraceWizardPage</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
@SuppressWarnings("restriction")
public class ImportTraceWizardPage extends WizardFileSystemResourceImportPage1 {
	
	private boolean isContainerSet = false;
	private String  initialContainerString = "";
	private String  selectedSourceDirectory = "";
	
	public ImportTraceWizardPage(IWorkbench workbench, IStructuredSelection selection) {
		super(workbench, selection);
		
		LTTngProjectNode folder = (LTTngProjectNode) selection.getFirstElement();
		String path = folder.getTracesFolder().getFolder().getFullPath().toOSString();
		
		initialContainerString = path;
		setContainerFieldValue(path);
	}
	
	
	public String getTraceDirectory() {
		String tmpPath = "";
		if ( (getSourceDirectory() != null) && (getSourceDirectory().getName() != null) ) {
			tmpPath = this.getSourceDirectory().getName().toString();
		}
		
		return tmpPath;
	}
	
	public String getInitialContainerString() {
		return initialContainerString;
	}
	
	public String getTracepath() {
		String tmpPath = "";
		if ( (getSourceDirectory() != null) && (getSourceDirectory().getPath() != null) ) {
			tmpPath = this.getSourceDirectory().getPath().toString();
		}
		
		return tmpPath;
	}
	
	public String getDestination() {
		String returnPath = null;
		
		if ( getContainerFullPath() != null ) {
			returnPath = getContainerFullPath().toString();
		}
		return returnPath;
	}
	
	public boolean isSelectedElementsValidLttngTraces() {
		boolean returnedValue = true;
		
		// We don't want to test until something is selected
		if ( selectionGroup.getCheckedElementCount() > 0 ) {
			
			// We don't want to revalidate each time, only want a new directory is selected
			if ( ! selectedSourceDirectory.equals(getSourceDirectory().getAbsolutePath().toString()) )
			{
				try {
					if ( isPathLttngTrace( getSourceDirectory().getAbsolutePath() ) == false ) {
						returnedValue = false;
						selectedSourceDirectory = "";
						
						String errMessage[] = { "Couldn't get LTTng version number for the path : " }; 
						errMessage = extendErrorMessage(errMessage, getSourceDirectory().getAbsolutePath() );
						errMessage = extendErrorMessage(errMessage, "");
		    			errMessage = extendErrorMessage(errMessage, "Verify that the directory is a valid LTTng trace directory.");
		    			errMessage = extendErrorMessage(errMessage, "Make sure the top directory is the trace itself and not any of its parent.");
		    			showVersionErrorPopup(errMessage);
		    			selectionGroup.setAllSelections(false);
					}
					else {
						selectedSourceDirectory = getSourceDirectory().getAbsolutePath();
						
						if ( isContainerSet == false ) {
							isContainerSet = true;
							
							if ( ! getDestination().toString().equals( getInitialContainerString() + "/" + getTraceDirectory() )  ) {
								// *** HACK ***
						    	// Force a sane destination to avoid imported files to end up in the root of the "Traces/" directory
						    	setContainerFieldValue(getInitialContainerString() + "/" + getTraceDirectory());
							}
						}
					}
		    	}
		    	catch (LttngException e) {
		    		String[] errorMessages = e.toString().split("\n");
		    		String exceptionMessage[] = { "Version check failed for the path : ", this.getTracepath(), "", "Returned error was :" }; 
		    		
		    		for ( int pos=0; pos<errorMessages.length; pos++) {
		    			exceptionMessage = extendErrorMessage(exceptionMessage, errorMessages[pos]);
		    		}
		    		
		    		showVersionErrorPopup(exceptionMessage);
		    		selectionGroup.setAllSelections(false);
		    		returnedValue = false;
		    	}
			}
		}
		isContainerSet = false;
		
    	return returnedValue;
	}
	
	
	public boolean isPathLttngTrace(String path) throws LttngException {
		
		boolean returnedValue = true;
		
		// Ask for a LttngTraceVersion for the given path
		LTTngTraceVersion traceVersion = new LTTngTraceVersion( path );
		
		// If this is not a valid LTTng trace
		if ( traceVersion.isValidLttngTrace() == false ) {
    		returnedValue = false;
    	}
		
		return returnedValue;
	}
	
	
	public String[] extendErrorMessage(String[] oldErrorMessage, String lineToAdd) {
		String tmSwapMessage[] = new String[oldErrorMessage.length + 1];
		for ( int pos = 0; pos<oldErrorMessage.length; pos++) {
			tmSwapMessage[pos] = oldErrorMessage[pos];
		}
		tmSwapMessage[oldErrorMessage.length] = lineToAdd;
		
		return tmSwapMessage;
	}
	
	
    /**
     * This function will show a version error popup that contain the given message.
     * 
     */
    public void showVersionErrorPopup(String[] errMessages) {
    	TraceErrorHandler errorDialog = new TraceErrorHandler(errMessages);
    	try {
    		errorDialog.execute(null);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
       
    
//    // *** HACK HACK AND HACK ***
//    // Everything below is a proof of concept on how we could tweak the import wizard to act according to our plan
//    // Uncomment everything below if you want to test it, but please, does not put any of this into production
//    @SuppressWarnings({ "unchecked", "rawtypes" })
//	@Override
//    public boolean finish() {
//        if (!ensureSourceIsValid()) {
//			return false;
//		}
//        saveWidgetValues();
//        
//        Iterator resourcesEnum = getSelectedResources().iterator();
//        List listRealFiles = new ArrayList();
//        
//        // ****
//        // HACK #1 : 
//        // We need to convert everything into java.io.File because ImportOperation does NOT support FileSystemElement
//        while (resourcesEnum.hasNext()) {
//        	FileSystemElement tmpFileElement = ((FileSystemElement)resourcesEnum.next());
//        	java.io.File tmpRealFile = new java.io.File(tmpFileElement.getFileSystemObject().toString());
//        	
//            listRealFiles.add(tmpRealFile);
//        }
//        
//        if (listRealFiles.size() > 0) {
//        	// Call import ressources (code is below)
//			return importResources(listRealFiles);
//		}
//
//        MessageDialog.openInformation(getContainer().getShell(),
//                DataTransferMessages.DataTransfer_information,
//                DataTransferMessages.FileImport_noneSelected);
//        return false;
//    }
//    
//    @Override
//    protected boolean importResources(List fileSystemObjects) {
//    	// *** Explanation of the hackssss
//    	// We want the import wizard to import everything in the form of : 
//    	//		trace1/ -> tracefiles*
//    	//
//    	// However, the wizard is too dumb to do the following and will recreate the full architecture the user selected.
//    	// So, depending what the user select, we could end up with something like : 
//    	// 		home/user/somewhere/trace1/ -> tracefiles*
//    	//
//    	// Since there is nothing to do with that, we need to change the "source" and the "to-import files" to reflect this.
//    	// Basically, from the case above, the "source" should be changed to "trace1/" and "to-import files" 
//    	//		should have the correct parent so the wizard can still find them
//    	//
//    	// Let's see how fun it is to do with mr. import wizard.
//    	
//    	
//    	List listRealFilesShortPath = new ArrayList();
//    	java.io.File newFullSource = getSourceDirectory();
//    	
//    	// We will loop for every "to-import full path files" we have and recreate "short path" files
//    	// Mean, the current path of the file is currently something like : 
//    	//		Path : /home/billybob/mytraces/trace1/metadata_0  	Parent : null
//    	// And we want something less dumb like : 
//    	//		Path : metadata_0 									Parent : /home/billybob/mytraces/trace1/
//    	for (int pos=0; pos<fileSystemObjects.size(); pos++) {
//    		java.io.File oldFile  = (java.io.File)fileSystemObjects.get(pos);
//    		java.io.File newShortPathFile = oldFile;
//    		
//    		// ***
//    		// HACK #2 : We need to ajust the source of the files!
//    		// Our current source is probably like : 
//    		//		(Source) 	Path : / (or null?)
//    		//		(Files)		Path : /home/billybob/mytraces/trace1/metadata_0  	Parent : null
//    		// We want something like : 
//    		//		(Source) 	Path : /home/billybob/mytraces/trace1/
//    		//    	(Files)		Path : metadata_0  									Parent : /home/billybob/mytraces/trace1/
//    		// 
//    		// *BUG : However, we might need MULTIPLE sources since we could have MULTIPLE traces selected... 
//    		//	THIS IS NOT HANDLED YET.
//    		
//    		// Make a new path like -> /home/billybob/mytraces/trace1/
//    		String newParent = oldFile.getAbsolutePath().substring(0, oldFile.getAbsolutePath().lastIndexOf("/") );
//    		
//    		// Create a "short path file" with the good parent from it. This give : 
//    		// (Files)	Path : metadata_0  Parent : /home/billybob/mytraces/trace1/
//			newShortPathFile = new java.io.File(newParent, oldFile.getName() );
//			
//			// Create a new "full source" directory -> /home/billybob/mytraces/trace1/
//			newFullSource = new java.io.File( newParent );
//			
//			// Add our pretty file to the new List
//    		listRealFilesShortPath.add(newShortPathFile);
//    	}
//    	
//    	// ***
//    	// HACK #3
//    	// Now that we have everything, we need to AJUST THE DESTINATION
//    	// To do so, we ajust the "ContainerValue" text field.
//    	//
//    	// Right now we have something like : 
//    	//		Path -> /where/to/import/
//    	//    	(Files)		Path : metadata_0  	Parent : /home/billybob/mytraces/trace1/
//		// We want something like : 
//    	//		Path -> /where/to/import/trace1/
//    	//    	(Files)		Path : metadata_0  	Parent : /home/billybob/mytraces/trace1/
//    	//
//    	
//    	// We take the current text field and we add the "full source" name
//    	//	Note : the "name" is the last directory name so "trace1" is returned for a path like "/home/billybob/mytraces/trace1/"
//    	setContainerFieldValue(getContainerFullPath() + "/" + newFullSource.getName());
//    	
//    	/*
//    	System.out.println("\n\n" + getContainerFullPath());
//    	System.out.println(newFullSource);
//    	System.out.println(FileSystemStructureProvider.INSTANCE);
//    	System.out.println(this.getContainer());
//    	System.out.println(fileSystemObjects);
//    	*/
//    	
//    	// Finally import !!
//        ImportOperation operation = new ImportOperation(getContainerFullPath(), newFullSource, FileSystemStructureProvider.INSTANCE, this, listRealFilesShortPath);
//        
//        operation.setContext(getShell());
//        return executeImportOperation(operation);
//    }
//    
//    // This function test if the selected directory are LTTng traces
//    // This one is made to work with the madness above.
//	public boolean isSelectedElementsValidLttngTraces() {
//		boolean returnedValue = true;
//		
//		String errMessage[] = { "Couldn't get LTTng version number for the path : " }; 
//		
//		// We don't want to test until something is selected
//		if ( selectionGroup.getCheckedElementCount() > 0 ) {
//			try {
//				List<MinimizedFileSystemElement> selectionList = selectionGroup.getAllWhiteCheckedItems();
//				MinimizedFileSystemElement tmpSelectedElement = null;
//				
//				for ( int x=0; x<selectionList.size(); x++) {
//					tmpSelectedElement = selectionList.get(x);
//					
//					// *** VERIFY ***
//					// Not sure ALL directory are checked.
//					if ( tmpSelectedElement.isDirectory() ) {
//						String tmpPath = tmpSelectedElement.getFileSystemObject().toString();
//						if ( isPathLttngTrace( tmpPath ) == false ) {
//							returnedValue = false;
//							errMessage = extendErrorMessage(errMessage, tmpPath);
//						}
//					}
//				}
//				
//	    		if ( returnedValue == false ) {
//	    			errMessage = extendErrorMessage(errMessage, "");
//	    			errMessage = extendErrorMessage(errMessage, "Verify that the directory is a valid LTTng trace directory.");
//	    			showVersionErrorPopup(errMessage);
//	    			selectionGroup.setAllSelections(false);
//	    		}
//	    	}
//	    	catch (LttngException e) {
//	    		String exceptionMessage[] = { "Version check failed for the path : ", this.getTracepath(), "", "Returned error was :", e.toString() }; 
//	    		showVersionErrorPopup(exceptionMessage);
//	    		selectionGroup.setAllSelections(false);
//	    		returnedValue = false;
//	    	}
//		}
//		
//    	return returnedValue;
//	}
    
}
