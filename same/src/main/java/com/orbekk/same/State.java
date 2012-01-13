package com.orbekk.same;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class State {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Map<String, Component> state = new HashMap<String, Component>(); 
    private ObjectMapper mapper = new ObjectMapper();

    public State(String networkName) {
        update(".networkName", networkName, 0);
        updateFromObject(".participants", new ArrayList<String>(), 0);
    }
    
    public boolean update(String componentName, String data, long revision) {
        Component component = null;
        if (!state.containsKey(componentName)) {
            component = new Component(0, "");
        } else {
            component = state.get(componentName);           
        }
        
        if (revision == component.getRevision()) {
            component.setRevision(revision + 1);
            component.setData(data);
            state.put(componentName, component);
            return true;
        } else {
            return false;
        }
    }
      
    public String getDataOf(String componentName) {
        Component component = state.get(componentName);
        if (component != null) {
            return component.getData();
        } else {
            return null;
        }
    }
    
    public long getRevision(String componentName) {
        Component component = state.get(componentName);
        if (component != null) {
            return component.getRevision();
        } else {
            logger.warn("getRevision: Unknown component {}. Returning 0",
                    componentName);
            return 0;
        }
    }
    
    /**
     * Parses a JSON value using Jackson ObjectMapper.
     */
    public <T> T getParsedData(String componentName, TypeReference<T> type) {
        String data = getDataOf(componentName);
        if (data != null) {
            try {
                return mapper.readValue(data, type);
            } catch (JsonParseException e) {
                logger.warn("Failed to parse value {} ", data);
                logger.warn("Parse exception: {}", e);
            } catch (JsonMappingException e) {
                logger.warn("Failed to parse value {} ", data);
                logger.warn("Parse exception: {}", e);

            } catch (IOException e) {
                logger.warn("Failed to parse value {} ", data);
                logger.warn("Parse exception: {}", e);
            }
        }
        return null;
    }
    
    public List<String> getList(String componentName) {
        return getParsedData(componentName,
                new TypeReference<List<String>>(){});
    }
    
    public boolean updateFromObject(String componentName, Object data, long revision) {
        String dataS;
        try {
            dataS = mapper.writeValueAsString(data);
            return update(componentName, dataS, revision);
        } catch (JsonGenerationException e) {
            logger.warn("Failed to convert to JSON: {} ", data);
            logger.warn("Parse exception: {}", e);
            return false;
        } catch (JsonMappingException e) {
            logger.warn("Failed to convert to JSON: {} ", data);
            logger.warn("Parse exception: {}", e);
            return false;
        } catch (IOException e) {
            logger.warn("Failed to convert to JSON: {} ", data);
            logger.warn("Parse exception: {}", e);
            return false;
        }
    }
    
    public static class Component {
        private long revision;
        private String data;
        
        public Component(long revision, String data) {
            this.revision = revision;
            this.data = data;
        }
        
        public long getRevision() {
            return revision;
        }
        public void setRevision(long revision) {
            this.revision = revision;
        }
        
        public String getData() {
            return data;
        }
        public void setData(String data) {
            this.data = data;
        }       
        
        @Override public String toString() {
            return this.data + " @" + revision;
        }
    }
//    
//    @Override
//    public String toString() {
//        StringBuilder participantsString = new StringBuilder();
//        participantsString.append("[");
//        boolean first = true;
//        for (Map.Entry<String, String> e : participants.entrySet()) {
//            if (!first) {
//                participantsString.append(", ");
//            }
//            first = false;
//            participantsString.append(e.getKey())
//                    .append("(")
//                    .append(e.getValue())
//                    .append(")");
//            String clientId = e.getKey();
//            String url = e.getValue();
//        }
//        participantsString.append("]");
//
//        return String.format(
//            "State( \n" +
//            "      stateIteration = %d,\n" +
//            "      networkName    = %s,\n" +
//            "      masterId       = %s,\n" +
//            "      data           = %s,\n" +
//            "      participants   = %s\n" +
//            ")", stateIteration, networkName, masterId, data,
//            participantsString);
//    }
}
