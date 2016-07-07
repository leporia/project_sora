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
	//TODO add remove from list debug

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

		mainPanel.version = this.version;

		this.initUI();
	}

	private void initUI() {
		mainPanel.ipLabel.setText(linker.localIP);
		mainPanel.input.addActionListener(this);
		mainPanel.connectButton.addActionListener(this);
		mainPanel.disconnectButton.addActionListener(this);

		mainPanel.connectButton.setEnabled(true);
		mainPanel.disconnectButton.setEnabled(false);
	}

	public void start() {
		t = new Thread (this, "listener");
		t.start();
	}

	public void run() {
		while (this.listen) {
			String text = linker.receive();
			if (text != null) {
				System.out.println(text);

				Message decodedMsg =
					gson.fromJson(text, Message.class);

				if (decodedMsg.Type.equals("ERROR")) {
					this.chatPrint(
						"Error: Connection rejected, wrong"
						+ " version"
						, Color.RED);
				} else if (decodedMsg.version.equals(this.version)) {
					switch (decodedMsg.Type) {
						case "COMMAND":
							switch (decodedMsg.cmd) {
								case "Print":
									this.chatPrint("[" + decodedMsg.time + "] "
										+ decodedMsg.username + ": "
										+ decodedMsg.text,
											new Color(0, 100, 0));

									if (decodedMsg.text.equals("left!")) {
										usernameList.remove(
											decodedMsg.username);
										this.printList();
										linker.ipList.remove(decodedMsg.ip);
										System.out.println(
												"User removed from list");
									} else if (
										decodedMsg.text.equals("joined!")) {
										usernameList.add(decodedMsg.username);
										this.printList();
									}
									break;
								case "SendList":
									System.out.println(
										"List send request recived");

									this.sendList(
										decodedMsg.ip, decodedMsg.username);
							}
							break;
						case "MESSAGE":
							this.chatPrint("[" + decodedMsg.time + "] "
								+ decodedMsg.username + ": "
								+ decodedMsg.text, Color.BLACK);
							break;
						case "PEERLIST":
							System.out.println("New list arrived");
							linker.ipList = decodedMsg.ipList;
							usernameList = decodedMsg.usernameList;
							usernameList.add(this.username);
							this.printList();

							this.chatPrint(
									"Connected to the network", Color.BLUE);	

							Message join = new Message();
							join.Type = "COMMAND";
							join.cmd = "Print";
							join.text = "joined!";

							this.send(join);
							break;
						case "PEER":
							System.out.println("New peer ip arrived");
							System.out.println(decodedMsg.ip);
							linker.ipList.add(decodedMsg.ip);
					}

				} else {
					System.out.println("Rejecting connection");
					linker.ipList.remove(decodedMsg.ip);
					Message error = new Message();
					error.version = this.version;
					error.Type = "ERROR";
					error.error = "VERSION";
					System.out.println(gson.toJson(error));
					linker.sendSingle(gson.toJson(error), decodedMsg.ip);

				}
			}
		}
	}

	public void connect() {
		this.listen = true;
		this.start();
		this.connected = true;
		this.username = mainPanel.username.getText();	

		mainPanel.connectButton.setEnabled(false);
		mainPanel.disconnectButton.setEnabled(true);
		mainPanel.username.setEditable(false);
		mainPanel.ip.setEditable(false);

		usernameList.add(this.username);
		this.printList();

		if (mainPanel.ip.getText().equals("localhost")) {
			linker.ipList.add(linker.localIP);
			this.chatPrint(
				"Connection ready, waiting for connections on "
				+ linker.localIP, Color.BLUE);

		} else {
			linker.ipList.add(mainPanel.ip.getText());
			Message msg = new Message();
			msg.Type = "COMMAND";
			msg.cmd = "SendList";

			this.send(msg);
		}
	}

	public void disconnect() {
		Message left = new Message();
		left.Type = "COMMAND";
		left.cmd = "Print";
		left.text = "left!";
		this.send(left);

		this.listen = false;
		mainPanel.connectButton.setEnabled(true);
		mainPanel.disconnectButton.setEnabled(false);
		mainPanel.username.setEditable(true);
		mainPanel.ip.setEditable(true);
		linker.ipList = new ArrayList<String>();
		usernameList = new ArrayList<String>();
		this.chatPrint("Disconnected", Color.BLUE);
		mainPanel.connectedLabel.setText("");
		this.connected = false;
	}

	public void send(Message msg) {
		if (this.connected == true) {
			msg.version = this.version;
			msg.time = timeFormat.format(System.currentTimeMillis());
			msg.ip = linker.localIP;
			msg.username = this.username;
			linker.send(gson.toJson(msg));
		} else {
			this.chatPrint("Error: Not connected", Color.RED);
		}
	}

	public void sendList(String remoteIP, String remoteUsername) {
		if (this.connected == true) {
			Message list = new Message();
			list.version = this.version;
			list.Type = "PEERLIST";
			list.ipList = linker.ipList;
			list.usernameList = usernameList;
			list.time = timeFormat.format(System.currentTimeMillis());
			list.ip = linker.localIP;
			list.username = this.username;

			Message msg = new Message();
			msg.version = this.version;
			msg.Type = "PEER";
			msg.ip = remoteIP;
			msg.username = remoteUsername;
			msg.time = timeFormat.format(System.currentTimeMillis());

			linker.sendList(gson.toJson(list), remoteIP, gson.toJson(msg));
		} else {
			this.chatPrint("Error: Not connected", Color.RED);
		}
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == mainPanel.connectButton) {
			if (mainPanel.username.getText().hashCode() != 0 &&
					mainPanel.ip.getText().hashCode() != 0) {
			this.connect();
			} else {
				this.chatPrint(
					"Please insert a username and a valid ip", Color.RED);
			}
		} else if (e.getSource() == mainPanel.disconnectButton) {
			this.disconnect();
		} else if (e.getSource() == mainPanel.input) {
			JTextField textbox = (JTextField) e.getSource();

			String text = textbox.getText();
			textbox.setText("");

			if (text.equals("/admin")) {
				this.adminPanel = new AdminPanel();
				this.adminPanel.sendBtn.addActionListener(this);
			} else if (text.hashCode() != 0) {
				this.chatPrint("["
					+ timeFormat.format(System.currentTimeMillis())
					+ "] You: " + text, Color.BLACK);

				Message msg = new Message();
				msg.Type = "MESSAGE";
				msg.text = text;

				this.send(msg);
			}
		} else if (e.getSource() == adminPanel.sendBtn) {
			Message msg = new Message();
			msg.version = this.adminPanel.version.getText();
			msg.Type = this.adminPanel.type.getText();
			msg.text = this.adminPanel.text.getText();
			msg.error = this.adminPanel.error.getText();
			msg.cmd = this.adminPanel.cmd.getText();
			msg.ip = this.adminPanel.ip.getText();
			msg.username = this.adminPanel.username.getText();
			if (this.adminPanel.time.getText().equals("now")) {
				msg.time = timeFormat.format(System.currentTimeMillis());
			} else {
				msg.time = this.adminPanel.time.getText();
			}

			linker.send(gson.toJson(msg));
		}
	}

	private void printList() {
		String tmp = "<html>Connected users:<br>" + this.username + "<br>";
		for (int i = 0; i < usernameList.size(); i++) {
			if (!usernameList.get(i).equals(this.username)) {
				tmp = tmp + usernameList.get(i) + "<br>";
			}
		}

		mainPanel.connectedLabel.setText(tmp + "</html>");
	}

	private void chatPrint(String msg, Color c) {
		StyledDocument doc = mainPanel.chat.getStyledDocument();

		Style style = mainPanel.chat.addStyle("Default", null);
		StyleConstants.setForeground(style, c);

		try {
			doc.insertString(doc.getLength(), msg + "\n", style);
		} catch (BadLocationException e) {}

		mainPanel.chat.setCaretPosition(doc.getLength());
	}
}
