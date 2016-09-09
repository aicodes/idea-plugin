package codes.ai.intention;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

/** @author xuy. Copyright (c) Ai.codes */
public class EditorIntention extends AnAction {
  @Override
  public void actionPerformed(AnActionEvent anActionEvent) {
    final Editor editor = anActionEvent.getRequiredData(CommonDataKeys.EDITOR);

    // Probably get PSI_FILE
    PsiElement element = anActionEvent.getRequiredData(CommonDataKeys.PSI_ELEMENT);
    System.out.println(element.getText());
  }

  @Override
  public void update(@NotNull final AnActionEvent anActionEvent) {
    final Project project = anActionEvent.getData(CommonDataKeys.PROJECT);
    final Editor editor = anActionEvent.getData(CommonDataKeys.EDITOR);
    anActionEvent
        .getPresentation()
        .setVisible(
            (project != null
                && editor != null
                && !editor.getCaretModel().getAllCarets().isEmpty()));
  }
}
