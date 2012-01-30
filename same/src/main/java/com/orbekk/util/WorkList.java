package com.orbekk.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WorkList<E> extends ArrayList<E> {
    public WorkList() {
        super();
    }
    
    public WorkList(Collection<? extends E> collection) {
        super(collection);
    }
    
    public synchronized List<E> copyAndClear() {
        List<E> copy = new WorkList<E>(this);
        clear();
        return copy;
    }
}
