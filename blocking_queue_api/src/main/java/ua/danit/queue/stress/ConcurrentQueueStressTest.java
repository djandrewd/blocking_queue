package ua.danit.queue.stress;

import java.util.Collection;
import java.util.Queue;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.LL_Result;
import ua.danit.queue.ConcurrentLockQueue;

/**
 * Stress test for concurrent queue.
 *
 * @author Andrey Minov
 */
public class ConcurrentQueueStressTest {
  private static Queue<Integer> createQueue() {
    return new ConcurrentLockQueue<>();
  }

  private static Queue<Integer> createQueue(Collection<Integer> values) {
    Queue<Integer> queue = createQueue();
    queue.addAll(values);
    return queue;
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
    private Queue<Integer> queue = createQueue();

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
    private Queue<Integer> queue = createQueue();

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
  * Two actors are adding elements into the queue and peek one. Expect after execution both results
  * are same after peek.
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
    private Queue<Integer> queue = createQueue();

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
  * Two actors are adding elements into the queue. Expect after execution to see both
  * results and size equals to 2.
  * */
  @JCStressTest
  @Outcome(id = "1, 2", expect = Expect.FORBIDDEN, desc = "Size must be same for both actors!")
  @Outcome(id = "2, 1", expect = Expect.FORBIDDEN, desc = "Size must be same for both actors!")
  @Outcome(id = "1, 1", expect = Expect.FORBIDDEN, desc = "Not yet inserted!")
  @Outcome(id = "2, 2", expect = Expect.ACCEPTABLE, desc = "Implemented correctly!")
  @State
  public static class SizeTest {
    private Queue<Integer> queue = createQueue();

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
  * Two actors are adding elements into the queue. Expect after execution to see both
  * results using contains method.
  * */
  @JCStressTest
  @Outcome(id = "false, true", expect = Expect.FORBIDDEN, desc = "Not yet inserted!")
  @Outcome(id = "true, false", expect = Expect.FORBIDDEN, desc = "Not yet inserted!")
  @Outcome(id = "false, false", expect = Expect.FORBIDDEN, desc = "Not yet inserted!")
  @Outcome(id = "true, true", expect = Expect.ACCEPTABLE, desc = "Implemented correctly!")
  @State
  public static class ContainsTest {
    private Queue<Integer> queue = createQueue();

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
