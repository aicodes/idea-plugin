package codes.ai.ui;

import com.intellij.openapi.util.IconLoader;

import javax.swing.Icon;

/** @author xuy. Copyright (c) Ai.codes */
public class AicodesIcons {
  private static Icon load(String path) {
    return IconLoader.getIcon(path, AicodesIcons.class);
  }

  public static final Icon AICODES = load("/icons/aicodes.png"); // 16x16
}
