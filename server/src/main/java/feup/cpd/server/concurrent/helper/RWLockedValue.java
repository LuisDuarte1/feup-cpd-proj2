package feup.cpd.server.concurrent.helper;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public class RWLockedValue<V> {
    private V value;
    private final ReentrantReadWriteLock reentrantLock;

    private final Lock read;

    private final Lock write;

    public RWLockedValue(V value){
        this.value = value;
        this.reentrantLock = new ReentrantReadWriteLock(true);
        this.write = reentrantLock.writeLock();
        this.read = reentrantLock.readLock();
    }

    public RWLockedValue(V value, ReentrantReadWriteLock reentrantLock) {
        this.value = value;
        this.reentrantLock = reentrantLock;
        this.write = reentrantLock.writeLock();
        this.read = reentrantLock.readLock();
    }

    public <R> R lockAndRead(Function<V,R> function){
        read.lock();
        try{
            return function.apply(value);
        } finally {
            read.unlock();
        }
    }

    public <R> R lockAndWrite(Function<V,R> function){
        write.lock();
        try{
            return function.apply(value);
        } finally {
            write.unlock();
        }
    }
}
