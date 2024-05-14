package feup.cpd.server.concurrent.helper;

import java.util.concurrent.locks.ReentrantLock;

public class LockedValue<V> {
    public V value;
    public final ReentrantLock reentrantLock;

    public LockedValue(V value, ReentrantLock reentrantLock) {
        this.value = value;
        this.reentrantLock = reentrantLock;
    }
}
