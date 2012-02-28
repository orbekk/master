package com.orbekk.same.android;

import android.os.Bundle;

import com.orbekk.same.State;

public class ComponentBundle {
    private State.Component component;
    private Bundle bundle;
    
    public ComponentBundle(State.Component component) {
        this.component = component;
    }
    
    public ComponentBundle(Bundle bundle) {
        this.bundle = bundle;
    }
    
    private void makeBundle() {
        if (bundle == null) {
            bundle = new Bundle();
            bundle.putString("identifier", component.getName());
            bundle.putString("data", component.getData());
            bundle.putLong("revision", component.getRevision());
        }
    }
    
    private void makeComponent() {
        if (component == null) {
            String name = bundle.getString("identifier");
            String data = bundle.getString("data");
            long revision = bundle.getLong("revision");
            component = new State.Component(name, revision, data);
        }
    }
    
    public Bundle getBundle() {
        makeBundle();
        return bundle;
    }
    
    public State.Component getComponent() {
        makeComponent();
        return component;
    }
}
