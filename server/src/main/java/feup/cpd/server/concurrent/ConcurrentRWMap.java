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
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
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


    public <R> List<R> getAllValues(Function<V,R> func){
        r.lock();
        try{
            return internalMap.values().stream().map(func).toList();
        }
        finally {
            r.unlock();
        }
    }

}
