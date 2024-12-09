package salesian.university.broker;

import java.util.concurrent.CompletableFuture;

public class BackpressureHandler {
  private final QueueManager queueManager;
  private static final int THRESHOLD = 100;

  public BackpressureHandler(QueueManager queueManager) {
    this.queueManager = queueManager;
  }

  public CompletableFuture<Boolean> checkLoadAsync(String topic) {
    return queueManager.getQueueSizeAsync(topic)
            .thenApply(queueSize -> queueSize > THRESHOLD);
  }

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
