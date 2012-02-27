package com.orbekk.same;

import com.orbekk.util.DelayedOperation;

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
    DelayedOperation set(T value);
    void update();
    void setOnChangeListener(OnChangeListener<T> listener);
}
