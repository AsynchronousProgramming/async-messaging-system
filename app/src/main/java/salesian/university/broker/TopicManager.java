package salesian.university.broker;

import java.util.*;

public class TopicManager {
    private final Map<String, List<String>> subscriptions;

    public TopicManager() {
        this.subscriptions = new HashMap<>();
    }

    public void createTopic(String topic) {
        subscriptions.putIfAbsent(topic.toLowerCase(), new ArrayList<>());
    }

    public void addSubscriber(String topic, String consumerUrl) {
        String topicLowerCase = topic.toLowerCase();
        if (!subscriptions.containsKey(topicLowerCase)) {
            throw new IllegalArgumentException("Topic does not exist: " + topic);
        }
        subscriptions.get(topicLowerCase).add(consumerUrl);
    }

    public List<String> getSubscribers(String topic) {
        return subscriptions.getOrDefault(topic.toLowerCase(), Collections.emptyList());
    }
}
