package salesian.university.broker;

import org.json.JSONObject;

/**
 * A functional interface for handling HTTP requests with a JSON body.
 */
@FunctionalInterface
public interface IRequestHandler {

  /**
   * This method processes an HTTP request body and returns a response.
   *
   * @param body the JSON object representing the request body.
   * @return the response as a string.
   * @throws Exception if an error occurs during processing.
   */
  String handle(JSONObject body) throws Exception;
}
