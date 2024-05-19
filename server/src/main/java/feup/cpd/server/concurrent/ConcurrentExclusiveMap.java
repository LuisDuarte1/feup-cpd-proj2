package feup.cpd.server.concurrent;

import feup.cpd.server.concurrent.helper.LockedValue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

/**
 * It ensures that map value accesses/writes are exclusive which is different from ConcurrentRWMap which
 * multiple threads can read at the same time but only one can write. It ensures also that changes in the Map
 * data structure are also synchronized.
 * @param <K> should be an immutable type
 * @param <V> any value type, can be mutable
 */
public class ConcurrentExclusiveMap<K, V> {

    private final Map<K, LockedValue<V>> internalMap;
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
    private final Lock r = readWriteLock.readLock();
    private final Lock w = readWriteLock.writeLock();


    public ConcurrentExclusiveMap(Map<K, LockedValue<V>> internalMap) {
        this.internalMap = internalMap;
    }

    public ConcurrentExclusiveMap(){
        this.internalMap = new HashMap<>();
    }

    public LockedValue<V> get(K key){
        r.lock();
        try {
            return internalMap.get(key);
        } finally {
            r.unlock();
        }
    }

    public LockedValue<V> put(K key, V value){
        w.lock();
        try {
            var lv = new LockedValue<>(value, new ReentrantLock(true));
            internalMap.put(key, lv);
            return lv;
        } finally {
            w.unlock();
        }
    }

    public void delete(K key){
        w.lock();
        try {
            internalMap.remove(key);
        } finally {
            w.unlock();
        }
    }

    public LockedValue<V> getUntilFirstInverse(Function<LockedValue<V>, Boolean> booleanFunction){
        r.lock();
        try {
            for(var entry : internalMap.entrySet()){
                if(booleanFunction.apply(entry.getValue())) return entry.getValue();
            }
            return null;
        } finally {
            r.unlock();
        }
    }

}
