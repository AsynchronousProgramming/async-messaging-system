package salesian.university.broker;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class manages message queues for different topics,
 * providing enqueue, dequeue, and queue size operations.
 */
public class QueueManager {
  private final Map<String, Queue<String>> queues;

  /**
   * A constructor to initialize the QueueManager
   * with an empty set of queues.
   */
  public QueueManager() {
    this.queues = new ConcurrentHashMap<>();
    System.out.println("QueueManager initialized.");
  }

  /**
   * This method asynchronously enqueues a message
   * to a specified topic.
   *
   * @param topic   the name of the topic.
   * @param message the message to enqueue.
   * @return a CompletableFuture representing the enqueue operation.
   */
  public CompletableFuture<Void> enqueueAsync(String topic, String message) {
    return CompletableFuture.runAsync(() -> {
      queues.computeIfAbsent(topic, k -> new LinkedList<>()).offer(message);
      System.out.println("\nMessage " + message + " added to topic: " + topic + ", queue size: " + queues.get(topic).size());
    });
  }

  /**
   * This method asynchronously dequeues a message
   * from a specified topic.
   *
   * @param topic the name of the topic.
   * @return a CompletableFuture containing an Optional with
   * the dequeued message, or an empty Optional if the queue is empty.
   */
  public CompletableFuture<Optional<String>> dequeueAsync(String topic) {
    return CompletableFuture.supplyAsync(() -> {
      Queue<String> queue = queues.get(topic);
      if (queue == null || queue.isEmpty()) {
        return Optional.empty();
      }
      String message = queue.poll();
      System.out.println("\nMessage dequeued from topic: " + topic + ", remaining queue size: " + queue.size());
      return Optional.ofNullable(message);
    });
  }

  /**
   * This method asynchronously retrieves the size
   * of the queue for a specified topic.
   *
   * @param topic the name of the topic.
   * @return a CompletableFuture containing the size of the queue.
   */
  public CompletableFuture<Integer> getQueueSizeAsync(String topic) {
    return CompletableFuture.supplyAsync(() -> {
      Queue<String> queue = queues.get(topic);
      int size = queue == null ? 0 : queue.size();
      System.out.println("Queue size for topic " + topic + ": " + size);
      return size;
    });
  }
}
