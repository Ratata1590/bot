package com.bot;

import java.io.IOException;

import com.bot.Thread.ServerBot;

public class Launcher {
  private static String proxyUrl;
  private static String botName;

  public static void main(String[] args) throws Exception {
    Launcher.botName = args[0];
    Launcher.proxyUrl = args[1];
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        try {
          Runtime.getRuntime()
              .exec("java -jar "
                  + Launcher.class.getProtectionDomain().getCodeSource().getLocation().getPath()
                  + " " + botName + " " + proxyUrl);
        } catch (IOException e2) {
          e2.printStackTrace();
        }
      }
    });
    (new ServerBot()).startServer(args[1], args[0]);
  }
}
