package ua.danit.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Publisher-subscriber application based on queue.
 *
 * @author Andrey Minov
 */
public class QueuePubSubApplication implements PubSubApplication {
  private static final int MAX_THREADS = 10;
  private static final long TIMEOUT_SEC = 2;

  private ExecutorService executor;
  private BlockingQueue<String> queue;
  private AtomicBoolean open;

  /**
   * Instantiates a new Queue pub sub application.
   */
  public QueuePubSubApplication() {
    this(MAX_THREADS);
  }

  /**
   * Instantiates a new Queue pub sub application.
   *
   * @param threadNumber the thread number for application to start.
   */
  public QueuePubSubApplication(int threadNumber) {
    this.executor = Executors.newFixedThreadPool(threadNumber);
    this.queue = new TwoWayLockingQueue<>();
    this.open = new AtomicBoolean(true);
  }

  @Override
  public void registerConsumer(Consumer<String> messageConsumer) {
    executor.execute(() -> {
      while (open.get()) {
        try {
          String message = queue.poll(TIMEOUT_SEC, TimeUnit.SECONDS);
          if (message == null) {
            continue;
          }
          Logger.getGlobal().log(Level.INFO, () -> String
              .format("Received message %s by %s", message, Thread.currentThread()));
          messageConsumer.accept(message);
        } catch (InterruptedException e) {
          Logger.getGlobal().log(Level.SEVERE, e, () -> "Interruption error.");
        }
      }
      Logger.getGlobal().log(Level.INFO, () -> String
          .format("Exiting consumer from %s", Thread.currentThread()));
    });
  }

  @Override
  public void publish(String message) {
    Logger.getGlobal().log(Level.INFO, () -> String.format("Publish message %s", message));
    queue.offer(message);
  }

  @Override
  public void close() {
    if (open.compareAndSet(true, false)) {
      executor.shutdown();
    }
  }
}
