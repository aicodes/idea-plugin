/*
 * Copyright ai.codes
 */
package codes.ai;

import codes.ai.data.Snippet;
import codes.ai.localapi.ApiClient;
import com.intellij.codeInsight.completion.CompletionLocation;
import com.intellij.codeInsight.completion.CompletionWeigher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

public class MethodWeigher extends CompletionWeigher {
  private static final double DEFAULT_WEIGHT = 0.0;
  // IntelliJ sometimes assign weights (0/1) to lookup elements. In this case our probability-based system
  // would not dominate the ranking order. Scale the number by 10x to get proper rankings.
  private static final double WEIGHT_MULTIPLIER = 10.0;

  @Override
  public Comparable weigh(@NotNull LookupElement element, @NotNull CompletionLocation location) {
    if (element.getPsiElement() instanceof PsiMethod) { // only handle method sorting for now.
      Context context = Context.of(location);
      PsiMethod psiMethod = (PsiMethod) element.getPsiElement();
      double weight = ApiClient.getInstance().getMethodWeight(psiMethod, context) * WEIGHT_MULTIPLIER;
      return weight;
    }
    if (element.getObject() instanceof Snippet) {
      Snippet s = (Snippet) element.getObject();
      return 10 - s.rank; // first element get weight 9 and so on.
    }
    return DEFAULT_WEIGHT;
  }
}
