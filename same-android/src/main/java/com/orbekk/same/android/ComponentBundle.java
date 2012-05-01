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
