package codes.ai.intention;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author xuy.
 *         Copyright (c) Ai.codes
 * Payload from editor to dashboard. Immutable value class.
 */
class IntentionPayload {
	String methodName = null;
	List<String> intentions = new ArrayList<>();
	List<String> parameters = new ArrayList<>();
	List<String> localVariables = new ArrayList<>();
	List<String> fields = new ArrayList<>();

	@Override
	public int hashCode() {
		return Objects.hash(methodName, intentions, parameters);
	}
}
