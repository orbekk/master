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
