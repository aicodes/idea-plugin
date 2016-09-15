package codes.ai.localapi;

import java.util.HashMap;
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

  ApiResponse() {
    header = new ApiResponseHeader();
    response = new HashMap<>();
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
}
