package com.orbekk.same.config;

import static org.junit.Assert.*;

import org.junit.Test;

public class ConfigurationTest {
    @Test public void testLoadConfiguration() {
        String resources = "src/test/resources/";
        System.setProperty(Configuration.configurationProperty,
                resources + "com/orbekk/same/config/test.properties");
        Configuration configuration = Configuration.load();
        assertEquals("Test Value",
                configuration.getProperty("testProperty"));
                
    }
}
