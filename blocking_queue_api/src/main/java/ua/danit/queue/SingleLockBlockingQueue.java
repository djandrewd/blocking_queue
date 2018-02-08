package ua.danit.queue;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of thread safe {@link java.util.Queue}, using single lock for marking conditions.
 *
 * @param <T> the type parameter
 * @author Andrey Minov
 */
public class SingleLockBlockingQueue<T> extends AbstractQueue<T> implements BlockingQueue<T> {
  private int capacity;

  /**
   * Instantiates a new Single blocking queue.
   */
  public SingleLockBlockingQueue() {
    this(Integer.MAX_VALUE);
  }

  /**
   * Instantiates a new Single blocking queue.
   *
   * @param capacity the max queue capacity
   */
  public SingleLockBlockingQueue(int capacity) {
    this.capacity = capacity;
  }

  @Override
  public Iterator<T> iterator() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void put(T t) throws InterruptedException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean offer(T t) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean offer(T t, long timeout, TimeUnit unit) throws InterruptedException {
    throw new UnsupportedOperationException();
  }

  @Override
  public T take() throws InterruptedException {
    throw new UnsupportedOperationException();
  }

  @Override
  public T poll(long timeout, TimeUnit unit) throws InterruptedException {
    throw new UnsupportedOperationException();
  }

  @Override
  public T poll() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int remainingCapacity() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int drainTo(Collection<? super T> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int drainTo(Collection<? super T> c, int maxElements) {
    throw new UnsupportedOperationException();
  }

  @Override
  public T peek() {
    throw new UnsupportedOperationException();
  }
}