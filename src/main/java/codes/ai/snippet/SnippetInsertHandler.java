package codes.ai.snippet;

import codes.ai.java.pojo.ResultSnippet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiImportStatement;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.intellij.util.DocumentUtil;

/**
 * Snippets recommended by AI.codes need to be processed (term is "homogenized") to fit in to the
 * context. This class takes care of that.
 *
 * <p>A few things this class do: * Correct indent the code snippet; * Trigger auto-import;
 */
public class SnippetInsertHandler implements InsertHandler<LookupElement> {
  public static SnippetInsertHandler INSTANCE = new SnippetInsertHandler();

  @Override
  public void handleInsert(InsertionContext context, LookupElement lookupElement) {
    Project project = context.getProject();
    Document document = context.getDocument();
    int lineStartOffset = DocumentUtil.getLineStartOffset(context.getStartOffset(), document);
    PsiDocumentManager.getInstance(project).commitDocument(document);

    CodeStyleManager.getInstance(project)
        .adjustLineIndent(
            context.getFile(),
            new TextRange(lineStartOffset, lineStartOffset + lookupElement.toString().length()));

    final PsiFile file = context.getFile();
    if (file instanceof PsiJavaFile && lookupElement.getObject() instanceof ResultSnippet) {
      final PsiJavaFile javaFile = (PsiJavaFile) file;
      final ResultSnippet resultSnippet = (ResultSnippet) lookupElement.getObject();
      if (resultSnippet.imports == null) {
        return;
      }
      PsiImportList importList = javaFile.getImportList();
      if (importList == null) {
        return;
      }

      final JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);
      final PsiElementFactory elementFactory = psiFacade.getElementFactory();
      final GlobalSearchScope projectScope = ProjectScope.getAllScope(project);

      resultSnippet
          .imports
          .stream()
          .filter(classToImport -> importList.findSingleClassImportStatement(classToImport) == null)
          .filter(classToImport -> psiFacade.findClass(classToImport, projectScope) != null)
          .map(classToImport -> psiFacade.findClass(classToImport, projectScope))
          .forEach(
              importClass -> {
                final PsiImportStatement importStatement =
                    elementFactory.createImportStatement(importClass);
                importList.add(importStatement);
              });
      // TODO: send result_id back to server.
    }
  }
}
