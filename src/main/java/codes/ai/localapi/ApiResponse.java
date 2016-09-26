package codes.ai.localapi;

import codes.ai.data.Snippet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author xuy. Copyright (c) Ai.codes */



// POJO for converting JSON object to a Java POJO.
class ApiResponse {

  private class ApiResponseHeader {
    int status;
    String message;
  }

  private ApiResponseHeader header;
  private Map<String, Double> weights;
  private List<Snippet> snippets;

  ApiResponse() {
    header = new ApiResponseHeader();
    weights = new HashMap<>();
    snippets = new ArrayList<>();
  }

  int getStatus() {
    return header.status;
  }

  Map<String, Double> getWeights() {
    return weights;
  }

  List<Snippet> getSnippets() { return snippets; }
}
