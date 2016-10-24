package codes.ai.ep;

/**
 * @author xuy.
 * Copyright (c) Ai.codes
 */

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.codeInsight.editorActions.smartEnter.JavaSmartEnterProcessor;
import com.intellij.codeInsight.editorActions.smartEnter.SmartEnterProcessor;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actions.EditorActionUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

// Not used for now, as it is only triggered by typing `normal` characters to editors.
public class AiCompletionTypedHandler extends TypedHandlerDelegate {
  @Override
  public Result charTyped(char c, Project project, @NotNull Editor editor, @NotNull PsiFile file) {
    if (file.getLanguage() == JavaLanguage.INSTANCE) {
      AutoPopupController.getInstance(project).scheduleAutoPopup(editor);
    }
    return super.charTyped(c, project, editor, file);
  }
}


class AiEnterProcessorDelegate extends SmartEnterProcessor {
  private SmartEnterProcessor smartEnterProcessor;
  
  public AiEnterProcessorDelegate() {
    smartEnterProcessor = new JavaSmartEnterProcessor();
  }
  
  @Override
  public boolean process(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile psiFile) {
    return smartEnterProcessor.process(project, editor, psiFile);
  }
}
