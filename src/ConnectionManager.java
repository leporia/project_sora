import java.net.*;
import java.io.*;
import java.util.*;

class ConnectionManager {

	public int port = 6789;
	public ArrayList<String> ipList = new ArrayList<String>();
	public String localIP;
	public ServerSocket socket;
	public Socket remoteSocket;
	public String msg;
	public boolean accepted;

	public ConnectionManager() {
		try {
			localIP = Inet4Address.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) { e.printStackTrace(); }

	}

	public String receive() {
		try {
			accepted = false;

			socket = new ServerSocket();
			socket.setReuseAddress(true);
			socket.bind(new InetSocketAddress(this.port));
			socket.setSoTimeout(0);

			remoteSocket = socket.accept();
			accepted = true;

			InetSocketAddress sockaddr = 
				(InetSocketAddress)remoteSocket.getRemoteSocketAddress();
			InetAddress inaddr = sockaddr.getAddress();
			Inet4Address in4addr = (Inet4Address)inaddr;
			String remoteIP = in4addr.toString().substring(1);

			System.out.println("Message received from: " + remoteIP);

			if (!this.ipList.contains(remoteIP)) {
				System.out.println("Address not in list, adding...");
				this.ipList.add(remoteIP);
			}

			PrintWriter out =
				new PrintWriter(remoteSocket.getOutputStream(), true);

			BufferedReader in = new BufferedReader(
				new InputStreamReader(remoteSocket.getInputStream()));

			String msg = in.readLine();

			remoteSocket.close();
			socket.close();
			
			return msg;
		} catch (IOException e) { 
			//e.printStackTrace();

			try {
				socket.close();
				if (accepted) {
					remoteSocket.close();
				}
			} catch (IOException ei) {
					e.printStackTrace();
			}
		}
		return null;
	}

	public void send(String msg) {
		System.out.println(msg);
		for (int i = 0; i < ipList.size(); i++) {
			System.out.println("Sending message at " + ipList.get(i));
			if (!ipList.get(i).equals(localIP)) {
				try {
					Socket socket = new Socket(ipList.get(i), port);

					PrintWriter out = new PrintWriter(
						socket.getOutputStream(), true);

					BufferedReader in = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));

					out.println(msg);
					System.out.println("Message succesful sent!");
					socket.close();

				} catch (Exception e) { 
					System.out.println(
						"Incoming warning from last connection");
					e.printStackTrace();
				}
			}
		}
	}

	public void sendList(String list, String ip, String msg) {
		System.out.println("Sending new list");

		try {
			Socket socket = new Socket(ip, port);

			PrintWriter out = new PrintWriter(
				socket.getOutputStream(), true);

			BufferedReader in = new BufferedReader(
				new InputStreamReader(socket.getInputStream()));

			out.println(list);
			System.out.println("Message succesful sent!");
			socket.close();

		} catch (Exception e) { 
			System.out.println("Incoming warning from last connection");
			e.printStackTrace();
			}

		for (int i = 0; i < ipList.size(); i++) {
			if (!ipList.get(i).equals(ip) && !ipList.get(i).equals(localIP)) {
				try {
					Socket socket = new Socket(ipList.get(i), port);

					PrintWriter out = new PrintWriter(
						socket.getOutputStream(), true);

					BufferedReader in = new BufferedReader(
						new InputStreamReader(socket.getInputStream()));

					out.println(msg);
					System.out.println("Message succesful sent!");
					socket.close();

				} catch (Exception e) { 
					System.out.println("Incoming warning from last connection");
					e.printStackTrace();
					}
			}
		}
	}

	public void sendSingle(String msg, String ip) {
		System.out.println("Sending message at only " + ip);

		try {
			Socket socket = new Socket(ip, this.port);

			PrintWriter out = new PrintWriter(
				socket.getOutputStream(), true);

			BufferedReader in = new BufferedReader(
				new InputStreamReader(socket.getInputStream()));

			out.println(msg);
			System.out.println("Message succesful sent!");
			socket.close();

		} catch (Exception e) { 
			System.out.println("Incoming warning from last connection");
			e.printStackTrace();
			}
	}
}
