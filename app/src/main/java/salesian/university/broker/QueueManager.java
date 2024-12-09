package salesian.university.broker;

import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class QueueManager {
  private final Map<String, Queue<String>> queues;

  public QueueManager() {
    this.queues = new ConcurrentHashMap<>();
    System.out.println("QueueManager initialized.");
  }

  public CompletableFuture<Void> enqueueAsync(String topic, String message) {
    return CompletableFuture.runAsync(() -> {
      queues.computeIfAbsent(topic, k -> new LinkedList<>()).offer(message);
      System.out.println("\nMessage " + message + " added to topic: " + topic + ", queue size: " + queues.get(topic).size());
    });
  }

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

  public CompletableFuture<Integer> getQueueSizeAsync(String topic) {
    return CompletableFuture.supplyAsync(() -> {
      Queue<String> queue = queues.get(topic);
      int size = queue == null ? 0 : queue.size();
      System.out.println("Queue size for topic " + topic + ": " + size);
      return size;
    });
  }
}
