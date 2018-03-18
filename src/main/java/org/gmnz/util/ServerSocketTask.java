package org.gmnz.util;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;


public class ServerSocketTask implements Runnable {

	private ServerSocket server;
	private ServerSocketListener listener;
	private boolean active;

	private static final Logger log = Logger.getLogger(ServerSocketTask.class);




	public ServerSocketTask(String hostname, int port, ServerSocketListener listener) {
		this.listener = listener;
		try {
			InetAddress addr = InetAddress.getByName(hostname);
			server = new ServerSocket(port, 2, addr);
			active = true;
			log.info("initialized shutdown listening socket");
		}
		catch (IOException e) {
			log.error("unable to initialize shutdown socket", e);
			active = false;
		}
	}




	@Override
	public void run() {
		while (active) {
			Socket socket = null;
			ObjectInputStream ois = null;
			try {
				log.info("shutdown listening socket ready");
				socket = server.accept();
				// this should spawn a new thread to handle the request, but we're simply
				// shutting down everything here
				ois = new ObjectInputStream(socket.getInputStream());
				int codeReceived = ois.readInt();
				if (codeReceived == ServerSocketListener.ORDER_66) {
					log.warn("shutdown command received");
					active = false;
					listener.serverShutdownRequested();
				}
			}
			catch (IOException e) {
				e.printStackTrace();
				active = false;
			}
			finally {
				if (ois != null)
					try {
						ois.close();
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				if (socket != null)
					try {
						socket.close();
					}
					catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
	}


}
