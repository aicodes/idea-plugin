package codes.ai.intention;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author xuy.
 *         Copyright (c) Ai.codes
 * Payload from editor to dashboard. Immutable value class
 */
public class IntentionPayload {
	public String methodName = null;
	public List<String> intentions = new ArrayList<>();

	@Override
	public int hashCode() {
		return Objects.hash(methodName, intentions);
	}
}
