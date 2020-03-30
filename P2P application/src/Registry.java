// A Java program for a Server 
import java.net.*;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*; 


	// class to keep track of what servents are 
	//available on network based on their last 
	//communication with server
  

public class Registry 
{ 
	
	static int PORT = 1234;
	static int UDP_PORT = 1233;
	static int TIMEOUT = 3; //timeout in seconds for UDP last contact with a client
	static int IDS_AVAILABLE = 10;

	//initialize socket and input stream 
	private Socket		 	socket = null; 
	private ServerSocket 	server = null; 
	
	int[] idsAvailable;
	
	private DatagramSocket datagramSocket;
	private byte[] receiveBytes;
	private DatagramPacket datagramPacket;
	
	private ConcurrentHashMap<Integer, RegistryWorker> registryWorkers;
	
	//table for available servents
	private ConcurrentHashMap<Socket, Date> serventEntries;
	private ArrayList<Socket> sockets;
	

	// constructor starts server setting up communication
	//ports. TCP and UPD entities are initialized listening to 
	//dedicated ports
	public Registry() 
	{ 
		sockets = new ArrayList<Socket>();
		registryWorkers = new ConcurrentHashMap<Integer,RegistryWorker>();
		serventEntries = new ConcurrentHashMap<Socket, Date>();
		idsAvailable = new int[IDS_AVAILABLE];
		generateIds();

			try {
				
				//bind the sockets to the ports to establish communication
				server = new ServerSocket(PORT);
				datagramSocket = new DatagramSocket(UDP_PORT);
				receiveBytes = new byte[1000];
				
				datagramPacket = new DatagramPacket(receiveBytes, receiveBytes.length);
				System.out.println("Server started: "+server); 

			} catch (IOException e) {
				e.printStackTrace();
			} 
			
	}
	
	public ServerSocket getServerSocket() {
		return this.server;
	}

	public DatagramSocket getDatagramSocket() {
		return datagramSocket;
	}
	

	
	//adds a servent to the registry
	public void addServent(Socket socket) {
		if(!serventEntries.containsKey(socket)) {
			System.out.println("socket coming in:  "+socket);
			//start a registryWorker
			RegistryWorker rw = new RegistryWorker(socket, sockets);
			rw.start();
			registryWorkers.put(getFreshId(), rw);
			serventEntries.put(socket, new Date());
			sockets.add(socket);	
//			System.out.println("added! "+ serventEntries.size());
		}
	}
	
	
	//spawns a new thread to listen to the UDP port for messages from
	//all servents connected to our server
	public void receiveDatafromUDP() {
		Thread t = new Thread() {
			@Override
			public void run() {
				while(true) {
					try {
						datagramSocket.receive(datagramPacket);

						ByteArrayInputStream byteStream = new
                                ByteArrayInputStream(receiveBytes);
						ObjectInputStream is = new
						           ObjectInputStream(new BufferedInputStream(byteStream));
						
						HelloMessage hm = (HelloMessage)is.readObject();
						
//						String line = data(receiveBytes).toString();
						System.out.println(hm.msg+" -- "+ hm.localport);
						updateUDPTable(hm.localport, hm.date);
						
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
		 
		Thread checkUDPTableThread = new Thread() {
			@Override
			public void run() {
				
				while(true) {
//					System.out.println("size: " +serventEntries.size());
//					for(int i=0; i<idsAvailable.length;i++) {
//						System.out.print(idsAvailable[i]+",");
//					}
//					System.out.println();
					for(Map.Entry<Socket, Date> entry: serventEntries.entrySet()) {
						
						if(isLongerThan(TIMEOUT, new Date(), entry.getValue())) {
							deleteEntry(entry);
						}
					}
					
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
				}
			}
		};
		checkUDPTableThread.start();
		
	}
	
	
	
	public void updateUDPTable(int port, Date date) {
		for(Socket s : serventEntries.keySet()) {
			if(port == s.getPort()) {
				Date dateReplaced=null;
				dateReplaced=serventEntries.replace(s, date);
			}
		}
	}
	public void returnIdToTable(int i) {
		if(i>0 || i <= idsAvailable.length)
			idsAvailable[i-1] = i;
		else
			System.out.println("invalid id return:"+i);
	}
	
	public int getFreshId() {
		int id = 0;
		for(int i = 0; i<idsAvailable.length; i++) {
			if(idsAvailable[i] != 0) {
				id =idsAvailable[i];
				//mark it as used with a 0
				idsAvailable[i] =0;
				return id;
			}
		}
		return id;
	}
	
	public void generateIds() {
		for(int i = 0; i<idsAvailable.length; i++) {
			idsAvailable[i] = i+1;
		}
	}
	
	public void deleteEntry( Map.Entry<Socket, Date> entry) {
		//delete socket from list
		Socket socket = null;
		for(Socket s : sockets) {
			if (s.getLocalPort() == entry.getKey().getPort())
				socket = s;
				
		}
		if(socket!=null) {
			sockets.remove(socket);
		}
		
		for(Map.Entry<Integer, RegistryWorker> current : registryWorkers.entrySet()) {

			if(current.getValue().serventSoccket.getPort() == entry.getKey().getPort()) {
				current.getValue().interuptThread();;
				registryWorkers.remove(current.getKey());
				returnIdToTable(current.getKey());
			}
				
		}
		
		serventEntries.remove(entry.getKey());
	}
	
	//helper function to check for a ServentEntry

	
	//function to use for timeouts in communications over UDP
	public boolean isLongerThan(int timeInSeconds, Date d1, Date d2) {
		
		long diff = d1.getTime() - d2.getTime();
		int diffSec =  (int) diff / 1000; // difference in seconds
		if(diffSec >= timeInSeconds)
			return true;
		else 
			return false;
	}
	
	//helper function to transform byte data into string
	public static StringBuilder data(byte[] a) 
	    { 
	        if (a == null) 
	            return null; 
	        StringBuilder ret = new StringBuilder(); 
	        int i = 0; 
	        while (a[i] != 0) 
	        { 
	            ret.append((char) a[i]); 
	            i++; 
	        } 
	        return ret; 
	    } 

	
	//server driver
	public static void main(String args[]) 
	{ 
		
		//start server
		Registry registry = new Registry();
		
		// set up UDP thread to start listening for Datagrams
		registry.receiveDatafromUDP();
		
		while(true) {
			
			try {
				//listen for more connections
				System.out.println("waitng for connection...");
				Socket socket = registry.getServerSocket().accept();
				registry.addServent(socket);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	} 
} 
