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
package com.orbekk.same.directory;

public class NetworkEntry {
    public String networkName;
    public String masterUrl;
    private long lastRegisteredMillis = -1;
    
    public NetworkEntry(String networkName, String masterUrl) {
        this.networkName = networkName;
        this.masterUrl = masterUrl;
    }

    public void register(long registeredTime) {
        lastRegisteredMillis = registeredTime;
    }
    
    public boolean hasExpired(long latestValidTimeMillis) {
        return lastRegisteredMillis < latestValidTimeMillis;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof NetworkEntry) {
            NetworkEntry o = (NetworkEntry)other;
            return stringEquals(networkName, o.networkName) &&
                    stringEquals(masterUrl, o.masterUrl);
        }
        return false;
    }
    
    private boolean stringEquals(String s1, String s2) {
        if (s1 == null) {
            return s2 == null;
        } else {
            return s1.equals(s2);
        }
    }
}
