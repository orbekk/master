/**
 * Copyright 2012 Kjetil Ørbekk <kjetil.orbekk@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orbekk.same;

import java.util.List;

import org.codehaus.jackson.type.TypeReference;

@Deprecated
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
     * Set from an object: Pass it, e.g., a List<String>.
     */
    void setObject(String id, Object data);

    void addStateChangedListener(StateChangedListener listener);
    void removeStateChangedListener(StateChangedListener listener);
}
