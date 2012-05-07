package com.orbekk.same;

import java.util.ArrayList;
import java.util.List;

public class ServicesPbConversion {
    public static List<Services.Component> componentsToPb(List<State.Component> components) {
        List<Services.Component> results = new ArrayList<Services.Component>();
        for (State.Component c : components) {
            results.add(Services.Component.newBuilder()
                            .setId(c.getName())
                            .setRevision(c.getRevision())
                            .setData(c.getData())
                            .build());
        }
        return results;
    }
}
