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
package com.orbekk.same;

import java.util.concurrent.atomic.AtomicBoolean;

/** Updates a variable on-demand.
 */
public class VariableUpdaterTask<T> extends Thread
        implements Variable.OnChangeListener<T> {
    private Variable<T> variable;
    private volatile T newValue;
    private AtomicBoolean hasNewValue = new AtomicBoolean(false);
    private AtomicBoolean isReady = new AtomicBoolean(true);
    
    public VariableUpdaterTask(Variable<T> variable) {
        super("VariableUpdater");
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
        if (Thread.currentThread().isInterrupted()) {
            return;
        }
        while(!v.get()) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
    
    @Override
    public void run() {
        variable.addOnChangeListener(this);
        while (true) {
            waitFor(isReady);
            waitFor(hasNewValue);
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            performWork();
        }
        variable.removeOnChangeListener(this);
    }

    @Override
    public synchronized void valueChanged(Variable<T> unused) {
        isReady.set(true);
        notifyAll();
    }
}