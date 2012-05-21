package com.orbekk.stats;

import java.io.IOException;
import java.net.UnknownHostException;

import com.google.protobuf.RpcCallback;
import com.orbekk.protobuf.Rpc;
import com.orbekk.protobuf.RpcChannel;
import com.orbekk.stats.Experiments.Empty;
import com.orbekk.stats.Experiments.Experiment1;
import com.orbekk.stats.Experiments.SimpleTiming;

public class TestClient {
    public static void main(String[] args) {
        RpcChannel channel = null;
        try {
            RpcCallback<Empty> done = new RpcCallback<Empty>() {
                @Override public void run(Empty unused) {
                }
            };
            channel = RpcChannel.create("localhost", Common.PORT);
            Experiment1 exp1 = Experiment1Impl.newStub(channel);
            Rpc rpc = new Rpc();
            rpc.setTimeout(5000);
            SimpleTiming timing = SimpleTiming.newBuilder()
                    .setTiming(1337.0)
                    .setNumDevices(0)
                    .build();
            exp1.registerSample(rpc, timing, done);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (channel != null) {
                channel.close();
            }
        }
    }
}
