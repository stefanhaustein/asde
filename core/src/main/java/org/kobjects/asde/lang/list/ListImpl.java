package org.kobjects.asde.lang.list;

import org.kobjects.asde.lang.type.Type;
import org.kobjects.asde.lang.type.Typed;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ListImpl implements Typed, Iterable<Object> {

    private final ListType type;
    private final ArrayList<Object> data;
    private ArrayList<Runnable> changeListeners;

    public ListImpl(Type elementType, Object... data) {
        type = new ListType(elementType);
        this.data = new ArrayList<>(data.length);
        for (Object value : data) {
            this.data.add(value);
        }
    }


    public synchronized Object get(int index) {
        return data.get(index);
    }


    public synchronized void clear() {
        data.clear();
        notifyChanged();
    }

    public synchronized void remove(int index) {
        data.remove(index);
        notifyChanged();
    }

    public synchronized void remove(Object object) {
        data.remove(object);
        notifyChanged();
    }

    public synchronized void append(Object object) {
        data.add(object);
        notifyChanged();
    }

    private synchronized void notifyChanged() {
        if (changeListeners != null) {
            for (Runnable listener : changeListeners) {
                listener.run();
            }
        }
    }


    synchronized public void setValueAt(Object value, int... indices) {
        ListImpl target = this;
        for (int i = 0; i < indices.length - 1; i++) {
            target = (ListImpl) target.data.get(indices[i]);
        }
        target.data.set(indices[indices.length - 1], value);
    }

    @Override
    synchronized public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < data.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(data.get(i));
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    synchronized public boolean equals(Object o) {
        if (!(o instanceof ListImpl)) {
            return false;
        }
        ListImpl other = (ListImpl) o;
        return data.equals(other.data);
    }


    public synchronized int length() {
        return data.size();
    }

    public synchronized boolean contains(Object object) {
        return data.contains(object);
    }

    public synchronized  ArrayList<Object> defensiveCopy() {
        ArrayList result = new ArrayList();
        result.addAll(data);
        return result;
    }

    public Iterator<Object> iterator() {
        return data.iterator();
    }

    @Override
    public ListType getType() {
        return type;
    }

    public synchronized void addChangeListener(Runnable changeListener) {
        if (changeListeners == null) {
            changeListeners = new ArrayList<>();
        }
        changeListeners.add(changeListener);
    }
}
