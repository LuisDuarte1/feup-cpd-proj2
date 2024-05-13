package feup.cpd.server.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public class ConcurrentRWList<T> {

    private List<T> internalList = List.of();
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock r = readWriteLock.readLock();
    private final Lock w = readWriteLock.writeLock();


    public ConcurrentRWList() {
    }

    public ConcurrentRWList(List<T> internalList) {
        this.internalList = internalList;
    }

    public void add(T val){
        w.lock();
        try{
            internalList.add(val);
        } finally {
            w.unlock();
        }
    }

    public void addAll(Collection<T> valCollection){
        w.lock();
        try{
            internalList.addAll(valCollection);
        } finally {
            w.unlock();
        }
    }

    public int size(){
        r.lock();
        try{
            return internalList.size();
        } finally {
            r.unlock();
        }
    }
    public <R> R get(int index, Function<T,R> func){
        r.lock();
        try{
            return func.apply(internalList.get(index));
        }
        finally {
            r.unlock();
        }
    }

    public <R> R mutateValue(int index, Function<T,R> func){
        w.lock();
        try {
            return func.apply(internalList.get(index));
        } finally {
            w.unlock();
        }
    }
    public <R> List<R> getAll(Function<T,R> func){
        r.lock();
        try{
            return internalList.stream().map(func).toList();
        }
        finally {
            r.unlock();
        }
    }

    public void remove(int index){
        w.lock();
        try {
            internalList.remove(index);
        } finally {
            w.unlock();
        }
    }

}
