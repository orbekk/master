package com.orbekk.same.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {
    public final static String configurationProperty =
            "com.orbekk.same.config.file";
            
    Logger logger = LoggerFactory.getLogger(getClass());
    Properties configuration = new Properties();
    
    private Configuration() {
        // Use factory methods.
    }
    
    public static Configuration load() {
        Configuration configuration = new Configuration();
        configuration.loadDefault();
        return configuration;
    }
    
    public void loadDefault() {
        String filename = System.getProperty(configurationProperty);
        if (filename != null) {
            try {
                configuration.load(new FileReader(filename));
            } catch (FileNotFoundException e) {
                logger.error("Failed to load configuration. {}", e);
                logger.error("Failed to load configuration. {}={}",
                        configurationProperty, filename);
            } catch (IOException e) {
                logger.error("Failed to load configuration. {}", e);
                logger.error("Failed to load configuration. {}={}",
                        configurationProperty, filename);
            }
        } else {
            logger.error("Failed to load configuration. {}={}",
                    configurationProperty, filename);
        }
    }
    
    public String getProperty(String name) {
        return configuration.getProperty(name);
    }
}
