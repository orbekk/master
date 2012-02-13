package com.orbekk.same.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {
    public final static String configurationProperty =
            "com.orbekk.same.config.file";
            
    static final Logger logger = LoggerFactory.getLogger(Configuration.class);
    Properties configuration = new Properties();
    
    public Configuration(Properties properties) {
        this.configuration = properties;
    }
    
    Configuration() {
        // Use factory methods.
    }
    
    public static Configuration loadOrDie() {
        Configuration configuration = new Configuration();
        boolean status = configuration.loadDefault();
        if (!status) {
            logger.error("Could not load configurotion.");
            System.exit(1);
        }
        return configuration;
    }
    
    public static Configuration load() {
        Configuration configuration = new Configuration();
        configuration.loadDefault();
        return configuration;
    }
    
    public boolean loadDefault() {
        String filename = System.getProperty(configurationProperty);
        if (filename != null) {
            try {
                configuration.load(new FileReader(filename));
                return true;
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
        return false;
    }
    
    public String get(String name) {
        String value = configuration.getProperty(name);
        if (value == null) {
            logger.error("Property {} = null", name);
        }
        return value;
    }
    
    public Integer getInt(String name) {
        if (get(name) == null) {
            return null;
        }
        return Integer.valueOf(get(name));
    }
}
