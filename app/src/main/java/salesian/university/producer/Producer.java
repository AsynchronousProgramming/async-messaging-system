package salesian.university.producer;
import org.json.JSONObject;
import salesian.university.helper.HttpHelper;

import java.io.IOException;
import java.net.HttpURLConnection;

public class Producer {
    private final String brokerUrl;
    private final HttpHelper httpHelper;

    public Producer(String brokerUrl) {
        this.brokerUrl = brokerUrl;
        this.httpHelper = new HttpHelper();
    }

    public void publishEvent(String topic, String message) {
        try {
            JSONObject body = new JSONObject();
            body.put("topic", topic);
            body.put("message", message);
            HttpURLConnection connection = httpHelper.sendPostRequest(brokerUrl + "/publish", body);

            System.out.println("Published event: " + message + " to topic: " + topic);
            connection.getResponseCode();
        } catch (IOException e) {
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}

