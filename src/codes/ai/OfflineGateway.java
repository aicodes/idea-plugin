package codes.ai;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OfflineGateway manages when we can issue an actual HTTP request. Always check with it before
 * issuing HTTP requests in the plugin.
 *
 * <p>Keys stay for 1s here, to temporarily prevent issuing requests to local servers over and over
 * while waiting for results.
 *
 * <p>In future we will query things async. Created by xuy on 8/21/16.
 */
public class OfflineGateway {
  private boolean offline;
  private static final long TTL_MS = 1000L;
  private Map<String, Long> keyTTL = new ConcurrentHashMap<>();

  boolean shouldIssueRequest(String requestId) {
    if (offline) return false;
    if (keyTTL.containsKey(requestId)) {
      if (System.currentTimeMillis() < keyTTL.get(requestId)) {
        return false;
      } else { // Invalidate the cache because 1s has expired.
        keyTTL.remove(requestId);
      }
    }
    keyTTL.put(requestId, TTL_MS);
    return true;
  }

  void setOffline(boolean isOffline) {
    this.offline = isOffline;
    if (this.offline) {
      keyTTL.clear();
    }
  }
}
