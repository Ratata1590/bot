package com.bot;

import java.io.File;
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
        Launcher.restart();
      }
    });
    (new ServerBot()).startServer(args[1], args[0], null);
  }

  public static void restart() {
    try {
      Runtime.getRuntime().exec("java -jar "
          + new File(Launcher.class.getProtectionDomain().getCodeSource().getLocation().getPath())
              .getName()
          + " " + botName + " " + proxyUrl);
    } catch (IOException e2) {
      e2.printStackTrace();
    }
  }
}
