package com.orbekk.same;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import java.net.MalformedURLException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionManagerImpl implements ConnectionManager {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public ConnectionManagerImpl() {
    }

    private <T>T getClassProxy(String url, Class<T> clazz) {
        T service = null;
        try {
            JsonRpcHttpClient client = new JsonRpcHttpClient(new URL(url));
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
}
