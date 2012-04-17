package com.orbekk.same;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jsonrpc4j.ProxyUtil;
import com.orbekk.net.MyJsonRpcHttpClient;
import com.orbekk.paxos.PaxosService;
import com.orbekk.protobuf.RpcChannel;

public class ConnectionManagerImpl implements ConnectionManager {
    private int connectionTimeout;
    private int readTimeout;
    private Map<String, MyJsonRpcHttpClient> connectionCache =
            new HashMap<String, MyJsonRpcHttpClient>();
    private ConcurrentMap<String, Future<RpcChannel>> channels =
            new ConcurrentHashMap<String, Future<RpcChannel>>();
    
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * @param connectionTimout Timeout for establishing connection in milliseconds.
     * @param readTimeout Timeout for waiting for answer in milliseconds.
     */
    public ConnectionManagerImpl(int connectionTimout, int readTimeout) {
        this.connectionTimeout = connectionTimout;
        this.readTimeout = readTimeout;
    }

    private MyJsonRpcHttpClient getConnection(String url)
            throws MalformedURLException {
        if (!connectionCache.containsKey(url)) {
            connectionCache.put(url, new MyJsonRpcHttpClient(new URL(url),
                    connectionTimeout, readTimeout));
        }
        return connectionCache.get(url);
    }

    private <T>T getClassProxy(String url, Class<T> clazz) {
        T service = null;
        try {
            MyJsonRpcHttpClient client = getConnection(url);
            service = ProxyUtil.createProxy(
                    this.getClass().getClassLoader(),
                    clazz,
                    client);
        } catch (MalformedURLException e) {
            logger.warn("Unable to create client for {}, {}", url, e);
        }
        return service;
    }
    
    private RpcChannel getChannel(String location) {
        Future<RpcChannel> channel = channels.get(location);
        if (channel == null) {
            final String hostname = location.split(":")[0];
            final int port = Integer.valueOf(location.split(":")[1]);
            Callable<RpcChannel> channelFactory =
                    new Callable<RpcChannel>() {
                @Override public RpcChannel call() {
                    try {
                        return RpcChannel.create(hostname, port);
                    } catch (UnknownHostException e) {
                        logger.error("Could not connect: ", e);
                        return null;
                    } catch (IOException e) {
                        logger.error("Could not connect: ", e);
                        return null;
                    }
                }
            };
            FutureTask<RpcChannel> task =
                    new FutureTask<RpcChannel>(channelFactory);
            channel = channels.putIfAbsent(location, task);
            if (channel == null) {
                task.run();
                channel = task;
            }
        }
        try {
            return channel.get();
        } catch (ExecutionException e) {
            logger.error("Could not connect: ", e);
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public ClientService getClient(String url) {
        return getClassProxy(url, ClientService.class);
    }

    @Override
    public MasterService getMaster(String url) {
        return getClassProxy(url, MasterService.class);
    }

    @Override
    public PaxosService getPaxos(String url) {
        return getClassProxy(url, PaxosService.class);
    }
    
    @Override
    public Services.Master getMaster0(String location) {
        RpcChannel channel = getChannel(location);
        if (channel != null) {
            return Services.Master.newStub(channel);
        } else {
            return null;
        }
    }
    
    @Override
    public Services.Client getClient0(String location) {
        RpcChannel channel = getChannel(location);
        if (channel != null) {
            return Services.Client.newStub(channel);
        } else {
            return null;
        }
    }
    
    @Override
    public Services.Directory getDirectory(String location) {
        RpcChannel channel = getChannel(location);
        if (channel != null) {
            return Services.Directory.newStub(channel);
        } else {
            return null;
        }
    }
}
