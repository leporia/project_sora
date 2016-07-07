import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class MainPanel extends JPanel implements ActionListener {

	public JTextField input = new JTextField();
	public JTextPane chat = new JTextPane();
	public JLabel connectedLabel = new JLabel();
	public JButton connectButton;
	public JButton disconnectButton;
	public JButton addButton;
	public JTextField username;
	public JTextField ip;
	public JLabel ipLabel = new JLabel();
	public JButton aboutButton = new JButton("About");
	public String version;

	public MainPanel() {
		this.aboutButton.addActionListener(this);
		this.initUi();
	}

	private void initUi() {
		//Setting login panel
		JPanel loginPanel = new JPanel();

		this.connectButton = new JButton("Connect");
		this.disconnectButton = new JButton("Disconnect");
		this.username = new JTextField();
		this.ip = new JTextField();
		JLabel usernameLabel = new JLabel("Username:");
		JLabel ipLabel = new JLabel("IP:");

		loginPanel.setLayout(new GridLayout(4, 2));
		loginPanel.add(this.aboutButton);
		loginPanel.add(this.ipLabel);
		loginPanel.add(usernameLabel);
		loginPanel.add(this.username);
		loginPanel.add(ipLabel);
		loginPanel.add(this.ip);
		loginPanel.add(this.connectButton);
		loginPanel.add(this.disconnectButton);

		loginPanel.setVisible(true);

		//Setting node panel
		JPanel nodePanel = new JPanel();

		this.connectedLabel = new JLabel();

		nodePanel.setLayout(new BorderLayout());

		nodePanel.add(connectedLabel, BorderLayout.NORTH);

		nodePanel.setVisible(true);

		//Setting side panel
		JPanel sidePanel = new JPanel();

		sidePanel.setPreferredSize(new Dimension(250, 100));
		sidePanel.setMaximumSize(new Dimension(250, 100));

		sidePanel.setLayout(new BorderLayout());

		sidePanel.add(loginPanel, BorderLayout.NORTH);
		sidePanel.add(nodePanel, BorderLayout.SOUTH);

		sidePanel.setVisible(true);

		//Setting main panel
		this.setLayout(new BorderLayout());

		JScrollPane scroll = new JScrollPane(this.chat);

		this.chat.setEditable(false);
		this.chat.setPreferredSize(new Dimension(200, 200));

		this.add(input, BorderLayout.SOUTH);
		this.add(sidePanel, BorderLayout.EAST);
		this.add(scroll, BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.aboutButton) {
			JFrame about = new JFrame("About");
			about.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			about.setSize(new Dimension(200, 150));
			about.setLocationRelativeTo(null);

			JLabel text = new JLabel();
			text.setText(
				"<html><div style='text-align: center;'>"
				+ "Project Sora<br>"
				+ "Version " + this.version + "<br>"
				+ "<br>"
				+ "Local PeerToPeer Chat<br>"
				+ "written by Andrea Lepori<br>"
				+ "</div></html>");

			text.setHorizontalAlignment(JLabel.CENTER);
			
			about.add(text);
			about.setVisible(true);
		}
	}
}
