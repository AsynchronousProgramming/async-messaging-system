package salesian.university.broker;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;
import salesian.university.helpers.HttpHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.util.List;

public class MessageBroker {
    private final int port;
    private final HttpServer server;
    private final TopicManager topicManager;
    private final HttpHelper httpHelper;

    public MessageBroker(TopicManager topicManager, int port) throws IOException {
        this.topicManager = topicManager;
        this.port = port;
        this.httpHelper = new HttpHelper();
        server = HttpServer.create(new InetSocketAddress(port), 0);
    }

    public MessageBroker(int port) throws IOException {
        this(new TopicManager(), port);
    }

    public void start() {
        createTopicContext();
        publishMessageContext();
        subscribeContext();

        server.start();
        System.out.printf("Message Broker started on port %d%n", port);
    }

    private void createTopicContext() {
        server.createContext("/createTopic", exchange -> {
            handlePostRequest(exchange, body -> {
                String topic = body.getString("topic");
                topicManager.createTopic(topic);
                return "Topic " + topic + " created successfully";
            });
        });
    }

    private void publishMessageContext() {
        server.createContext("/publish", exchange -> {
            handlePostRequest(exchange, body -> {
                String topic = body.getString("topic");
                String message = body.getString("message");
                publish(topic, message);
                return "Message published successfully";
            });
        });
    }

    private void subscribeContext() {
        server.createContext("/subscribe", exchange -> {
            handlePostRequest(exchange, body -> {
                String topic = body.getString("topic");
                String consumerUrl = body.getString("consumerUrl");
                subscribe(topic, consumerUrl);
                return "Subscription added successfully";
            });
        });
    }

    private void handlePostRequest(HttpExchange exchange, RequestHandler handler) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            try (InputStream inputStream = exchange.getRequestBody()) {
                String body = new String(inputStream.readAllBytes());
                JSONObject requestBody = new JSONObject(body);
                String responseMessage = handler.handle(requestBody);
                sendResponse(exchange, 200, responseMessage);
            } catch (Exception e) {
                sendResponse(exchange, 400, "Invalid request body");
            }
        } else {
            sendResponse(exchange, 405, "Method not allowed");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseMessage) throws IOException {
        byte[] responseBytes = responseMessage.getBytes();
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    public void publish(String topic, String message) {
        List<String> subscribers = topicManager.getSubscribers(topic);
        if (subscribers.isEmpty()) {
            System.out.println("No subscribers for topic: " + topic);
        } else {
            for (String subscriber : subscribers) {
                try {
                    JSONObject messageBody = new JSONObject();
                    messageBody.put("message", message);
                    HttpURLConnection response = httpHelper.sendPostRequest(subscriber + "/receive", messageBody);
                    System.out.println("Sent message to subscriber: " + subscriber + " with status: " + response.getResponseCode());
                } catch (IOException e) {
                    System.err.println("Failed to send message to subscriber: " + subscriber);
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public void subscribe(String topic, String consumerUrl) {
        topicManager.addSubscriber(topic, consumerUrl);
        System.out.println("Added subscriber: " + consumerUrl + " to topic: " + topic);
    }

    @FunctionalInterface
    private interface RequestHandler {
        String handle(JSONObject body) throws Exception;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new MessageBroker(8080).start();
        HttpHelper httpHelper = new HttpHelper();
        JSONObject bodyTopic = new JSONObject();

        Thread.sleep(1000);
        bodyTopic.put("topic", "sports");
        HttpURLConnection response = httpHelper.sendPostRequest("http://localhost:8080/createTopic", bodyTopic);
        System.out.println(response.getResponseMessage());
        bodyTopic.put("topic", "news");
        response = httpHelper.sendPostRequest("http://localhost:8080/createTopic", bodyTopic);
        System.out.println(response.getResponseMessage());
        bodyTopic.put("topic", "science");
        response = httpHelper.sendPostRequest("http://localhost:8080/createTopic", bodyTopic);
        System.out.println(response.getResponseMessage());
    }
}
