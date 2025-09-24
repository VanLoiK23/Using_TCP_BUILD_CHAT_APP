//package controller;
//
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.io.OutputStreamWriter;
//import java.net.Socket;
//
//import javafx.scene.layout.VBox;
//
//public class ServerController {
//	private Socket socket;
//	private BufferedWriter out;
//	private BufferedReader in;
//	private BufferedWriter clientPrintWriter;
//
//	public ServerController(Socket socket, BufferedWriter clientPrintWriter) {
//		this.socket = socket;
//		this.clientPrintWriter = clientPrintWriter;
//	}
//
//	public ServerController() {
//		try {
//			in = new BufferedReader((new InputStreamReader(socket.getInputStream())));
//			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
//
////			synchronized (clientPrintWriter) {
////				clientPrintWriter.
////			}
//
//		} catch (IOException exception) {
//			exception.printStackTrace();
//		} finally {
//			close(socket, in, out);
//		}
//	}
//
//	public void sendtoServer(String message) {
//		try {
//			out.write(message);
//			out.newLine();
//			out.flush();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			close(socket, in, out);
//		}
//	}
//
//	public void receiveFromServer(VBox vbox) {
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				String message;
//
//				try {
//					while ((message = in.readLine()) != null) {
//
//						ClientController clientController = new ClientController();
//						clientController.onSendAndReceiveMessenge(message, vbox, false);
//
//					}
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					close(socket, in, out);
//
//				}
//			}
//		}).start();
//
//	}
//
//	public void close(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
//		try {
//			if (bufferedWriter != null) {
//				bufferedWriter.close();
//			}
//			if (bufferedReader != null) {
//				bufferedReader.close();
//			}
//			if (socket != null) {
//				socket.close();
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//}

package controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

//Client handle
public class ServerController implements Runnable {
	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;

	// Danh sách tất cả client đang kết nối (dùng Vector để quản lý)
	private static Vector<ServerController> clients = new Vector<>();

	public ServerController(Socket socket) {
		try {
			this.socket = socket;
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

			clients.add(this); // thêm client vào danh sách

			System.out.println("Client connected: " + socket.getInetAddress());

		} catch (IOException e) {
			close();
		}
	}

	@Override
	public void run() {
		try {
			String message;
			while ((message = in.readLine()) != null) {
				System.out.println("Received: " + message);
				broadcast(message); // gửi cho tất cả client khác
			}
		} catch (IOException e) {
			close();
		}
	}

	// Gửi tin nhắn cho tất cả client trong Vector
	private void broadcast(String message) {
		for (ServerController client : clients) {
			if (client != this) {
				try {
					if (client.socket.isClosed())
						continue;

					client.out.write(message);
					client.out.newLine();
					client.out.flush();
				} catch (IOException e) {
					client.close();
				}
			}
		}
	}

	// Đóng kết nối
	private void close() {
		try {
			if (in != null)
				in.close();
			if (out != null)
				out.close();
			if (socket != null)
				socket.close();

			clients.remove(this);

			System.out.println("Client disconnected.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
