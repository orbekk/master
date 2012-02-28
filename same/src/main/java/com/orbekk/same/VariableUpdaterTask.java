package com.orbekk.same;

import java.util.concurrent.atomic.AtomicBoolean;

/** Updates a variable on-demand.
 */
public class VariableUpdaterTask<T> extends Thread
        implements Variable.OnChangeListener<T> {
    private Variable<T> variable;
    private T newValue;
    private AtomicBoolean hasNewValue = new AtomicBoolean(false);
    private AtomicBoolean isReady = new AtomicBoolean(true);
    
    public VariableUpdaterTask(Variable<T> variable) {
        this.variable = variable;
    }
    
    public synchronized void set(T newValue) {
        this.newValue = newValue;
        hasNewValue.set(true);
        notifyAll();
    }
    
    /** Update the variable once. */
    public void performWork() {
        boolean shouldDoWork = false;
        synchronized(this) {
            shouldDoWork = hasNewValue.get() && isReady.get();
            hasNewValue.set(false);
            isReady.set(false);
        }
        if (shouldDoWork) {
            variable.set(newValue);
        }
    }
    
    private synchronized void waitFor(AtomicBoolean v) {
        while(!v.get()) {
            try {
                wait();
            } catch (InterruptedException e) {
                return;
            }
        }
    }
    
    public void run() {
        while (true) {
            waitFor(isReady);
            waitFor(hasNewValue);
            if (Thread.interrupted()) {
                break;
            }
            performWork();
        }
    }

    @Override
    public synchronized void valueChanged(Variable<T> unused) {
        isReady.set(true);
        notifyAll();
    }
}