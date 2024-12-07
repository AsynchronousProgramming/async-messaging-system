package salesian.university.broker;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class MessageBroker {
    private final int port;
    private final HttpServer server;
    private final TopicManager topicManager;

    public MessageBroker(TopicManager topicManager, int port) throws IOException {
        this.topicManager = topicManager;
        this.port = port;
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
            if ("POST".equals(exchange.getRequestMethod())) {
                try (InputStream inputStream = exchange.getRequestBody()) {
                    String body = new String(inputStream.readAllBytes());
                    JSONObject requestBody = new JSONObject(body);
                    String topic = requestBody.getString("topic");
                    topicManager.createTopic(topic);
                    sendResponse(exchange, 200, "Topic created successfully");
                } catch (Exception e) {
                    sendResponse(exchange, 400, "Invalid request body");
                }
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        });
    }

    private void publishMessageContext() {
        server.createContext("/publish", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                try (InputStream inputStream = exchange.getRequestBody()) {
                    String body = new String(inputStream.readAllBytes());
                    JSONObject requestBody = new JSONObject(body);
                    String topic = requestBody.getString("topic");
                    String message = requestBody.getString("message");
                    publish(topic, message);
                    sendResponse(exchange, 200, "Message published successfully");
                } catch (Exception e) {
                    sendResponse(exchange, 400, "Invalid request body");
                }
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        });
    }

    private void subscribeContext() {
        server.createContext("/subscribe", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                try (InputStream inputStream = exchange.getRequestBody()) {
                    String body = new String(inputStream.readAllBytes());
                    JSONObject requestBody = new JSONObject(body);
                    String topic = requestBody.getString("topic");
                    String consumerUrl = requestBody.getString("consumerUrl");
                    subscribe(topic, consumerUrl);
                    sendResponse(exchange, 200, "Subscription added successfully");
                } catch (Exception e) {
                    sendResponse(exchange, 400, "Invalid request body");
                }
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        });
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseMessage) throws IOException {
        byte[] responseBytes = responseMessage.getBytes();
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    public void publish(String topic, String message) {
        var subscribers = topicManager.getSubscribers(topic);
        if (subscribers.isEmpty()) {
            System.out.println("No subscribers for topic: " + topic);
        } else {
            for (String subscriber : subscribers) {
                System.out.println("Sending message to " + subscriber + ": " + message);
            }
        }
    }

    public void subscribe(String topic, String consumerUrl) {
        topicManager.addSubscriber(topic, consumerUrl);
        System.out.println("Added subscriber: " + consumerUrl + " to topic: " + topic);
    }

    public static void main(String[] args) throws IOException {
        new MessageBroker(8080).start();
    }
}
