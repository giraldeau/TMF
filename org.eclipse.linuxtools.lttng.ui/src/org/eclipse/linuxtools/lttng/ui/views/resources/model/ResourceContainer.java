/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.resources.model;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeEventResource;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeResourceFactory;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeEventResource.ResourceTypes;

/**
 * Common location to allocate the resources in use by the resource view
 * 
 * @author alvaro
 * 
 */
public class ResourceContainer {
	// ========================================================================
	// Data
	// ========================================================================
	private final HashMap<ResourceKey, TimeRangeEventResource> resources = new HashMap<ResourceKey, TimeRangeEventResource>();
	private static Integer uniqueId = 0;
	
	
	// ========================================================================
	// Constructor
	// ========================================================================
	/**
	 * Package level constructor
	 */
	public ResourceContainer() { }
	
	/**
	 * Interface to add resources.
	 * 
	 * @param process
	 */
	public void addResource(TimeRangeEventResource newResource) {
		if (newResource != null) {
		    resources.put( new ResourceKey(newResource),newResource);
		}
	}
	
	// ========================================================================
	// Methods
	// ========================================================================
	/**
     * Request a unique ID
     * 
     * @return Integer
     */
    public Integer getUniqueId() {
        return uniqueId++;
    }

	/**
	 * This method is intended for read only purposes in order to keep the
	 * internal data structure in Synch
	 * 
	 * @return
	 */
	public TimeRangeEventResource[] readResources() {
		return resources.values().toArray(
				new TimeRangeEventResource[resources.size()]);
	}
	
	/**
	 * Clear the children information for resources related to a specific trace
	 * e.g. just before refreshing data with a new time range
	 * 
	 * @param traceId
	 */
	public void clearChildren(String traceId) {
	    TimeRangeEventResource newRes = null;
        Iterator<ResourceKey> iterator = resources.keySet().iterator();
        
        while (iterator.hasNext()) {
            newRes = resources.get(iterator.next());
            
            if (newRes.getTraceId().equals(traceId)) {
				newRes.reset();
            }
        }
	}

	/**
	 * Clear all resources items e.g. when a new experiment is selected
	 */
	public void clearResources() {
		resources.clear();
	}

	/**
	 * Remove the resources related to a specific trace e.g. during trace
	 * removal
	 * 
	 * @param traceId
	 */
	public void removeResources(String traceId) {
	    ResourceKey newKey = null;

	    Iterator<ResourceKey> iterator = resources.keySet().iterator();
        while (iterator.hasNext()) {
            newKey = iterator.next();
            
            if (resources.get(newKey).getTraceId().equals(traceId)) {
                resources.remove(newKey);
            }
        }
	}
	
	
	/**
	 * Search by keys (resourceId, traceId and type)<p>
	 * 
     * A match is returned if the three arguments received match an entry
     *  Otherwise null is returned
     *  
     * @param searchedId        The ressourceId we are looking for
     * @param searchedType      The ressourceType we are looking for
     * @param searchedTraceId   The traceId (trace name?) we are looking for
     * 
     * @return TimeRangeEventResource
     */
    public TimeRangeEventResource findResource(Long searchedId, ResourceTypes searchedType, String searchedTraceId) {
		// Get the EventResource associated to a key we create here
        TimeRangeEventResource foundResource = resources.get( new ResourceKey(searchedId, searchedTraceId, searchedType) );
        
        return foundResource;
    }
	
	/* 
     *  MAIN : For testing only!
     */
     public static void main(String[] args) {
         
         System.out.println("**** TEST PART 1 WITH STANDALONE *** ");
         HashMap<ResourceKey, String> newMap = new HashMap<ResourceKey, String>();
         
         ResourceKey test1 = new ResourceKey(0L,"trace1", ResourceTypes.CPU);
         ResourceKey test2 = new ResourceKey(0L, "trace1", ResourceTypes.CPU);
         newMap.put(test2, "BUG BUG BUG");
         newMap.put(test1, "JOY JOY JOY");
         
         // Test1 and TestKey  return the same value!
         ResourceKey testKey = new ResourceKey(0L, "trace1", ResourceTypes.CPU);
         System.out.println( newMap.get(testKey) + " == " + newMap.get(test1) );
         
         // Test2 should return the same as Test1
         System.out.println( "JOY JOY JOY == " + newMap.get(test2) );
         
         
         
         
         System.out.println("**** TEST PART 2 WITH TimeRangeEventResource *** ");
         newMap.clear();
		TimeRangeResourceFactory rfactory = TimeRangeResourceFactory
				.getInstance();
		TimeRangeEventResource tmpRes1 = rfactory.createResource(0, 0, 0,
				"name1", "trace1", "classname1", ResourceTypes.CPU, 0L, 0l);
		TimeRangeEventResource tmpRes2 = rfactory.createResource(0, 0, 0,
				"name2", "trace1", "classname2", ResourceTypes.CPU, 0L, 0l);
         
         ResourceKey test3 = new ResourceKey(tmpRes1);
         ResourceKey test4 = new ResourceKey(tmpRes2);
         
         newMap.put(test3, "BUG BUG BUG");
         newMap.put(test4, "JOY JOY JOY");
         
         // Test3 and Test4  return the same value!
         System.out.println( newMap.get(test3) + " == " + newMap.get(test4) );
         
         
         ResourceKey testKey2 = new ResourceKey(0L, "trace1", ResourceTypes.CPU);
         // TestKey2 should return the same as Test3 AND Test4
         System.out.println( newMap.get(test4) + " == " + newMap.get(test4) + " == " + newMap.get(testKey2) );
     }
}


class ResourceKey {
    
    private TimeRangeEventResource valueRef = null;
    
    private Long       resourceId = null;
    private String        traceId = null;
    private ResourceTypes type = null;
    
    @SuppressWarnings("unused")
    private ResourceKey() { }
    
    public ResourceKey(TimeRangeEventResource newRef) {
        valueRef = newRef;
    }
    
    public ResourceKey(Long newId, String newTraceId, ResourceTypes newType) {
        resourceId = newId;
        traceId = newTraceId;
        type = newType;
    }
    
    @Override
    public boolean equals(Object obj) {
        boolean isSame = false;
        
        if ( obj instanceof ResourceKey ) {
            if ( (  ((ResourceKey)obj).getResourceId().equals(this.getResourceId()) ) &&
                 (  ((ResourceKey)obj).getTraceId().equals(this.getTraceId()) ) &&
                 (  ((ResourceKey)obj).getType().equals(this.getType()) ) )
            {
                isSame = true;
            }
        }
        
        return isSame;
    }
    
    // *** WARNING : Everything in there work because the check "valueRef != null" is the same for ALL getter
    // Do NOT change this check without checking.
    public Long getResourceId() {
        if ( valueRef != null ) {
            return valueRef.getResourceId();
        }
        else {
            return resourceId;
        }
    }

    public String getTraceId() {
        if ( valueRef != null ) {
            return valueRef.getTraceId();
        }
        else {
            return traceId;
        }
    }

    public ResourceTypes getType() {
        if ( valueRef != null ) {
            return valueRef.getType();
        }
        else {
            return type;
        }
    }

    @Override
    public int hashCode() { 
        return this.toString().hashCode();
    }
    
    
    @Override
    public String toString() {
        if ( valueRef != null ) {
            return (valueRef.getResourceId().toString() + ":" + valueRef.getTraceId().toString() + ":" + valueRef.getType().toString());
        }
        return (resourceId + ":" + traceId + ":" + type);
    }
}
