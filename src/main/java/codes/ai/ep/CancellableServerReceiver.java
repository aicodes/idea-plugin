package codes.ai.ep;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.builtInWebServer.BuiltInWebServer;
import sun.net.httpserver.HttpServerImpl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author xuy.
 *         Copyright (c) Ai.codes
 */
enum State {
  SUCCESS,
  CANCEL,
  FAILURE,
}

class CancellableServerReceiver {
  private HttpServer server ;  // need a simple HTTP Server
  private boolean login = false;
  private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
  public void start() {
    try {
      server = HttpServer.create(new InetSocketAddress(8000), 0);
      server.setExecutor(executor);
      server.createContext("/success", new HttpHandler() {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
          // Response with 200 OK
          String response = "ok";
          httpExchange.sendResponseHeaders(200, response.length());
          OutputStream os = httpExchange.getResponseBody();
          os.write(response.getBytes());
          os.close();
          login = true;
        }
      });
      server.start();   // non-blocking as it is delegated to executor.
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public void cancel() {
    executor.shutdownNow();
    server.stop(0);
  }
  
  State waitForStateChange() {
    while (!login) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
        return State.CANCEL;
      }
    }
    return State.SUCCESS;
  }
}
