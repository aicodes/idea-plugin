package codes.ai.intention;

import codes.ai.ReconnectDashAction;
import codes.ai.resources.ApiRequestGateway;
import codes.ai.websocket.WsClient;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionContributorEP;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.Constraints;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.extensions.ExtensionPoint;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.extensions.ExtensionsArea;
import com.intellij.openapi.extensions.LoadingOrder;
import org.jetbrains.annotations.NotNull;

/** @author xuy. Copyright (c) Ai.codes */
public class IntentionComponent implements ApplicationComponent {
  public static final String ID = "AiCodesMenuItem";
  public static final String GROUP_ID = "ToolsMenu";
  
  public ApiRequestGateway getGateway() {
    return gateway;
  }
  
  // IntentionComponent is a singleton. It owns resources used by the plugin.
  ApiRequestGateway gateway;
  
  public static IntentionComponent getInstance() {
    return ApplicationManager.getApplication().getComponent(IntentionComponent.class);
  }
  
  
  @Override
  public void initComponent() {
    gateway = new ApiRequestGateway();
    gateway.setOffline(false);
    IntentionCaretListener listener = new IntentionCaretListener();
      
    /// Register Caret listener.
    EditorFactory.getInstance()
        .getEventMulticaster()
        .addCaretListener(listener);
    
    /// Register action.
    ActionManager am = ActionManager.getInstance();
    ReconnectDashAction action = new ReconnectDashAction(gateway);
    am.registerAction(ID, action);
    DefaultActionGroup toolsMenu =
        (DefaultActionGroup) am.getAction(GROUP_ID);
    toolsMenu.add(action, Constraints.LAST);
  }

  @Override
  public void disposeComponent() {}

  @NotNull
  @Override
  public String getComponentName() {
    return "Intention Component";
  }
}
