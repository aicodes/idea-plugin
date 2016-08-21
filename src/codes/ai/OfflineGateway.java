package codes.ai;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OfflineGateway manages when we can issue an actual HTTP request.
 * Always check with it before issuing HTTP requests in the plugin.
 *
 * Created by xuy on 8/21/16.
 */
public class OfflineGateway {
	private boolean offline;
	private Map<String, Long> keyTTL = new ConcurrentHashMap<>();

	// bette name would be "should stay offline"
	boolean hasKey(String key) {
		if (offline) return true;
		if (keyTTL.containsKey(key)) {
			if (System.currentTimeMillis() < keyTTL.get(key)) {
				return true;
			} else {    // Invalidate the cache because 1s has expired.
				keyTTL.remove(key);
			}
		}
		return false;
	}

	void putKey(String key, long ttl) {
		if (offline) return; // no need to put anything in offline mode.
		keyTTL.put(key, ttl);
	}

	void setOffline(boolean isOffline) {
		this.offline = isOffline;
		if (this.offline) {
			keyTTL.clear();
		}
	}
}
