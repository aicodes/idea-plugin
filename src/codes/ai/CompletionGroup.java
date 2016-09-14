package codes.ai;

import com.google.common.base.Joiner;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Code complete, even though calculated per method, is always invoked on a type/module etc.
 * CompletionGroup represents the grouped view of auto-completion. It is also used for bundling
 * requests into one.
 *
 * <p>This class is not thread-safe, it effectively act as ephemeral singleton.
 */
class CompletionGroup {
  private static CompletionGroup CURRENT_GROUP;

  private static final Joiner CONTEXT_JOIN = Joiner.on(':');
  private static final Joiner NAME_JOIN = Joiner.on('.');

  private Context context; // Context
  private String clazz; // Essentially Group Name
  private List<String> methods; // Essentially elements in the group.
  private MethodWeighCache cache;

  private CompletionGroup(Context context, String clazz, List<String> methods) {
    this.context = context;
    this.clazz = clazz;
    this.methods = methods;
    this.cache = new MethodWeighCache();
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

  public String getClazz() {
    return clazz;
  }

  /**
   * @Override public String toString() { if (context == null) { return NAME_JOIN.join(clazz,
   * method); } else { return CONTEXT_JOIN.join(context.getContextMethod(), NAME_JOIN.join(clazz,
   * method)); } }
   */
  public Context getContext() {
    return context;
  }

  public List<String> getMethods() {
    return methods;
  }

  public MethodWeighCache getCache() {
    return cache;
  }
}