package codes.ai.ep;

import codes.ai.localapi.ApiRequestGateway;
import codes.ai.async.WebSocketClient;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.util.PlatformIcons;

/**
 * @author xuy.
 *         Copyright (c) Ai.codes
 */
class ReconnectDashAction extends AnAction {
  
  private final ApiRequestGateway gateway;
  
  ReconnectDashAction(ApiRequestGateway gateway) {
    super("Reconnect to AI.codes Server", "Reconnect to AI AI.codes Server", PlatformIcons.WEB_ICON);
    this.gateway = gateway;
  }
  
  @Override
  public void actionPerformed(AnActionEvent anActionEvent) {
    gateway.setOffline(false);
    WebSocketClient.reconnect();
  }
}
