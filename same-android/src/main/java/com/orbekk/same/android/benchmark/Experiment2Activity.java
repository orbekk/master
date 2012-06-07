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

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.protobuf.RpcCallback;
import com.orbekk.protobuf.Rpc;
import com.orbekk.protobuf.RpcChannel;
import com.orbekk.same.Services;
import com.orbekk.same.Types;
import com.orbekk.same.Variable;
import com.orbekk.same.Variable.OnChangeListener;
import com.orbekk.same.VariableUpdaterTask;
import com.orbekk.same.android.ClientInterfaceBridge;
import com.orbekk.same.android.SameService;
import com.orbekk.stats.Common;
import com.orbekk.stats.Experiments.Empty;
import com.orbekk.stats.Experiments.Experiment2;
import com.orbekk.stats.Experiments.SimpleTiming;

public class Experiment2Activity extends Activity {
    private Logger logger = LoggerFactory.getLogger(getClass());
    public static final int WARMUP_ITERATIONS = 2;
    public static final int ITERATIONS = 50;
    private ClientInterfaceBridge client;
    
    private int warmupIterationsPerformed;
    private int iterationsPerformed;
    private volatile Timer timer;
    
    private volatile VariableUpdaterTask<Integer> updater;    
    private volatile Variable<Integer> variable;
    private volatile RpcChannel channel;
    
    private OnChangeListener<Integer> changeListener =
            new OnChangeListener<Integer>() {
        @Override
        public void valueChanged(Variable<Integer> variable) {
            stopIteration();
            if (iterationsPerformed < ITERATIONS) {
                startIteration();
            } else {
                finalizeBenchmark();
            }
        }
    };
    
    private void startIteration() {
        class Callback implements RpcCallback<Services.Empty> {
            private Rpc rpc;
            
            public Callback(Rpc rpc) {
                this.rpc = rpc;
            }
            
            @Override public void run(Services.Empty response) {
                if (response == null) {
                    logger.error("Benchmark failed: " + rpc);
                }
            }
        }
        Rpc rpc = new Rpc();
        try {
            Services.SystemService system = Services.SystemService.newStub(channel);
            rpc.setTimeout(15000);
            system.killMaster(rpc, Services.Empty.getDefaultInstance(), new Callback(rpc));
            rpc.await();
            Thread.sleep(200);
            if (rpc.isOk()) {
                logger.info("Master killed. Timing recovery.");
            }
        } catch (InterruptedException e) {
            logger.error("Benchmark failed.");
            e.printStackTrace();
        }
        
        timer.start();
        variable.update();
        updater.set(0);
    }
    
    private void finalizeBenchmark() {
        Variable<List<String>> participants =
                client.createVariableFactory().create(
                        ".participants0", Types.STRING_LIST);
        int numDevices = participants.get().size();
        RpcChannel channel = null;
        try {
            RpcCallback<Empty> done = new RpcCallback<Empty>() {
                @Override public void run(Empty response) {
                }
            };
            channel = RpcChannel.create(Common.HOSTNAME, Common.PORT);
            Experiment2 exp2 = Experiment2.newStub(channel);
            int warmupIterationsLeft = WARMUP_ITERATIONS;
            for (Long sample : timer.getTimes()) {
                if (warmupIterationsLeft-- > 0) {
                    continue;
                }
                SimpleTiming timing = SimpleTiming.newBuilder()
                        .setTiming(sample)
                        .setNumDevices(numDevices)
                        .build();
                Rpc rpc = new Rpc();
                rpc.setTimeout(5000);
                exp2.registerSample(rpc, timing, done);
                rpc.await();
                if (!rpc.isOk()) {
                    logger.warn("Could not register data: " + rpc.toString());
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (channel != null) {
                channel.close();
            }
        }
        Toast.makeText(this, "Finished benchmark", Toast.LENGTH_LONG).show();
    }
    
    private void stopIteration() {
        timer.stop();
        if (warmupIterationsPerformed < WARMUP_ITERATIONS) {
            warmupIterationsPerformed += 1;
            logger.info("Recovered. Finished warmup iteration " + warmupIterationsPerformed + "/" + WARMUP_ITERATIONS);
        } else {
            iterationsPerformed += 1;
            logger.info("Recovered. Finished iteration " + iterationsPerformed + "/" + ITERATIONS);
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted!");
            e.printStackTrace();
        }
    }
    
    @Override public void onCreate(Bundle savedBundle) {
        super.onCreate(savedBundle);
    }
    
    public void onResume() {
        super.onResume();
        try {
            channel = RpcChannel.create("localhost", SameService.PPORT);
        } catch (UnknownHostException e) {
            logger.error("Unable to create RPC channel.");
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("Unable to create RPC channel.");
            e.printStackTrace();
        }
        
//        java.util.logging.Level level = java.util.logging.Level.FINEST;
//        java.util.logging.Logger rpcLog = java.util.logging.Logger.getLogger(
//                com.orbekk.protobuf.RequestDispatcher.class.getName());
//        rpcLog.setLevel(level);
//        java.util.logging.Logger channelLog = java.util.logging.Logger.getLogger(
//                com.orbekk.protobuf.RpcChannel.class.getName());
//        channelLog.setLevel(level);
//        java.util.logging.Handler handler = new java.util.logging.ConsoleHandler();
//        handler.setLevel(level);
//        rpcLog.addHandler(handler);
//        channelLog.addHandler(handler);
        
        Toast.makeText(this, "Starting benchmark", Toast.LENGTH_LONG).show();

        timer = new Timer(WARMUP_ITERATIONS + ITERATIONS);
        warmupIterationsPerformed = 0;
        iterationsPerformed = 0;
        client = new ClientInterfaceBridge(this);
        client.connect();
        initializeVariable();
        startIteration();
    }
    
    public void initializeVariable() {
        variable = client.createVariableFactory()
                .create("BenchmarkVariable", Types.INTEGER);
        variable.addOnChangeListener(changeListener);
        updater = new VariableUpdaterTask(variable);
        updater.start();
    }
    
    public void onPause() {
        super.onPause();
        if (channel != null) {
            channel.close();
        }
        updater.interrupt();
        client.disconnect();
    }
    
}
