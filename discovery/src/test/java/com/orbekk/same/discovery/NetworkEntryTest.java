package com.orbekk.same.discovery;

import static org.junit.Assert.*;

import org.junit.Test;

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
