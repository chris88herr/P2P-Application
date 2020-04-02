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
	static int IDS_AVAILABLE = 3;

	//initialize socket and input stream 
	private ServerSocket 	server = null; 
	
	private static int[] idsAvailable;
	
	private DatagramSocket datagramSocket;
	private byte[] receiveBytes;
	private DatagramPacket datagramPacket;
	
	private ArrayList<RegistryWorker> registryWorkers;
	
	//table for available servents
	private ArrayList< ServentEntry> serventEntries;
	private ArrayList<Socket> sockets;
	

	// constructor starts server setting up communication
	//ports. TCP and UPD entities are initialized listening to 
	//dedicated ports
	public Registry() 
	{ 
		sockets = new ArrayList<Socket>();
		registryWorkers = new ArrayList<RegistryWorker>();
		serventEntries = new ArrayList< ServentEntry>();
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
			
		receiveDataFromUDP();
	}
	
//	public ServerSocket getServerSocket() {
//		return this.server;
//	}
//
//	public DatagramSocket getDatagramSocket() {
//		return datagramSocket;
//	}
	
	//adds a servent to the registry
	public void addServent(Socket socket) {
		
			System.out.println("socket coming in:  "+socket);
			OutputStream out;
			InputStream in ;
			try {
				int id  = getFreshId();
				out = socket.getOutputStream();
				in = socket.getInputStream();
				RegistryWorker rw = new RegistryWorker(id,this, socket, sockets, out, in);
				rw.start();
				registryWorkers.add(rw);
				sockets.add(socket);	
			} catch (IOException e) {
				e.printStackTrace();
			}
		
	}
	
	//spawns a new thread to listen to the UDP port for messages from
	//all servents connected to our server
	public void receiveDataFromUDP() {
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
						//System.out.println(hm.msg+" -- "+ hm.localport);
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
					for(RegistryWorker rw: registryWorkers) {
						
						if(isLongerThan(TIMEOUT, new Date(), rw.getServentEntry().getDate())) {
							System.out.println("deleting: "+rw.getServentEntry().getPort());
							rw.closeConnectionWithServent();
						}
					}
					
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
				}
			}
		};
		checkUDPTableThread.start();
		
	}
	 
	
	public void updateUDPTable(int port, Date date) {
		
		for(ServentEntry s : serventEntries) {
			if(port == s.getPort()) {
				s.changeDate(date);
			}
		}
	}
	
	public  void returnIdToTable(int i) {
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
	
	public String searchTableForFile(String fileLookedUp) {
		for(ServentEntry entry : serventEntries ){
			for(String file : entry.getFiles()) {
				//System.out.println("looking  |"+file+"|  |"+fileLookedUp+"| ---"+(fileLookedUp.equals(file)));

				if(fileLookedUp.equals(file))
				{
					return entry.getRemoteAddress();
				}
			}
		
		}
		return "FILE NOT FOUND ON REGISTRY";
	}
	
	
	public void deleteServentEntryFromList(ServentEntry serventEntry)
	{
		System.out.println("before deleting entries... "+serventEntries.size());
		serventEntries.remove(serventEntry);
		System.out.println("after deleting entries... "+serventEntries.size());

	}
	
	public void addServentEntry(ServentEntry serventEntry) {

		serventEntries.add( serventEntry);
	}
	
	public void removeRegistryWorkerFromList(RegistryWorker registryWroker) {
		System.out.println("before deleting workers... "+registryWorkers.size());
		registryWorkers.remove(registryWroker);
		
		System.out.println("after deleting wrokers... "+registryWorkers.size());

	}
	
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
	
	public void runReport() {
		Thread t = new Thread() {
			@Override
			public void run() {
				while(true) {
					if(registryWorkers.size()==0) 
						System.out.println("no workers");
					else {
						System.out.println("workers: "+registryWorkers.size());
						for(RegistryWorker rw : registryWorkers){
							rw.printSockets();
							System.out.println("is alive?"+rw.isAlive());
						}
					}
					
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		t.start();
	}

	private void acceptConnection() throws IOException {
		addServent( server.accept());
	}
	//server driver
	public static void main(String args[]) 
	{
		//start server
		Registry registry = new Registry();		
		
//		registry.runReport();
		
		while(true) {
			
			try {
				//listen for more connections
				System.out.println("waitng for connection...");
				registry.acceptConnection();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	} 
} 
