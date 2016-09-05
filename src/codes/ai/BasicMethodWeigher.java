/*
 * Copyright ai.codes
 */
package codes.ai;

import com.google.common.annotations.VisibleForTesting;
import com.intellij.codeInsight.completion.CompletionLocation;
import com.intellij.codeInsight.completion.CompletionWeigher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

public class BasicMethodWeigher extends CompletionWeigher {
//	private static final String CACHE_TTL_KEY = ".expiresIn";
	private static final double DEFAULT_WEIGHT = 1.0;


	@Override
	public Comparable weigh(@NotNull LookupElement element, @NotNull CompletionLocation location) {
		if (element.getPsiElement() instanceof PsiMethod) {     // only handle method sorting for now.
			Context context = Context.of(location);
			PsiMethod psiMethod = (PsiMethod) element.getPsiElement();
			return AsyncApiClient.getInstance().getMethodWeight(toJvmName(psiMethod), context);
		}
		return DEFAULT_WEIGHT;      // Do not weight anything else.
	}

	@VisibleForTesting
	private String toJvmName(@NotNull PsiMethod method) {
		PsiClass psiClass = method.getContainingClass();
		if (psiClass == null || psiClass.getQualifiedName() == null) {
			return method.getName();
		}
		else {
			return toJvmName(psiClass) + '.' + method.getName();
		}
	}

	/**
	 * JVM uses $ between class and its inner class names. For instance, Java class (java.util.)Map.Entry is
	 * an inner class of Map. It is denoted as java.util.Map$Entry in JVM.
	 *
	 * IntelliJ's PsiClass name follows Java convention, while our API follows JVM convention,
	 * thus the conversion.
	 *
	 * @param psiClass the psi element that represents a class.
	 * @return
	 */
	@VisibleForTesting
	private String toJvmName(@NotNull PsiClass psiClass) {
		if (psiClass.getContainingClass() == null) {
			return psiClass.getQualifiedName();
		}
		else {
			return toJvmName(psiClass.getContainingClass()) + "$" + psiClass.getName();
		}
	}
}
