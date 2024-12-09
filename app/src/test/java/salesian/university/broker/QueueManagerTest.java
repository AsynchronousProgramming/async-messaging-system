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
  public void testEnqueueAndDequeue_SingleMessage() throws ExecutionException, InterruptedException {
    queueManager.enqueue("testTopic", "message1").get();

    Optional<String> dequeuedMessage = queueManager.dequeue("testTopic").get();

    assertNotNull(dequeuedMessage);
    assertTrue(dequeuedMessage.isPresent());
    assertEquals("message1", dequeuedMessage.get());
  }

  @Test
  public void testEnqueueAndDequeue_MultipleMessages() throws ExecutionException, InterruptedException {
    queueManager.enqueue("testTopic", "message1").get();
    queueManager.enqueue("testTopic", "message2").get();

    Optional<String> firstMessage = queueManager.dequeue("testTopic").get();
    Optional<String> secondMessage = queueManager.dequeue("testTopic").get();
    Optional<String> thirdMessage = queueManager.dequeue("testTopic").get();

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
  public void testGetQueueSize_EmptyQueue() throws ExecutionException, InterruptedException {
    int size = queueManager.getQueueSize("emptyTopic").get();

    assertEquals(0, size);
  }

  @Test
  public void testGetQueueSize_AfterEnqueueAndDequeue() throws ExecutionException, InterruptedException {
    queueManager.enqueue("testTopic", "message1").get();
    queueManager.enqueue("testTopic", "message2").get();
    queueManager.dequeue("testTopic").get();

    int size = queueManager.getQueueSize("testTopic").get();

    assertEquals(1, size);
  }

  @Test
  public void testDequeueFromNonExistentTopic() throws ExecutionException, InterruptedException {
    Optional<String> dequeuedMessage = queueManager.dequeue("nonExistentTopic").get();

    assertNotNull(dequeuedMessage);
    assertTrue(dequeuedMessage.isEmpty());
  }

  @Test
  public void testDequeueFromEmptyQueue() throws ExecutionException, InterruptedException {
    queueManager.enqueue("testTopic", "message1").get();
    queueManager.dequeue("testTopic").get();

    Optional<String> dequeuedMessage = queueManager.dequeue("testTopic").get();

    assertNotNull(dequeuedMessage);
    assertTrue(dequeuedMessage.isEmpty());
  }

  @AfterEach
  public void tearDown() {
    queueManager = null;
  }
}
