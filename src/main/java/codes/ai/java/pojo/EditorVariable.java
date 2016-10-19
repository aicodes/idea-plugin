package codes.ai.java.pojo;

/**
 * @author xuy.
 *         Copyright (c) Ai.codes
 */

/** EditorVariable adds additional fields that are known to local editors.
 */
public class EditorVariable extends Variable {
  private VariableKind variableKind;   // variableKind = "local", "field" or "argument".
  
  public EditorVariable(String name, VariableKind variableKind, String type) {
    this.name = name;
    this.variableKind = variableKind;
    this.type = type;
  }
}

