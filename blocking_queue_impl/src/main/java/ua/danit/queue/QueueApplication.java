package ua.danit.queue;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Queue application for processing.
 *
 * @author Andrey Minov
 */
public class QueueApplication {
  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    try (PubSubApplication application = new QueuePubSubApplication()) {
      Scanner scanner = new Scanner(System.in);
      String message;

      application.registerConsumer(m -> System.out.println("Message received in consumer 1: " + m));
      application.registerConsumer(m -> System.out.println("Message received in consumer 2: " + m));

      while (!"exit".equals(message = scanner.next())) {
        application.publish(message);
      }
    } catch (Exception e) {
      Logger.getGlobal().log(Level.SEVERE, e, e::getMessage);
    }
  }
}
