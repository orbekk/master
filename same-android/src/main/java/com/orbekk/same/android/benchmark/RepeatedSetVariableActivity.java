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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.orbekk.same.Types;
import com.orbekk.same.Variable;
import com.orbekk.same.Variable.OnChangeListener;
import com.orbekk.same.android.ClientInterfaceBridge;

public class RepeatedSetVariableActivity extends Activity {
    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final int WARMUP_ITERATIONS = 10;
    public static final int ITERATIONS = 100;
    private ClientInterfaceBridge client;
    
    private int warmupIterationsPerformed;
    private int iterationsPerformed;
    private Timer timer;
    
    private Variable<Integer> variable;
    
    private OnChangeListener<Integer> changeListener =
            new OnChangeListener<Integer>() {
        @Override
        public void valueChanged(Variable<Integer> variable) {
            variable.update();
            timer.stop();
            iterationFinished();
            if (iterationsPerformed < ITERATIONS) {
                timer.start();
                variable.set(variable.get() + 1);
            } else {
                finalizeBenchmark();
            }
        }
    };
    
    private void finalizeBenchmark() {
        logger.info("Benchmark finished. Samples: " + timer);
    }
    
    /** Returns whether or not we should continue. */
    private void iterationFinished() {
        if (warmupIterationsPerformed < WARMUP_ITERATIONS) {
            warmupIterationsPerformed += 1;
        } else {
            iterationsPerformed += 1;
        }
    }
    
    @Override public void onCreate(Bundle savedBundle) {
        super.onCreate(savedBundle);
    }
    
    public void onResume() {
        super.onResume();
        Toast.makeText(this, "Starting benchmark", Toast.LENGTH_LONG).show();

        timer = new Timer(WARMUP_ITERATIONS + ITERATIONS);
        warmupIterationsPerformed = 0;
        iterationsPerformed = 0;
        client = new ClientInterfaceBridge(this);
        client.connect();
        initializeVariable();
    }
    
    public void initializeVariable() {
        variable = client.createVariableFactory()
                .create("BenchmarkVariable", Types.INTEGER);
        variable.addOnChangeListener(changeListener);
        timer.start();
        variable.set(0);
    }
    
    public void onStop() {
        super.onStop();
        client.disconnect();
    }
    
}
