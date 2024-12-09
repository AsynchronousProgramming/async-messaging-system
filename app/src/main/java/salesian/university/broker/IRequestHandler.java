package salesian.university.broker;

import org.json.JSONObject;

@FunctionalInterface
public interface IRequestHandler {
  String handle(JSONObject body) throws Exception;
}
