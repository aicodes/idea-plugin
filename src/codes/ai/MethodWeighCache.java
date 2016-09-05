package codes.ai;

import com.google.common.base.Joiner;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xuy.
 *         Copyright (c) Ai.codes
 */
public class MethodWeighCache {
	private ConcurrentHashMap<String, Double> cache = new ConcurrentHashMap<>();
	private Joiner joiner = Joiner.on(':');

	public boolean hasMethodWeight(String methodName, Context context) {
		return cache.containsKey(
				getKey(methodName, context)
		);
	}

	public double getMethodWeight(String methodName, Context context) {
		return cache.get(
				getKey(methodName, context));
	}

	public void putMethodWeight(String methodName, Context context, double value) {
		cache.put(getKey(methodName, context), value);
	}

	private String getKey(String methodName, Context context) {
		return joiner.join(methodName, context.getMethodName());
	}
}
