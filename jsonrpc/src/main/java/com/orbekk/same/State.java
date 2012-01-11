package com.orbekk.same;

import java.util.Map;
import java.util.HashMap;

public class State {
    private long stateIteration = 0;
    private Map<String, String> participants = new HashMap<String, String>();
    private String networkName = "";
    private String masterId = "";
    private String data = "";

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
