package codes.ai.snippet;

/**
 * @author xuy.
 * Copyright (c) Ai.codes
 */

import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Chars;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/** Gets intention lines from editor */
public class IntentionExtractor {
  private static IntentionExtractor INSTANCE = null;
  private static final String INTENTION_PREFIX = "///";
  private static final int INTENTION_PREFIX_LENGTH = 3;
  
  public static IntentionExtractor getINSTANCE() {
    return INSTANCE;
  }
  
  protected IntentionExtractor() {}
  
  public static IntentionExtractor getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new IntentionExtractor();
    }
    return INSTANCE;
  }
  
  /** Extracts intention from editor (if any) */
  @Nullable public Intention getIntention(Project project, Editor editor) {
    PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
    if (psiFile != null && psiFile instanceof PsiJavaFile) {
      final int offset = editor.getCaretModel().getOffset();
      Intention intention = tryGetExplicitIntention(psiFile, offset);
      if (intention != null){
        return intention;
      }
      intention = tryGetImplicitIntention(editor, offset);
      if (intention != null) {
        return intention;
      }
    }
    return null;
  }
  
  private Intention tryGetExplicitIntention(PsiFile psiFile, int offset) {
    PsiElement currentPosition = psiFile.findElementAt(offset);
    PsiElement comment = PsiTreeUtil.skipSiblingsBackward(currentPosition, PsiWhiteSpace.class);
    if (comment != null && comment.getText().startsWith(INTENTION_PREFIX)) {
      return new Intention(comment.getText().substring(INTENTION_PREFIX_LENGTH).trim(), Label.EXPLICIT);
    }
    return null;
  }
  
  /**
   * ML classifier that decides a line is regular code or intention.
   * TODO: naive Bayesian classifier
   */
  private Intention tryGetImplicitIntention(Editor editor, int offset) {
    /// Try to capture the previous line as text.
    Document document = editor.getDocument();
    int lineNumber = document.getLineNumber(offset);
    if (lineNumber > 0) {
      lineNumber -= 1;
      TextRange previousLineRange = new TextRange(
          document.getLineStartOffset(lineNumber),
          document.getLineEndOffset(lineNumber));
      String text = document.getText(previousLineRange).trim();
      if (text.isEmpty()) return null;
      
      // A poor man's classifier for prototyping, so ad hoc features.
      Set<Character> chars = new HashSet<>(Chars.asList(text.toCharArray()));
      Set<Character> specialChars = ImmutableSet.of(
          '{', '}', '[', ']', '(', ')', '=', ';');
      chars.retainAll(specialChars);
      if (chars.isEmpty()) {  // no special characters, likely to be natural language.
        return new Intention(text, Label.IMPLICIT);
      }
    }
    return null;
  }
}
