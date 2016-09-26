package codes.ai.intention;

import codes.ai.data.Snippet;
import codes.ai.localapi.ApiClient;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/** @author xuy. Copyright (c) Ai.codes */
public class SnippetCompletion extends CompletionContributor {
  public SnippetCompletion() {
    extend(
        CompletionType.BASIC,
        /* After comment, skipping comments in between */
        PlatformPatterns.psiElement()
            .afterLeafSkipping(
                PlatformPatterns.psiElement(PsiWhiteSpace.class), PlatformPatterns.psiComment())
            .withLanguage(JavaLanguage.INSTANCE),
        new CompletionProvider<CompletionParameters>() {
          public void addCompletions(
              @NotNull CompletionParameters parameters,
              ProcessingContext context,
              @NotNull CompletionResultSet resultSet) {
            // Get the intention line, which is a comment starting with three slashes.
            PsiElement comment =
                PsiTreeUtil.skipSiblingsBackward(
                    parameters.getOriginalPosition(), PsiWhiteSpace.class);
            if (comment == null || !comment.getText().startsWith("///")) {
              return;
            }
            String intention = comment.getText().substring(3).trim();
            // Issue query to API.
            List<Snippet> candidates = new ArrayList<>();
            ApiClient.getInstance().getSnippets(intention, candidates);
            for (Snippet candidate : candidates) {
              resultSet.addElement(
                  LookupElementBuilder.create(candidate, candidate.code)
                      .withIcon(PlatformIcons.JAVA_OUTSIDE_SOURCE_ICON)
                      .withInsertHandler(SnippetInsertHandler.INSTANCE));
            }
            System.out.println("Number of snippets:");
            System.out.println(candidates.size());
          }
        });
  }
}
