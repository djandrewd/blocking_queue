package ua.danit.queue;

import java.util.Scanner;

/**
 * Queue application for processing.
 *
 * @author Andrey Minov
 */
public class Application {
  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    PubSubApplication application = null;
    Scanner scanner = new Scanner(System.in);
    String message;

    application.registerConsumer(m -> System.out.println("Message received in consumer 1: " + m));
    application.registerConsumer(m -> System.out.println("Message received in consumer 2: " + m));

    while (!"exit".equals(message = scanner.next())) {
      application.publish(message);
    }
  }
}
