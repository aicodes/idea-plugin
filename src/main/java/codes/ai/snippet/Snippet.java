package codes.ai.snippet;

import java.util.List;

public class Snippet {
  String id;
  public String code;
  public List<String> imports;
  public List<Variable> variables;
  public int rank;
  
  @Override
  public String toString() {
    return code;
  }
}
