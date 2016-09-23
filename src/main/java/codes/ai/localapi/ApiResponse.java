package codes.ai.localapi;

import codes.ai.data.Snippet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author xuy. Copyright (c) Ai.codes */



// POJO for converting JSON response to Java POJO.
class ApiResponse {

  private class ApiResponseHeader {
    int status;
    String message;
  }

  private ApiResponseHeader header;
  private Map<String, Double> response;
  private List<Snippet> snippets;

  ApiResponse() {
    header = new ApiResponseHeader();
    response = new HashMap<>();
    snippets = new ArrayList<>();
  }

  void setStatus(int status) {
    this.header.status = status;
  }

  void setMessage(String message) {
    this.header.message = message;
  }

  int getStatus() {
    return header.status;
  }

  Map<String, Double> getResponse() {
    return response;
  }

  List<Snippet> getSnippets() { return snippets; }
}
