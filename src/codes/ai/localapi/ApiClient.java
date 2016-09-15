package codes.ai.localapi;

import codes.ai.Context;
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
 * ApiClient is responsible for HTTP communications between IntelliJ and local ai.codes server.
 * It is a singleton.
 *
 * <p>For good user experience, local server returns results immediately. If results are not available
 * at local server, local server returns HTTP 202 (Accepted) and triggers an aync API call to Ai.codes
 * server.
 *
 * <p>Results of API requests is cached for efficiency reasons.
 */
public class ApiClient {
  private static final Logger log = Logger.getInstance(ApiClient.class);

  private Gson gson;
  private final ApiRequestGateway gateway;

  private static ApiClient INSTANCE = null;
  private static final String API_ENDPOINT = "http://localhost:26337"; // 26337 = CODES

  private static final int REQUEST_TIMEOUT_MILLS = 200;
  private static final String EMPTY_JSON = "{}";
  private static final int HTTP_ERROR = 1000;
  private static final int RADIO_SILENCE = 1001;
  private static final int JSON_ERROR = 1002;

  private static final NotificationGroup NOTIFICATION_GROUP =
      new NotificationGroup("Ai.codes Notification Group", NotificationDisplayType.BALLOON, true);

  // Singleton method
  public static ApiClient getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ApiClient();
    }
    return INSTANCE;
  }

  private ApiClient() {
    gson = new Gson();
    gateway = new ApiRequestGateway();
  }

  public double getMethodWeight(@NotNull PsiMethod method, @NotNull Context context) {
    CompletionGroup completionGroup = CompletionGroup.from(method, context);
    /// Skip empty completion groups.
    if (completionGroup == null) {
      return ApiRequestType.USAGE.getDefaultValue();
    }

    int status;
    if (completionGroup.getContext().hasContextMethod()) {
      String similarityKey = ApiRequestType.SIMILARITY.encodeRequest(method, context);
      if (completionGroup.hasWeight(similarityKey)) {
        return completionGroup.getWeight(similarityKey);
      }

      status = tryGetSimilarityFromAI(completionGroup);
      if (status == 200 && completionGroup.hasWeight(similarityKey)) {
        return completionGroup.getWeight(similarityKey);
      }
    } else {
      // independent of context, usually happens when we define fields.
      String usageKey = ApiRequestType.USAGE.encodeRequest(method, context);
      if (completionGroup.hasWeight(usageKey)) {
        return completionGroup.getWeight(usageKey);
      }

      status = tryGetUsageFromAI(completionGroup);
      if (status == 200 && completionGroup.hasWeight(usageKey)) {
        return completionGroup.getWeight(usageKey);
      }
    }
    return ApiRequestType.USAGE.getDefaultValue();
  }

  private int tryGetSimilarityFromAI(@NotNull CompletionGroup cg) {
    if (!gateway.shouldIssueRequest(ApiRequestType.SIMILARITY.toString() + cg.toString())) {
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
      cg.putWeights(ApiRequestType.SIMILARITY, result);
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
      group.putWeights(ApiRequestType.USAGE, result);
      return httpResult;
    }
    return 204; // don't have the result yet.
  }

  private int pokeLocalAiServer(String url, Context context, Map<String, Double> results) {
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
        ApiResponse json = parseJson(response);
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

  private ApiResponse parseJson(CloseableHttpResponse response) {
    ApiResponse json = new ApiResponse();
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
