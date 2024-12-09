package salesian.university.producer;
import org.json.JSONObject;
import salesian.university.helpers.HttpHelper;

import java.io.IOException;
import java.net.HttpURLConnection;

public class Producer {
    private final String brokerUrl;
    private final HttpHelper httpHelper;

    public Producer(String brokerUrl) {
        this.brokerUrl = brokerUrl;
        this.httpHelper = new HttpHelper();
    }

    public Producer(String brokerUrl, HttpHelper httpHelper) {
        this.brokerUrl = brokerUrl;
        this.httpHelper = httpHelper;
    }

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

    public static void main(String[] args) {
        Producer producer = new Producer("http://localhost:8080");

        producer.publishEvent("sports", "Message 1 for sports");
        producer.publishEvent("news", "Message 1 for news");
        producer.publishEvent("science", "Message 2 for science");
    }
}
