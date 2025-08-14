package cs321.cache;

/**
 * The blueprint for cache class
 *
 * The cache can hold only a fixed number of items and always keeps
 * the most recently used item in the front.
 * Each item that gets stored must be able to tell the
 * cache its own key by calling
 * getKey() and the cache uses that key to find, move, or
 * delete the item
 *
 * @author CS321 Instructors
 */

public interface CacheInterface<K, V extends KeyInterface<K>> {

    /**
     * Looks up a value by key
     * If the key is found the corresponding value is returned and promoted to the front of the cache
     * If the key is absent {@code null} is returned and the cache remains unchanged

     * @param key which it is looking for
     * @return the cached value if its present or else it returns null otherwise
     */
    public V get(K key);

    /**
     * Insert a new value in the front of the cache
     * If the cache is not full then the value is added
     * If the cache is full the least recently used entry at the back is removed to make room
     * and the removed value is returned
     *
     * @param value where value is the value you are trying to add
     * @return the removed value when something is removed or return null if nothing is removed
     */
    public V add(V value);

    /**
     * Remove a value that matches the given key
     * @param key to the value that is to be removed
     * @return the removed value or null if the value wasnt found
     */
    public V remove(K key);

    /**
     * Empty the cache completely and reset hit and reference counters
     */
    public void clear();

    /**
     * Returns a readable summary which matches the format which is required for the project
     */
    public String toString();
}
