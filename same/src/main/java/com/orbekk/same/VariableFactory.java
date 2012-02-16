package com.orbekk.same;

import org.codehaus.jackson.type.TypeReference;

/**
 * TODO: Use WeakReference in order to make variables GC-able.
 */
public class VariableFactory {
    Client.ClientInterface client;
    
    private class VariableImpl<T> implements Variable<T> {
        String identifier;
        TypeReference<T> type;
        T value;
    
        public VariableImpl(String identifier, TypeReference<T> type) {
            this.identifier = identifier;
            this.type = type;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public void set(T value) {
        }

        @Override
        public void update() {
            value = client.getState().getParsedData(identifier, type);
        }

        @Override
        public void setOnChangeListener(Variable.OnChangeListener<T> listener) {
            throw new RuntimeException("Not implemented.");
        }
    }
    
    public static VariableFactory create(Client.ClientInterface client) {
        return new VariableFactory(client);
    }
    
    VariableFactory(Client.ClientInterface client) {
        this.client = client;
    }
    
    public <T> Variable<T> create(String identifier, TypeReference<T> type) {
        Variable<T> variable = new VariableImpl<T>(identifier, type);
        variable.update();
        return variable;
    }
}
