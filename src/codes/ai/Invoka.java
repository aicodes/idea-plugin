package codes.ai;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class Invoka {
  private String clazz;
  private String method;
  private PsiMethod psiMethod;

  private Invoka(String clazz, String method) {
    this.clazz = clazz;
    this.method = method;
  }

  @Nullable
  static Invoka from(@NotNull PsiMethod method) {
    PsiClass psiClass = method.getContainingClass();
    if (psiClass != null) {
      return new Invoka(Utils.getJvmName(psiClass), method.getName());
    } else {
      throw new IllegalArgumentException("Method does not have class");
    }
  }

  public PsiMethod getPsiMethod() {
    return psiMethod;
  }
}
