package feup.cpd.server.concurrent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 *
 * @param <K> should be immutable and never change (because then, it's hash changes)
 * @param <V> should be cloneable
 */
public class ConcurrentRWMap<K, V> {

    private final Map<K, V> internalMap;
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
    private final Lock r = readWriteLock.readLock();
    private final Lock w = readWriteLock.writeLock();

    public ConcurrentRWMap(){
        this.internalMap = new HashMap<>();
    }

    public ConcurrentRWMap(Map<K, V> initialMap) {
        this.internalMap = initialMap;
    }

    public <R> R get(K key, Function<V,R> func){
        r.lock();
        try{
            return func.apply(internalMap.get(key));
        }
        finally {
            r.unlock();
        }
    }

    public <R> R mutateValue(K key, Function<V,R> func){
        w.lock();
        try {
            return func.apply(internalMap.get(key));
        } finally {
            w.unlock();
        }
    }

    public void put(K key, V value){
        w.lock();
        try{
            internalMap.put(key, value);
        } finally {
            w.unlock();
        }
    }


    public void clear(){
        w.lock();
        try{
            internalMap.clear();
        } finally {
            w.unlock();
        }

    }

    public int size(){
        r.lock();
        try {
            return internalMap.size();
        } finally {
            r.unlock();
        }
    }


    public <R> List<R> getAllValues(Function<V,R> func){
        r.lock();
        try{
            return internalMap.values().stream().map(func).toList();
        }
        finally {
            r.unlock();
        }
    }

    public <R> Map<K,R> getAll(Function<V,R> function){
        r.lock();
        try {
            Map<K,R> newMap = new HashMap<>();
            internalMap.forEach(((k, v) -> newMap.put(k, function.apply(v))));
            return newMap;
        }
        finally {
            r.unlock();
        }

    }

    public Map<K,V> removeCount(int countToRemove){
        w.lock();
        try {
            Map<K,V> removedPairs = new HashMap<>();
            var i = 0;
            for(var entry : internalMap.entrySet()){
                if (i >= countToRemove) break;
                removedPairs.put(entry.getKey(), entry.getValue());
                internalMap.remove(entry.getKey());
                i++;
            }
            return removedPairs;
        } finally {
            w.unlock();
        }
    }

    public void putAll(Map<K,V> map){
        w.lock();
        try {
            internalMap.putAll(map);
        } finally {
            w.unlock();
        }
    }

    public void remove(K key){
        w.lock();
        try {
            internalMap.remove(key);
        } finally {
            w.unlock();
        }
    }
}
