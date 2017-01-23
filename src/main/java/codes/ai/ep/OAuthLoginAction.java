package codes.ai.ep;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.util.AbstractProgressIndicatorExBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.ex.ProgressIndicatorEx;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * @author xuy.
 *         Copyright (c) Ai.codes
 */
class OAuthLoginAction extends AnAction {
  private Task.Modal taskModal;
  private CancellableServerReceiver receiver;
  
  OAuthLoginAction() {
    super("AI.codes OAuth login", "AI.codes OAuth login", PlatformIcons.UI_FORM_ICON);
  }
  
  /** Workflow for OAuth login
   *  As a general statement, editor plugins do not store any states. Local proxy manages everything.
   *  The role of editor plugin is to activate the login process, and (potentially) remind user to login.
   *
   *  Steps:
   *  1. User triggers a login action (later can be triggered from notification)
   *  2. We redirect user to a local proxy hosted login page, in their browser.
   *  3. User types in credential, through some oAuth magic we get access token.
   *  4. The access token is granted by a callback URL, which is hosted by local proxy.
   *  5. Local proxy remembers the token etc.
   *  6. Local proxy notify editor plugin that user has successfully log in.
   *
   *  This class does 1 and 6, by exposing a CancellableServerReceiver.
   */
  @Override
  public void actionPerformed(AnActionEvent anActionEvent) {
    taskModal = new Task.Modal(
        getCurrentProject(),
        "Please Sign in via the Opened Browser...",
        true) {
      @Override
      public void run(@NotNull ProgressIndicator indicator) {
        indicator.setIndeterminate(true);
        if (!(indicator instanceof ProgressIndicatorEx)) {
          return;
        }
  
        ((ProgressIndicatorEx) indicator).addStateDelegate(new AbstractProgressIndicatorExBase() {
          @Override
          public void cancel() {
            super.cancel();
          }
        });
        receiver = new CancellableServerReceiver();
        receiver.start();               // non-blocking.
        receiver.waitForStateChange();  // blocking operation.
      }
  
      @Override
      public void onCancel() {
        notifyOnComplete();
      }
  
      @Override
      public void onSuccess() {
        notifyOnComplete();
      }
      
      private void notifyOnComplete() {
        receiver.cancel();
      }
    };
    // TODO(eric): use the URL that James provided.
    BrowserUtil.browse("https://www.ai.codes");
    taskModal.queue();
  }
  
  @Nullable
  private static Project getCurrentProject() {
    return ProjectManager.getInstance().getOpenProjects()[0];
  }
}
