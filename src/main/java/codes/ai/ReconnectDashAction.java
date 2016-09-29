package codes.ai;

import codes.ai.resources.ApiRequestGateway;
import codes.ai.websocket.WsClient;
import com.intellij.ide.ui.laf.intellij.MacIntelliJIconCache;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.util.PlatformIcons;

/**
 * @author xuy.
 *         Copyright (c) Ai.codes
 */
public class ReconnectDashAction extends AnAction {
  
  private final ApiRequestGateway gateway;
  
  public ReconnectDashAction(ApiRequestGateway gateway) {
    super("Reconnect to AI.codes Server", "Reconnect to AI AI.codes Server", PlatformIcons.WEB_ICON);
    this.gateway = gateway;
  }
  
  @Override
  public void actionPerformed(AnActionEvent anActionEvent) {
    gateway.setOffline(false);
    WsClient.reconnect();
  }
}
