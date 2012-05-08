package com.orbekk.same;

import java.util.ArrayList;
import java.util.List;

public class ServicesPbConversion {
    public static List<Services.Component> componentsToPb(List<State.Component> components) {
        List<Services.Component> results = new ArrayList<Services.Component>();
        for (State.Component c : components) {
            results.add(componentToPb(c));
        }
        return results;
    }
    
    public static Services.Component componentToPb(State.Component component) {
        return Services.Component.newBuilder()
                .setId(component.getName())
                .setRevision(component.getRevision())
                .setData(component.getData())
                .build();
    }
}
