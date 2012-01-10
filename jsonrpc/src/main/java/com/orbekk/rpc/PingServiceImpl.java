package com.orbekk.rpc;

public class PingServiceImpl implements PingService {
    @Override
    public String ping() {
        return "Pong";
    }
}
