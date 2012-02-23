package com.orbekk.same;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: Use WeakReference in order to make variables GC-able.
 */
public class VariableFactory {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Client.ClientInterface client;
    private ObjectMapper mapper = new ObjectMapper();
    
    private class VariableImpl<T> implements Variable<T> {
        String identifier;
        TypeReference<T> type;
        T value;
        long revision = 0;
    
        public VariableImpl(String identifier, TypeReference<T> type) {
            this.identifier = identifier;
            this.type = type;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public void set(T value) throws UpdateConflict {
            try {
                String serializedValue = mapper.writeValueAsString(value);
                client.set(identifier, serializedValue, revision);
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
    
    public Variable<String> createString(String identifier) {
        return create(identifier, new TypeReference<String>() {});
    }
}
