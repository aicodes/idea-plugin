/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package codes.ai;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.intellij.codeInsight.completion.CompletionLocation;
import com.intellij.codeInsight.completion.CompletionWeigher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MethodWeigher extends CompletionWeigher {
	private static final Logger log = Logger.getInstance(MethodWeigher.class);
	private static final String EMPTY = "";
	private static final String NO_SUCH_CLASS = "";
	private CloseableHttpClient httpClient;
	private Map<String, Map<String, Double>> cache;
	private Gson gson;

	public MethodWeigher() {
		httpClient = HttpClients.createDefault();
		gson = new Gson();
		cache = new HashMap<>();
	}

	@Override
	public Comparable weigh(@NotNull LookupElement element, @NotNull CompletionLocation location) {
		// TODO(exu): for now, skip some elements.
		if (element.getPsiElement() instanceof PsiMethod) {
			PsiMethod psiMethod = (PsiMethod) element.getPsiElement();
			String query = getQueryString(psiMethod);
			log.info("Class to query is " + query);
			if (!cache.containsKey(query)) {
				log.info("API query string is " + query);
				String json = getResponseOrEmpty(query);
				Map<String, Double> usage = new HashMap<>();
				usage = gson.fromJson(json, usage.getClass());
				cache.put(query, usage);
			}
			return cache.get(query).getOrDefault(psiMethod.getName(), 0.0);
		}
		return 0.0; // default value for all elements.
	}

	@NotNull private String getResponseOrEmpty(String queryString) {
		HttpGet get = new HttpGet("http://api.ai.codes/jvm/usage/" + queryString); // 26337=codes
		get.setConfig(RequestConfig.custom().setConnectTimeout(100).build());
		try {
			long startTime = System.nanoTime();
			CloseableHttpResponse response = httpClient.execute(get);
			long endTime = System.nanoTime();
			long duration = (endTime - startTime);  //divide by 1000000 to get milliseconds.
			log.info("API request took " + Long.toString(duration / 1000000) + " ms");

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				java.util.Scanner s = new java.util.Scanner(
						entity.getContent()).useDelimiter("\\A");
				return s.hasNext() ? s.next() : EMPTY;
			}
		} catch (IOException e) {
			log.warn("caught on exception in API requests ", e);
		}
		return EMPTY;
	}

	@VisibleForTesting
	public String getJvmStringForClass(@NotNull PsiClass psiClass) {
		if (psiClass.getContainingClass() == null) {
			return psiClass.getQualifiedName();
		} else {
			return getJvmStringForClass(psiClass.getContainingClass()) + "$" + psiClass.getName();
		}
	}

	public String getQueryString(@NotNull PsiMethod method) {
		PsiClass psiClass = method.getContainingClass();
		if (psiClass == null || psiClass.getQualifiedName() == null) {
			return NO_SUCH_CLASS;
		}
		return getJvmStringForClass(psiClass);
	}

	// TODO(exu): PsiMethod some times is invoked on base class.
	//              this will bias the probability because base class has different
	//              method distribution. The weigher itself does not know if it is from the same call.
	// For instance lower the baseline for java.lang.Object.
	// OR figure out if I can get the right PsiClass here.
	private String getCononicalName(@NotNull PsiMethod method) {
		PsiClass psiClass = method.getContainingClass();
		String className = "java.lang.unknownClass";
		if (psiClass.getQualifiedName() != null) {
			className = psiClass.getQualifiedName();
		}
		return className + ":" + method.getName();
	}
}
