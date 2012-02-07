package com.orbekk.net;

import static org.junit.Assert.*;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BroadcastListenerTest {
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    @Test
    public void interruptWorks() throws Exception {
        final BroadcastListener listener = new BroadcastListener(0);

        Thread t = new Thread() {
            @Override public void run() {
                listener.listen();
            }
        };
        t.start();
        
        while (listener.socket == null) {
            logger.info("Waiting for listener to start.");
            Thread.sleep(100);
        }
        
        listener.interrupt();
        t.join();
    }

}
