package codes.ai;

/** @author xuy. Copyright (c) Ai.codes */
public enum RequestType {
  USAGE("U>", 0.1),
  SIMILARITY("S>", 0.2);

  private String name;

  private double defaultValue;

  RequestType(String name, double defaultValue) {
    this.name = name;
    this.defaultValue = defaultValue;
  }

  public String getName() {
    return name;
  }

  public double getDefaultValue() {
    return defaultValue;
  }
}
