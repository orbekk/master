package com.orbekk.same;

import java.util.Map;
import java.util.HashMap;

public class State {
    private Map<String, Component> state = new HashMap<String, Component>(); 
    
    private long stateIteration = 0;
    private Map<String, String> participants = new HashMap<String, String>();
    private String networkName = "";
    private String masterId = "";
    private String data = "";

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
    
    public long getStateIteration() {
        return stateIteration;
    }

    public void setStateIteration(long stateIteration) {
        this.stateIteration = stateIteration;
    }

    public Map<String, String> getParticipants() {
        return participants;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        StringBuilder participantsString = new StringBuilder();
        participantsString.append("[");
        boolean first = true;
        for (Map.Entry<String, String> e : participants.entrySet()) {
            if (!first) {
                participantsString.append(", ");
            }
            first = false;
            participantsString.append(e.getKey())
                    .append("(")
                    .append(e.getValue())
                    .append(")");
            String clientId = e.getKey();
            String url = e.getValue();
        }
        participantsString.append("]");

        return String.format(
            "State( \n" +
            "      stateIteration = %d,\n" +
            "      networkName    = %s,\n" +
            "      masterId       = %s,\n" +
            "      data           = %s,\n" +
            "      participants   = %s\n" +
            ")", stateIteration, networkName, masterId, data,
            participantsString);
    }
}
