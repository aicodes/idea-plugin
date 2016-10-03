package codes.ai.localapi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ApiRequestGateway is a performance optimization. It gates HTTP request issued to local server,
 * such that ApiClient does not repetitively issue requests to local server that are known to fail.
 *
 * <p>The following two scenarios would return false when shouldIssueRequest is called: 1. When an
 * identical request has just been issued within a second; 2. When we cannot talk to local server
 * (offline = true).
 */
public class ApiRequestGateway {
  private boolean offline = false;
  private static final long TTL_MS = 1000L;
  private Map<String, Long> keyTTL = new ConcurrentHashMap<>();

  public boolean shouldIssueRequest(String requestId) {
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

  public void setOffline(boolean isOffline) {
    this.offline = isOffline;
    if (this.offline) {
      keyTTL.clear();
    }
  }
}
