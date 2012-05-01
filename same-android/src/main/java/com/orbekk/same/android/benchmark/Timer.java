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
package com.orbekk.same.android.benchmark;

import java.util.ArrayList;

public class Timer {
    ArrayList<Long> samples;
    boolean running = false;
    long startTime = 0;
    
    public Timer() {
        samples = new ArrayList<Long>();
    }
    
    public Timer(int capacity) {
        samples = new ArrayList<Long>(capacity);
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public void start() {
        startTime = System.currentTimeMillis();
        running = true;
    }
    
    public void stop() {
        long stopTime = System.currentTimeMillis();
        running = false;
        samples.add(stopTime - startTime);
    }
    
    public long getLastMeasurement() {
        return samples.get(samples.size() - 1);
    }
    
    public ArrayList<Long> getTimes() {
        return new ArrayList<Long>(samples);
    }

    public long getSum() {
        long sum = 0;
        for (long s : samples) {
            sum += s;
        }
        return sum;
    }
    
    public String toString() {
        return "Timer[sum(" + getSum() + "), samples(" + samples + ")]";
    }
}
