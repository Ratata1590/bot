package com.bot.Thread;

import java.net.Socket;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;

public abstract class LinkAbstract extends Thread {
  protected String proxyUrl;
  protected Socket sock;
  protected String sessionId;

  protected int retry = 5;

  public void startSend(String proxyUrl, Socket sock, String sessionId) {
    this.proxyUrl = proxyUrl;
    this.sock = sock;
    this.sessionId = sessionId;
    start();
  }

  protected void disconnectRemoteSocket() {
    HttpClient client = HttpClientBuilder.create().build();
    HttpPost post = new HttpPost(proxyUrl.concat("/socketControl/disconnect"));
    try {
      HttpResponse response;
      int ctry = 5;
      do {
        post.setHeader("sessionId", sessionId);
        response = client.execute(post);
        ctry--;
        if (ctry == 0) {
          throw new Exception("out of retry");
        }
      } while (response.getStatusLine().getStatusCode() != 200);
    } catch (Exception e) {
      try {
        sock.close();
      } catch (Exception e2) {
      }
    }
  }
}
