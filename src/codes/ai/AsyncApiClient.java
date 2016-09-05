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
 * This class is responsible for all communications between IntelliJ and local ai.codes server.
 * It is a singleton.
 *
 * Results of API requests may be cached for efficiency reasons.
 */
class AsyncApiClient {
	private static final Logger log = Logger.getInstance(AsyncApiClient.class);
	/**
	 * Map ephemeralCache is a time-based cache where the TTL of entries is one second.
	 * The purpose of it is to avoid sending repeated requests to local server when local
	 * server does not have the desired entry yet.
	 * <p>
	 * Before we issue requests to local server, we check if the key is in ephemeralCache.
	 * If it has an entry that has not expired yet, we return directly. If we encounter an
	 * expired entry, we issue request again to local server.
	 * <p>
	 * When we issue requests to local server. If local server gives an ".expire: 1" field
	 * in the result JSON, it means local server does not have the entry either. As a result,
	 * we put an entry in ephemeralCache.
	 */

	private CloseableHttpClient httpClient;
	private Gson gson;

//	private MethodWeighCache cache;  // not used for now.
	private final OfflineGateway gateway; // not used for now

	private static AsyncApiClient INSTANCE = null;
	private static final String API_ENDPOINT = "http://localhost:26337";  // 26337 = CODES
	private static final double DEFAULT_WEIGHT = 0.01;
	private static final int REQUEST_TIMEOUT_MILLS = 200;
	private static final String EMPTY_JSON = "{}";

	private static final NotificationGroup NOTIFICATION_GROUP =
			new NotificationGroup("Ai.codes Notification Group",
					NotificationDisplayType.BALLOON, true);

	// Singleton method
	static AsyncApiClient getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new AsyncApiClient();
		}
		return INSTANCE;
	}

	private AsyncApiClient() {
		httpClient = HttpClients.createDefault();
		gson = new Gson();
		gateway = new OfflineGateway();
//		cache = new MethodWeighCache();
	}


	// Cache is on methodName + context.methodName
	double getMethodWeight(@NotNull String methodName, @NotNull Context context) {
		return getMethodUsageProbability(methodName, context);
	}

	private double getMethodUsageProbability(@NotNull String methodName, @NotNull Context context) {
		return getWeightFromAI(methodName, context);
	}

	private double getWeightFromResponse(CloseableHttpResponse response) {
		String json = EMPTY_JSON;
		// Get JSON payload.
		HttpEntity entity = response.getEntity();
		try {
			Scanner s = new Scanner(entity.getContent()).useDelimiter("\\A");
			if (s.hasNextLine()) {
				json = s.next();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Get weight from JSON payload.
		Map<String, Double> weights = new HashMap<>();
		try {
			weights = gson.fromJson(json, weights.getClass());
			if (weights == null) {
				return DEFAULT_WEIGHT;
			}
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			return DEFAULT_WEIGHT;
		}
		return weights.getOrDefault("weight", DEFAULT_WEIGHT);
	}

	private double getWeightFromAI(@NotNull String methodName, @NotNull Context context) {
		// URL format: http://localhost:26337/<ice_id>/<outer_method_name>/<method_name>
		String url = Joiner.on('/').join(API_ENDPOINT, context.getId(), context.getMethodName(), methodName);
		log.info("Request url: " + url);
		HttpGet get = new HttpGet(url);
		get.setConfig(RequestConfig.custom().setConnectTimeout(REQUEST_TIMEOUT_MILLS).build());
		try {
			CloseableHttpResponse response = httpClient.execute(get);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				return getWeightFromResponse(response);
			} else if (statusCode == HttpStatus.SC_ACCEPTED) {
				// Local server does not have this information yet.
				return DEFAULT_WEIGHT;
			}
		} catch (HttpHostConnectException e) {
			gateway.setOffline(true);   // avoid repeated requests when cannot make HTTP connect.
			Notification notification = NOTIFICATION_GROUP.createNotification(
					"AI.codes Plugin Running in offline mode.", NotificationType.ERROR);
			Notifications.Bus.notify(notification, context.getProject());
			return DEFAULT_WEIGHT;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return DEFAULT_WEIGHT;
	}
}
