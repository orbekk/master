package com.orbekk.same;

import com.orbekk.util.DelayedOperation;

public interface ClientInterface {
    State getState();
    DelayedOperation set(State.Component component);
    void addStateListener(StateChangedListener listener);
    void removeStateListener(StateChangedListener listener);
}