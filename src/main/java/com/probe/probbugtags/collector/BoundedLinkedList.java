package com.probe.probbugtags.collector;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.LinkedList;

/**
 * A {@link LinkedList} version with a maximum number of elements. When adding
 * elements to the end of the list, first elements in the list are discarded if
 * the maximum size is reached.
 * <p/>
 * Created by chengqianqian-xy on 2016/8/18.
 */
public class BoundedLinkedList<E> extends LinkedList<E> {

    private int maxSize;

    public BoundedLinkedList(int maxSize) {
        this.maxSize = maxSize;
    }


    @Override
    public boolean add(E object) {
        if (size() == maxSize) {
            removeFirst();
        }

        return super.add(object);
    }

    @Override
    public void add(int location, E object) {
        if (size() == maxSize) {
            removeFirst();
        }

        super.add(location, object);
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends E> collection) {
        int size = collection.size();
        if (size > maxSize) {
            LinkedList<? extends E> list = new LinkedList<>(collection);
            for (int i = 0; i < size - maxSize; i++) {
                list.removeFirst();
            }
            collection = list;
        }

        int totalNeedSize = size + collection.size();
        int overhead = totalNeedSize - maxSize;
        if (overhead > 0) {
            removeRange(0, overhead);
        }

        return super.addAll(collection);
    }


    @Override
    public boolean addAll(int location, Collection<? extends E> collection) {
        if (location == size()) {
            return super.addAll(location, collection);
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public void addFirst(E object) {
        throw new UnsupportedOperationException();
    }


    @Override
    public void addLast(E object) {
        add(object);
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (E object : this) {
            if (!first) {
                result.append("," + object.toString());
            } else {
                result.append(object.toString());
                first = false;
            }
        }
        return result.toString();
    }

    @Override
    public boolean offer(E o) {
        return add(o);
    }

    @Override
    public boolean offerFirst(E e) {
        addFirst(e);
        return true;
    }

    @Override
    public boolean offerLast(E e) {
        return add(e);
    }

    @Override
    public void push(E e) {
        add(e);
    }
}

