package com.bot.Thread;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClientBuilder;

public class SEND extends LinkAbstract {

  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      HttpClient client = HttpClientBuilder.create().build();
      HttpPost post = new HttpPost(proxyUrl.concat("/socketHandler"));
      post.addHeader("sockRestId", sessionId.toString());
      byte[] tmpbuff;
      byte[] resultBuff;
      try {
        resultBuff = new byte[sock.getReceiveBufferSize()];
        tmpbuff = new byte[sock.getInputStream().read(resultBuff, 0, resultBuff.length)];
        System.arraycopy(resultBuff, 0, tmpbuff, 0, tmpbuff.length);
        post.setEntity(new ByteArrayEntity(tmpbuff));

        HttpResponse response;
        int ctry = retry;
        do {
          response = client.execute(post);
          ctry--;
          if (ctry == 0) {
            throw new Exception("out of retry");
          }
        } while (response.getStatusLine().getStatusCode() != 200);
      } catch (Exception e) {
        disconnectRemoteSocket();
        try {
          sock.close();
        } catch (Exception e2) {
        }
        break;
      }
    }
  }
}
