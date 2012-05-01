/**
 * Copyright 2012 Kjetil Ørbekk <kjetil.orbekk@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orbekk.same;

import java.io.IOException;
import java.util.ArrayList;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.same.State.Component;
import com.orbekk.util.DelayedOperation;

/**
 * TODO: Use WeakReference in order to make variables GC-able.
 */
public class VariableFactory {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ClientInterface client;
    private ObjectMapper mapper = new ObjectMapper();

    private class VariableImpl<T> implements Variable<T>, StateChangedListener {
        String identifier;
        TypeReference<T> type;
        T value;
        long revision = 0;
        ArrayList<OnChangeListener<T>> listeners =
                new ArrayList<OnChangeListener<T>>();

        public VariableImpl(String identifier, TypeReference<T> type) {
            this.identifier = identifier;
            this.type = type;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public DelayedOperation set(T value) {
            try {
                String serializedValue = mapper.writeValueAsString(value);
                State.Component update = new State.Component(identifier,
                        revision, serializedValue);
                return client.set(update);
            } catch (JsonGenerationException e) {
                logger.warn("Failed to convert to JSON: {}", value);
                logger.warn("Parse exception.", e);
                throw new RuntimeException(e);
            } catch (JsonMappingException e) {
                logger.warn("Failed to convert to JSON: {}", value);
                logger.warn("Parse exception.", e);
                throw new RuntimeException(e);
            } catch (IOException e) {
                logger.warn("Failed to cornvert to JSON: {}", value);
                logger.warn("Parse exception.", e);
                throw new RuntimeException(e);
            }
        }

        @Override
        public void update() {
            value = client.getState().getParsedData(identifier, type);
            revision = client.getState().getRevision(identifier);
        }

        @Override
        public synchronized void addOnChangeListener(OnChangeListener<T> listener) {
            listeners.add(listener);
        }
        
        @Override
        public synchronized void removeOnChangeListener(OnChangeListener<T> listener) {
            listeners.remove(listener);
        }

        @Override
        public synchronized void stateChanged(Component component) {
            if (component.getName().equals(identifier)) {
                for (OnChangeListener<T> listener : listeners) {
                    listener.valueChanged(this);
                }
            }
        }
    }

    public static VariableFactory create(ClientInterface client) {
        return new VariableFactory(client);
    }

    VariableFactory(ClientInterface client) {
        this.client = client;
    }

    public <T> Variable<T> create(String identifier, TypeReference<T> type) {
        VariableImpl<T> variable = new VariableImpl<T>(identifier, type);
        variable.update();
        client.addStateListener(variable);
        return variable;
    }

    public Variable<String> createString(String identifier) {
        return create(identifier, new TypeReference<String>() {});
    }
}
