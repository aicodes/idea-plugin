package codes.ai;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xuy.
 *         Copyright (c) Ai.codes
 */
// POJO for converting JSON response to Java POJO.
public class ResponseJson {
	private ResponseHeader header;
	private Map<String, Double> response;

	public ResponseJson() {
		header = new ResponseHeader();
		response = new HashMap<>();
	}

	public void setStatus(int status) {
		this.header.status = status;
	}

	public void setMessage(String message) {
		this.header.message = message;
	}

	public int getStatus() {
		return header.status;
	}

	public Map<String,Double> getResponse() {
		return response;
	}
}

class ResponseHeader {
	int status;
	String message;
}

/**
 {
    "header": {
        "status": int
    }
    "response": {
        ... a bunch of maps for now
    }
 }*/