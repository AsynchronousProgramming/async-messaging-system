package salesian.university.broker;


import salesian.university.helpers.StringManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

public class TopicManager {
    private final Map<String, List<String>> subscriptions;
    private final StringManager stringManager;

    public TopicManager() {
        this.subscriptions = new HashMap<>();
        this.stringManager = new StringManager();
    }

    public void createTopic(String topic) {
        subscriptions.putIfAbsent(stringManager.getLowerCaseString(topic), new ArrayList<>());
    }

    public void addSubscriber(String topic, String consumerUrl) {
        String topicLowerCase = stringManager.getLowerCaseString(topic);
        if (!subscriptions.containsKey(topicLowerCase)) {
            throw new IllegalArgumentException("Topic does not exist: " + topic);
        }
        subscriptions.get(topicLowerCase).add(consumerUrl);
    }

    public List<String> getSubscribers(String topic) {
        return subscriptions.getOrDefault(stringManager.getLowerCaseString(topic), Collections.emptyList());
    }
}
