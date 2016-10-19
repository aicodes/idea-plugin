package codes.ai.localapi;

import codes.ai.java.pojo.ResultSnippet;
import codes.ai.ep.AiPluginComponent;
import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiMethod;
import org.apache.http.client.fluent.Request;
import org.apache.http.conn.HttpHostConnectException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

  private static final int ASYNC_REQUEST_TIMEOUT_MILLS = 200;
  private static final int SYNC_REQUEST_TIMEOUT_MILLS = 2000;
  
  private static final int HTTP_ERROR = 1000;
  private static final int RADIO_SILENCE = 1001;

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
    gateway = AiPluginComponent.getInstance().getGateway();
  }

  /// Synchronized request for now.
  public boolean getSnippets(@NotNull String intention, List<ResultSnippet> candidates) {
    String responseJson;
    try {
      responseJson = Request.Get(API_ENDPOINT + "/snippet/" + URLEncoder.encode(intention, "UTF-8"))
          .socketTimeout(SYNC_REQUEST_TIMEOUT_MILLS)
          .execute().returnContent().asString();
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
    ApiResponse apiResponse = gson.fromJson(responseJson, ApiResponse.class);
    System.out.println("Snippets from response has " + String.valueOf(apiResponse.getSnippets().size()) + " entries" );
    candidates.addAll(apiResponse.getSnippets());
    return true;
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
    if (context.getContextMethod() == null) {
      return 404; // empty method.
    }
    if (cg.getClazz() == null) {
      return 404; // no type (can be primitive)
    }
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
    try {
      String responseJson = Request.Get(url)
          .connectTimeout(ASYNC_REQUEST_TIMEOUT_MILLS)
          .socketTimeout(ASYNC_REQUEST_TIMEOUT_MILLS)
          .execute().returnContent().asString();
      ApiResponse response = gson.fromJson(responseJson, ApiResponse.class);
      results.putAll(response.getWeights());
      return response.getStatus();
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
    }
  }
}
