package com.bot.Thread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class ServerMirror extends Thread {

	private int retryNumber = 5;

	private int openPort;
	private String proxyUrl;
	private String botName;
	private String destHost;
	private String destPort;

	private ServerSocket serverSocket;

	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			Socket sock = null;
			try {
				sock = serverSocket.accept();
				String sockRestId = connectRemoteMirrorSocket(destHost, destPort, sock);
				if (sockRestId == null) {
					sock.close();
					continue;
				}
				SEND send = new SEND();
				send.startSend(proxyUrl.concat("/mirror"), sock, sockRestId);
				RECV recv = new RECV();
				recv.startSend(proxyUrl.concat("/mirror"), sock, sockRestId);
				System.out.println(sock.getInetAddress() + ":" + sock.getPort() + "=>" + openPort + "=>" + proxyUrl
						+ "=>" + botName + "=>" + destHost + ":" + destPort);
			} catch (Exception e) {
				try {
					sock.close();
				} catch (Exception e1) {
				}
			}
		}
		try {
			serverSocket.close();
		} catch (IOException e) {
		}
	}

	public void startServer(int openPort, String proxyUrl, String botName, String destHost, String destPort)
			throws Exception {
		this.openPort = openPort;
		this.proxyUrl = proxyUrl;
		this.botName = botName;
		this.destHost = destHost;
		this.destPort = destPort;
		this.serverSocket = new ServerSocket(openPort);
		start();
	}

	private String connectRemoteMirrorSocket(String destHost, String destPort, Socket sock) {
		String sockRestId = null;
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(proxyUrl.concat("/botGetConnection"));
		post.addHeader("botName", botName);
		try {
			HttpResponse response;
			int ctry = retryNumber;
			do {
				response = client.execute(post);
				ctry--;
				if (ctry == 0) {
					throw new Exception("out of retry");
				}
			} while (response.getStatusLine().getStatusCode() != 200);
			sockRestId = EntityUtils.toString(response.getEntity());
			sendConfigToBot(sockRestId);
		} catch (Exception e) {
			try {
				sock.close();
			} catch (Exception e2) {
			}
		}
		return sockRestId;
	}

	private void sendConfigToBot(String sockRestId) throws Exception {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(proxyUrl.concat("/mirror").concat("/socketHandler"));
		post.addHeader("sockRestId", sockRestId);
		post.setEntity(new StringEntity("ready"));
		HttpResponse response;
		int ctry = retryNumber;
		do {
			response = client.execute(post);
			ctry--;
			if (ctry == 0) {
				throw new Exception("out of retry");
			}
		} while (response.getStatusLine().getStatusCode() != 200);
	}
}
