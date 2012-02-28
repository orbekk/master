package com.orbekk.same.http;

public interface ServerContainer {
    public abstract int getPort();
    public abstract void start() throws Exception;
    public abstract void stop() throws Exception;
    public abstract void join();
}