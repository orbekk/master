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
