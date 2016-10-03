package codes.ai.intention;

import com.google.gson.Gson;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

/** @author xuy. Copyright (c) Ai.codes */
public class IntentionCaretListener implements CaretListener {
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
          IntentionPayload payload = new IntentionPayload();
          payload.methodName = method.getName();

          Collection<PsiField> fields =
                  PsiTreeUtil.findChildrenOfType(clazz, PsiField.class);

          try {
            payload.fields.addAll(
              fields.stream().map(
                      PsiField::getTypeElement)
                      .map(PsiTypeElement::getType)
                      .filter(psiType -> psiType.toString() != null)
                      .map(PsiType::getCanonicalText) // this requires IntelliJ index
                      .collect(Collectors.toList()));
          } catch (IndexNotReadyException e) {
            payload.fields.clear();
          }

          Collection<PsiLocalVariable> localVariables =
              PsiTreeUtil.findChildrenOfType(method, PsiLocalVariable.class);
          try {
            payload.localVariables.addAll(
                localVariables
                    .stream()
                    .map(PsiLocalVariable::getTypeElement)
                    .map(PsiTypeElement::getType)
                    .filter(psiType -> psiType.toString() != null)
                    .map(PsiType::getCanonicalText) // this requires IntelliJ index
                    .collect(Collectors.toList()));
          } catch (IndexNotReadyException e) {
            payload.localVariables.clear();
          }

          PsiParameterList parameterList = method.getParameterList();
          Collection<PsiParameter> parameters =
              PsiTreeUtil.findChildrenOfType(parameterList, PsiParameter.class);
          try {
            payload.parameters.addAll(
                parameters
                    .stream()
                    .filter(parameter -> parameter.getTypeElement() != null)
                    .map(PsiParameter::getTypeElement)
                    .map(PsiTypeElement::getType)
                    .filter(psiType -> psiType.toString() != null)
                    .map(PsiType::getCanonicalText) // this requires IntelliJ index
                    .collect(Collectors.toList()));
          } catch (IndexNotReadyException e) {
            payload.parameters.clear();
          }

          Collection<PsiComment> comments =
              PsiTreeUtil.findChildrenOfType(method, PsiComment.class);
          payload.intentions.addAll(
              comments
                  .stream()
                  .filter(comment -> comment.getText().startsWith("///"))
                  .map(comment -> comment.getText().substring(3).trim())
                  .collect(Collectors.toList()));
          WsClient.getInstance().sendMessage(gson.toJson(payload));
        }
      }
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

