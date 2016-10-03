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
	String method = null;
	List<String> intentions = new ArrayList<>();
	List<Symbol> parameters = new ArrayList<>();
	List<Symbol> variables = new ArrayList<>();
	List<Symbol> fields = new ArrayList<>();

	@Override
	public int hashCode() {
		return Objects.hash(method, intentions, parameters);
	}
}
