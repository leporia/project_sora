import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.UIManager;

public class ChatClient extends Thread {

	public static final Dimension WINDOW_SIZE = new Dimension(800, 400);

	public ChatClient() {
		try {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			//TODO handle this
		}

		ConnectionManager linker = new ConnectionManager();
		MainPanel mainPanel = new MainPanel();
		Manager manager = new Manager(mainPanel, linker);

		JFrame frame = new JFrame("Project Sora - " + manager.version);
		frame.add(mainPanel);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				manager.disconnect();
				System.out.println(
					"Disconnecting, preventing eventuals errors");
				System.exit(0);
			}
		});

		frame.setSize(WINDOW_SIZE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		ChatClient client = new ChatClient();
	}
}
