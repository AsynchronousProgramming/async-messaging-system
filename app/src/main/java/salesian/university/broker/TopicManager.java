package salesian.university.broker;

import salesian.university.helpers.StringManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

/**
 * This class manages topics and their subscribers,
 * allowing topic creation, subscription, and retrieval
 * of topics and subscribers.
 */
public class TopicManager {
    private final Map<String, List<String>> subscriptions;
    private final StringManager stringManager;

    /**
     * A constructor to initialize the TopicManager
     * with no topics or subscriptions.
     */
    public TopicManager() {
        this.subscriptions = new HashMap<>();
        this.stringManager = new StringManager();
    }

    /**
     * This method creates a new topic.
     *
     * @param topic the name of the topic to create.
     */
    public void createTopic(String topic) {
        subscriptions.putIfAbsent(stringManager.getLowerCaseString(topic), new ArrayList<>());
    }

    /**
     * This method adds a subscriber to a specified topic.
     *
     * @param topic       the name of the topic.
     * @param consumerUrl the URL of the subscriber.
     * @throws IllegalArgumentException if the topic does not exist.
     */
    public void addSubscriber(String topic, String consumerUrl) {
        String topicLowerCase = stringManager.getLowerCaseString(topic);
        if (!subscriptions.containsKey(topicLowerCase)) {
            throw new IllegalArgumentException("Topic does not exist: " + topic);
        }
        subscriptions.get(topicLowerCase).add(consumerUrl);
    }

    /**
     * This method retrieves the subscribers for a specified topic.
     *
     * @param topic the name of the topic.
     * @return a list of subscriber URLs.
     */
    public List<String> getSubscribers(String topic) {
        return subscriptions.getOrDefault(stringManager.getLowerCaseString(topic), Collections.emptyList());
    }

    /**
     * This method retrieves all topic names.
     *
     * @return a list of topic names.
     */
    public List<String> getTopics() {
        return new ArrayList<>(subscriptions.keySet());
    }
}
