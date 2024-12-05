package salesian.university.broker;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;

public class MessageBroker {
    private final int port;
    private final HttpServer server;
    private final TopicManager topicManager;

    public MessageBroker(TopicManager topicManager, int port) throws IOException {
        this.topicManager = topicManager;
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
    }

    public MessageBroker(int port) throws IOException {
        this(new TopicManager(), port);
    }

    public void start() {
        configureCreateTopicContext();
        configurePublishContext();
        configureSubscribeContext();
        server.start();
        System.out.printf("Message Broker started on port %d%n", port);
    }

    private void configureCreateTopicContext() {
        server.createContext("/createTopic", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream inputStream = exchange.getRequestBody();
                String topic = new String(inputStream.readAllBytes()).trim();
                createTopic(topic);
                exchange.sendResponseHeaders(200, 0);
            } else {
                exchange.sendResponseHeaders(405, 0);
            }
            exchange.close();
        });
    }

    private void configurePublishContext() {
        server.createContext("/publish", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream inputStream = exchange.getRequestBody();
                String body = new String(inputStream.readAllBytes());
                String[] parts = body.split(",", 2);
                publish(parts[0], parts[1]);
                exchange.sendResponseHeaders(200, 0);
            } else {
                exchange.sendResponseHeaders(405, 0);
            }
            exchange.close();
        });
    }

    private void configureSubscribeContext() {
        server.createContext("/subscribe", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream inputStream = exchange.getRequestBody();
                String body = new String(inputStream.readAllBytes());
                String[] parts = body.split(",", 2);
                subscribe(parts[0], parts[1]);
                exchange.sendResponseHeaders(200, 0);
            } else {
                exchange.sendResponseHeaders(405, 0);
            }
            exchange.close();
        });
    }

    public void createTopic(String topic) {
        topicManager.createTopic(topic);
        System.out.println("Created topic: " + topic);
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
