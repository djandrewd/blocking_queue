package ua.danit.queue;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Implementation of thread safe {@link java.util.Queue},
 * using monitor synchronization lock for entering critical sections
 *
 * @author Andrey Minov
 */
public class SynchronizedQueue<T> extends AbstractQueue<T> {
  /*
  * Simple synchronized queue bazed on mutex object.
  * */
  private final Object mu;
  private final Queue<T> queue;

  public SynchronizedQueue() {
    this.mu = new Object();
    this.queue = new LinkedList<>();
  }

  @Override
  public Iterator<T> iterator() {
    synchronized (mu) {
      return new LinkedList<>(queue).iterator();
    }
  }

  @Override
  public int size() {
    synchronized (mu) {
      return queue.size();
    }
  }

  @Override
  public boolean offer(T t) {
    synchronized (mu) {
      return queue.offer(t);
    }
  }

  @Override
  public T poll() {
    synchronized (mu) {
      return queue.poll();
    }
  }

  @Override
  public T peek() {
    synchronized (mu) {
      return queue.peek();
    }
  }
}
