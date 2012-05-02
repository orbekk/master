package com.orbekk.util;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;

public class CyclicCountDownLatchTest {
    CyclicCountDownLatch latch = new CyclicCountDownLatch(1);

    @Test public void initialCount() {
        assertThat(latch.getCount(), is(1));
    }
    
    @Test public void releasesCorrectly() throws Exception {
        latch.countDown();
        assertThat(latch.getCount(), is(0));
        latch.await();
    }
    
    @Test public void testCycle() throws Exception {
        latch.countDown();
        latch.await();
        assertThat(latch.getCount(), is(1));
        latch.countDown();
        latch.await();
    }
    
    @Test public void notAccumulative() throws Exception {
        latch.countDown();
        latch.countDown();
        latch.await();
        assertThat(latch.getCount(), is(1));
    }
}
