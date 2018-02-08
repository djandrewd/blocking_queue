package ua.danit.queue.stress;

import java.util.Collection;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Mode;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.Signal;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.LL_Result;
import ua.danit.queue.SingleLockBlockingQueue;

/**
 * Stress test for concurrent blocking queue.
 *
 * @author Andrey Minov
 */
public class SingleLockQueueStressTest {
  private static BlockingQueue<Integer> createQueue(int capacity) {
    return new SingleLockBlockingQueue<>(capacity);
  }

  private static BlockingQueue<Integer> createQueue(int capacity, Collection<Integer> values) {
    BlockingQueue<Integer> queue = createQueue(capacity);
    queue.addAll(values);
    return queue;
  }

  /*
  * One actor waits for queue to be not empty, other signals element. Expect first take element
  * and return.
   * */
  @JCStressTest(Mode.Termination)
  @Outcome(id = "TERMINATED", expect = Expect.ACCEPTABLE, desc = "Gracefully finished.")
  @Outcome(id = "STALE", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Test hung up.")
  @State
  public static class TakeWaitTest {
    private BlockingQueue<Integer> queue = createQueue(1);


    /**
     * Single actor call.
     */
    @Actor
    public void actor1() throws InterruptedException {
      // One actor will wait for element to come. Another put element in
      // and expect thread to terminate.
      queue.take();
    }

    @Signal
    public void signal() throws InterruptedException {
      queue.put(1);
    }
  }

  /*
  * One actor waits for queue to be not full, other signals element by removing it.
  * Expect first put element and return.
  * */
  @JCStressTest(Mode.Termination)
  @Outcome(id = "TERMINATED", expect = Expect.ACCEPTABLE, desc = "Gracefully finished.")
  @Outcome(id = "STALE", expect = Expect.ACCEPTABLE_INTERESTING, desc = "Test hung up.")
  @State
  public static class PutWaitTest {
    private BlockingQueue<Integer> queue = createQueue(1, Collections.singleton(1));

    /**
     * Single actor call.
     */
    @Actor
    public void actor1() {
      // One actor will wait for queue to be free to insert element. Another poll element in
      // and expect thread to terminate.
      try {
        queue.put(1);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    /**
     * Single actor call.
     */
    @Signal
    public void signal() throws InterruptedException {
      try {
        queue.take();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /*
   * Two actors are adding elements into the queue. Expect after execution to see both
   * results.
  * */
  @JCStressTest
  @Outcome(id = "1, 1", expect = Expect.FORBIDDEN, desc = "Both actors came up with the same value")
  @Outcome(id = "2, 2", expect = Expect.FORBIDDEN, desc = "Both actors came up with the same value")
  @Outcome(id = "1, null", expect = Expect.FORBIDDEN, desc = "Second actor came with null value!")
  @Outcome(id = "null, 1", expect = Expect.FORBIDDEN, desc = "First author came with null value!")
  @Outcome(id = "2, null", expect = Expect.FORBIDDEN, desc = "Second actor came with null value!")
  @Outcome(id = "null, 2", expect = Expect.FORBIDDEN, desc = "First author came with null value!")
  @Outcome(id = "1, 2", expect = Expect.ACCEPTABLE, desc = "Implemented correctly!")
  @Outcome(id = "2, 1", expect = Expect.ACCEPTABLE, desc = "Implemented correctly!")
  @State
  public static class PutTest {
    private BlockingQueue<Integer> queue = createQueue(2);

    /**
     * Single actor call.
     */
    @Actor
    public void actor1() {
      try {
        queue.put(1);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    /**
     * Single actor call.
     */
    @Actor
    public void actor2() {
      try {
        queue.put(2);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    /**
     * Single actor check.
     */
    @Arbiter
    public void arbiter(LL_Result s) {
      try {
        s.r1 = queue.take();
        s.r2 = queue.take();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /*
  * Two actors are adding elements into the queue. Expect after execution to see both
  * results.
  * */
  @JCStressTest
  @Outcome(id = "1, 1", expect = Expect.FORBIDDEN, desc = "Both actors came up with the same value")
  @Outcome(id = "2, 2", expect = Expect.FORBIDDEN, desc = "Both actors came up with the same value")
  @Outcome(id = "1, null", expect = Expect.FORBIDDEN, desc = "Second actor came with null value!")
  @Outcome(id = "null, 1", expect = Expect.FORBIDDEN, desc = "First author came with null value!")
  @Outcome(id = "2, null", expect = Expect.FORBIDDEN, desc = "Second actor came with null value!")
  @Outcome(id = "null, 2", expect = Expect.FORBIDDEN, desc = "First author came with null value!")
  @Outcome(id = "1, 2", expect = Expect.ACCEPTABLE, desc = "Implemented correctly!")
  @Outcome(id = "2, 1", expect = Expect.ACCEPTABLE, desc = "Implemented correctly!")
  @State
  public static class AddTest {
    private Queue<Integer> queue = createQueue(2);

    @Actor
    public void actor1() {
      queue.add(1);
    }

    @Actor
    public void actor2() {
      queue.add(2);
    }

    @Arbiter
    public void arbiter(LL_Result s) {
      s.r1 = queue.poll();
      s.r2 = queue.poll();
    }
  }

  /*
  * Two actors are adding elements into the queue. Expect after execution to see both
  * results.
  * */
  @JCStressTest
  @Outcome(id = "1, 1", expect = Expect.FORBIDDEN, desc = "Both actors came up with the same value")
  @Outcome(id = "2, 2", expect = Expect.FORBIDDEN, desc = "Both actors came up with the same value")
  @Outcome(id = "1, null", expect = Expect.FORBIDDEN, desc = "Second actor came with null value!")
  @Outcome(id = "null, 1", expect = Expect.FORBIDDEN, desc = "First author came with null value!")
  @Outcome(id = "2, null", expect = Expect.FORBIDDEN, desc = "Second actor came with null value!")
  @Outcome(id = "null, 2", expect = Expect.FORBIDDEN, desc = "First author came with null value!")
  @Outcome(id = "1, 2", expect = Expect.ACCEPTABLE, desc = "Implemented correctly!")
  @Outcome(id = "2, 1", expect = Expect.ACCEPTABLE, desc = "Implemented correctly!")
  @State
  public static class OfferTest {
    private Queue<Integer> queue = createQueue(2);

    @Actor
    public void actor1() {
      queue.offer(1);
    }

    @Actor
    public void actor2() {
      queue.offer(2);
    }

    @Arbiter
    public void arbiter(LL_Result s) {
      s.r1 = queue.poll();
      s.r2 = queue.poll();
    }
  }

  /*
  * Two actors are adding elements into the queue. Each peek element after execution. We expect
  * both of calls gives same result.
  * */
  @JCStressTest
  @Outcome(id = "1, null", expect = Expect.FORBIDDEN, desc = "Second actor came with null value!")
  @Outcome(id = "null, 1", expect = Expect.FORBIDDEN, desc = "First author came with null value!")
  @Outcome(id = "2, null", expect = Expect.FORBIDDEN, desc = "Second actor came with null value!")
  @Outcome(id = "null, 2", expect = Expect.FORBIDDEN, desc = "First author came with null value!")
  @Outcome(id = "1, 2", expect = Expect.FORBIDDEN, desc = "Peek must get same element twice!")
  @Outcome(id = "2, 1", expect = Expect.FORBIDDEN, desc = "Peek must get same element twice!")
  @Outcome(id = "1, 1", expect = Expect.ACCEPTABLE, desc = "Implemented correctly!")
  @Outcome(id = "2, 2", expect = Expect.ACCEPTABLE, desc = "Implemented correctly!")
  @State
  public static class PeekTest {
    private Queue<Integer> queue = createQueue(2);

    @Actor
    public void actor1(LL_Result r) {
      queue.offer(1);
      r.r1 = queue.peek();
    }

    @Actor
    public void actor2(LL_Result r) {
      queue.offer(2);
      r.r2 = queue.peek();
    }
  }

  /*
  * Two actors are adding elements into the queue. We expect both of them added and size will
  * be equals to two.
   * */
  @JCStressTest
  @Outcome(id = "1, 2", expect = Expect.FORBIDDEN, desc = "Size must be same for both actors!")
  @Outcome(id = "2, 1", expect = Expect.FORBIDDEN, desc = "Size must be same for both actors!")
  @Outcome(id = "1, 1", expect = Expect.FORBIDDEN, desc = "Not yet inserted!")
  @Outcome(id = "2, 2", expect = Expect.ACCEPTABLE, desc = "Implemented correctly!")
  @State
  public static class SizeTest {
    private Queue<Integer> queue = createQueue(2);

    @Actor
    public void actor1() {
      queue.offer(1);
    }

    @Actor
    public void actor2() {
      queue.offer(2);
    }

    @Arbiter
    public void arbiter(LL_Result s) {
      s.r1 = queue.size();
      s.r2 = queue.size();
    }
  }

  /*
  * Two actors are adding elements into the queue.
  * We expect both of them added using contains method.
  * */
  @JCStressTest
  @Outcome(id = "false, true", expect = Expect.FORBIDDEN, desc = "Not yet inserted!")
  @Outcome(id = "true, false", expect = Expect.FORBIDDEN, desc = "Not yet inserted!")
  @Outcome(id = "false, false", expect = Expect.FORBIDDEN, desc = "Not yet inserted!")
  @Outcome(id = "true, true", expect = Expect.ACCEPTABLE, desc = "Implemented correctly!")
  @State
  public static class ContainsTest {
    private Queue<Integer> queue = createQueue(2);

    @Actor
    public void actor1() {
      queue.offer(1);
    }

    @Actor
    public void actor2() {
      queue.offer(2);
    }

    @Arbiter
    public void arbiter(LL_Result s) {
      s.r1 = queue.contains(1);
      s.r2 = queue.contains(2);
    }
  }
}
