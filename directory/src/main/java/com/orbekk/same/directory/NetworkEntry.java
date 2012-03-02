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
            NetworkEntry otherEntry = (NetworkEntry)other;
            return networkName.equals(otherEntry.networkName) &&
                    masterUrl.equals(otherEntry.masterUrl);
        }
        return false;
    }
}
