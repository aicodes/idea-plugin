package codes.ai.intention;

<<<<<<< HEAD
import codes.ai.websocket.Client;
import com.google.gson.Gson;
=======
>>>>>>> 170987abd2fb23fd56a5e09cfacb97fde64f5d01
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.Collection;

/** @author xuy. Copyright (c) Ai.codes */
public class IntentionCaretListener implements CaretListener {
<<<<<<< HEAD
  private Client wsClient = Client.getInstance();
  private Gson gson = new Gson();

  @Override
  public void caretPositionChanged(CaretEvent caretEvent) {
    Project project = caretEvent.getEditor().getProject();
    /// Skip parsing event if caret stays in the same line.
    if (caretEvent.getOldPosition().line == caretEvent.getNewPosition().line) {
      return;
    }
=======
  @Override
  public void caretPositionChanged(CaretEvent caretEvent) {
    Project project = caretEvent.getEditor().getProject();
    if (caretEvent.getOldPosition().line == caretEvent.getNewPosition().line)
      return; // quick optimization.
>>>>>>> 170987abd2fb23fd56a5e09cfacb97fde64f5d01
    if (project != null) {
      PsiDocumentManager manager = PsiDocumentManager.getInstance(project);
      PsiFile file = manager.getPsiFile(caretEvent.getEditor().getDocument());
      Caret c = caretEvent.getCaret();
      if (c != null && file != null) {
        PsiElement element = file.findElementAt(c.getOffset());
        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if (method != null) {
<<<<<<< HEAD
          IntentionPayload payload = new IntentionPayload();
          payload.methodName = method.getName();
=======
          StringBuilder sb = new StringBuilder();
          sb.append(method.getName());
>>>>>>> 170987abd2fb23fd56a5e09cfacb97fde64f5d01

          Collection<PsiComment> comments =
              PsiTreeUtil.findChildrenOfType(method, PsiComment.class);
          for (PsiComment comment : comments) {
            if (comment.getText().startsWith("///")) {
<<<<<<< HEAD
              payload.intentions.add(comment.getText().substring(3).trim());
            }
          }
          wsClient.sendMessage(gson.toJson(payload));
=======
              sb.append("+").append(comment.getText().substring(3));
            }
          }
          String message = sb.toString();
          System.out.println("Send to local server " + message);
          sendMessage(message);
>>>>>>> 170987abd2fb23fd56a5e09cfacb97fde64f5d01
        }
      }
    }
  }

  @Override
  public void caretAdded(CaretEvent caretEvent) {
    System.out.println("Added");
  }

  @Override
  public void caretRemoved(CaretEvent caretEvent) {
    // do nothing
    System.out.println("Removed");
  }

  // Poor man's websocket
  // TODO: get this to JSON and send to express via websocket.
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
