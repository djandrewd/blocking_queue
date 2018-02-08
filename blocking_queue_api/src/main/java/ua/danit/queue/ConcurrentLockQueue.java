package ua.danit.queue;

import java.util.AbstractQueue;
import java.util.Iterator;

/**
 * Implementation of thread safe {@link java.util.Queue},
 * using single lock for entering critical sections.
 *
 * @author Andrey Minov
 */
public class ConcurrentLockQueue<T> extends AbstractQueue<T> {
  @Override
  public Iterator<T> iterator() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean offer(T t) {
    throw new UnsupportedOperationException();
  }

  @Override
  public T poll() {
    throw new UnsupportedOperationException();
  }

  @Override
  public T peek() {
    throw new UnsupportedOperationException();
  }
}
