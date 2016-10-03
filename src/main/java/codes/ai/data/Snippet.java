package codes.ai.data;

import java.util.List;

public class Snippet {
  public String id;
  public String code;
  public List<String> imports;
  public List<Variable> variables;
  public int rank;
  
  @Override
  public String toString() {
    return code;
  }
}

/** Value object that represents a snippet result returned from local JSON api. */
class Variable {
  String name;
  String type;
}