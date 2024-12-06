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
  }

  public CompletableFuture<Void> enqueueAsync(String topic, String message) {
    return CompletableFuture.runAsync(() -> {
      queues.computeIfAbsent(topic, k -> new LinkedList<>()).offer(message);
    });
  }

  public CompletableFuture<Optional<String>> dequeueAsync(String topic) {
    return CompletableFuture.supplyAsync(() -> {
      Queue<String> queue = queues.get(topic);
      if (queue == null || queue.isEmpty()) {
        return Optional.empty();
      }
      return Optional.ofNullable(queue.poll());
    });
  }

  public CompletableFuture<Integer> getQueueSizeAsync(String topic) {
    return CompletableFuture.supplyAsync(() -> {
      Queue<String> queue = queues.get(topic);
      return queue == null ? 0 : queue.size();
    });
  }
}
