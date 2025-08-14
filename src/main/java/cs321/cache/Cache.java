package cs321.cache;

import java.util.LinkedList;

/**
 * A simple and fixed size cache that always keeps the most recently used item at the front
 *
 * @param <K> the type of key used to find items
 * @param <V> the type of item stored which it must
 *
 * @author Ahmad Rao
 */
public class Cache<K, V extends KeyInterface<K>>
				    implements CacheInterface<K, V>{

    private int maxSize;
    private LinkedList<V> cache;

    private int numHits = 0;
    private int numReferences = 0;


    public Cache(int maxSize) {
        if (maxSize < 1) {
            throw new IllegalArgumentException("maxSize must be greater than 0");
        }
        this.maxSize = maxSize;
        this.cache = new LinkedList<>();
    }

    @Override
    public V get(K key) {
        numReferences++;
        for(int i = 0; i < cache.size(); i++) {
            V current = cache.get(i);
            if(current.getKey().equals(key)) {
                numHits++;
                cache.remove(i);
                cache.addFirst(current);
                return current;

            }
        }
        return null;

    }

    @Override
    public V add(V value) {
        remove(value.getKey());
        V removed = null;
        if(cache.size() == maxSize) {
            removed = cache.removeLast();
        }
        cache.addFirst(value);
        return removed;
    }

    @Override
    public V remove(K key) {
        for(int i = 0; i < cache.size(); i++) {
            V current = cache.get(i);
            if(current.getKey().equals(key)) {
                cache.remove(i);
                return current;
            }
        }
        return null;
    }

    @Override
    public void clear() {
        cache.clear();
        numHits = 0;
        numReferences = 0;

    }

    public String toString() {
        double hitPercent;
        if (numReferences == 0) {
            hitPercent = 0.0;
        } else {
            hitPercent = (numHits * 100.0) / numReferences;
        }
        return  "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                "Cache with " + maxSize + " entries has been created\n" +
                "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
                String.format("%-30s %10d%n", "Total number of references:", numReferences) +
                String.format("%-30s %10d%n", "Total number of cache hits:", numHits) +
                String.format("%-30s %10.2f%%%n", "Cache hit percent:", hitPercent);
    }
}