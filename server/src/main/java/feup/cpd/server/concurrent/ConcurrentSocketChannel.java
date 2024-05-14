package feup.cpd.server.concurrent;

import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ConcurrentSocketChannel is needed because at most one thread can read at a time, and at most one thread
 * can write. However, both operations can be done at the same time. This encapsulates SocketChannel to
 * include two locks.
 **/
public class ConcurrentSocketChannel {

    public final SocketChannel socketChannel;
    public final ReentrantLock readLock = new ReentrantLock();
    public final ReentrantLock writeLock = new ReentrantLock();

    public ConcurrentSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }
}
