package gtanks.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class FastHashMap<K, V> implements Iterable<V>, Map<K, V> {
    private final ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();

    @Override
    public V put(K key, V value) {
        if (key != null && value != null) {
            return this.map.put(key, value);
        }
        return null;
    }

    @Override
    public V remove(Object key) {
        if (key != null) {
            return this.map.remove(key);
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {

    }

    @Override
    public boolean remove(Object key, Object value) {
        if (key != null && value != null) {
            return this.map.remove(key, value);
        }
        return false;
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public Set<K> keySet() {
        return null;
    }

    @Override
    public V get(Object key) {
        V value = null;
        if (key != null && this.map.containsKey(key)) {
            value = this.map.get(key);
        }

        return value;
    }

    @Override
    public boolean containsKey(Object key) {
        return key != null && this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public Collection<V> values() {
        return this.map.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return null;
    }

    public Set<Map.Entry<K, V>> entries() {
        return this.map.entrySet();
    }

    @Override
    public Iterator<V> iterator() {
        return this.map.values().iterator();
    }
}
