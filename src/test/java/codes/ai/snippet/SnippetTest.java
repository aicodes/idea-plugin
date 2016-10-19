package codes.ai.snippet;

import codes.ai.java.pojo.ResultSnippet;
import com.google.gson.Gson;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author xuy.
 *         Copyright (c) Ai.codes
 */
public class SnippetTest {
  @Test
  public void shouldParseJsonCorrectly() throws Exception {
    java.net.URL url = getClass().getResource("/snippet.json");
    java.nio.file.Path resPath = java.nio.file.Paths.get(url.toURI());
    String json = new String(java.nio.file.Files.readAllBytes(resPath), "UTF8");
    Gson gson = new Gson();
    ResultSnippet resultSnippet = gson.fromJson(json, ResultSnippet.class);
    assertEquals("e17355e3-abb8-4b78-800c-23c4d900f4c9:0", resultSnippet.result_id);
    assertEquals(1, resultSnippet.imports.size());
    assertEquals(1, resultSnippet.variables.size());
    assertEquals("myFile", resultSnippet.variables.get(0).name);
    assertEquals("java.util.File", resultSnippet.variables.get(0).type);
  }
}