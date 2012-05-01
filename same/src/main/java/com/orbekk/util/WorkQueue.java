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
package com.orbekk.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A WorkList is a list for pending units of work. 
 */
abstract public class WorkQueue<E> extends Thread implements List<E> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private volatile List<E> list = null;
    private volatile boolean done = false;

    public WorkQueue() {
        list = new ArrayList<E>();
    }

    public WorkQueue(Collection<? extends E> collection) {
        list = new ArrayList<E>(collection);
    }

    public synchronized List<E> getAndClear() {
        List<E> copy = new ArrayList<E>(list);
        list.clear();
        return copy;
    }

    /**
     * OnChange event.
     * 
     * May be run even if the WorkQueue has not been changed.
     * Called until the queue is empty.
     */
    protected abstract void onChange();

    /**
     * Perform work until the queue is empty.
     *
     * Can be used for testing or for combining several WorkQueues in
     * a single thread.
     */
    public synchronized void performWork() {
        while (!isEmpty()) {
            onChange();
        }
    }

    @Override
    public void run() {
        while (!done) {
            if (!isEmpty()) {
                onChange();
            }
            synchronized (this) {
                try {
                    if (isEmpty()) {
                        wait();
                    }
                } catch (InterruptedException e) {
                    done = true;
                }
                if (Thread.interrupted()) {
                    done = true;
                }
            }
        }
    }


    @Override
    public synchronized boolean add(E e) {
        notifyAll();
        return list.add(e);
    }

    @Override
    public synchronized void add(int index, E element) {
        notifyAll();
        list.add(index, element);
    }

    @Override
    public synchronized boolean addAll(Collection<? extends E> c) {
        notifyAll();
        return list.addAll(c);
    }

    @Override
    public synchronized boolean addAll(int index, Collection<? extends E> c) {
        notifyAll();
        return list.addAll(index, c);
    }

    @Override
    public synchronized void clear() {
        notifyAll();
        list.clear();
    }

    @Override
    public synchronized boolean contains(Object o) {
        notifyAll();
        return list.contains(o);
    }

    @Override
    public synchronized boolean containsAll(Collection<?> c) {
        notifyAll();
        return containsAll(c);
    }

    @Override
    public synchronized E get(int index) {
        notifyAll();
        return list.get(index);
    }

    @Override
    public synchronized int indexOf(Object o) {
        notifyAll();
        return list.indexOf(o);
    }

    @Override
    public synchronized boolean isEmpty() {
        notifyAll();
        return list.isEmpty();
    }

    @Override
    public synchronized Iterator<E> iterator() {
        notifyAll();
        return list.iterator();
    }

    @Override
    public synchronized int lastIndexOf(Object o) {
        notifyAll();
        return list.lastIndexOf(o);
    }

    @Override
    public synchronized ListIterator<E> listIterator() {
        notifyAll();
        return list.listIterator();
    }

    @Override
    public synchronized ListIterator<E> listIterator(int index) {
        notifyAll();
        return list.listIterator(index);
    }

    @Override
    public synchronized boolean remove(Object o) {
        notifyAll();
        return list.remove(o);
    }

    @Override
    public synchronized E remove(int index) {
        notifyAll();
        return list.remove(index);
    }

    @Override
    public synchronized boolean removeAll(Collection<?> c) {
        notifyAll();
        return list.removeAll(c);
    }

    @Override
    public synchronized boolean retainAll(Collection<?> c) {
        notifyAll();
        return list.retainAll(c);
    }

    @Override
    public synchronized E set(int index, E element) {
        notifyAll();
        return list.set(index, element);
    }

    @Override
    public synchronized int size() {
        notifyAll();
        return list.size();
    }

    @Override
    public synchronized List<E> subList(int fromIndex, int toIndex) {
        notifyAll();
        return list.subList(fromIndex, toIndex);
    }

    @Override
    public synchronized Object[] toArray() {
        notifyAll();
        return list.toArray();
    }

    @Override
    public synchronized <T> T[] toArray(T[] a) {
        notifyAll();
        return list.toArray(a);
    }

}
