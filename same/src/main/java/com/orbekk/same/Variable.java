package com.orbekk.same;

public interface Variable<T> {
    public interface OnChangeListener<T> {
        /**
         * A notification that 'variable' has been changed.
         * 
         * The user must run variable.update() to get the updated value.
         */
        void valueChanged(Variable<T> variable);
    }
    
    T get();
    void set(T value) throws UpdateConflict;
    void update();
    void waitForChange();
    boolean waitingForUpdate();
    void setOnChangeListener(OnChangeListener<T> listener);
}
