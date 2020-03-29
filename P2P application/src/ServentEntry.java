import java.util.Date;

class ServentEntry{
	  
	private Date date;
	private int localPort;

	public ServentEntry( Date d, int port) {
		  date = d ;
		  localPort = port;
	  }
	
	public int getPort() {
		return localPort;
	}
	
	public Date getDate() {
		return date;
	}
	
  }