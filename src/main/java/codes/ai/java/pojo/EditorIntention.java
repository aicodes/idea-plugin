package codes.ai.java.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author xuy.
 *         Copyright (c) Ai.codes
 *         Intention data, sent from editor to dashboard (in JSON format).
 */
public class EditorIntention {
  public String method = null;    // surrounding method
  public List<String> intentions = new ArrayList<>();
  public List<EditorVariable> variables = new ArrayList<>();
  
  @Override
  public int hashCode() {
    return Objects.hash(method, intentions, variables);
  }
}
