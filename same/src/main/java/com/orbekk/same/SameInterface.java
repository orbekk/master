package com.orbekk.same;

import java.util.List;

import org.codehaus.jackson.type.TypeReference;

public interface SameInterface {
    /**
     * Get the state with identifier 'id'.
     */
    String get(String id);
    
    /**
     * Get the state with identifier 'id', converted to a Java
     * object of type T using Jackson.
     */
    <T> T get(String id, TypeReference<T> type);
    
    /**
     * Get the state with identifier 'id' as a list.
     */
    List<String> getList(String id);
    
    /**
     * Set the state.
     * 
     * @throws UpdateConflictException
     */
    void set(String id, String data) throws UpdateConflict;

    /**
     * Set the state.
     * 
     * Retry until there is no conflict.
     */
    void forceSet(String id, String data);
    
    /**
     * Set from an object: Pass it, e.g., a List<String>.
     */
    void setObject(String id, Object data);
}
