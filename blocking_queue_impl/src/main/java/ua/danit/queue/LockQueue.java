package ua.danit.queue;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation of thread safe {@link java.util.Queue},
 * using single lock for entering critical sections.
 *
 * @author Andrey Minov
 */
public class LockQueue<T> extends AbstractQueue<T> {

  /**
   * Instance of lock using for purposes of synchronization.
   */
  private final Lock lock;
  private final LinkedList<T> queue;

  public LockQueue() {
    this.lock = new ReentrantLock();
    this.queue = new LinkedList<>();
  }

  @Override
  public Iterator<T> iterator() {
    lock.lock();
    try {
      return new LinkedList<>(queue).iterator();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public int size() {
    lock.lock();
    try {
      return queue.size();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean offer(T t) {
    lock.lock();
    try {
      return queue.offer(t);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public T poll() {
    lock.lock();
    try {
      return queue.poll();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public T peek() {
    lock.lock();
    try {
      return queue.peek();
    } finally {
      lock.unlock();
    }
  }

}
