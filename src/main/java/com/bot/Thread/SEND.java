package com.bot.Thread;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class SEND extends LinkAbstract {

  public void run() {
    while (!Thread.currentThread().isInterrupted()) {
      CloseableHttpClient client = HttpClientBuilder.create().build();
      HttpPost post = new HttpPost(proxyUrl.concat("/socketHandler"));
      post.addHeader("sockRestId", sessionId);
      byte[] tmpbuff;
      byte[] resultBuff;
      try {
        resultBuff = new byte[sock.getReceiveBufferSize()];
        tmpbuff = new byte[sock.getInputStream().read(resultBuff, 0, resultBuff.length)];
        System.arraycopy(resultBuff, 0, tmpbuff, 0, tmpbuff.length);
        post.setEntity(new ByteArrayEntity(tmpbuff));
        System.out.println("send:"+tmpbuff);
        HttpResponse response = client.execute(post);
        if (response.getStatusLine().getStatusCode() != 200) {
          throw new Exception("status not 200");
        }
        post.releaseConnection();
        client.close();

      } catch (Exception e) {
        disconnectRemoteSocket();
        break;
      }
    }
  }
}
