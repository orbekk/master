/**
 * Copyright 2012 Kjetil Ørbekk <kjetil.orbekk@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orbekk.util;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/** This class provides something of a cyclic version of a CountDownLatch.
 * 
 * The latch resets when await() completes. It may ignore calls to countDown()
 * that occur before await() returns (it is not accumulative).
 * 
 * Use this class with caution.
 */
public class CyclicCountDownLatch {
    private static class Sync extends AbstractQueuedSynchronizer {
        final int count;
        
        public Sync(int count) {
            this.count = count;
            setState(count);
        }
        
        @Override protected boolean tryReleaseShared(int unused) {
            boolean success = false;
            while (!success) {
                int expect = getState();
                int update = Math.max(expect - 1, 0);
                success = compareAndSetState(expect, update);
            }
            return getState() <= 0;
        }
        
        @Override protected int tryAcquireShared(int unused) {
            boolean success = false;
            while (!success) {
                int expect = getState();
                if (expect > 0) {
                    break;
                }
                int update = count;
                success = compareAndSetState(expect, update);
            }
            
            if (success) {
                // Non-exclusive success. Other threads may attempt to acquire.
                return 1;
            } else {
                // Failure.
                return -1;
            }
        }
        
        public int getCount() {
            return getState();
        }
    }
    
    private final Sync sync;
    
    public CyclicCountDownLatch(int count) {
        sync = new Sync(count);
    }
    
    public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(-1);
    }
    
    public void countDown() {
        sync.releaseShared(-1);
    }
    
    public int getCount() {
        return sync.getCount();
    }
}
