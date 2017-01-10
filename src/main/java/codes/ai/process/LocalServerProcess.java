package codes.ai.process;

import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @author xuy.
 *         Copyright (c) Ai.codes
 */
public class LocalServerProcess {
  Logger logger = LoggerFactory.getLogger(LocalServerProcess.class);
  
  private static List<String> nodePaths = ImmutableList.of(
      "/usr/local/bin/node",
      "/opt/local/bin/node",
      "/usr/bin/node",
      "C:\\Program Files\\Nodejs",
      "C:\\Program Files (x86)\\Nodejs");
  
  private static String SCRIPT_NAME = "dist/app.js";
  private static String PORT_LINE = "Hello! listening on port "; // there is a trailing space.
  private final String basedir;
  private Process process;
  private Thread outThread;
  private Thread errThread;
  
  public LocalServerProcess(String pluginBaseDir) {
    this.basedir = pluginBaseDir;
  }
  
  public void start() {
    if (hasStarted()) {
      return;
    }
    ProcessBuilder b = new ProcessBuilder();
    String node = findNodeCommand();
    if (node != null) {
      b.command(node, SCRIPT_NAME);
      try {
        b.directory(new File(this.basedir));
        logger.info("The base directory is " + b.directory().toString());
        this.process = b.start();
        outThread = new Thread(new StdOut());
        outThread.setDaemon(true);
        outThread.start();
        logger.info("process is alive value is " + this.process.isAlive());
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      // cannot find node.
      System.err.println("Cannot seem to find node.");
    }
  }
  
  public boolean hasStarted() {
    return this.process != null;
  }
  
  private boolean fileExists(String fname) {
    File f = new File(fname);
    return f.exists() && !f.isDirectory();
  }
  
  /**
   * Find where node.js executable is.
   * @return command string to invoke node
   */
  private String findNodeCommand() {
    for (String nodePath : nodePaths) {
      if (fileExists(nodePath)) {
        return nodePath;
      }
    }
    return null;
  }
  
  public void join() throws InterruptedException {
    if (outThread != null) {
      outThread.join();
    }
  }
  
  private class StdOut implements Runnable {
    
    @Override
    public void run() {
      try {
        Integer port = null;
        String line;
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        try {
          while ((line = br.readLine()) != null) {
            if (port == null) {
              if (line.startsWith(PORT_LINE)) {
                port = Integer.parseInt(line.substring(PORT_LINE.length(), line.length()));
                logger.info("Started proxy on port {}", port);
              }
            }
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
        
        if (process != null) {
          process.waitFor();
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }
  
  /**
   * StdErr of the node.js process.
   */
  private class StdErr implements Runnable {
    @Override
    public void run() {
      InputStream is = process.getErrorStream();
      InputStreamReader isr = new InputStreamReader(is);
      BufferedReader br = new BufferedReader(isr);
      try {
        String line;
        while ((line = br.readLine()) != null) {
          // report error
          logger.error(line);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
  
}
