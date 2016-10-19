package codes.ai.ep;

import codes.ai.snippet.Intention;
import codes.ai.snippet.IntentionExtractor;
import com.intellij.codeInsight.AutoPopupController;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author xuy.
 *         Copyright (c) Ai.codes
 */
public class AiCodesEnterActionHandler extends EditorWriteActionHandler implements DumbAware {
  private final EditorActionHandler delegate;
  
  public AiCodesEnterActionHandler(EditorActionHandler handler) {
    this.delegate = handler;
  }
  
  @Override
  public void executeWriteAction(Editor editor, Caret caret, DataContext dataContext) {
    delegate.execute(editor, caret, dataContext);
    Project project = CommonDataKeys.PROJECT.getData(dataContext);
    if (project != null && IntentionExtractor.getInstance().getIntention(project, editor) != null) {
      AutoPopupController.getInstance(project).autoPopupMemberLookup(editor, null);
    }
  }
  
  @Override
  public boolean isEnabledForCaret(@NotNull Editor editor, @NotNull Caret caret, DataContext dataContext) {
    return delegate.isEnabled(editor, caret, dataContext);
  }
}
