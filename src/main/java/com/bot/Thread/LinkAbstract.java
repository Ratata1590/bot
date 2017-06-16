package com.bot.Thread;

import java.net.Socket;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public abstract class LinkAbstract extends Thread {
  protected String proxyUrl;
  protected Socket sock;
  protected String sessionId;

  protected int retry = 5;
  
  public static int delay = 1000;

  public void startSend(String proxyUrl, Socket sock, String sessionId) {
    this.proxyUrl = proxyUrl;
    this.sock = sock;
    this.sessionId = sessionId;
    start();
  }

  protected void disconnectRemoteSocket() {
    CloseableHttpClient client = HttpClientBuilder.create().build();
    HttpPost post = new HttpPost(proxyUrl.concat("/socketControl/disconnect"));
    post.setHeader("sessionId", sessionId);
    System.out.println("disconnectRemoteSocket recv send");
    try {
      client.execute(post);
      post.releaseConnection();
      client.close();
    } catch (Exception e2) {
    }
    try {
      sock.close();
    } catch (Exception e) {
    }
  }
}
