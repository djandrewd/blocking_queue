package ua.danit.queue;

import java.util.function.Consumer;

/**
 * Publisher-subscriber application.
 *
 * @author Andrey Minov
 */
public interface PubSubApplication extends AutoCloseable {

  /**
   * Register message consumer into publish subscribe application.
   *
   * @param messageConsumer consumer for message coming from message queue.
   */
  void registerConsumer(Consumer<String> messageConsumer);

  /**
   * Publish message into processing system.
   *
   * @param message the message to publish.
   */
  void publish(String message);
}
