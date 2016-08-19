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

import com.google.gson.Gson;
import com.intellij.codeInsight.completion.CompletionLocation;
import com.intellij.codeInsight.completion.CompletionWeigher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MethodWeigher extends CompletionWeigher {
	private static final String EMPTY = "";
	private CloseableHttpClient httpClient;
	private Map<String, Double> cache;
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
			String query;
			query = getCononicalName(psiMethod);
			if (!cache.containsKey(query)) {
				String json = getResponseOrEmpty(query);
				ClassMethodProbability probability = gson.fromJson(json, ClassMethodProbability.class);
				for (MethodProbability p : probability.methods) {
					cache.put(probability.name + ":" + p.method, p.probability);
				}
			}
			return cache.getOrDefault(query, 0.0);
		}
		return 0.0; // default value for all elements.
	}

	@NotNull private String getResponseOrEmpty(String queryString) {
		HttpGet get = new HttpGet("http://localhost:26337?q=" + queryString); // codes
		try {
			CloseableHttpResponse response = httpClient.execute(get);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				java.util.Scanner s = new java.util.Scanner(
						entity.getContent()).useDelimiter("\\A");
				return s.hasNext() ? s.next() : EMPTY;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return EMPTY;
	}

	// TODO(exu): figure out how to handle distributions of methods in super-class.
	// May as well bump certain probablility baselines up and down for now
	// For instance lower the baseline for java.lang.Object.
	// OR figure out if I can get the right PsiClass here.
	private String getCononicalName(@NotNull PsiMethod method) {
		PsiClass psiClass = method.getContainingClass();
		String className = "java.lang.unknownClass";
		if (psiClass.getQualifiedName() != null) {
			className = psiClass.getQualifiedName();
		}
		className.
		return className + ":" + method.getName();
	}
}
