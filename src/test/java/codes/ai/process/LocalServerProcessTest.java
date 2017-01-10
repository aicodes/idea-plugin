package codes.ai.process;

import org.junit.Test;

/**
 * @author xuy.
 *         Copyright (c) Ai.codes
 */
/** Not the best way to test it yet */
public class LocalServerProcessTest {
  @Test
  public void canStartProcess() throws InterruptedException {
    // TODO: basedir will be provided by Plugin
    LocalServerProcess process = new LocalServerProcess("/Users/xuy/Aicodes/aicodes-idea-plugin/src/main/js/local_proxy/");
    process.start();
    process.join();   // will hang here forever, for now. you can check if server is up by visit http://localhost:8080
  }
}
