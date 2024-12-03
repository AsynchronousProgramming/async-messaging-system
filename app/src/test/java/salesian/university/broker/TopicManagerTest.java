package salesian.university.broker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class TopicManagerTest {
    private TopicManager topicManager;

    @BeforeEach
    public void setUp() {
        topicManager = new TopicManager();
    }

    @Test
    public void testCreateTopic_ShouldAddNewTopic() {
        String topic = "Sports";

        topicManager.createTopic(topic);

        List<String> subscribers = topicManager.getSubscribers(topic);
        assertNotNull(subscribers, "Subscribers list should not be null");
        assertTrue(subscribers.isEmpty(), "New topic should have no subscribers initially");
    }

    @Test
    public void testCreateTopic_ShouldNotOverrideExistingTopic() {
        String topic = "Sports";

        topicManager.createTopic(topic);
        topicManager.createTopic(topic);

        List<String> subscribers = topicManager.getSubscribers(topic);
        assertNotNull(subscribers, "Subscribers list should not be null");
        assertTrue(subscribers.isEmpty(), "Existing topic should not be overridden");
    }

    @Test
    public void testAddSubscriber_ShouldAddSubscriber() {
        String topic = "Sports";
        String consumerUrl = "consumer1.com";

        topicManager.createTopic(topic);
        topicManager.addSubscriber(topic, consumerUrl);

        List<String> subscribers = topicManager.getSubscribers(topic);
        assertEquals(1, subscribers.size(), "There should be one subscriber");
        assertTrue(subscribers.contains(consumerUrl), "Subscriber should be added to the topic");
    }

    @Test
    public void testAddSubscriber_ShouldThrowExceptionWhenTopicDoesNotExist() {
        String topic = "Sports";
        String consumerUrl = "consumer1.com";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
                topicManager.addSubscriber(topic, consumerUrl));

        assertEquals("Topic does not exist: " + topic, exception.getMessage(), "Exception message should match");
    }

    @Test
    public void testGetSubscribers_ShouldReturnEmptyListForNonExistentTopic() {
        String topic = "NonExistentTopic";

        List<String> subscribers = topicManager.getSubscribers(topic);

        assertNotNull(subscribers, "Subscribers list should not be null");
        assertTrue(subscribers.isEmpty(), "There should be no subscribers for a non-existent topic");
    }

    @Test
    public void testGetSubscribers_ShouldReturnSubscribersForExistingTopic() {
        String topic = "Sports";
        String consumerUrl1 = "consumer1.com";
        String consumerUrl2 = "consumer2.com";

        topicManager.createTopic(topic);
        topicManager.addSubscriber(topic, consumerUrl1);
        topicManager.addSubscriber(topic, consumerUrl2);

        List<String> subscribers = topicManager.getSubscribers(topic);
        assertEquals(2, subscribers.size(), "There should be two subscribers");
        assertTrue(subscribers.contains(consumerUrl1), "Subscriber 1 should be in the list");
        assertTrue(subscribers.contains(consumerUrl2), "Subscriber 2 should be in the list");
    }
}

