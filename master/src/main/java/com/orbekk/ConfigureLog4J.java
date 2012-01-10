package com.orbekk;
import org.apache.log4j.Level;
import android.os.Environment;
import de.mindpipe.android.logging.log4j.LogConfigurator;

public class ConfigureLog4J {
    static {
        final LogConfigurator logConfigurator = new LogConfigurator();

        logConfigurator.setFileName(Environment.getExternalStorageDirectory() + "myapp.log");
        logConfigurator.setRootLevel(Level.DEBUG);
        logConfigurator.setUseLogCatAppender(true);
        // Set log level of a specific logger
        // logConfigurator.setLevel("org.apache", Level.ERROR);
        logConfigurator.configure();
        System.err.println("GOT HERE.");
    }
}