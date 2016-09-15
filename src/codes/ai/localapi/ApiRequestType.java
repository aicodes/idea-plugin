package codes.ai.localapi;

import codes.ai.Context;
import com.google.common.base.Joiner;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.Nullable;

/** @author xuy. Copyright (c) Ai.codes */
enum ApiRequestType {
  USAGE("U>", 0.1),
  SIMILARITY("S>", 0.2);

  private static final Joiner joiner = Joiner.on('.');

  private String name;

  private double defaultValue;

  ApiRequestType(String name, double defaultValue) {
    this.name = name;
    this.defaultValue = defaultValue;
  }

  public String getName() {
    return name;
  }

  public String encodeRequest(PsiMethod method, @Nullable Context context) {
    if (context == null) {
      return joiner.join(getName(), Utils.getJvmName(method));
    }
    return joiner.join(getName(), context.getContextMethod(), Utils.getJvmName(method));
  }

  public double getDefaultValue() {
    return defaultValue;
  }
}
