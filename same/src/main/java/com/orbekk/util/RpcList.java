package com.orbekk.util;

import java.util.ArrayList;
import java.util.List;

import com.orbekk.protobuf.Rpc;

public class RpcList {
    private List<Rpc> rpcs = new ArrayList<Rpc>();
    
    public synchronized void add(Rpc rpc) {
        rpcs.add(rpc);
    }
    
    public synchronized void awaitAll() throws InterruptedException {
        for (Rpc rpc : rpcs) {
            rpc.await();
        }
        rpcs.clear();
    }
}
