package salesian.university.consumer;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;
import salesian.university.helpers.HttpHelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;

public class Consumer {
    private final int port;
    private final String brokerUrl;
    private final String consumerUrl;
    private HttpHelper httpHelper;

    public Consumer(String brokerUrl, int port) {
        this.brokerUrl = brokerUrl;
        this.port = port;
        this.consumerUrl = "http://localhost:" + port;
        this.httpHelper = new HttpHelper();
    }

    public Consumer(String brokerUrl, int port, HttpHelper httpHelper){
        this(brokerUrl, port);
        this.httpHelper = httpHelper;
    }

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

    public void handleEvent(String message) {
        System.out.println("Received message: " + message + ", Consumer: " + consumerUrl);
    }

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

