package salesian.university.producer;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import salesian.university.helpers.HttpHelper;

import java.io.IOException;
import java.net.HttpURLConnection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ProducerTest {
    private String brokerUrl;

    @BeforeEach
    void setUp() {
        brokerUrl = "http://localhost:8080";
    }

    @Test
    void testPublishEvent_success() {
        HttpHelper mockHelper = new HttpHelper() {
            @Override
            public HttpURLConnection sendPostRequest(String url, JSONObject body) {
                assertEquals(brokerUrl + "/publish", url);
                assertEquals("test-topic", body.getString("topic"));
                assertEquals("test-message", body.getString("message"));

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

        Producer producerWithMockHelper = new Producer(brokerUrl, mockHelper);
        assertDoesNotThrow(() -> producerWithMockHelper.publishEvent("test-topic", "test-message"));
    }

    @Test
    void testPublishEvent_failure() {
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

        Producer producerWithMockHelper = new Producer(brokerUrl, mockHelper);
        assertThrows(RuntimeException.class,
                () -> producerWithMockHelper.publishEvent("test-topic", "test-message"));
    }

    @Test
    void testPublishEvent_throwsIOException() {
        HttpHelper mockHelper = new HttpHelper() {
            @Override
            public HttpURLConnection sendPostRequest(String url, JSONObject body) throws IOException {
                throw new IOException("Simulated IOException");
            }
        };

        Producer producerWithMockHelper = new Producer(brokerUrl, mockHelper);
        Exception exception = assertThrows(RuntimeException.class,
                () -> producerWithMockHelper.publishEvent("test-topic", "test-message"));
        assertEquals("Failed to publish event", exception.getMessage());
    }
}
