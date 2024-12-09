package salesian.university.consumer;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import salesian.university.helpers.HttpHelper;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class ConsumerTest {
    private Consumer consumer;
    private String brokerUrl;
    private int testPort;

    @BeforeEach
    void setUp() {
        try (ServerSocket socket = new ServerSocket(0)) {
            testPort = socket.getLocalPort();
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up test port", e);
        }

        brokerUrl = "http://localhost:" + testPort;
        consumer = new Consumer(brokerUrl, testPort);
    }

    @Test
    void testSubscribe_success() {
        HttpHelper mockHelper = new HttpHelper() {
            @Override
            public HttpURLConnection sendPostRequest(String url, JSONObject body) {
                assertEquals(brokerUrl + "/subscribe", url);
                assertEquals("test-topic", body.getString("topic"));
                assertEquals("http://localhost:" + testPort, body.getString("consumerUrl"));

                return new HttpURLConnection(null) {
                    @Override
                    public void connect() {}

                    @Override
                    public void disconnect() {}

                    @Override
                    public boolean usingProxy() {
                        return false;
                    }

                    @Override
                    public int getResponseCode() {
                        return 200;
                    }
                };
            }
        };

        Consumer consumerWithMockHelper = new Consumer(brokerUrl, testPort, mockHelper);
        assertDoesNotThrow(() -> consumerWithMockHelper.subscribe("test-topic"));
    }

    @Test
    void testSubscribe_failure() {
        HttpHelper mockHelper = new HttpHelper() {
            @Override
            public HttpURLConnection sendPostRequest(String url, JSONObject body) {
                return new HttpURLConnection(null) {
                    @Override
                    public void connect() {}

                    @Override
                    public void disconnect() {}

                    @Override
                    public boolean usingProxy() {
                        return false;
                    }

                    @Override
                    public int getResponseCode() {
                        return 400;
                    }
                };
            }
        };

        Consumer consumerWithMockHelper = new Consumer(brokerUrl, testPort, mockHelper);
        assertThrows(RuntimeException.class, () -> consumerWithMockHelper.subscribe("test-topic"));
    }

    @Test
    void testHandleEvent() {
        String testMessage = "Test message";
        assertDoesNotThrow(() -> consumer.handleEvent(testMessage));
    }

    @Test
    void testStart() {
        Thread serverThread = new Thread(() -> assertDoesNotThrow(consumer::start));
        serverThread.start();

        try {
            Thread.sleep(500);
            HttpURLConnection connection =
                    (HttpURLConnection) new URL("http://localhost:" + testPort + "/receive").openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write("Test message".getBytes());
            outputStream.flush();
            outputStream.close();

            int responseCode = connection.getResponseCode();
            assertEquals(200, responseCode);
        } catch (Exception e) {
            fail("Failed to send a message to the consumer: " + e.getMessage());
        } finally {
            serverThread.interrupt();
        }
    }
}
