import java.util.*;

class Message {

	public String version;
	public MessageType type;
	public MessageCmd cmd;
	public MessageError error;
	public String text;
	public String ip;
	public String username;
	public String time;
	public ArrayList<String> ipList = new ArrayList<String>();
	public ArrayList<String> usernameList = new ArrayList<String>();

	public Message() {

	}
}
