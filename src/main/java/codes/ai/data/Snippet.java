package codes.ai.data;

import java.util.List;

/** @author xuy. Copyright (c) Ai.codes */
/** Value object that represents a snippet result returned from local JSON api. */
public class Snippet {
  public String code;
  public List<String> imports;

  @Override
  public String toString() {
    return code;
  }
}
