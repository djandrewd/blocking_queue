package ua.danit.queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for blocking queue.
 *
 * @author Andrey Minov
 */
@Ignore
public class BlockingQueueTest {

  private BlockingQueue<Integer> queue;

  @Before
  public void setUp() throws Exception {
    queue = createQueue(5);
  }

  private BlockingQueue<Integer> createQueue(int capacity) {
    return new SingleLockBlockingQueue<>(capacity);
  }

  @Test
  public void testEmpty() {
    assertTrue(queue.isEmpty());
  }

  @Test
  public void testAddAndRemove() {
    assertTrue("Incorrent add result!", queue.add(1));
    assertEquals("Incorrect value removed from queue!", 1, (int) queue.remove());
    assertTrue("Queue is not empty", queue.isEmpty());
  }

  @Test(expected = NoSuchElementException.class)
  public void testRemoveOnEmpty() {
    queue.remove();
  }

  @Test
  public void testOfferAndPoll() throws Exception {
    assertTrue("Incorrent add result!", queue.offer(1));
    assertEquals("Incorrect value removed from queue!", 1, (int) queue.poll());
    assertTrue("Queue is not empty", queue.isEmpty());
  }

  @Test
  public void testPeekElement() throws Exception {
    assertTrue("Incorrent add result!", queue.offer(1));
    assertEquals("Incorrect peek element!", 1, (int) queue.peek());
    assertFalse("Queue is empty", queue.isEmpty());
  }

  @Test
  public void testElement() throws Exception {
    assertTrue("Incorrent add result!", queue.add(1));
    assertEquals("Incorrect peek element!", 1, (int) queue.element());
    assertFalse("Queue is empty", queue.isEmpty());
  }

  @Test(expected = NoSuchElementException.class)
  public void testElementOnEmpty() {
    queue.element();
  }

  @Test
  public void testContains() throws Exception {
    assertTrue("Incorrent add result!", queue.add(1));
    assertTrue("Element is not contained in collection!", queue.contains(1));
  }

  @Test
  public void testAddAll() throws Exception {
    assertTrue("Incorrent add result!", queue.addAll(Arrays.asList(1, 2, 3, 4, 5)));
    assertTrue("Incorrent contains result!", queue.containsAll(Arrays.asList(1, 2, 3, 4, 5)));
    assertEquals("Incorrent size of queue!", 5, queue.size());
  }

  @Test
  public void testIterator() throws Exception {
    assertTrue("Incorrent add result!", queue.addAll(Arrays.asList(1, 2, 3, 4, 5)));
    Iterator<Integer> iterator = queue.iterator();
    assertNotNull("Iterator is empty!", iterator);
    assertTrue(iterator.hasNext());
    assertEquals(1, (int) iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(2, (int) iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(3, (int) iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(4, (int) iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(5, (int) iterator.next());
    assertFalse(iterator.hasNext());
  }

  @Test
  public void testWait() throws InterruptedException {
    queue = createQueue(1);
    queue.add(1);
    long time = System.currentTimeMillis();
    assertFalse("Element is inserted, while must not!", queue.offer(2, 3, TimeUnit.SECONDS));
    assertTrue("Timeout is less then expected",
        System.currentTimeMillis() - time >= TimeUnit.SECONDS.toMillis(3));
  }

  @Test
  public void testPollWait() throws InterruptedException {
    queue = createQueue(1);
    long time = System.currentTimeMillis();
    assertNull("Element is exists!", queue.poll(3, TimeUnit.SECONDS));
    assertTrue("Timeout is less then expected",
        System.currentTimeMillis() - time >= TimeUnit.SECONDS.toMillis(3));
  }

  @Test
  public void testPubSubProcess() throws InterruptedException {
    int n = 10;
    CountDownLatch cd = new CountDownLatch(n);
    Executor executor = Executors.newCachedThreadPool();
    for (int k = 0; k < n / 2; k++) {
      executor.execute(() -> {
        for (int i = 0; i < n && cd.getCount() > 0; i++) {
          cd.countDown();
        }
      });
    }
    for (int i = 0; i < n; i++) {
      queue.put(i);
    }
    assertTrue("Not all messages are send!", cd.await(3, TimeUnit.SECONDS));
  }

}