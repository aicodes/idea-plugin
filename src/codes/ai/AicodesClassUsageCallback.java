package codes.ai;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xuy on 8/12/16.
 */
public class AicodesClassUsageCallback implements FutureCallback<HttpResponse> {
	private final Gson gson = new Gson();

	public AicodesClassUsageCallback(ConcurrentHashMap<String, Integer> cache) {
	}

	@Override
	public void completed(HttpResponse httpResponse) {
		String json = "";
		try {
			java.util.Scanner s = new java.util.Scanner(
					httpResponse.getEntity().getContent()).useDelimiter("\\A");
			json = s.hasNext() ? s.next() : "";
		} catch (IOException e) {
			e.printStackTrace();
		}
		ClassMethodProbability probability = gson.fromJson(json, ClassMethodProbability.class);

	}

	@Override
	public void failed(Exception e) {

	}

	@Override
	public void cancelled() {

	}
}
