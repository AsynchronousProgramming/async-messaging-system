package salesian.university.consumer;

import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import salesian.university.helpers.HttpHelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;

/**
 * This class represents a consumer in the message broker system.
 * <p>
 * A consumer can subscribe to topics and receive messages
 * published to those topics via an HTTP server.
 */
public class Consumer {
    private final int port;
    private final String brokerUrl;
    private final String consumerUrl;
    private HttpHelper httpHelper;

    /**
     * A constructor to create a new consumer
     * with the specified broker URL and port.
     *
     * @param brokerUrl the URL of the message broker.
     * @param port      the port on which the consumer's server will listen.
     */
    public Consumer(String brokerUrl, int port) {
        this.brokerUrl = brokerUrl;
        this.port = port;
        this.consumerUrl = "http://localhost:" + port;
        this.httpHelper = new HttpHelper();
    }

    /**
     * A constructor to create a new consumer with
     * the specified broker URL, port, and HTTP helper.
     *
     * @param brokerUrl  the URL of the message broker.
     * @param port       the port on which the consumer's server will listen.
     * @param httpHelper the helper for handling HTTP requests.
     */
    public Consumer(String brokerUrl, int port, HttpHelper httpHelper){
        this(brokerUrl, port);
        this.httpHelper = httpHelper;
    }

    /**
     * This method subscribes the consumer to the specified topic
     * on the message broker.
     *
     * @param topic the topic to subscribe to.
     * @throws RuntimeException if the subscription fails.
     */
    public void subscribe(String topic) {
        try {
            JSONObject body = new JSONObject();
            body.put("topic", topic);
            body.put("consumerUrl", consumerUrl);
            HttpURLConnection connection = httpHelper.sendPostRequest(brokerUrl + "/subscribe", body);

            int responseCode = connection.getResponseCode();
            if(responseCode == 200) {
                System.out.println(consumerUrl + " consumer subscribed to topic: " + topic);
            } else {
                throw new RuntimeException("Failed to subscribe to topic");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to subscribe to topic", e);
        }
    }

    /**
     * This method starts the consumer's HTTP server to receive messages
     * from the message broker.
     *
     * @throws RuntimeException if the server fails to start.
     */
    public void start() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/receive", exchange -> {
                InputStream inputStream = exchange.getRequestBody();
                String message = new String(inputStream.readAllBytes());
                handleEvent(message);
                exchange.sendResponseHeaders(200, 0);
                exchange.close();
            });

            server.start();
            System.out.println("Consumer server started on port " + port);
        } catch (IOException e) {
            throw new RuntimeException("Failed to start consumer server", e);
        }
    }

    /**
     * This method handles an incoming event message.
     *
     * @param message the message received from the broker.
     */
    public void handleEvent(String message) {
        System.out.println("Received message: " + message + ", Consumer: " + consumerUrl);
    }

    /**
     * Main method to run the consumer application.
     *
     * @param args command-line arguments.
     * @throws InterruptedException if the thread is interrupted while sleeping.
     */
    public static void main(String[] args) throws InterruptedException {
        Consumer consumer1 = new Consumer("http://localhost:8080", 9002);
        new Thread(consumer1::start).start();

        Consumer consumer2 = new Consumer("http://localhost:8080", 9003);
        new Thread(consumer2::start).start();

        Consumer consumer3 = new Consumer("http://localhost:8080", 9004);
        new Thread(consumer3::start).start();

        Thread.sleep(3000);

        consumer1.subscribe("sports");
        consumer2.subscribe("sports");
        consumer2.subscribe("news");
        consumer3.subscribe("science");
    }
}
