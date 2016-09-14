package codes.ai;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * This class is responsible for all communications between IntelliJ and local ai.codes server. It
 * is a singleton.
 *
 * <p>It is called Async because local AI server will immediately return something if it does not
 * have results available (HTTP 202 Accepted).
 *
 * <p>Results of API requests may be cached for efficiency reasons.
 */
class AsyncApiClient {
  private static final Logger log = Logger.getInstance(AsyncApiClient.class);

  private Gson gson;
  private final OfflineGateway gateway;

  private static AsyncApiClient INSTANCE = null;
  private static final String API_ENDPOINT = "http://localhost:26337"; // 26337 = CODES

  private static final int REQUEST_TIMEOUT_MILLS = 200;
  private static final String EMPTY_JSON = "{}";
  private static final int HTTP_ERROR = 1000;
  private static final int RADIO_SILENCE = 1001;
  private static final int JSON_ERROR = 1002;

  private static final NotificationGroup NOTIFICATION_GROUP =
      new NotificationGroup("Ai.codes Notification Group", NotificationDisplayType.BALLOON, true);

  // Singleton method
  static AsyncApiClient getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new AsyncApiClient();
    }
    return INSTANCE;
  }

  private AsyncApiClient() {
    gson = new Gson();
    gateway = new OfflineGateway();
  }

  public double getMethodWeight(@NotNull PsiMethod method, @NotNull Context context) {
    String similarityKey = MethodWeighCache.getCacheKey(RequestType.SIMILARITY, method, context);

    CompletionGroup cg = CompletionGroup.from(method, context);
    /// Skip empty completion groups.
    if (cg == null) {
      return RequestType.USAGE.getDefaultValue();
    }

    int status;
    if (cg.getContext().getContextMethod()
        == null) { // independent of context, usually happens when we define fields.
      String usageKey = MethodWeighCache.getCacheKey(RequestType.USAGE, method, context);
      if (cg.getCache().hasKey(usageKey)) {
        return cg.getCache().get(usageKey);
      }

      status = tryGetUsageFromAI(cg);
      if (status == 200 && cg.getCache().hasKey(usageKey)) {
        return cg.getCache().get(usageKey);
      }
    } else {
      if (cg.getCache().hasKey(similarityKey)) {
        return cg.getCache().get(similarityKey);
      }

      status = tryGetSimilarityFromAI(cg);
      if (status == 200 && cg.getCache().hasKey(similarityKey)) {
        return cg.getCache().get(similarityKey);
      }
    }
    return RequestType.USAGE.getDefaultValue();
  }

  private int tryGetSimilarityFromAI(@NotNull CompletionGroup cg) {
    if (!gateway.shouldIssueRequest(RequestType.SIMILARITY.toString() + cg.toString())) {
      return RADIO_SILENCE;
    }

    Context context = cg.getContext();

    // URL format: http://localhost:26337/similarity/<ice_id>/ClassName/context
    String url =
        Joiner.on('/')
            .join(
                API_ENDPOINT,
                "similarity",
                context.getId(),
                cg.getClazz(),
                context.getContextMethod());
    Map<String, Double> result = new HashMap<>();

    int httpResult = pokeLocalAiServer(url, context, result); // poke server
    if (httpResult == 200) {
      cg.getCache().put(RequestType.SIMILARITY, cg, result);
    }
    return httpResult;
  }

  private int tryGetUsageFromAI(@NotNull CompletionGroup group) {
    // URL format: http://localhost:26337/usage/<ice_id>/<class_name>
    Context context = group.getContext();
    String url = Joiner.on('/').join(API_ENDPOINT, "usage", group.getClazz());
    Map<String, Double> result = new HashMap<>();
    int httpResult = pokeLocalAiServer(url, context, result); // poke server
    if (httpResult == 200 && !result.isEmpty()) {
      group.getCache().put(RequestType.USAGE, group, result);
      return httpResult;
    }
    return 204; // don't have the result yet.
  }

  private int pokeLocalAiServer(String url, Context context, Map<String, Double> results) {
    System.out.println("Going to request local server on " + url);
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet get = new HttpGet(url);
    get.setConfig(
        RequestConfig.custom()
            .setConnectTimeout(REQUEST_TIMEOUT_MILLS)
            .setConnectionRequestTimeout(REQUEST_TIMEOUT_MILLS)
            .setSocketTimeout(REQUEST_TIMEOUT_MILLS)
            .build());
    CloseableHttpResponse response = null;
    try {
      response = httpClient.execute(get);
      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode == HttpStatus.SC_OK) {
        ResponseJson json = parseJson(response);
        results.putAll(json.getResponse());
        return json.getStatus();
      }
      return statusCode;
    } catch (HttpHostConnectException e) {
      gateway.setOffline(true); // avoid repeated requests when cannot make HTTP connect.
      Notification notification =
          NOTIFICATION_GROUP.createNotification(
              "AI.codes Plugin Running in offline mode.", NotificationType.ERROR);
      Notifications.Bus.notify(notification, context.getProject());
      return HTTP_ERROR;
    } catch (IOException e) {
      e.printStackTrace();
      return HTTP_ERROR;
    } finally { // super ugly because we use an older version of HTTP Clients
      if (response != null) {
        try {
          response.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private ResponseJson parseJson(CloseableHttpResponse response) {
    ResponseJson json = new ResponseJson();
    String jsonString = EMPTY_JSON;

    // Get JSON Object
    HttpEntity entity = response.getEntity();
    try {
      Scanner s = new Scanner(entity.getContent()).useDelimiter("\\A");
      if (s.hasNextLine()) {
        jsonString = s.next();
      }

    } catch (IOException e) {
      json.setStatus(JSON_ERROR);
      json.setMessage(e.toString());
    }

    // Parse JSON
    try {
      json = gson.fromJson(jsonString, json.getClass());
    } catch (JsonSyntaxException e) {
      json.setStatus(JSON_ERROR);
      json.setMessage(e.toString());
    }
    return json;
  }
}
