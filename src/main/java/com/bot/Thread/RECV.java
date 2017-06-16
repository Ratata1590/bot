package com.bot.Thread;

import java.io.PushbackInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class RECV extends LinkAbstract {
  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      CloseableHttpClient client = HttpClientBuilder.create().build();
      HttpGet get = new HttpGet(this.proxyUrl.concat("/socketHandler"));
      get.addHeader("sockRestId", sessionId);
      try {
        HttpResponse response = client.execute(get);
        if (response.getStatusLine().getStatusCode() != 200) {
          throw new Exception("status not 200");
        }
        if (response.getEntity() == null) {
          get.releaseConnection();
          client.close();
          Thread.sleep(LinkAbstract.delay);
          continue;
        }
        PushbackInputStream in = new PushbackInputStream(response.getEntity().getContent());
        int firstByte = in.read();
        if (firstByte == -1) {
          get.releaseConnection();
          client.close();
          Thread.sleep(LinkAbstract.delay);
          continue;
        }
        in.unread(firstByte);
        IOUtils.copy(in, sock.getOutputStream());
        // response.getEntity().writeTo(sock.getOutputStream());
        sock.getOutputStream().flush();
        get.releaseConnection();
        client.close();
      } catch (Exception e) {
        disconnectRemoteSocket();
        break;
      }
    }
  }
}
