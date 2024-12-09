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

/**
 * MessageBroker is a custom implementation of a message broker
 * that facilitates publishing, subscribing, and dispatching messages
 * between producers and consumers based on topics.
 * <p>
 * It supports backpressure handling, asynchronous operations, and a
 * modular structure for topic management and queue handling.
 */
public class MessageBroker {
    private final int port;
    private final HttpServer server;
    private final TopicManager topicManager;
    private final HttpHelper httpHelper;
    private final QueueManager queueManager;
    private final BackpressureHandler backpressureHandler;
    private final ExecutorService executorService;

    /**
     * A constructor to initialize the MessageBroker with a specified
     * TopicManager and port.
     *
     * @param topicManager The TopicManager instance for managing topics
     *                     and subscriptions.
     * @param port         The port on which the broker server will run.
     * @throws IOException If an error occurs while starting the server.
     */
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

    /**
     * A constructor to initialize the MessageBroker with
     * a default TopicManager and specified port.
     *
     * @param port The port on which the broker server will run.
     * @throws IOException If an error occurs while starting the server.
     */
    public MessageBroker(int port) throws IOException {
        this(new TopicManager(), port);
    }

    /**
     * This method starts the HTTP server for the message broker
     * and sets up contexts for handling topic creation,
     * publishing messages, and subscribing consumers.
     */
    public void start() {
        createTopicContext();
        publishMessageContext();
        subscribeContext();

        server.start();
        System.out.printf("Message Broker started on port %d%n", port);
    }

    /**
     * This method creates the HTTP context for handling
     * topic creation requests.
     * Listens on the endpoint `/createTopic`.
     */
    private void createTopicContext() {
        server.createContext("/createTopic", exchange -> handlePostRequest(exchange, body -> {
            String topic = body.getString("topic");
            topicManager.createTopic(topic);
            return "Topic " + topic + " created successfully";
        }));
    }

    /**
     * This method creates the HTTP context for handling
     * message publishing requests.
     * Listens on the endpoint `/publish`.
     */
    private void publishMessageContext() {
        server.createContext("/publish", exchange -> handlePostRequest(exchange, body -> {
            String topic = body.getString("topic");
            String message = body.getString("message");
            publish(topic, message);
            return "Message published successfully";
        }));
    }

    /**
     * This method creates the HTTP context for handling
     * subscription requests.
     * Listens on the endpoint `/subscribe`.
     */
    private void subscribeContext() {
        server.createContext("/subscribe", exchange -> handlePostRequest(exchange, body -> {
            String topic = body.getString("topic");
            String consumerUrl = body.getString("consumerUrl");
            subscribe(topic, consumerUrl);
            return "Subscription added successfully";
        }));
    }

    /**
     * This method handles HTTP POST requests for specific
     * contexts and processes the request body using
     * the provided handler.
     *
     * @param exchange The HttpExchange object representing
     *                 the HTTP request and response.
     * @param handler  A functional interface to process the request body.
     * @throws IOException If an I/O error occurs while handling the request.
     */
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

    /**
     * This method sends a response to the HTTP client with
     * a specified status code and message.
     *
     * @param exchange         The HttpExchange object representing
     *                         the HTTP response.
     * @param statusCode       The HTTP status code to be sent.
     * @param responseMessage  The message to be included in the response body.
     * @throws IOException If an I/O error occurs while sending the response.
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String responseMessage) throws IOException {
        byte[] responseBytes = responseMessage.getBytes();
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    /**
     * This method publishes a message to a specified topic asynchronously.
     * If the load on the topic exceeds a threshold,
     * the backpressure handler throttles publishing.
     *
     * @param topic   The name of the topic to publish the message to.
     * @param message The message to be published.
     */
    public void publish(String topic, String message) {
        backpressureHandler.checkLoadAsync(topic).thenAccept(isOverloaded -> {
            if (isOverloaded) {
                backpressureHandler.throttleAsync(topic).join();
            }
            queueManager.enqueueAsync(topic, message).thenRun(() ->
                    System.out.println("Message enqueued for topic: " + topic));
        });
    }

    /**
     * This method subscribes a consumer to a specified topic
     * by adding the consumer's URL to the list of subscribers
     * for the topic.
     *
     * @param topic       The name of the topic to subscribe to.
     * @param consumerUrl The URL of the consumer to be subscribed.
     */
    public void subscribe(String topic, String consumerUrl) {
        topicManager.addSubscriber(topic, consumerUrl);
        System.out.println("Added subscriber: " + consumerUrl + " to topic: " + topic);
    }

    /**
     * This method starts dispatching messages from queues
     * to subscribers asynchronously.
     * Runs in a continuous loop and processes messages for each topic.
     */
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

    /**
     * This method sends a message to a specific subscriber
     * by making an HTTP POST request.
     *
     * @param subscriber The URL of the subscriber to send the message to.
     * @param message    The message to be sent to the subscriber.
     */
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

    /**
     * The main entry point for running the MessageBroker.
     * Initializes the broker, creates default topics, and starts the server.
     *
     * @param args Command-line arguments (not used).
     * @throws IOException          If an error occurs while initializing the broker.
     * @throws InterruptedException If the thread is interrupted while sleeping.
     */
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
