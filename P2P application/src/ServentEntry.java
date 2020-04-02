import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

class ServentEntry{
	  
	private Date date; 
	private Socket socket;
	private ArrayList<String> serventFiles;
	private String serverAddress;

	public ServentEntry( Date d, Socket s, ArrayList<String> files, String serverAddress) {
		  date = d ;
		  socket = s;
		  serventFiles = new ArrayList<String>(files);
		  this.serverAddress=serverAddress;
	  }
	
	public int getPort() {
		return socket.getPort();
	}
	
	public String getRemoteAddress() {
		return serverAddress;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setServentFiles(ArrayList<String> files) {
		
		if(serventFiles == null) {
			serventFiles = new ArrayList<String>();
		}
		
		serventFiles = files;
	}
	
	public ArrayList<String> getFiles(){
		return serventFiles;
	}
	
	public void changeDate(Date d) {
		if (date == null) {
			date = new Date();
		}
		date = d;
	}
	
	public Socket getSocket() {
		return socket;
	}
	
  }