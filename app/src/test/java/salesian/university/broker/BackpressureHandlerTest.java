package salesian.university.broker;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class BackpressureHandlerTest {
  private BackpressureHandler backpressureHandler;
  private QueueManager mockQueueManager;

  @BeforeEach
  public void setUp() {
    mockQueueManager = Mockito.mock(QueueManager.class);
    backpressureHandler = new BackpressureHandler(mockQueueManager);
  }

  @Test
  public void testCheckLoadAsync_WhenBelowThreshold() throws ExecutionException, InterruptedException {
    String topic = "testTopic";

    when(mockQueueManager.getQueueSizeAsync(topic))
            .thenReturn(CompletableFuture.completedFuture(50));

    boolean isOverloaded = backpressureHandler.checkLoadAsync(topic).get();

    assertFalse(isOverloaded);
    verify(mockQueueManager, times(1)).getQueueSizeAsync(topic);
  }

  @Test
  public void testCheckLoadAsync_WhenAboveThreshold() throws ExecutionException, InterruptedException {
    String topic = "testTopic";

    when(mockQueueManager.getQueueSizeAsync(topic))
            .thenReturn(CompletableFuture.completedFuture(150));

    boolean isOverloaded = backpressureHandler.checkLoadAsync(topic).get();

    assertTrue(isOverloaded);
    verify(mockQueueManager, times(1)).getQueueSizeAsync(topic);
  }

  @Test
  public void testThrottleAsync() throws ExecutionException, InterruptedException {
    String topic = "testTopic";

    CompletableFuture<Void> throttleFuture = backpressureHandler.throttleAsync(topic);

    assertNotNull(throttleFuture);
    throttleFuture.get();

    System.out.println("Throttling completed successfully for topic: " + topic);
  }

  @Test
  public void testThrottleAsync_WhenInterrupted() {
    String topic = "testTopic";

    Thread testThread = new Thread(() -> {
      try {
        backpressureHandler.throttleAsync(topic).get();
      } catch (ExecutionException | InterruptedException e) {
        assertInstanceOf(RuntimeException.class, e.getCause());
        assertEquals("Throttling interrupted", e.getCause().getMessage());
      }
    });
    testThread.start();
    testThread.interrupt();

    try {
      testThread.join();
    } catch (InterruptedException e) {
      fail("Test thread interrupted unexpectedly");
    }
  }

  @AfterEach
  public void tearDown() {
    backpressureHandler = null;
    mockQueueManager = null;
  }
}
