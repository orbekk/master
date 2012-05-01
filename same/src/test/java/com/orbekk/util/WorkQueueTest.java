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
