package com.bot.Thread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class Server extends Thread {

	private int openPort;
	private String proxyUrl;
	private String destHost;
	private String destPort;

	private ServerSocket serverSocket;

	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			Socket sock = null;
			try {
				sock = serverSocket.accept();
				String sockRestId = connectRemoteSocket(destHost, destPort, sock);
				if (sockRestId == null) {
					sock.close();
					continue;
				}
				SEND send = new SEND();
				send.startSend(proxyUrl, sock, sockRestId);
				RECV recv = new RECV();
				recv.startSend(proxyUrl, sock, sockRestId);
				System.out.println(sock.getInetAddress() + ":" + sock.getPort() + "=>" + openPort + "=>" + proxyUrl
						+ "=>" + destHost + ":" + destPort);
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

	public void startServer(int openPort, String proxyUrl, String destHost, String destPort) throws Exception {
		this.openPort = openPort;
		this.proxyUrl = proxyUrl;
		this.destHost = destHost;
		this.destPort = destPort;
		this.serverSocket = new ServerSocket(openPort);
		start();
	}

	private String connectRemoteSocket(String destHost, String destPort, Socket sock) {
		HttpClient client = HttpClientBuilder.create().build();
		HttpPost post = new HttpPost(proxyUrl.concat("/socketControl/connect"));
		String sockRestId = null;
		post.addHeader("host", destHost);
		post.addHeader("port", destPort);
		try {
			HttpResponse response;
			int ctry = 5;
			do {
				response = client.execute(post);
				ctry--;
				if (ctry == 0) {
					throw new Exception("out of retry");
				}
			} while (response.getStatusLine().getStatusCode() != 200);
			sockRestId = EntityUtils.toString(response.getEntity());
		} catch (Exception e) {
			try {
				sock.close();
			} catch (Exception e2) {
			}
		}
		return sockRestId;
	}

}
