package ua.danit.queue;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of thread safe {@link java.util.concurrent.BlockingQueue},
 * using monitor synchronization lock for entering critical sections
 *
 * @param <T> the type parameter
 * @author Andrey Minov
 */
public class SynchronizedBlockingQueue<T> extends AbstractQueue<T> implements BlockingQueue<T> {

  /**
   * We using single mutex to sychronize this queue.
   * This is simple possibility to put waiters in the queue when
   * lock cannot be aquired. Note that readers blocks writers as
   * they both share lock for purposes of sharing same data.
   */
  private final int capacity;
  private final Object mu;
  private final Queue<T> queue;

  /**
   * Instantiates a new Synchronized blocking queue with Integer.MAX_VALUE capacity.
   */
  public SynchronizedBlockingQueue() {
    this(Integer.MAX_VALUE);
  }

  /**
   * Instantiates a new Synchronized blocking queue.
   *
   * @param capacity the capacity of the queue
   */
  public SynchronizedBlockingQueue(int capacity) {
    this.capacity = capacity;
    this.mu = new Object();
    this.queue = new LinkedList<>();
  }


  @Override
  public void put(T t) throws InterruptedException {
    synchronized (mu) {  //
      // When no more space to put element in the queue, wait some time for space to become
      // available. As thread may wake up suddenly - check in a loop;
      while (queue.size() == capacity) {
        mu.wait();
      }
      boolean empty = queue.isEmpty();
      queue.offer(t);
      // Avoid useless signalling we do this in case when queue for previously emplty.
      // Also here used signalAll instead of signal when waking up the thread in order
      // some thread not to wait forever for new entry to receive.
      if (empty) {
        mu.notifyAll();
      }
    }
  }

  @Override
  public boolean offer(T t) {
    synchronized (mu) {
      if (queue.size() == capacity) {
        return false;
      }
      boolean empty = queue.isEmpty();
      boolean result = queue.offer(t);
      // Avoid useless signalling we do this in case when queue for previously emplty.
      // Also here used signalAll instead of signal when waking up the thread in order
      // some thread not to wait forever for new entry to receive.
      if (empty) {
        mu.notifyAll();
      }
      return result;
    }
  }

  @Override
  public boolean offer(T t, long timeout, TimeUnit unit) throws InterruptedException {
    boolean run = false;
    synchronized (mu) {
      //
      // When no more space to put element in the queue, wait some time for space to become
      // available. As thread may wake up suddenly - check in a loop;
      while (queue.size() == capacity) {
        if (run) {
          return false;
        }
        run = true;
        mu.wait(unit.toMillis(timeout));
      }

      boolean empty = queue.isEmpty();
      boolean result = queue.offer(t);
      // Avoid useless signalling we do this in case when queue for previously empty.
      // Also here used signalAll instead of signal when waking up the thread in order
      // some thread not to wait forever for new entry to receive.
      if (empty) {
        mu.notifyAll();
      }
      return result;
    }
  }

  @Override
  public T take() throws InterruptedException {
    synchronized (mu) {
      //
      // When no more space to put element in the queue, wait some time for space to become
      // available. As thread may wake up suddenly - check in a loop;
      while (queue.isEmpty()) {
        mu.wait();
      }

      int size = queue.size();
      T result = queue.poll();
      // Avoid useless signalling we do this in case when queue for previously full.
      // Also here used signalAll instead of signal when waking up the thread in order
      // some thread not to wait forever for new entry to receive.
      if (size >= queue.size()) {
        mu.notifyAll();
      }
      return result;
    }
  }

  @Override
  public T poll(long timeout, TimeUnit unit) throws InterruptedException {
    boolean run = false;
    synchronized (mu) {
      //
      // When no more space to put element in the queue, wait some time for space to become
      // available. As thread may wake up suddenly - check in a loop;
      while (queue.isEmpty()) {
        if (run) {
          return null;
        }
        run = true;
        mu.wait(unit.toMillis(timeout));
      }

      int size = queue.size();
      T result = queue.poll();
      // Avoid useless signalling we do this in case when queue for previously full.
      // Also here used signalAll instead of signal when waking up the thread in order
      // some thread not to wait forever for new entry to receive.
      if (size >= queue.size()) {
        mu.notifyAll();
      }
      return result;
    }
  }


  @Override
  public T poll() {
    synchronized (mu) {
      if (queue.isEmpty()) {
        return null;
      }
      int size = queue.size();
      T result = queue.remove();
      // Avoid useless signalling we do this in case when queue for previously full.
      // Also here used signalAll instead of signal when waking up the thread in order
      // some thread not to wait forever for new entry to receive.
      if (size == queue.size()) {
        mu.notifyAll();
      }
      return result;
    }
  }


  @Override
  public int remainingCapacity() {
    synchronized (mu) {
      return capacity - queue.size();
    }
  }

  @Override
  public int drainTo(Collection<? super T> c) {
    synchronized (mu) {
      int size = queue.size();
      c.addAll(queue);
      queue.clear();
      if (size >= capacity) {
        mu.notifyAll();
      }
      return size;
    }
  }

  @Override
  public int drainTo(Collection<? super T> c, int maxElements) {
    synchronized (mu) {
      int size = queue.size();
      int number = 0;
      T element;
      while (number < maxElements && (element = queue.poll()) != null) {
        c.add(element);
        number++;
      }
      if (size >= capacity) {
        mu.notifyAll();
      }
      return number;
    }
  }


  @Override
  public T peek() {
    synchronized (mu) {
      return queue.peek();
    }
  }

  @Override
  public int size() {
    synchronized (mu) {
      return queue.size();
    }
  }

  @Override
  public Iterator<T> iterator() {
    synchronized (mu) {
      return new LinkedList<>(queue).iterator();
    }
  }

}
