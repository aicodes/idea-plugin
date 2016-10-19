package codes.ai.java.pojo;

public class ResultSnippet extends Snippet {
  public String result_id;   // globally unique request id.
  public int rank;
  
  @Override
  public String toString() {
    return code;
  }
}
