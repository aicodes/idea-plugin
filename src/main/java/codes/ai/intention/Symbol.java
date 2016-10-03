package codes.ai.intention;

/**
 * @author xuy.
 *         Copyright (c) Ai.codes
 */
public class Symbol {
  String name;
  Kind kind;
  String type;
  
  public Symbol(String name, Kind kind, String type) {
    this.name = name;
    this.kind = kind;
    this.type = type;
  }
}

enum Kind {
  METHOD_PARAM,
  FIELD,
  LOCAL_VARIABLE
}