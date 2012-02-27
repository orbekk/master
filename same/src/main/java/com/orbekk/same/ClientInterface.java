package com.orbekk.same;

public interface ClientInterface {
    State getState();
    void set(State.Component component) throws UpdateConflict;
    void addStateListener(StateChangedListener listener);
    void removeStateListener(StateChangedListener listener);
}