package ua.danit.queue.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import ua.danit.queue.ConcurrentLockQueue;

/**
 * Test for simple lock queue.
 *
 * @author Andrey Minov
 */
@Ignore
public class ConcurrentQueueTest {

  private Queue<Integer> queue;

  @Before
  public void setUp() throws Exception {
    queue = new ConcurrentLockQueue<>();
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
}