package com.orbekk.same;

import com.orbekk.protobuf.Rpc;

public class RpcFactory {
    private final long timeoutMillis;
    
    public RpcFactory(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }
    
    public Rpc create() {
        Rpc rpc = new Rpc();
        rpc.setTimeout(timeoutMillis);
        return rpc;
    }
}
