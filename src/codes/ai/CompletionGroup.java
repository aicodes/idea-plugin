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
 * CompletitionGroup abstracts the group of candidates.
 */
class CompletitionGroup {

  private static final Joiner CONTEXT_JOIN = Joiner.on(':');
  private static final Joiner NAME_JOIN = Joiner.on('.');

  private Context context;

  private String clazz;
  private List<String> methods;

  private CompletitionGroup(String clazz, List<String> methods, Context context) {
    this.clazz = clazz;
    this.methods = methods;
    this.context = context;
  }

  @Nullable
  static CompletitionGroup from(@NotNull PsiMethod method) {
    return from(method, null);
  }

  @Nullable
  static CompletitionGroup from(@NotNull PsiMethod method, @Nullable Context context) {
    PsiClass psiClass = method.getContainingClass();
    if (psiClass != null) {
      PsiMethod[] methods = psiClass.getMethods();
      List<String> methodNames = new ArrayList<>(methods.length);
      for (PsiMethod method1 : methods) {
        methodNames.add(method1.getName());
      }
      return new CompletitionGroup(toJvmString(psiClass), methodNames, context);
    }
    return null;
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
}
