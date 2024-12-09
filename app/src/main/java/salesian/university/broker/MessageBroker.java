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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageBroker {
    private final int port;
    private final HttpServer server;
    private final TopicManager topicManager;
    private final HttpHelper httpHelper;
    private final QueueManager queueManager;
    private final BackpressureHandler backpressureHandler;
    private final ExecutorService executorService;

    public MessageBroker(TopicManager topicManager, int port) throws IOException {
        this.topicManager = topicManager;
        this.port = port;
        this.httpHelper = new HttpHelper();
        this.queueManager = new QueueManager();
        this.backpressureHandler = new BackpressureHandler(queueManager);
        this.executorService = Executors.newCachedThreadPool();
        server = HttpServer.create(new InetSocketAddress(port), 0);

        new Thread(this::startDispatchingMessages).start();
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
        server.createContext("/createTopic", exchange -> handlePostRequest(exchange, body -> {
            String topic = body.getString("topic");
            topicManager.createTopic(topic);
            return "Topic " + topic + " created successfully";
        }));
    }

    private void publishMessageContext() {
        server.createContext("/publish", exchange -> handlePostRequest(exchange, body -> {
            String topic = body.getString("topic");
            String message = body.getString("message");
            publish(topic, message);
            return "Message published successfully";
        }));
    }

    private void subscribeContext() {
        server.createContext("/subscribe", exchange -> handlePostRequest(exchange, body -> {
            String topic = body.getString("topic");
            String consumerUrl = body.getString("consumerUrl");
            subscribe(topic, consumerUrl);
            return "Subscription added successfully";
        }));
    }

    private void handlePostRequest(HttpExchange exchange, IRequestHandler handler) throws IOException {
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
        backpressureHandler.checkLoadAsync(topic).thenAccept(isOverloaded -> {
            if (isOverloaded) {
                backpressureHandler.throttleAsync(topic).join();
            }
            queueManager.enqueueAsync(topic, message).thenRun(() ->
                    System.out.println("Message enqueued for topic: " + topic));
        });
    }

    public void subscribe(String topic, String consumerUrl) {
        topicManager.addSubscriber(topic, consumerUrl);
        System.out.println("Added subscriber: " + consumerUrl + " to topic: " + topic);
    }

    private void startDispatchingMessages() {
        while (true) {
            try {
                for (String topic : topicManager.getTopics()) {
                    queueManager.dequeueAsync(topic).thenAccept(optionalMessage -> {
                        optionalMessage.ifPresent(message -> {
                            List<String> subscribers = topicManager.getSubscribers(topic);
                            for (String subscriber : subscribers) {
                                executorService.submit(() -> sendMessageToSubscriber(subscriber, message));
                            }
                        });
                    }).join();
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Message dispatch thread interrupted");
                break;
            }
        }
    }

    private void sendMessageToSubscriber(String subscriber, String message) {
        try {
            JSONObject messageBody = new JSONObject();
            messageBody.put("message", message);
            HttpURLConnection response = httpHelper.sendPostRequest(subscriber + "/receive", messageBody);
            System.out.println("Sent message to subscriber: " + subscriber + " with status: " + response.getResponseCode());
        } catch (IOException e) {
            System.err.println("Failed to send message to subscriber: " + subscriber);
        }
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
