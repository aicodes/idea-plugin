package codes.ai.intention;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.DocumentUtil;

/**
 * Snippets recommended by AI.codes need to be processed (term is "homogenized") to fit
 * in to the context. This class takes care of that.
 *
 * A few things this class do:
 *      * Correct indent the code snippet;
 *      * Trigger auto-import;
 *
 */
class SnippetInsertHandler implements InsertHandler<LookupElement> {
	static SnippetInsertHandler INSTANCE = new SnippetInsertHandler();

	@Override
	public void handleInsert(InsertionContext context, LookupElement lookupElement) {
		Project project = context.getProject();
		Document document = context.getDocument();
		int lineStartOffset = DocumentUtil.getLineStartOffset(context.getStartOffset(), document);
		PsiDocumentManager.getInstance(project).commitDocument(document);

		CodeStyleManager.getInstance(project).adjustLineIndent(context.getFile(),
				new TextRange(lineStartOffset, lineStartOffset + lookupElement.toString().length()));
	}
}