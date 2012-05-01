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

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class VariableUpdaterTaskTest {
    Variable<String> v;
    VariableUpdaterTask<String> updater;
    
    @Before public void setUp() {
        v = mock(Variable.class);
        updater = new VariableUpdaterTask<String>(v);
    }
        
    @Test
    public void updatesValue() {
        updater.set("FirstValue");
        updater.performWork();
        verify(v).set("FirstValue");
    }

    @Test
    public void noUpdateIfNotSet() {
        updater.set("FirstValue");
        updater.performWork();
        reset(v);
        updater.performWork();
        verify(v, never()).set(anyString());
    }
    
    @Test
    public void noUpdateIfNotReady() {
        updater.set("FirstValue");
        updater.performWork();
        reset(v);
        updater.set("SecondValue");
        updater.performWork();
        verify(v, never()).set(anyString());
    }
    
    @Test
    public void updatesWhenReady() {
        updater.set("Value1");
        updater.performWork();
        reset(v);
        updater.valueChanged(null);
        updater.set("Value2");
        updater.performWork();
        verify(v).set("Value2");
    }
    
    @Test
    public void choosesLastUpdate() {
        updater.set("FirstValue");
        updater.set("SecondValue");
        updater.performWork();
        verify(v).set("SecondValue");
    }
}
