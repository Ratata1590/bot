package com.bot;

import java.io.File;

import com.bot.Thread.ServerBot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Launcher {

	public static void main(String[] args) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		for (String config : args) {
			JsonNode configNode = mapper.readTree(new File(config));
			int poolSize = 2;
			if (configNode.has("poolSize")) {
				poolSize = configNode.get("poolSize").asInt();
			}
			for (int i = 0; i < poolSize; i++) {
				ServerBot server = new ServerBot();
				server.startServer(configNode.get("proxyUrl").asText(), configNode.get("botName").asText());
			}
		}
	}
}
