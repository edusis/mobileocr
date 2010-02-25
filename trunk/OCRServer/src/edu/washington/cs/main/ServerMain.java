package edu.washington.cs.main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
	private static ServerSocket mobileListen;
	private static final int MOBILE_PORT = 9897;
	
	public static void main(String[] args) {
		
		System.out.println("[  OK  ] Server started.");
		try {
			mobileListen = new ServerSocket(MOBILE_PORT);
			
			
			while(true) {
				System.out.println("[  OK  ] Waiting for connection..");
				Socket clientSocket = mobileListen.accept();
				
				System.out.println("[  OK  ] Connection established");
				ConnectionHandler cc = new ConnectionHandler(clientSocket);
			}
		}catch(Exception e) {
			System.out.println("Exception:"+e.getMessage());
		}
		
	}
}
