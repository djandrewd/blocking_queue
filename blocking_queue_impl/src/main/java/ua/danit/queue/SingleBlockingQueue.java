package ua.danit.queue;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation of thread safe {@link java.util.Queue}, using single lock for marking conditions.
 *
 * @param <T> the type parameter
 * @author Andrey Minov
 */
public class SingleBlockingQueue<T> extends AbstractQueue<T> implements BlockingQueue<T> {

  /**
   * We using single lock and single condition to sychronize this queue.
   * This is simple possibility to put waiters in the queue when
   * lock cannot be aquired. Note that readers blocks writers as
   * they both share lock for purposes of sharing same data.
   */
  private final int capacity;
  private final Lock lock;
  private final Condition sychCondition;
  private final Queue<T> queue;

  /**
   * Instantiates a new Single blocking queue.
   */
  public SingleBlockingQueue() {
    this(Integer.MAX_VALUE);
  }

  /**
   * Instantiates a new Single blocking queue.
   *
   * @param capacity the max queue capacity
   */
  public SingleBlockingQueue(int capacity) {
    this.capacity = capacity;
    this.lock = new ReentrantLock();
    this.sychCondition = lock.newCondition();
    this.queue = new LinkedList<>();
  }

  @Override
  public boolean offer(T t) {
    lock.lock();
    try {
      if (queue.size() == capacity) {
        return false;
      }
      boolean empty = queue.isEmpty();
      boolean result = queue.offer(t);
      // Avoid useless signalling we do this in case when queue for previously emplty.
      // Also here used signalAll instead of signal when waking up the thread in order
      // some thread not to wait forever for new entry to receive.
      if (empty) {
        sychCondition.signalAll();
      }
      return result;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean offer(T t, long timeout, TimeUnit unit) throws InterruptedException {
    long nanos = unit.toNanos(timeout);
    lock.lock();
    try {
      //
      // When no more space to put element in the queue, wait some time for space to become
      // available. As thread may wake up suddenly - check in a loop;
      while (queue.size() == capacity) {
        if (nanos <= 0) {
          return false;
        }
        nanos = sychCondition.awaitNanos(nanos);
      }

      boolean empty = queue.isEmpty();
      boolean result = queue.offer(t);
      // Avoid useless signalling we do this in case when queue for previously empty.
      // Also here used signalAll instead of signal when waking up the thread in order
      // some thread not to wait forever for new entry to receive.
      if (empty) {
        sychCondition.signalAll();
      }
      return result;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void put(T t) throws InterruptedException {
    lock.lock();
    try {
      //
      // When no more space to put element in the queue, wait some time for space to become
      // available. As thread may wake up suddenly - check in a loop;
      while (queue.size() == capacity) {
        sychCondition.await();
      }
      boolean empty = queue.isEmpty();
      queue.offer(t);
      // Avoid useless signalling we do this in case when queue for previously emplty.
      // Also here used signalAll instead of signal when waking up the thread in order
      // some thread not to wait forever for new entry to receive.
      if (empty) {
        sychCondition.signalAll();
      }
    } finally {
      lock.unlock();
    }
  }


  @Override
  public T take() throws InterruptedException {
    lock.lock();
    try {
      //
      // When no more space to put element in the queue, wait some time for space to become
      // available. As thread may wake up suddenly - check in a loop;
      while (queue.isEmpty()) {
        sychCondition.await();
      }

      int size = queue.size();
      T result = queue.poll();
      // Avoid useless signalling we do this in case when queue for previously full.
      // Also here used signalAll instead of signal when waking up the thread in order
      // some thread not to wait forever for new entry to receive.
      if (size >= queue.size()) {
        sychCondition.signalAll();
      }
      return result;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public T poll(long timeout, TimeUnit unit) throws InterruptedException {
    long nanos = unit.toNanos(timeout);
    lock.lock();
    try {
      //
      // When no more space to put element in the queue, wait some time for space to become
      // available. As thread may wake up suddenly - check in a loop;
      while (queue.isEmpty()) {
        if (nanos <= 0) {
          return null;
        }
        nanos = sychCondition.awaitNanos(nanos);
      }

      int size = queue.size();
      T result = queue.poll();
      // Avoid useless signalling we do this in case when queue for previously full.
      // Also here used signalAll instead of signal when waking up the thread in order
      // some thread not to wait forever for new entry to receive.
      if (size >= queue.size()) {
        sychCondition.signalAll();
      }
      return result;
    } finally {
      lock.unlock();
    }
  }


  @Override
  public T poll() {
    lock.lock();
    try {
      if (queue.isEmpty()) {
        return null;
      }
      int size = queue.size();
      T result = queue.remove();
      // Avoid useless signalling we do this in case when queue for previously full.
      // Also here used signalAll instead of signal when waking up the thread in order
      // some thread not to wait forever for new entry to receive.
      if (size == queue.size()) {
        sychCondition.signalAll();
      }
      return result;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public int remainingCapacity() {
    lock.lock();
    try {
      return capacity - queue.size();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public int drainTo(Collection<? super T> c) {
    lock.lock();
    try {
      int size = queue.size();
      c.addAll(queue);
      queue.clear();
      if (size >= capacity) {
        sychCondition.signalAll();
      }
      return size;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public int drainTo(Collection<? super T> c, int maxElements) {
    lock.lock();
    try {
      int size = queue.size();
      int number = 0;
      T element;
      while (number < maxElements && (element = queue.poll()) != null) {
        c.add(element);
        number++;
      }
      if (size >= capacity) {
        sychCondition.signalAll();
      }
      return number;
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
  public Iterator<T> iterator() {
    lock.lock();
    try {
      return new LinkedList<>(queue).iterator();
    } finally {
      lock.unlock();
    }
  }
}
