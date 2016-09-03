/*
 * Copyright ai.codes
 */
package codes.ai;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.intellij.codeInsight.completion.CompletionLocation;
import com.intellij.codeInsight.completion.CompletionWeigher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
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

public class BasicMethodWeigher extends CompletionWeigher {
	private static final Logger log = Logger.getInstance(BasicMethodWeigher.class);
	private static final String CACHE_TTL_KEY = ".expiresIn";
	private static final double DEFAULT_WEIGHT = 0.0;
	private static final String EMPTY = "";
	private static final String NO_SUCH_CLASS = "";
	private static final String API_ENDPOINT = "http://localhost:26337";  // 26337 = CODES

	private static final NotificationGroup NOTIFICATION_GROUP =
			new NotificationGroup("Ai.codes Notification Group",
					NotificationDisplayType.BALLOON, true);

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
	private final OfflineGateway gateway;
	private CloseableHttpClient httpClient;
	private Gson gson;

	public BasicMethodWeigher() {
		httpClient = HttpClients.createDefault();
		gson = new Gson();
		gateway = new OfflineGateway();
	}

	@Override
	public Comparable weigh(@NotNull LookupElement element, @NotNull CompletionLocation location) {
		if (element.getPsiElement() instanceof PsiMethod) {     // only handle method sorting for now.
			PsiMethod psiMethod = (PsiMethod) element.getPsiElement();
			String className = getClassName(psiMethod);

			if (gateway.hasKey(className)) {
				return DEFAULT_WEIGHT;
			}
			log.info("API query string is " + className);

			Context context = Context.of(location);
			String json = getResponseOrEmpty(className, context);
			if (json.isEmpty()) {
				return DEFAULT_WEIGHT;
			}
			Map<String, Double> usage = new HashMap<>();
			try {
				usage = gson.fromJson(json, usage.getClass());
				if (usage == null) {
					return DEFAULT_WEIGHT;
				}
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
				return DEFAULT_WEIGHT;
			}
			if (usage.containsKey(CACHE_TTL_KEY)) {
				gateway.putKey(className,
						System.currentTimeMillis() + Math.round(1000 * usage.get(CACHE_TTL_KEY)));
				return DEFAULT_WEIGHT;
			}
			return usage.getOrDefault(psiMethod.getName(), DEFAULT_WEIGHT);
		}
		return DEFAULT_WEIGHT;
	}

	@NotNull
	private String getResponseOrEmpty(String className, Context context) {
		String url = Joiner.on('/').join(API_ENDPOINT, context.getId(), className);
		HttpGet get = new HttpGet(url);
		get.setConfig(RequestConfig.custom().setConnectTimeout(100).build());
		try {
			long startTime = System.nanoTime();
			CloseableHttpResponse response = httpClient.execute(get);
			long endTime = System.nanoTime();
			long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
			log.info("API request took " + Long.toString(duration / 1000000) + " ms");
			if (response.getStatusLine().getStatusCode() != 200) {
				return EMPTY;
			}
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				java.util.Scanner s = new java.util.Scanner(
						entity.getContent()).useDelimiter("\\A");
				return s.hasNext() ? s.next() : EMPTY;
			}
		} catch (HttpHostConnectException e) {
			gateway.setOffline(true);   // avoid repeated requests when cannot make HTTP connect.
			Notification notification = NOTIFICATION_GROUP.createNotification(
					"AI.codes Plugin Running in offline mode.", NotificationType.ERROR);
			Notifications.Bus.notify(notification, context.getProject());
			return EMPTY;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return EMPTY;
	}


	/**
	 * JVM uses $ between class and inner class, while IntelliJ PsiClass gives "." in between.
	 * This method converts the PsiClass class name to the JVM standard.
	 *
	 * @param psiClass the psi element that represents a class.
	 * @return
	 */
	@VisibleForTesting
	private String toJvmName(@NotNull PsiClass psiClass) {
		if (psiClass.getContainingClass() == null) {
			return psiClass.getQualifiedName();
		}
		else {
			return toJvmName(psiClass.getContainingClass()) + "$" + psiClass.getName();
		}
	}

	private String getClassName(@NotNull PsiMethod method) {
		PsiClass psiClass = method.getContainingClass();
		if (psiClass == null || psiClass.getQualifiedName() == null) {
			return NO_SUCH_CLASS;
		}
		return toJvmName(psiClass);
	}
}
