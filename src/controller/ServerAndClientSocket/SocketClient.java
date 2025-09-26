package controller.ServerAndClientSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import model.ChatMessage;
import model.Packet;

public class SocketClient {
	private static SocketClient instance;
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	public Gson gson;
	private static final String SERVER = "10.60.227.189";
	private static final int PORT = 12345;
	private Consumer<ChatMessage> messageHandler;
	public BlockingQueue<Packet> responseQueue;

	private SocketClient() {
		try {
			socket = new Socket(SERVER, PORT);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			gson = Converters.registerAll(new GsonBuilder()).setDateFormat("EEE MMM dd HH:mm:ss z yyyy").create();
			responseQueue = new LinkedBlockingQueue<>();
			
			listenForMessages();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// create singleton
	public static SocketClient getInstance() {
		if (instance == null) {
			instance = new SocketClient();
		}
		return instance;
	}

	public void sendPacket(Packet packet) {
		String json = gson.toJson(packet);
		out.println(json);
		out.flush();
	}

//	public Packet receivePacket() throws IOException {
//		String json = in.readLine();
//		return gson.fromJson(json, Packet.class);
//	}

	public void setMessageHandler(Consumer<ChatMessage> handler) {
		this.messageHandler = handler;
	}

	// receive difference message from other client
	private void listenForMessages() {
		new Thread(() -> {
			try {
				String msg;
				while ((msg = in.readLine()) != null) {
					Packet packet = gson.fromJson(msg, Packet.class);
					if (packet.getType().equals("MESSAGE")) {

						ChatMessage chatMessage = gson.fromJson(gson.toJson(packet.getData()), ChatMessage.class);

//                    ChatMessage chatMessage = gson.fromJson(msg, ChatMessage.class);
						if (messageHandler != null) {
							messageHandler.accept(chatMessage);
						}
					}else {
						responseQueue.offer(packet);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	public void close() {
		try {
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}