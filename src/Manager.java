import java.util.*;
import java.text.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import com.google.gson.Gson;

class Manager implements ActionListener, Runnable {

	//TODO fix rejoin bug
	//TODO make your messages more outstanding

	public static final String version = "1.1";

	private MainPanel mainPanel;
	private ConnectionManager linker;
	private AdminPanel adminPanel;

	private ArrayList<String> usernameList = new ArrayList<String>();
	private boolean listen = false;
	private boolean connected = false;
	private String username;
	private Thread t;
	private Gson gson = new Gson();
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

	public Manager(MainPanel mainPanel, ConnectionManager linker) {
		this.mainPanel = mainPanel;
		this.linker = linker;
		this.mainPanel.version = this.version;
		
		this.initUI();
	}

	private void initUI() {
		this.mainPanel.ipLabel.setText(linker.localIP);
		this.mainPanel.input.addActionListener(this);
		this.mainPanel.connectButton.addActionListener(this);
		this.mainPanel.disconnectButton.addActionListener(this);
		this.mainPanel.ip.addActionListener(this);

		this.mainPanel.connectButton.setEnabled(true);
		this.mainPanel.disconnectButton.setEnabled(false);
	}

	public void start() {
		t = new Thread (this, "listener");
		t.start();
	}

	public void run() {
		while (this.listen) {
			String text = this.linker.receive();
			if (text != null) {
				System.out.println(text);

				Message decodedMsg =
					gson.fromJson(text, Message.class);

				if (decodedMsg.type.equals(MessageType.ERROR)) {
					this.chatPrint(
						"Error: Connection rejected, wrong"
						+ " version"
						, Color.RED);
				} else if (decodedMsg.version.equals(this.version)) {
					switch (decodedMsg.type) {
					case COMMAND:
						switch (decodedMsg.cmd) {
						case PRINT:
							this.chatPrint("[" + decodedMsg.time + "] "
								+ decodedMsg.username + ": "
								+ decodedMsg.text,
									new Color(0, 100, 0));

						case LEFT:
							this.usernameList.remove(decodedMsg.username);
							this.printList();
							linker.ipList.remove(decodedMsg.ip);
							System.out.println("User removed from list");
							this.chatPrint("[" + decodedMsg.time + "] "
								+ decodedMsg.username + " left!"
								, new Color(0, 100, 0));
							break;
						case JOIN:
							this.usernameList.add(decodedMsg.username);
							this.printList();
							this.chatPrint("[" + decodedMsg.time + "] "
								+ decodedMsg.username + " joined!"
								, new Color(0, 100, 0));
							break;
						case SENDLIST:
							System.out.println("List send request recived");
							this.sendList(decodedMsg.ip, decodedMsg.username);
						}
						break;
					case MESSAGE:
						this.chatPrint("[" + decodedMsg.time + "] "
							+ decodedMsg.username + ": "
							+ decodedMsg.text, Color.BLACK);
						break;
					case PEERLIST:
						System.out.println("New list arrived");
						this.linker.ipList = decodedMsg.ipList;
						this.usernameList = decodedMsg.usernameList;
						this.usernameList.add(this.username);
						this.printList();

						this.chatPrint(
								"Connected to the network", Color.BLUE);	

						Message join = new Message();
						join.type = MessageType.COMMAND;
						join.cmd = MessageCmd.JOIN;

						this.send(join);
						break;
					case PEER:
						System.out.println("New peer ip arrived");
						System.out.println(decodedMsg.ip);
						this.linker.ipList.add(decodedMsg.ip);
					}

				} else {
					System.out.println("Rejecting connection");
					this.linker.ipList.remove(decodedMsg.ip);
					Message error = new Message();
					error.version = this.version;
					error.type = MessageType.ERROR;
					error.error = MessageError.VERSION;
					System.out.println(gson.toJson(error));
					this.linker.sendSingle(gson.toJson(error), decodedMsg.ip);
				}
			}
		}
	}

	public void connect() {
		this.listen = true;
		this.start();
		this.connected = true;
		this.username = mainPanel.username.getText();	

		this.mainPanel.connectButton.setEnabled(false);
		this.mainPanel.disconnectButton.setEnabled(true);
		this.mainPanel.username.setEditable(false);
		this.mainPanel.ip.setEditable(false);

		this.usernameList.add(this.username);
		this.printList();

		if (this.mainPanel.ip.getText().equals("localhost")) {
			this.linker.ipList.add(linker.localIP);
			this.chatPrint(
				"Connection ready, waiting for connections on "
				+ this.linker.localIP, Color.BLUE);

		} else {
			this.linker.ipList.add(mainPanel.ip.getText());
			Message msg = new Message();
			msg.type = MessageType.COMMAND;
			msg.cmd = MessageCmd.SENDLIST;

			this.send(msg);
		}
	}

	public void disconnect() {
		Message left = new Message();
		left.type = MessageType.COMMAND;
		left.cmd = MessageCmd.LEFT;
		this.send(left);

		this.listen = false;
		this.mainPanel.connectButton.setEnabled(true);
		this.mainPanel.disconnectButton.setEnabled(false);
		this.mainPanel.username.setEditable(true);
		this.mainPanel.ip.setEditable(true);
		this.linker.ipList = new ArrayList<String>();
		this.usernameList = new ArrayList<String>();
		this.chatPrint("Disconnected", Color.BLUE);
		this.mainPanel.connectedLabel.setText("");
		this.connected = false;
	}

	public void send(Message msg) {
		if (this.connected == true) {
			msg.version = this.version;
			msg.time = timeFormat.format(System.currentTimeMillis());
			msg.ip = linker.localIP;
			msg.username = this.username;
			this.linker.send(gson.toJson(msg));
		} else {
			this.chatPrint("Error: Not connected", Color.RED);
		}
	}

	public void sendList(String remoteIP, String remoteUsername) {
		if (this.connected == true) {
			Message list = new Message();
			list.version = this.version;
			list.type = MessageType.PEERLIST;
			list.ipList = linker.ipList;
			list.usernameList = usernameList;
			list.time = timeFormat.format(System.currentTimeMillis());
			list.ip = linker.localIP;
			list.username = this.username;

			Message msg = new Message();
			msg.version = this.version;
			msg.type = MessageType.PEER;
			msg.ip = remoteIP;
			msg.username = remoteUsername;
			msg.time = timeFormat.format(System.currentTimeMillis());

			this.linker.sendList(
					gson.toJson(list), remoteIP, gson.toJson(msg));
		} else {
			this.chatPrint("Error: Not connected", Color.RED);
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.mainPanel.connectButton
				|| e.getSource() == this.mainPanel.ip) {
			if (this.mainPanel.username.getText().hashCode() != 0
					&& this.mainPanel.ip.getText().hashCode() != 0
					&& !this.connected) {
				this.connect();
			} else {
				this.chatPrint(
					"Please insert a username and a valid ip", Color.RED);
			}
		} else if (e.getSource() == this.mainPanel.disconnectButton) {
			this.disconnect();
		} else if (e.getSource() == this.mainPanel.input) {
			JTextField textbox = (JTextField) e.getSource();

			String text = textbox.getText();
			textbox.setText("");

			if (text.equals("/admin")) {
				this.adminPanel = new AdminPanel();
				this.adminPanel.sendBtn.addActionListener(this);
			} else if (text.hashCode() != 0) {
				this.chatPrint("["
					+ this.timeFormat.format(System.currentTimeMillis())
					+ "] You: " + text, Color.BLACK);

				Message msg = new Message();
				msg.type = MessageType.MESSAGE;
				msg.text = text;

				this.send(msg);
			}
		} else if (e.getSource() == adminPanel.sendBtn) {
			//TODO get this working

			/*
			Message msg = new Message();
			msg.version = this.adminPanel.version.getText();
			msg.type = this.adminPanel.type.getText();
			msg.text = this.adminPanel.text.getText();
			msg.error = this.adminPanel.error.getText();
			msg.cmd = this.adminPanel.cmd.getText();
			msg.ip = this.adminPanel.ip.getText();
			msg.username = this.adminPanel.username.getText();
			if (this.adminPanel.time.getText().equals("now")) {
				msg.time = this.timeFormat.format(System.currentTimeMillis());
			} else {
				msg.time = this.adminPanel.time.getText();
			}

			linker.send(gson.toJson(msg));
			*/
		}
	}

	private void printList() {
		String tmp = "<html>Connected users:<br>" + this.username + "<br>";
		for (int i = 0; i < usernameList.size(); i++) {
			if (!this.usernameList.get(i).equals(this.username)) {
				tmp = tmp + usernameList.get(i) + "<br>";
			}
		}

		mainPanel.connectedLabel.setText(tmp + "</html>");
	}

	private void chatPrint(String msg, Color c) {
		StyledDocument doc = this.mainPanel.chat.getStyledDocument();

		Style style = this.mainPanel.chat.addStyle("Default", null);
		StyleConstants.setForeground(style, c);

		try {
			doc.insertString(doc.getLength(), msg + "\n", style);
		} catch (BadLocationException e) {}

		this.mainPanel.chat.setCaretPosition(doc.getLength());
	}
}
