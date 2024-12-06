package salesian.university.broker;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueueManagerTest {
  private QueueManager queueManager;

  @BeforeEach
  public void setUp() {
    queueManager = new QueueManager();
  }

  @Test
  public void testEnqueueAsyncAndDequeueAsync_SingleMessage() throws ExecutionException, InterruptedException {
    queueManager.enqueueAsync("testTopic", "message1").get();

    Optional<String> dequeuedMessage = queueManager.dequeueAsync("testTopic").get();

    assertNotNull(dequeuedMessage);
    assertTrue(dequeuedMessage.isPresent());
    assertEquals("message1", dequeuedMessage.get());
  }

  @Test
  public void testEnqueueAsyncAndDequeueAsync_MultipleMessages() throws ExecutionException, InterruptedException {
    queueManager.enqueueAsync("testTopic", "message1").get();
    queueManager.enqueueAsync("testTopic", "message2").get();

    Optional<String> firstMessage = queueManager.dequeueAsync("testTopic").get();
    Optional<String> secondMessage = queueManager.dequeueAsync("testTopic").get();
    Optional<String> thirdMessage = queueManager.dequeueAsync("testTopic").get();

    assertNotNull(firstMessage);
    assertTrue(firstMessage.isPresent());
    assertEquals("message1", firstMessage.get());

    assertNotNull(secondMessage);
    assertTrue(secondMessage.isPresent());
    assertEquals("message2", secondMessage.get());

    assertNotNull(thirdMessage);
    assertTrue(thirdMessage.isEmpty());
  }

  @Test
  public void testGetQueueSizeAsync_EmptyQueue() throws ExecutionException, InterruptedException {
    int size = queueManager.getQueueSizeAsync("emptyTopic").get();

    assertEquals(0, size);
  }

  @Test
  public void testGetQueueSizeAsync_AfterEnqueueAsyncAndDequeueAsync() throws ExecutionException, InterruptedException {
    queueManager.enqueueAsync("testTopic", "message1").get();
    queueManager.enqueueAsync("testTopic", "message2").get();
    queueManager.dequeueAsync("testTopic").get();

    int size = queueManager.getQueueSizeAsync("testTopic").get();

    assertEquals(1, size);
  }

  @Test
  public void testDequeueAsyncFromNonExistentTopic() throws ExecutionException, InterruptedException {
    Optional<String> dequeuedMessage = queueManager.dequeueAsync("nonExistentTopic").get();

    assertNotNull(dequeuedMessage);
    assertTrue(dequeuedMessage.isEmpty());
  }

  @Test
  public void testDequeueAsyncFromEmptyQueue() throws ExecutionException, InterruptedException {
    queueManager.enqueueAsync("testTopic", "message1").get();
    queueManager.dequeueAsync("testTopic").get();

    Optional<String> dequeuedMessage = queueManager.dequeueAsync("testTopic").get();

    assertNotNull(dequeuedMessage);
    assertTrue(dequeuedMessage.isEmpty());
  }

  @AfterEach
  public void tearDown() {
    queueManager = null;
  }
}
