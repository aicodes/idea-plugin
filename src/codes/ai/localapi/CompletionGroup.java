package codes.ai.localapi;

import codes.ai.Context;
import com.google.common.base.Joiner;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Code-completion is always invoked on a type or module. Hence, CompletionGroup represents the
 * grouped view of auto-completion. Candidates are retrieved for a CompletionGroup. At a given time,
 * there is only one active CompletionGroup (e.g. CURRENT_GROUP).
 *
 * <p>This class is not thread-safe, it effectively act as ephemeral singleton.
 */
class CompletionGroup {
  private static CompletionGroup CURRENT_GROUP;

  private Context context; // Context
  private String clazz; // Essentially Group Name
  private List<String> methods; // Essentially elements in the group.
  private ConcurrentHashMap<String, Double> cache = new ConcurrentHashMap<>();
  private static final Joiner joiner = Joiner.on('.');

  private CompletionGroup(Context context, String clazz, List<String> methods) {
    this.context = context;
    this.clazz = clazz;
    this.methods = methods;
    this.cache = new ConcurrentHashMap<>();
  }

  static CompletionGroup from(@NotNull PsiMethod method, @NotNull Context context) {
    PsiClass psiClass = method.getContainingClass();
    if (psiClass == null) {
      // IntelliJ seems to construct some strange PsiMethods on the fly.
      // When it does, it does not have a corresponding PsiClass.
      return null;
    }

    if (CURRENT_GROUP != null
        && context.equals(CURRENT_GROUP.context)
        && Utils.getJvmName(psiClass).equals(CURRENT_GROUP.clazz)) {
      return CURRENT_GROUP;
    }
    PsiMethod[] methods = psiClass.getMethods();
    List<String> methodNames = new ArrayList<>(methods.length);
    for (PsiMethod method1 : methods) {
      methodNames.add(method1.getName());
    }
    CURRENT_GROUP = new CompletionGroup(context, Utils.getJvmName(psiClass), methodNames);
    return CURRENT_GROUP;
  }

  @Override
  public String toString() {
    return context.getContextMethod() + ":" + clazz;
  }

  String getClazz() {
    return clazz;
  }

  Context getContext() {
    return context;
  }

  boolean hasWeight(String key) {
    return this.cache.containsKey(key);
  }

  void putWeights(ApiRequestType type, Map<String, Double> weights) {
    // Use completion group as source of truth, iterate through it.
    // If we cannot find entries in response, pad it with a default value.
    for (String method : methods) {
      String cacheKey = joiner.join(type.getName(), context.getContextMethod(), clazz, method);

      if (weights.containsKey(method)) {
        cache.put(cacheKey, weights.get(method));
      } else {
        cache.put(cacheKey, type.getDefaultValue());
      }
    }
  }

  double getWeight(String key) {
    return this.cache.get(key);
  }
}
