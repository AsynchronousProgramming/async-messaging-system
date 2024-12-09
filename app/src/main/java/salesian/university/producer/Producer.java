package salesian.university.producer;
import org.json.JSONObject;
import salesian.university.helpers.HttpHelper;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * This class represents a producer in the message broker system.
 * <p>
 * A producer can publish events to specific topics on the message broker.
 */
public class Producer {
    private final String brokerUrl;
    private final HttpHelper httpHelper;

    /**
     * A constructor to create a new producer with the specified broker URL.
     *
     * @param brokerUrl the URL of the message broker.
     */
    public Producer(String brokerUrl) {
        this.brokerUrl = brokerUrl;
        this.httpHelper = new HttpHelper();
    }

    /**
     * A constructor to create a new producer with the specified
     * broker URL and HTTP helper.
     *
     * @param brokerUrl  the URL of the message broker.
     * @param httpHelper the helper for handling HTTP requests.
     */
    public Producer(String brokerUrl, HttpHelper httpHelper) {
        this.brokerUrl = brokerUrl;
        this.httpHelper = httpHelper;
    }

    /**
     * This method publishes an event to the specified topic
     * on the message broker.
     *
     * @param topic   the topic to publish the event to.
     * @param message the event message to be published.
     * @throws RuntimeException if the publication fails.
     */
    public void publishEvent(String topic, String message) {
        try {
            JSONObject body = new JSONObject();
            body.put("topic", topic);
            body.put("message", message);
            HttpURLConnection connection = httpHelper.sendPostRequest(brokerUrl + "/publish", body);

            int responseCode = connection.getResponseCode();
            if(responseCode == 200) {
                System.out.println("Published event: " + message + " to topic: " + topic);
            } else {
                throw new RuntimeException("Failed to publish to topic");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    /**
     * Main method to run the producer application.
     *
     * @param args command-line arguments.
     */
    public static void main(String[] args) {
        Producer producer = new Producer("http://localhost:8080");

        producer.publishEvent("sports", "Initial message for sports");
        producer.publishEvent("news", "Message 1 for news");
        producer.publishEvent("science", "Message 2 for science");

        for (int i = 0; i < 200; i++) {
            String message = "Message " + (i + 1) + " for sports";
            producer.publishEvent("sports", message);
        }
    }
}
