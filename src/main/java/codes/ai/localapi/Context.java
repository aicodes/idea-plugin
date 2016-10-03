package codes.ai.localapi;

import com.intellij.codeInsight.completion.CompletionLocation;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Editors tend to send multiple auto-complete and weighting requests to ICE, requesting various
 * different things. ICE id helps to group these requests, so user gets a unified view in the
 * dashboard. ICE id should be uniquely decided by the current cursor location and the file content.
 * Here to simplify we use the hashCode for the cursor location as the ICE id.
 */
public class Context {
  private static Context CURRENT_CONTEXT; // a singleton object.

  private Project project;
  private int id;
  private String contextMethod; // need to get tensor later.

  private Context(CompletionLocation location) {
    // Stores project and id from location.
    // TODO: kill this, get project info somewhere else for ApiClient.
    this.project = location.getProject();
    this.id = location.hashCode();
    PsiMethod m =
        PsiTreeUtil.getParentOfType(
            location.getCompletionParameters().getPosition(), PsiMethod.class);

    // Stores method.
    if (m != null) {
      this.contextMethod = m.getName();
    } else {
      this.contextMethod = null;
    }
  }

  // Avoid constructing new context repetitively if they are originated from the same location.
  public static Context of(@NotNull CompletionLocation location) {
    if (CURRENT_CONTEXT == null || CURRENT_CONTEXT.getId() != location.hashCode()) {
      CURRENT_CONTEXT = new Context(location);
    }
    return CURRENT_CONTEXT;
  }

  public Project getProject() {
    return project;
  }

  public int getId() {
    return id;
  }

  public String getContextMethod() {
    return contextMethod;
  }

  public boolean hasContextMethod() {
    return contextMethod != null;
  }
}
