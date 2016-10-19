package codes.ai.listener;

import codes.ai.async.WebSocketClient;
import codes.ai.java.pojo.EditorVariable;
import codes.ai.java.pojo.EditorIntention;
import codes.ai.java.pojo.VariableKind;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiForStatement;
import com.intellij.psi.PsiForeachStatement;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/** @author xuy. Copyright (c) Ai.codes */

/** Listens to caret moves. Asynchronously communicate editor context to dashboard
 * so when auto completion is called we have most of the information ready.
 */
public class ContextualCaretListener implements CaretListener {
  private Gson gson = new Gson();
  
  @Override
  public void caretPositionChanged(CaretEvent caretEvent) {
    /// Skip parsing caretEvent if caret stays in the same line.
    if (caretEvent.getOldPosition().line == caretEvent.getNewPosition().line) {
      return;
    }
    
    Project project = caretEvent.getEditor().getProject();
    if (project != null && project.isInitialized()) {
      PsiDocumentManager manager = PsiDocumentManager.getInstance(project);
      PsiFile file = manager.getPsiFile(caretEvent.getEditor().getDocument());
      Caret c = caretEvent.getCaret();
      if (c != null && file != null) {
        PsiElement element = file.findElementAt(c.getOffset());
        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        PsiClass clazz = PsiTreeUtil.getParentOfType(method, PsiClass.class);

        if (method != null && clazz != null) {
          EditorIntention payload = new EditorIntention();
          payload.method = method.getName();

          Collection<PsiField> fields =
                  PsiTreeUtil.findChildrenOfType(clazz, PsiField.class);
          addSymbols(VariableKind.FIELD, fields, payload.variables);
          
          Collection<PsiLocalVariable> localVariables =
              PsiTreeUtil.findChildrenOfType(method, PsiLocalVariable.class);
          addSymbols(VariableKind.LOCAL, localVariables, payload.variables);

          PsiParameterList parameterList = method.getParameterList();
          Collection<PsiParameter> parameters =
              PsiTreeUtil.findChildrenOfType(parameterList, PsiParameter.class);
          addSymbols(VariableKind.ARGUMENT, parameters, payload.variables);
          
          // Collect params declared inside a foreach loop.
          PsiForeachStatement forEachStatement =
              PsiTreeUtil.getParentOfType(element, PsiForeachStatement.class);
          if (forEachStatement != null) {
            PsiParameter forEachVariable = PsiTreeUtil.findChildOfType(forEachStatement, PsiParameter.class);
            if (forEachVariable != null) {
              addSymbols(VariableKind.LOCAL, ImmutableList.of(forEachVariable), payload.variables);
            }
          }
  
          // Collect variables declared in a for loop.
          PsiForStatement forStatement =
              PsiTreeUtil.getParentOfType(element, PsiForStatement.class);
          if (forStatement != null) {
            PsiLocalVariable forVariable = PsiTreeUtil.findChildOfType(forStatement, PsiLocalVariable.class);
            if (forVariable != null) {
              addSymbols(VariableKind.LOCAL, ImmutableList.of(forVariable), payload.variables);
            }
          }
          
          /// Collect async lines from comments.
          Collection<PsiComment> comments =
              PsiTreeUtil.findChildrenOfType(method, PsiComment.class);
          payload.intentions.addAll(
              comments
                  .stream()
                  .filter(comment -> comment.getText().startsWith("///"))
                  .map(comment -> comment.getText().substring(3).trim())
                  .collect(Collectors.toList()));
          WebSocketClient.getInstance().sendMessage(gson.toJson(payload));
        }
      }
    }
  }
  
  private void addSymbols(
      VariableKind symbolVariableKind,
      Collection<? extends PsiVariable> variables,
      List<EditorVariable> targetList) {
    try {
      for (PsiVariable variable : variables) {
        if (variable.getTypeElement() == null) continue;
        String variableType = variable.getTypeElement().getType().getCanonicalText();
        if (StringUtil.isEmpty(variableType)) continue;
        targetList.add(new EditorVariable(variable.getName(), symbolVariableKind, variableType));
      }
    }
    catch (IndexNotReadyException e) {
      targetList.clear();
    }
  }

  @Override
  public void caretAdded(CaretEvent caretEvent) {
  }

  @Override
  public void caretRemoved(CaretEvent caretEvent) {
  }

  /** We have switched everything to using Netty/WebSocket. This is only for debugging */
  @Deprecated
  private void sendMessage(String message) {
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet get = new HttpGet("http://127.0.0.1:26337/ping/" + message);
    get.setConfig(
        RequestConfig.custom()
            .setConnectTimeout(10)
            .setConnectionRequestTimeout(10)
            .setSocketTimeout(10)
            .build());
    CloseableHttpResponse response = null;
    try {
      response = httpClient.execute(get);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (response != null) response.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}

