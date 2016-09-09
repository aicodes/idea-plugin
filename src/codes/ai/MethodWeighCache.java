package codes.ai;

import com.google.common.base.Joiner;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Unified cache for all weights.
// Both usage and similarity are stored here.
class MethodWeighCache {
  private ConcurrentHashMap<String, Double> cache = new ConcurrentHashMap<>();
  private static final Joiner joiner = Joiner.on('.');

  public boolean hasKey(String key) {
    return cache.containsKey(key);
  }

  public double get(String key) {
    return cache.get(key);
  }

  public void put(RequestType type, CompletionGroup group, Map<String, Double> weights) {
    // Use completion group as source of truth, iterate through it.
    // If we cannot find entries in response, pad it with a default value.
    for (String method : group.getMethods()) {
      String cacheKey =
          joiner.join(
              type.getName(), group.getContext().getContextMethod(), group.getClazz(), method);

      if (weights.containsKey(method)) {
        cache.put(cacheKey, weights.get(method));
      } else {
        cache.put(cacheKey, type.getDefaultValue());
      }
    }
  }

  static String getCacheKey(RequestType type, PsiMethod method, @Nullable Context context) {
    if (context == null) {
      return joiner.join(type.getName(), Utils.getJvmName(method));
    }
    return joiner.join(type.getName(), context.getContextMethod(), Utils.getJvmName(method));
  }
}
