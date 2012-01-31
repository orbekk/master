package com.orbekk.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class WorkQueueTest {   
    @Test
    public void testPerformsWork() throws Exception {
        final ArrayList<Integer> doubled = new ArrayList<Integer>();
        WorkQueue<Integer> worker = new WorkQueue<Integer>() {
            @Override protected void onChange() {
                List<Integer> list = getAndClear();
                for (int x : list) {
                    doubled.add(x * 2);
                }
                synchronized (doubled) {
                    doubled.notifyAll();
                }
            }
        };
        
        synchronized (doubled) {
            worker.start();
            worker.add(1);
            doubled.wait();
            worker.interrupt();
        }
        
        worker.join();
        assertEquals(2, (int)doubled.get(0));
    }

}
