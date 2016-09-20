package codes.ai.intention;

import codes.ai.localapi.ApiClient;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author xuy.
 *         Copyright (c) Ai.codes
 */
public class SnippetCompletion extends CompletionContributor {
	public SnippetCompletion() {
		extend(CompletionType.BASIC,
				/* After comment, skipping comments in between */
				PlatformPatterns.psiElement().afterLeafSkipping(
						PlatformPatterns.psiElement(PsiWhiteSpace.class),
						PlatformPatterns.psiComment()).withLanguage(JavaLanguage.INSTANCE),
				new CompletionProvider<CompletionParameters>() {
					public void addCompletions(@NotNull CompletionParameters parameters,
					                           ProcessingContext context,
					                           @NotNull CompletionResultSet resultSet) {
						// Get the intention line.
						PsiElement comment = PsiTreeUtil.skipSiblingsBackward(parameters.getOriginalPosition(), PsiWhiteSpace.class);
						if (comment == null || !comment.getText().startsWith("///")) {
							return;
						}
						String intention = comment.getText().substring(3).trim();
						// Issue query to API.
						String snippet = ApiClient.getInstance().getSnippet(intention);
						resultSet.addElement(LookupElementBuilder.create(snippet));
					}
				}
		);

	}

	public void testMethod() {
		/// This is an intention.

	}
}