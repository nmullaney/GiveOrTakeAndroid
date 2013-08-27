package com.bitdance.giveortake;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This is a Collections class that keeps the Identifiable objects in a map
 * for easy lookup, but also keeps the ids in an ArrayList, for ordering.
 */
public class OrderedMap<T extends Identifiable> implements Collection<T> {
    public static final String TAG = "OrderedMap";

    private List<Long> ids;
    private Map<Long, T> map;

    public OrderedMap () {
        ids = new ArrayList<Long>();
        map = new HashMap<Long, T>();
    }

    public T get(int index) {
        Long id = ids.get(index);
        return map.get(id);
    }

    public T get(Long id) {
        if (map.containsKey(id))
            return map.get(id);
        return null;
    }

    public int indexOf(Long id) {
        return ids.indexOf(id);
    }

    public ArrayList<T> getAll() {
        ArrayList<T> all = new ArrayList<T>();
        for (Long id : ids) {
            all.add(map.get(id));
        }
        return all;
    }

    public void add(int index, T element) {
        ids.add(index, element.getId());
        map.put(element.getId(), element);
    }

    @Override
    public boolean add(T identifiable) {
        map.put(identifiable.getId(), identifiable);
        return ids.add(identifiable.getId());
    }

    @Override
    public boolean addAll(Collection<? extends T> ts) {
        for (T identifiable : ts) {
            map.put(identifiable.getId(), identifiable);
            ids.add(identifiable.getId());
        }
        return true;
    }

    @Override
    public void clear() {
        map.clear();
        ids.clear();
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof Identifiable)) {
            return false;
        }
        Identifiable identifiable = (Identifiable)o;
        return map.containsKey(identifiable.getId());
    }

    @Override
    public boolean containsAll(Collection<?> objects) {
        for (Object object : objects) {
            if (!contains(object)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isEmpty() {
        return ids.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return new OrderedMapIterator();
    }

    @Override
    public boolean remove(Object o) {
        if (!(o instanceof Identifiable)) {
            Log.i(TAG, "Cannot remove unidentifiable");
            return false;
        }
        Identifiable identifiable = (Identifiable)o;
        boolean changed = ids.remove(identifiable.getId());
        if (changed) {
            Log.i(TAG, "Successfully removed " + identifiable.getId());
        } else {
            Log.i(TAG, "Failed to remove " + identifiable.getId());
        }
        if (map.containsKey(identifiable.getId()) && !ids.contains(identifiable.getId())) {
            map.remove(identifiable.getId());
            changed = true;
        }
        return changed;
    }

    @Override
    public boolean removeAll(Collection<?> objects) {
        Log.i(TAG, "Attempting to remove all " + objects.size() + " objects");
        boolean changed = false;
        for (Iterator<?> objIterator = objects.iterator(); objIterator.hasNext(); ) {
            boolean success = remove(objIterator.next());
            changed = changed || success;
        }
        return changed;
    }

    @Override
    public boolean retainAll(Collection<?> objects) {
        boolean changed = false;
        for (Iterator<?> iterator = objects.iterator(); iterator().hasNext(); ) {
            Object next = iterator.next();
            if (!contains(next)) {
                boolean success = remove(next);
                changed = changed || success;
            }
        }
        return changed;
    }

    @Override
    public int size() {
        return ids.size();
    }

    @Override
    public Object[] toArray() {
        Object[] objects = new Object[size()];
        for (int i = 0; i < size(); i++) {
            Long id = ids.get(i);
            Object object = map.get(id);
            objects[i] = object;
        }
        return objects;
    }

    @Override
    public <T1> T1[] toArray(T1[] t1s) {
        ArrayList<T1> values = new ArrayList<T1>(size());
        for (int i = 0; i < size(); i++) {
            Long id = ids.get(i);
            T object = map.get(id);
            values.add((T1) object);
        }
        return values.toArray(t1s);
    }

    private class OrderedMapIterator implements Iterator<T> {

        Iterator<Long> listIterator;
        Long currentId = null;

        public OrderedMapIterator () {
           listIterator = ids.iterator();
        }

        @Override
        public boolean hasNext() {
            return listIterator.hasNext();
        }

        @Override
        public T next() {
            Long nextId = listIterator.next();
            currentId = Long.valueOf(nextId);
            return map.get(nextId);
        }

        @Override
        public void remove() {
            if (map.containsKey(currentId)) {
                map.remove(currentId);
            }
            currentId = null;
            listIterator.remove();
        }
    }
}
