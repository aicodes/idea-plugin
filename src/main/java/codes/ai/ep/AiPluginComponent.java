package codes.ai.ep;

import codes.ai.async.ContextualCaretListener;
import codes.ai.localapi.ApiRequestGateway;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.Constraints;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.editor.EditorFactory;
import org.jetbrains.annotations.NotNull;

/** @author xuy. Copyright (c) Ai.codes */
/**
 * AiPluginComponent is a singleton. It owns resources used by the plugin.
 */
public class AiPluginComponent implements ApplicationComponent {
  private static final String RECONNECT_MENU_ID = "AiCodesMenu_reconnect";
  private static final String OAUTH_MENU_ID = "AiCodesMenu_oauth";
  
  private static final String GROUP_ID = "ToolsMenu";
  private ApiRequestGateway gateway;
  
  public ApiRequestGateway getGateway() {
    return gateway;
  }
  
  public static AiPluginComponent getInstance() {
    return ApplicationManager.getApplication().getComponent(AiPluginComponent.class);
  }
  
  @Override
  public void initComponent() {
    gateway = new ApiRequestGateway();
    gateway.setOffline(false);
    ContextualCaretListener listener = new ContextualCaretListener();
      
    /// Register Caret listener.
    EditorFactory.getInstance()
        .getEventMulticaster()
        .addCaretListener(listener);
    
    /// Register action.
    ActionManager am = ActionManager.getInstance();
    ReconnectDashAction reconnectAction = new ReconnectDashAction(gateway);
    OAuthLoginAction oAuthLoginAction = new OAuthLoginAction();
    
    am.registerAction(RECONNECT_MENU_ID, reconnectAction);
    am.registerAction(OAUTH_MENU_ID, oAuthLoginAction);
    DefaultActionGroup toolsMenu =
        (DefaultActionGroup) am.getAction(GROUP_ID);
    
    toolsMenu.add(oAuthLoginAction, Constraints.LAST);
    toolsMenu.add(reconnectAction, Constraints.LAST);
  }

  @Override
  public void disposeComponent() {}

  @NotNull
  @Override
  public String getComponentName() {
    return "Intention Component";
  }
}
