package com.integration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MicropriceIntegrationTestUtil {
	
	public static final String ExchangeA_TCPServer = "ExchangeA-TCPData";
	public static final String ExchangeB_MDFeedServer = "ExchangeB_MDFeedServer";
	public static final String ExchangeB_MDFeedHandler = "ExchangeB_MDFeedHandler";
	public static final String ExchangeB_TCPData = "ExchangeB_TCPData";
	
	public static void logMessages(List<String> messages, String process) {
		String path = File.separator + "tmp" + File.separator + process + ".dat";
		try {
			createFile(path);
			System.out.println("Messages are copied to: "+path);
			try (BufferedWriter bufferredWriter = new BufferedWriter(new FileWriter(path))) {
				for (String message : messages) {
					bufferredWriter.write(message);
					bufferredWriter.newLine();
				}
			} finally {}
		} catch (IOException e) {
            e.printStackTrace();
		}
	}
	
	static void createFile(String path) throws IOException {
		File file = new File(path);
		if (file.exists()) file.delete();
		file.createNewFile();
	}

}
