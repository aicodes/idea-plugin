package codes.ai.localapi;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** @author xuy. Copyright (c) Ai.codes */
class Utils {
  private static final String UNKNOWN_CLASS = "UnknownClass";

  /**
   * JVM uses $ between class and its inner class names. For instance, Java class
   * (java.util.)Map.Entry is an inner class of Map. It is denoted as java.util.Map$Entry in JVM.
   *
   * <p>IntelliJ's PsiClass name follows Java convention, while our API follows JVM convention, thus
   * the conversion.
   *
   * @param psiMethod the psi element that represents a class.
   * @return
   */
  @VisibleForTesting
  static String getJvmName(@NotNull PsiMethod psiMethod) {
    return getJvmName(psiMethod.getContainingClass()) + "." + psiMethod.getName();
  }

  static String getJvmName(@Nullable PsiClass psiClass) {
    if (psiClass == null) {
      return UNKNOWN_CLASS;
    }
    if (psiClass.getContainingClass() == null) {
      return psiClass.getQualifiedName();
    } else {
      return getJvmName(psiClass.getContainingClass()) + "$" + psiClass.getName();
    }
  }
}
