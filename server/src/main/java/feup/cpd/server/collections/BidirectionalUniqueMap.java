package feup.cpd.server.collections;

import java.util.*;

/**
 * Bidirectional Map where there's a 1:1 relationship
 * @param <K>
 * @param <V>
 */
public class BidirectionalUniqueMap<K,V> {


    private final Map<K,V> privateMap;
    private final Map<V,K> privateMapInverse;


    public BidirectionalUniqueMap(Map<K, V> privateMap, boolean maintainOrder) {

        if(!maintainOrder){
            this.privateMap = privateMap;
            this.privateMapInverse = new HashMap<>();

        } else {
            this.privateMap = new LinkedHashMap<>(privateMap);
            this.privateMapInverse = new LinkedHashMap<>();
        }
        privateMap.forEach((k,v) -> privateMapInverse.put(v, k));

    }

    public BidirectionalUniqueMap(boolean maintainOrder){
        if(!maintainOrder){
            this.privateMap = new HashMap<>();
            this.privateMapInverse = new HashMap<>();

        } else {
            this.privateMap = new LinkedHashMap<>();
            this.privateMapInverse = new LinkedHashMap<>();
        }
    }


    public V get(K key){
        return privateMap.get(key);
    }

    public K getInverse(V value){
        return privateMapInverse.get(value);
    }

    public Set<Map.Entry<K,V>> getAll(){
        return privateMap.entrySet();
    }

    public Set<Map.Entry<V,K>> getAllInverse(){
        return privateMapInverse.entrySet();
    }

    public void put(K key, V value){
        privateMap.put(key, value);
        privateMapInverse.put(value, key);
    }

    public void remove(K key){
        V val = privateMap.get(key);
        privateMap.remove(key);
        privateMapInverse.remove(val);
    }

    public void removeInverse(V value){
        K key = privateMapInverse.get(value);
        privateMapInverse.remove(value);
        privateMap.remove(key);
    }

    public void addAll(Map<K,V> collection){
        privateMap.putAll(collection);
        collection.forEach((k,v) -> privateMapInverse.put(v,k));
    }

    public int size(){
        return privateMap.size();
    }

    public boolean containsKey(K key){
        return privateMap.containsKey(key);
    }

    public Map<K,V> removeCount(int countToRemove){
        Map<K,V> removedPairs = new LinkedHashMap<>();
        var i = 0;
        for(var entry : privateMap.entrySet()){
            if (i >= countToRemove) break;
            removedPairs.put(entry.getKey(), entry.getValue());
            i++;
        }
        removedPairs.forEach((k, v) -> {
            privateMap.remove(k);
            privateMapInverse.remove(v);
        });
        return removedPairs;
    }
}
