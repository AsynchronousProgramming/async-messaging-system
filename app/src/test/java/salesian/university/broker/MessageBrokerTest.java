package salesian.university.broker;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MessageBrokerTest {
    private MessageBroker messageBroker;
    private TopicManager topicManager;
    private final int PORT = 8082;

    @BeforeAll
    void setUp() throws IOException {
        topicManager = new TopicManager();
        messageBroker = new MessageBroker(topicManager, PORT);
        new Thread(messageBroker::start).start();
    }

    @AfterAll
    void tearDown() {
        messageBroker = null;
    }

    private HttpURLConnection sendPostRequest(String endpoint, JSONObject body) throws IOException {
        URL url = new URL("http://localhost:" + PORT + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        return connection;
    }


    @Test
    void testCreateTopic() throws IOException {
        String topic = "sports";
        JSONObject body = new JSONObject();
        body.put("topic", topic);

        HttpURLConnection connection = sendPostRequest("/createTopic", body);
        int responseCode = connection.getResponseCode();

        assertEquals(200, responseCode);
        assertTrue(topicManager.getSubscribers(topic).isEmpty());
    }

    @Test
    void testPublishWithNoSubscribers() throws IOException {
        String topic = "news";
        String message = "testMessage";
        JSONObject body = new JSONObject();
        body.put("topic", topic);
        body.put("message", message);

        topicManager.createTopic(topic);

        HttpURLConnection connection = sendPostRequest("/publish", body);
        int responseCode = connection.getResponseCode();

        assertEquals(200, responseCode);
        assertTrue(topicManager.getSubscribers(topic).isEmpty());
    }

    @Test
    void testSubscribe() throws IOException {
        String topic = "football";
        String consumerUrl = "http://localhost:9000";
        JSONObject body = new JSONObject();
        body.put("topic", topic);
        body.put("consumerUrl", consumerUrl);

        topicManager.createTopic(topic);

        HttpURLConnection connection = sendPostRequest("/subscribe", body);
        int responseCode = connection.getResponseCode();

        assertEquals(200, responseCode);
        List<String> subscribers = topicManager.getSubscribers(topic);
        assertEquals(1, subscribers.size());
        assertTrue(subscribers.contains(consumerUrl));
    }

    private final AtomicReference<String> receivedMessage = new AtomicReference<>();

    @Test
    void testPublishWithSubscribers() throws IOException {
        HttpServer mockConsumerServer = HttpServer.create(new InetSocketAddress(9004), 0);
        mockConsumerServer.createContext("/", this::handleMockConsumerRequest);
        mockConsumerServer.start();

        String topic = "programming";
        String message = "testMessage";
        String consumerUrl = "http://localhost:9004";

        JSONObject body = new JSONObject();
        body.put("topic", topic);
        body.put("message", message);

        topicManager.createTopic(topic);
        topicManager.addSubscriber(topic, consumerUrl);

        HttpURLConnection connection = sendPostRequest("/publish", body);
        int responseCode = connection.getResponseCode();

        assertEquals(200, responseCode);
        assertEquals(message, receivedMessage.get());
    }

    private void handleMockConsumerRequest(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(body);
            receivedMessage.set(jsonObject.getString("message"));
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().close();
        } else {
            exchange.sendResponseHeaders(405, 0);
            exchange.getResponseBody().close();
        }
    }


    @Test
    void testInvalidMethod() throws IOException {
        URL url = new URL("http://localhost:" + PORT + "/createTopic");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        int responseCode = connection.getResponseCode();
        assertEquals(405, responseCode);
    }
}
