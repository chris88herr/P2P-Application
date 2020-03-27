// A Java program for a Server 
import java.net.*;
import java.util.ArrayList;
import java.io.*; 

 
public class Registry 
{ 
	
	static int PORT = 8881;

	//initialize socket and input stream 
	private ArrayList<Socket> sockets = null;
	private int socketCount = 0;
	private Socket		 socket = null; 
	private ServerSocket server = null; 
	private DataInputStream in	 = null; 

	// constructor with port 
	public Registry() 
	{ 
		// starts server 
		sockets = new ArrayList<Socket>();

			try {
				server = new ServerSocket(PORT);
				System.out.println("Server started"); 

			} catch (IOException e) {
				e.printStackTrace();
			} 

			
	}
	
	public ServerSocket getServerSocket() {
		return this.server;
	}
	
	public ArrayList<Socket> getServents(){
		return this.sockets;
	}
	
	//adds a servent to the registry
	public void addServent(Socket socket) {
		if(getServents().contains(socket)) {
			getServents().add(socket);
			this.socketCount ++;
		}
	}

	public static void main(String args[]) 
	{ 
		Registry registry = new Registry(); 
		while(true) {
			
			try {
				Socket socket = registry.getServerSocket().accept();
				registry.addServent(socket);
				
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
	} 
} 
