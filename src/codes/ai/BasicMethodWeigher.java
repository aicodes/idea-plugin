/*
 * Copyright ai.codes
 */
package codes.ai;

import com.intellij.codeInsight.completion.CompletionLocation;
import com.intellij.codeInsight.completion.CompletionWeigher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

public class BasicMethodWeigher extends CompletionWeigher {
  //	private static final String CACHE_TTL_KEY = ".expiresIn";
  private static final double DEFAULT_WEIGHT = 0.0;

  @Override
  public Comparable weigh(@NotNull LookupElement element, @NotNull CompletionLocation location) {
    if (element.getPsiElement() instanceof PsiMethod) { // only handle method sorting for now.
      Context context = Context.of(location);
      PsiMethod psiMethod = (PsiMethod) element.getPsiElement();
      double weight = AsyncApiClient.getInstance().getMethodWeight(psiMethod, context) * 10.0;
      return weight;
    }
    return DEFAULT_WEIGHT; // Do not weight anything else.
  }
}
