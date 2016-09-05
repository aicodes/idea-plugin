package codes.ai;

import com.intellij.codeInsight.completion.CompletionLocation;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Editors tend to send multiple auto-complete and weighting requests to ICE, requesting
 * various different things. ICE id helps to group these requests, so user gets a unified
 * view in the dashboard. ICE id should be uniquely decided by the current cursor location
 * and the file content. Here to simplify we use the hashCode for the cursor location as the ICE id.
 *
 */

class Context {
	private static Context CURRENT_CONTEXT; // a singleton object.

	private Project project;
	private int id;
	private String methodName;  // need to get tensor later.

	private Context(CompletionLocation location) {
		this.project = location.getProject();
		this.id = location.hashCode();
		PsiMethod m = PsiTreeUtil.getParentOfType(location.getCompletionParameters().getPosition(), PsiMethod.class);
		if (m != null) {
			this.methodName = m.getName();
		} else {
			this.methodName = "<unk>";
		}
	}

	// Avoid constructing new context repetitively if they are originated from the same location.
	static Context of(@NotNull CompletionLocation location) {
		if (CURRENT_CONTEXT == null || CURRENT_CONTEXT.getId() != location.hashCode()) {
			CURRENT_CONTEXT = new Context(location);
		}
		return CURRENT_CONTEXT;
	}

	Project getProject() {
		return project;
	}

	int getId() {
		return id;
	}

	public String getMethodName() {
		return methodName;
	}
}
