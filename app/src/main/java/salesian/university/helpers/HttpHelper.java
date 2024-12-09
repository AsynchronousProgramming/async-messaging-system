package salesian.university.helpers;

import org.json.JSONObject;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * A utility class to handle HTTP POST requests.
 */
public class HttpHelper {

    /**
     * This method sends an HTTP POST request to the specified URL
     * with the given JSON body.
     *
     * @param baseUrl the URL to send the POST request to.
     * @param body    the JSON object to be sent as the request body.
     * @return the HTTP connection object for handling the response.
     * @throws IOException if an I/O error occurs during the request.
     */
    public HttpURLConnection sendPostRequest(String baseUrl, JSONObject body) throws IOException {
        URL url = new URL(baseUrl);
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
}
