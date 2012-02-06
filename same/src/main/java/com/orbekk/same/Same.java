package com.orbekk.same;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class Same implements SameInterface {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Client client;
    private StateChangedProxy stateChangedProxy = new StateChangedProxy();
    
    private class StateChangedProxy implements StateChangedListener {
        public List<StateChangedListener> listeners =
                new ArrayList<StateChangedListener>();

        @Override
        public void stateChanged(String id, String data) {
            for (StateChangedListener listener : listeners) {
                listener.stateChanged(id, data);
            }
        }
    }
    
    public static Same createSame(Client client) {
        Same same = new Same(client);
        client.setStateChangedListener(same.stateChangedProxy);
        return same;
    }
    
    Same(Client client) {
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

    @Override
    public void addStateChangedListener(StateChangedListener listener) {
        stateChangedProxy.listeners.add(listener);
    }

    @Override
    public void removeStateChangedListener(StateChangedListener listener) {
        stateChangedProxy.listeners.remove(listener);
    }

}
