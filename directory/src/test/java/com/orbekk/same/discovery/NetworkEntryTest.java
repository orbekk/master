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
package com.orbekk.same.discovery;

import static org.junit.Assert.*;

import org.junit.Test;

import com.orbekk.same.directory.NetworkEntry;

public class NetworkEntryTest {
    @Test
    public void testExpiry() {
        NetworkEntry entry = new NetworkEntry("NetworkA", "UrlA");
        entry.register(100);
        assertFalse(entry.hasExpired(50));
        assertTrue(entry.hasExpired(101));
    }
    
    @Test
    public void testRegister() {
        NetworkEntry entry = new NetworkEntry("NetworkB", "UrlB");
        long time = 1000000000000l;
        entry.register(time);
        assertTrue(entry.hasExpired(time+1));
        entry.register(time + 1000);
        assertFalse(entry.hasExpired(time + 500));
    }
}
