package com.orbekk.net;

public class DefaultBroadcasterFactory implements BroadcasterFactory {
    @Override
    public BroadcasterInterface create() {
        return new Broadcaster();
    }
}
