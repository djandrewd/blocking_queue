package ua.danit.queue;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation of thread safe {@link java.util.concurrent.BlockingQueue},
 * using two way locking algorithm.
 *
 * @param <T> the type parameter
 * @author Andrey Minov
 */
public class TwoWayLockingQueue<T> implements BlockingQueue<T> {
  /*
   * Two conditions and locks are using for synchronization purposes.
   * One lock is responsible to enqueing and one for dequeing. This make
   * possibility dequers, not block enqueuers during method calls.
   * Only one possible way it was block each other is then queue is full or empty.
   * For this purpose atomic size variable is used, and also it will be linearization
   * point for queue process.
   * */

  private final int capacity;
  private final Lock enqLock;
  private final Condition fullCondition;
  private final Lock deqLock;
  private final Condition emptyCondition;
  private final Queue<T> queue;
  private final AtomicInteger size;

  /**
   * Instantiates a new blocking queue bases on two way locking algorithm and unlimited capacity.
   */
  public TwoWayLockingQueue() {
    this(Integer.MAX_VALUE);
  }

  /**
   * Instantiates a new blocking queue bases on two way locking algorithm.
   *
   * @param capacity the capacity of the queue.
   */
  public TwoWayLockingQueue(int capacity) {
    this.capacity = capacity;
    this.queue = new LinkedList<>();

    this.enqLock = new ReentrantLock();
    this.deqLock = new ReentrantLock();

    this.fullCondition = enqLock.newCondition();
    this.emptyCondition = deqLock.newCondition();

    this.size = new AtomicInteger();
  }

  @Override
  public void put(T t) throws InterruptedException {
    int oldSize;
    // When we put element into then queue, we lock only deq. lock.
    // If size is bigger then capacity we wait is space become free on await full condition.
    // We do this in the queue, because application can abandon lock before capacity requiments
    // wont fit.
    enqLock.lock();
    try {
      while (size.get() == capacity) {
        fullCondition.await();
      }
      oldSize = size.getAndIncrement();
      queue.offer(t);
    } finally {
      enqLock.unlock();
    }
    signalEmpty(oldSize);
  }

  @Override
  public boolean offer(T t) {
    int oldSize;
    boolean result;
    enqLock.lock();
    try {
      if (size.get() == capacity) {
        return false;
      }
      oldSize = size.getAndIncrement();
      result = queue.offer(t);
    } finally {
      enqLock.unlock();
    }
    signalEmpty(oldSize);
    return result;

  }

  @Override
  public boolean offer(T t, long timeout, TimeUnit unit) throws InterruptedException {
    int oldSize;
    boolean result;
    long nanos = unit.toNanos(timeout);
    // When we put element into then queue, we lock only deq. lock.
    // If size is bigger then capacity we wait is space become free on await full condition.
    // We do this in the queue, because application can abandon lock before capacity requiments
    // wont fit.
    enqLock.lock();
    try {
      while (size.get() == capacity) {
        if (nanos <= 0) {
          return false;
        }
        nanos = fullCondition.awaitNanos(nanos);
      }
      oldSize = size.getAndIncrement();
      result = queue.offer(t);
    } finally {
      enqLock.unlock();
    }
    signalEmpty(oldSize);
    return result;
  }

  @Override
  public T take() throws InterruptedException {
    // During take we lock enq. lock and await for queue to have more elements
    // on empty conditions. We do this in the queue, because application
    // can abandon lock before capacity requiments
    // wont fit.
    int oldSize;
    T result;
    deqLock.lock();
    try {
      while (size.get() == 0) {
        emptyCondition.await();
      }
      oldSize = size.getAndDecrement();
      result = queue.poll();
    } finally {
      deqLock.unlock();
    }
    signalFull(oldSize);
    return result;
  }

  @Override
  public T poll() {
    int oldSize;
    T result;
    deqLock.lock();
    try {
      if (size.get() == 0) {
        return null;
      }
      oldSize = size.getAndDecrement();
      result = queue.poll();
    } finally {
      deqLock.unlock();
    }
    signalFull(oldSize);
    return result;
  }

  @Override
  public T poll(long timeout, TimeUnit unit) throws InterruptedException {
    // During take we lock enq. lock and await for queue to have more elements
    // on empty conditions.
    // We do this in the queue, because application can abandon lock before capacity requiments
    // wont fit.
    int oldSize;
    T result;
    long nanos = unit.toNanos(timeout);
    deqLock.lock();
    try {
      while (size.get() == 0) {
        if (nanos <= 0) {
          return null;
        }
        nanos = emptyCondition.awaitNanos(nanos);
      }
      oldSize = size.decrementAndGet();
      result = queue.poll();
    } finally {
      deqLock.unlock();
    }
    signalFull(oldSize);
    return result;
  }


  @Override
  public int remainingCapacity() {
    return capacity - size.get();
  }

  @Override
  public int size() {
    return size.get();
  }

  @Override
  public boolean isEmpty() {
    return size() == 0;
  }

  @Override
  public boolean add(T t) {
    int oldSize;
    boolean result;
    enqLock.lock();
    try {
      if (size.get() == capacity) {
        throw new RuntimeException("Queue is full!");
      }
      oldSize = size.getAndIncrement();
      result = queue.offer(t);
    } finally {
      enqLock.unlock();
    }
    signalEmpty(oldSize);
    return result;
  }

  @Override
  public T remove() {
    int oldSize;
    T result;
    deqLock.lock();
    try {
      if (size.get() == 0) {
        throw new RuntimeException("Queue is full!");
      }
      oldSize = size.getAndDecrement();
      result = queue.poll();
    } finally {
      deqLock.unlock();
    }
    signalFull(oldSize);
    return result;
  }

  @Override
  public boolean remove(Object o) {
    int oldSize = -1;
    boolean result;
    deqLock.lock();
    try {
      result = queue.remove(o);
      if (result) {
        oldSize = size.getAndDecrement();
      }
    } finally {
      deqLock.unlock();
    }
    if (result) {
      signalFull(oldSize);
    }
    return result;
  }

  @Override
  public T element() {
    enqLock.lock();
    try {
      return queue.element();
    } finally {
      enqLock.unlock();
    }
  }

  @Override
  public T peek() {
    enqLock.lock();
    try {
      return queue.peek();
    } finally {
      enqLock.unlock();
    }
  }

  @Override
  public boolean contains(Object o) {
    enqLock.lock();
    try {
      return queue.contains(o);
    } finally {
      enqLock.unlock();
    }
  }

  @Override
  public int drainTo(Collection<? super T> c) {
    int oldSize;
    deqLock.lock();
    try {
      if (c == queue) {
        throw new IllegalArgumentException("Queue cannot be drain to itself!");
      }
      c.addAll(queue);
      queue.clear();
      oldSize = size.getAndSet(0);
    } finally {
      deqLock.unlock();
    }
    signalEmpty(oldSize);
    return oldSize;
  }

  @Override
  public int drainTo(Collection<? super T> c, int maxElements) {
    int number;
    int oldSize;
    deqLock.lock();
    try {
      if (c == queue) {
        throw new IllegalArgumentException("Queue cannot be drain to itself!");
      }
      number = 0;
      T element;
      while (number < maxElements && (element = queue.poll()) != null) {
        c.add(element);
        number++;
      }
      oldSize = size.getAndAdd(-number);
    } finally {
      deqLock.unlock();
    }
    signalEmpty(oldSize);
    return number;
  }


  @Override
  public Iterator<T> iterator() {
    enqLock.lock();
    deqLock.lock();
    try {
      return new LinkedList<>(queue).iterator();
    } finally {
      deqLock.unlock();
      enqLock.unlock();
    }
  }

  @Override
  public Object[] toArray() {
    enqLock.lock();
    deqLock.lock();
    try {
      return queue.toArray();
    } finally {
      deqLock.unlock();
      enqLock.unlock();
    }
  }

  @Override
  public <T1> T1[] toArray(T1[] a) {
    enqLock.lock();
    deqLock.lock();
    try {
      return queue.toArray(a);
    } finally {
      deqLock.unlock();
      enqLock.unlock();
    }
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    enqLock.lock();
    try {
      return queue.containsAll(c);
    } finally {
      enqLock.unlock();
    }
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    int oldSize = -1;
    boolean result;
    enqLock.lock();
    try {
      if (c == null || size.get() + c.size() > capacity) {
        return false;
      }
      result = queue.addAll(c);
      if (result) {
        oldSize = size.getAndAdd(c.size());
      }
    } finally {
      enqLock.unlock();
    }
    if (result) {
      signalEmpty(oldSize);
    }
    return result;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    int oldSize = -1;
    boolean result;
    deqLock.lock();
    try {
      result = queue.removeAll(c);
      if (result) {
        oldSize = size.getAndSet(queue.size());
      }
    } finally {
      deqLock.unlock();
    }
    if (result) {
      signalEmpty(oldSize);
    }
    return result;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    int oldSize = -1;
    boolean result;
    deqLock.lock();
    try {
      result = queue.retainAll(c);
      if (result) {
        oldSize = size.getAndSet(queue.size());
      }
    } finally {
      deqLock.unlock();
    }
    if (result) {
      signalEmpty(oldSize);
    }
    return result;
  }

  @Override
  public void clear() {
    int oldSize;
    deqLock.lock();
    try {
      queue.clear();
      oldSize = size.getAndSet(0);
    } finally {
      deqLock.unlock();
    }
    signalEmpty(oldSize);
  }

  private void signalEmpty(int oldSize) {
    // Check is queue was empty at time we insert entry. Signal empty wait condition.
    if (oldSize == 0) {
      deqLock.lock();
      try {
        emptyCondition.signalAll();
      } finally {
        deqLock.unlock();
      }
    }
  }

  private void signalFull(int oldSize) {
    if (oldSize == capacity - 1) {
      enqLock.lock();
      try {
        fullCondition.signalAll();
      } finally {
        enqLock.unlock();
      }
    }
  }
}
