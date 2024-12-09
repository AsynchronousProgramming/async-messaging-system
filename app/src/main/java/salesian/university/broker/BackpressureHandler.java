package salesian.university.broker;

import java.util.concurrent.CompletableFuture;

/**
 * This class handles backpressure in the system by monitoring
 * the load on message queues and throttling when necessary.
 */
public class BackpressureHandler {
  private final QueueManager queueManager;
  private static final int THRESHOLD = 100;

  /**
   * A constructor to initialize BackpressureHandler
   * with the specified QueueManager.
   *
   * @param queueManager the QueueManager instance used
   *                     for queue operations.
   */
  public BackpressureHandler(QueueManager queueManager) {
    this.queueManager = queueManager;
  }

  /**
   * This method asynchronously checks if the load
   * on a specific topic exceeds the predefined threshold.
   *
   * @param topic the name of the topic to check.
   * @return a CompletableFuture containing {@code true} if the queue
   * size exceeds the threshold, otherwise {@code false}.
   */
  public CompletableFuture<Boolean> checkLoadAsync(String topic) {
    return queueManager.getQueueSizeAsync(topic)
            .thenApply(queueSize -> queueSize > THRESHOLD);
  }

  /**
   * This method asynchronously applies throttling
   * to a specific topic.
   *
   * @param topic the name of the topic to throttle.
   * @return a CompletableFuture representing the throttling operation.
   */
  public CompletableFuture<Void> throttleAsync(String topic) {
    System.out.println("Throttling started for topic: " + topic);
    return CompletableFuture.runAsync(() -> {
      try {
        Thread.sleep(500);
        System.out.println("Throttling ended for topic: " + topic);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("Throttling interrupted", e);
      }
    });
  }
}
