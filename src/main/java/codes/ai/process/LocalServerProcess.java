package codes.ai.process;

import com.google.common.collect.ImmutableList;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xuy.
 *         Copyright (c) Ai.codes
 */
public class LocalServerProcess {
  private static List<String> nodePaths = ImmutableList.of(
      "/usr/local/bin/node", "/opt/local/bin/node", "/usr/bin/node", "C:\\Program Files\\Nodejs", "C:\\Program Files (x86)\\Nodejs");
  private static String scriptName = "server.js";
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
    List<String> commands = findNodeCommand();
    commands.add(scriptName);
    b.command(commands);
    try {
      b.directory(new File(this.basedir));
      System.out.println("The base directory is " + b.directory().toString());
      this.process = b.start();
      outThread = new Thread(new StdOut());
      outThread.setDaemon(true);
      outThread.start();
      System.out.println("process is alive value is " + this.process.isAlive());
  
    } catch (IOException e) {
      e.printStackTrace();
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
   * Find where node is, returns command to execute node
   */
  private List<String> findNodeCommand() {
    List<String> results = new ArrayList<>();
    for (String nodePath : nodePaths) {
      if (fileExists(nodePath)) {
        results.add(nodePath);
        break;
      }
    }
    return results;
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
        
        long startTime = System.nanoTime();
        // start the node.js process with tern.
        Integer port = null;
        String line = null;
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        try {
          while ((line = br.readLine()) != null) {
            if (port == null) {
              if (line.startsWith("Listening on port ")) {
                port = Integer.parseInt(line.substring("Listening on port ".length(), line.length()));
                
                // port is acquired, notify that process is
                // started.
//                setPort(port);
                
                /*
                synchronized (lock) {
                  lock.notifyAll();
                }
                */
//                notifyStartProcess(startTime);
              }
            } else {
              // notify data
//              notifyDataProcess(line);
            }
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
        
        if (process != null) {
          process.waitFor();
        }
//        notifyStopProcess();
//        kill();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  };
  
  /**
   * StdErr of the node.js process.
   */
  private class StdErr implements Runnable {
    @Override
    public void run() {
      String line = null;
      InputStream is = process.getErrorStream();
      InputStreamReader isr = new InputStreamReader(is);
      BufferedReader br = new BufferedReader(isr);
      try {
        while ((line = br.readLine()) != null) {
//          notifyErrorProcess(line);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
  
}
