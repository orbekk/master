package com.orbekk.same;

import java.util.List;

import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Same implements SameInterface {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ClientServiceImpl client;
    
    public Same(ClientServiceImpl client) {
        this.client = client;
    }
    
    @Override
    public String get(String id) {
        return client.lib_get(id);
    }

    @Override
    public <T> T get(String id, TypeReference<T> type) {
        return client.lib_get(id, type);
    }

    @Override
    public List<String> getList(String id) {
        return client.lib_get(id, new TypeReference<List<String>>() { });
    }

    @Override
    public void set(String id, String data) throws UpdateConflict {
        client.lib_set(id, data);
    }

    @Override
    public void setObject(String id, Object data) {
        throw new RuntimeException("Not implemented.");   
    }

}
