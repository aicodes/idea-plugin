package codes.ai.ep;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.actions.EditorActionUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import javafx.scene.control.PopupControl;
import org.intellij.images.thumbnail.actions.EnterAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.KeyStroke;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * @author xuy.
 * Copyright (c) Ai.codes
 */

/** An editor action that can be triggered via menu and other means. */
public class AiCodesAction extends AnAction implements DumbAware {
  private static final String ACTION_ID = "AiCodesAction";
  
  @Override
  public void actionPerformed(AnActionEvent anActionEvent) {
    final Project project = anActionEvent.getProject();
    final Editor editor = anActionEvent.getData(PlatformDataKeys.EDITOR);
    if (project != null && editor != null) {
      AutoPopupController.getInstance(project).autoPopupMemberLookup(editor, null);
    }
  }
  
  
  @Override
  public void update(@NotNull AnActionEvent e) {
    final Project project = e.getProject();
    final Editor editor = e.getData(PlatformDataKeys.EDITOR);
    if (project == null || editor == null) {
      e.getPresentation().setEnabled(false);
    }
  }

/*
  @Nullable
  private KeyStroke getKeyStroke(@NotNull AnActionEvent e) {
    final InputEvent inputEvent = e.getInputEvent();
    if (inputEvent instanceof KeyEvent) {
      final KeyEvent keyEvent = (KeyEvent)inputEvent;
      return KeyStroke.getKeyStrokeForEvent(keyEvent);
    }
    return null;
  }
  
  // Singleton
  public static AnAction getInstance() {
    return ActionManager.getInstance().getAction(ACTION_ID);
  }
  */
}
