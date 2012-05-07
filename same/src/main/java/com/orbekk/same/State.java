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
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is thread-safe.
 */
public class State {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Map<String, Component> state = new HashMap<String, Component>(); 
    private ObjectMapper mapper = new ObjectMapper();

    public static final String PARTICIPANTS = ".participants0";
    
    public State() {
        updateFromObject(PARTICIPANTS, new ArrayList<String>(), 1);
    }

    public State(State other) {
        for (Component c : other.getComponents()) {
            state.put(c.getName(), c);
        }
    }

    public synchronized void clear() {
        state.clear();
    }

    public synchronized boolean checkRevision(String componentName,
            long expectedRevision) {
        Component component = state.get(componentName);
        if (component == null) {
            return true;
        } else if (component.getRevision() == expectedRevision) {
            return true;
        } else {
            return false;
        }
    }
    
    public synchronized void forceUpdate(String componentName,
            String data, long revision) {
        Component oldComponent = state.get(componentName);
        Component newComponent = new Component(componentName, revision, data);
        state.put(componentName, newComponent);
    }

    public synchronized boolean update(String componentName, String data,
            long revision) {
        Component component = null;
        if (!state.containsKey(componentName)) {
            component = new Component("", 0, "");
        } else {
            component = state.get(componentName);       
        }

        if (revision > component.getRevision()) {
            Component newComponent = new Component(componentName, revision, data);
            state.put(componentName, newComponent);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get a copy of a component.
     */
    public Component getComponent(String name) {
        Component component = state.get(name);
        if (component != null) {
            return new Component(component);
        } else {
            return null;
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

    /**
     * Pretty print a component.
     */
    public String show(String componentName) {
        return componentName + ": " + state.get(componentName);
    }

    /**
     * Returns a list of all the components in this State.
     * 
     * This method is thread-safe, and returns a deep copy.
     */
    public synchronized List<Component> getComponents() {
        ArrayList<Component> list = new ArrayList<Component>();
        for (Component component : state.values()) {
            list.add(new Component(component));
        }
        return list;
    }

    public static class Component {
        private String name;
        private long revision;
        private String data;

        /**
         * Copy constructor.
         */
        public Component(Component other) {
            this.name = other.name;
            this.revision = other.revision;
            this.data = other.data;
        }

        public Component(String name, long revision, String data) {
            this.name = name;
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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override public String toString() {
            return "[" + this.name + ": " + this.data + "@" + revision + "]";
        }

        @Override public boolean equals(Object other) {
            if (!(other instanceof Component)) {
                return false;
            }
            Component o = (Component)other;
            return name.equals(o.name) && data.equals(o.data) &&
                    revision == o.revision;
        }
    }

    @Override public String toString() {
        StringBuilder output = new StringBuilder();
        output.append("State(\n");
        for (Component c : getComponents()) {
            output.append("    " + c.toString() + "\n");
        }
        output.append(")");
        return output.toString();
    }

    @Override public boolean equals(Object other) {
        if (!(other instanceof State)) {
            return false;
        }
        State o = (State)other;
        return state.equals(o.state);
    }
}
