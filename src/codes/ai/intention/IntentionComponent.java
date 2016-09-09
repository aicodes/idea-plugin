package codes.ai.intention;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.editor.EditorFactory;
import org.jetbrains.annotations.NotNull;

/** @author xuy. Copyright (c) Ai.codes */
public class IntentionComponent implements ApplicationComponent {
  @Override
  public void initComponent() {
    EditorFactory.getInstance()
        .getEventMulticaster()
        .addCaretListener(new IntentionCaretListener());
  }

  @Override
  public void disposeComponent() {}

  @NotNull
  @Override
  public String getComponentName() {
    return "Intention Component";
  }
}
