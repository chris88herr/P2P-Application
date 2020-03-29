import java.io.Serializable;
import java.util.Date;

public class HelloMessage implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String msg;
	Date date;
	int localport;
	
	
	public HelloMessage(String s, Date d,int port) {
		msg = s;
		date =d;
		localport =port;
	}
	
}
