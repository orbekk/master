package com.orbekk.stats;

import java.util.List;

public class Analysis {
    final List<Double> samples;
    
    public Analysis(List<Double> samples) {
        this.samples = samples;
    }
    
    public double getAverage() {
        double sum = 0.0;
        for (Double d : samples) {
            sum += d;
        }
        return sum / samples.size();
    }
    
    public double getVariance() {
        double average = getAverage();
        double sumDifferences = 0.0;
        for (Double d : samples) {
            sumDifferences += (d - average) * (d - average);
        }
        return sumDifferences / samples.size();
    }
    
    public double getStandardDeviation() {
        return Math.sqrt(getVariance());
    }
}
