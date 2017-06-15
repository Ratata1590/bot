package com.bot.Thread;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

public class RECV extends LinkAbstract {
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      HttpClient client = HttpClientBuilder.create().build();
      HttpGet get = new HttpGet(this.proxyUrl.concat("/socketHandler"));
      get.addHeader("sockRestId", sessionId);
      try {
        HttpResponse response;
        int ctry = retry;
        do {
          Thread.sleep(1000);
          response = client.execute(get);
          ctry--;
          if (ctry == 0) {
            throw new Exception("out of retry");
          }
        } while (response.getStatusLine().getStatusCode() != 200);
        if (response.getEntity().getContent().available() != 0) {
          response.getEntity().writeTo(sock.getOutputStream());
          sock.getOutputStream().flush();
        } else {
          Thread.sleep(1000);
        }
      } catch (Exception e) {
        disconnectRemoteSocket();
        break;
      }
    }
  }
}
