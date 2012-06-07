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

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import com.orbekk.util.CyclicCountDownLatch;
import com.orbekk.util.DelayedOperation;


/** Updates a variable on-demand.
 */
public class VariableUpdaterTask<T> extends Thread
        implements Variable.OnChangeListener<T> {
    private static Logger logger = Logger.getLogger(VariableUpdaterTask.class.getName());
    private Variable<T> variable;
    private AtomicReference<T> newValue = new AtomicReference<T>();
    private CyclicCountDownLatch hasNewValue = new CyclicCountDownLatch(1);
    private CyclicCountDownLatch isReady = new CyclicCountDownLatch(1);
    
    public VariableUpdaterTask(Variable<T> variable) {
        super("VariableUpdater");
        this.variable = variable;
    }
    
    public void set(T newValue) {
        this.newValue.set(newValue);
        hasNewValue.countDown();
    }
    
    void setValue(T newValue) {
        while (!Thread.currentThread().isInterrupted()) {
            DelayedOperation op = variable.set(newValue);
            if (op.getStatus().isOk()) {
                return;
            } else if (op.getStatus().isConflict()) {
                // We have a new value, but we need to wait for an update
                // in order to overwrite it.
                hasNewValue.countDown();
                return;
            } else if (op.getStatus().isError()) {
                // Error during update. Just retry.
                logger.info("Error updating value. Status: " + op.getStatus());
            } else {
                throw new AssertionError("Unknown state.");
            }
        }
    }

    @Override
    public void valueChanged(Variable<T> variable) {
        isReady.countDown();
        variable.update();
    }
    
    @Override
    public void run() {
        isReady.countDown();  // Initially ready.
        variable.addOnChangeListener(this);
        while (!Thread.interrupted()) {
            try {
                hasNewValue.await();
                isReady.await();
                setValue(newValue.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        logger.info("VariableUpdaterTask finished.");
    }
}