package com.orbekk.same;

import com.googlecode.jsonrpc4j.ProxyUtil;
import com.orbekk.net.MyJsonRpcHttpClient;
import com.orbekk.paxos.PaxosService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionManagerImpl implements ConnectionManager {
    private int connectionTimeout;
    private int readTimeout;
    private Map<String, MyJsonRpcHttpClient> connectionCache =
            new HashMap<String, MyJsonRpcHttpClient>();

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
}
