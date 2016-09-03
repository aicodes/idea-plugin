package codes.ai;

import com.intellij.codeInsight.completion.CompletionLocation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author xuy.
 *         Copyright (c) Ai.codes
 * Editors tend to send multiple auto-complete and weighting requests to ICE, requesting
 * various different things. ICE id helps to group these requests, so user gets a unified
 * view in the dashboard. ICE id should be uniquely decided by the current cursor location
 * and the file content. Here to simplify we use the hashCode for the cursor location as the ICE id.
 *
 */
class Context {
	private Project project;
	private int id;

	private Context(CompletionLocation location) {
		this.project = location.getProject();
		this.id = location.hashCode();
	}

	static Context of(@NotNull CompletionLocation location) {
		return new Context(location);
	}

	Project getProject() {
		return project;
	}

	int getId() {
		return id;
	}
}
