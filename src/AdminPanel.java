import java.awt.*;
import javax.swing.*;

public class AdminPanel {

	public static final Dimension WINDOW_SIZE = new Dimension(200, 300);

	public JButton sendBtn;
	public JTextField version;
	public JTextField type;
	public JTextField text;
	public JTextField error;
	public JTextField cmd;
	public JTextField ip;
	public JTextField username;
	public JTextField time;
	
	public AdminPanel() {
		JLabel versionLabel = new JLabel("Version:");
		this.version = new JTextField("1.1");
		JLabel typeLabel = new JLabel("Type:");
		this.type = new JTextField();
		JLabel textLabel = new JLabel("Text:");
		this.text = new JTextField();
		JLabel errorLabel = new JLabel("Error:");
		this.error = new JTextField();
		JLabel cmdLabel = new JLabel("Command:");
		this.cmd = new JTextField();
		JLabel ipLabel = new JLabel("Ip:");
		this.ip = new JTextField();
		JLabel usernameLabel = new JLabel("Username:");
		this.username = new JTextField();
		JLabel timeLabel = new JLabel("Time:");
		this.time = new JTextField("now");

		JLabel emptyLabel = new JLabel();
		sendBtn = new JButton("Send");

		//init main panel
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(9, 2));

		panel.add(versionLabel);
		panel.add(this.version);
		panel.add(typeLabel);
		panel.add(this.type);
		panel.add(textLabel);
		panel.add(this.text);
		panel.add(errorLabel);
		panel.add(this.error);
		panel.add(cmdLabel);
		panel.add(this.cmd);
		panel.add(ipLabel);
		panel.add(this.ip);
		panel.add(usernameLabel);
		panel.add(this.username);
		panel.add(timeLabel);
		panel.add(this.time);
		panel.add(emptyLabel);
		panel.add(sendBtn);

		//init frame
		JFrame frame = new JFrame("Admin Panel");
		frame.add(panel);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(WINDOW_SIZE);
		frame.setLocationRelativeTo(null);

		panel.setVisible(true);
		frame.setVisible(true);
	}
}
