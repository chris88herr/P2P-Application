// A Java program for a Client 
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.io.*; 





public class Servent 
{ 
	static int PORT = 1234;
	static int UDP_PORT = 1233;
	static int UDP_MESSAGE_INTERVAL = 1000; //milliseconds
	static String SEARCHCOMMAND = "search";
	static String CLOSE_REGISTRY_CONNECTION = "Close_Registry_Connection";
	static String STATUS_CONNECTION_CLOSED = "CLOSED_CONNECTION";
	public static final String SENDING_FILE = "SENDING_FILE";

	
	// TCP socket 
	private Socket socket;
	public ObjectOutputStream out;
	public ObjectInputStream in;
	
	//UDP communication variables
	DatagramSocket datagramSocket;
	byte[] buffer;
	DatagramPacket datagramPacket;
	
	private static ArrayList<String> files;
	
	Thread helloThread;
	

	// constructor to put ip address and port 
	public Servent(String address, int port) 
	{ 
		// establish a connection 
		try
		{ 
			socket = new Socket(address, port);
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
			

			//send the files
			
	        datagramSocket = new DatagramSocket(); 
	        helloThread = publish();
			System.out.println("Connected to the registry: "+ socket.getRemoteSocketAddress().toString()+"---"+socket.getPort()); 
			
		} 
		catch(UnknownHostException u) 
		{ 
			System.out.println(u); 
		} 
		catch(IOException i) 
		{ 
			System.out.println(i); 
		} 
	} 
	
	//sends files to socket connected to. Starts transmitting
	//messages through UDP as required to stay on its registry table
	//of servants. Messages are sent on an interval specified in UDP_MESSAGE_INTERVAL
	public Thread publish()  {
		
		if(socket !=null) {
			try {
				System.out.println("publishing...");
				out.writeObject(getFiles());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return sendHellothroughUDP();
	}

	// closes the connection 
	public void closeConnection() {
		
		try
			{  
				socket.close(); 
				out.close();
				in.close();
				datagramSocket.close();
				System.out.print("closed");
			} 
		catch(IOException i) 
			{ 
				System.out.println(i); 
			} 
	}
	
	//This function creates a thread where the servent can
	// send a hello message through the UDP port so that the 
	//registry can receive every 60 seconds
	private Thread sendHellothroughUDP() {
		InetAddress ip;
		try {
			ip = InetAddress.getLocalHost();
			String hello = "Hello from "+this.toString()+"\n";
			buffer = new byte[1000];
			buffer =  hello.getBytes();
			Thread t = new Thread() {
				@Override
				public void run() {
					while(true) {
					try {
						
						ByteArrayOutputStream byteStream = new ByteArrayOutputStream(5000);
						ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
						HelloMessage helloMsg = new HelloMessage("Hello ", new Date(), socket.getLocalPort());
						os.flush();
						os.writeObject(helloMsg);
						os.close();
						
						buffer = byteStream.toByteArray();
						
						datagramPacket = new DatagramPacket(buffer, buffer.length,ip,UDP_PORT);

						datagramSocket.send(datagramPacket);
//						System.out.println("sent");
						
						Thread.sleep(UDP_MESSAGE_INTERVAL);
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace(); 
					}
					
					}
				}
				
			};
			t.start();
			return t;
			
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace(); 
		}
		
		return new Thread();

	}


	public static ArrayList<String> getFiles() {
		Path path = Paths.get("");
		String s = path.toAbsolutePath().toString();
		System.out.println(s);
		File f = new File(path.toAbsolutePath().toString());
		
		if(files == null) {
			files = new ArrayList<String>();

		}
		
		
		for(File file : f.listFiles()) {
			if(!file.isDirectory() || Files.isRegularFile(file.toPath()))
				files.add(file.getName());
				
		}
		
		return files;
	}
	
	public String searchForFileInRegistry(String fileToSearch) {
		
		String  response = "";
		try {
			String commandParam = SEARCHCOMMAND + ":" +fileToSearch;
			out.writeObject(new String(commandParam));
			response = (String) in.readObject();

			//response->  Servents address information
			if(!response.equals("FILE NOT FOUND ON REGISTRY")) {
				
				System.out.println("file ["+fileToSearch+"] found in: "+response);
			}
			else System.out.println("registry didnt find file "+fileToSearch);
			
			return response;

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return response;

		
	}
	
	public void closeConnectionWithRegistry() {
		try {
			out.writeObject(new String(CLOSE_REGISTRY_CONNECTION));
			String response = (String) in.readObject();
			System.out.println(response);

			if(response.equals(STATUS_CONNECTION_CLOSED)) {
				System.out.println("Connetion is closed");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void handleUserInput() {
		while(true) {
			Scanner in = new Scanner(System.in);
			String input = in.nextLine();
			String[] params =  input.split(" ");
			System.out.println("params." + params[0]);

			if(params!=null&&params.length==2) {
				
				switch(params[0]) {
				case "search":
					searchForFileInRegistry(params[1]);
					break;
				case "close":
					closeConnectionWithRegistry();
					break;
				case "send":
					sendFile(params[1]);
				default:
					System.out.println("not a valid command and/or params." + params[0]);
					break;
					}
				
				}
			else {
				System.out.println("try again.");
			}
		}
	}
	
	public void sendFile(String filNamme) {
		
		String test = "bitcoin.pdf";
		byte[] content;
		try {
			out.writeObject(new String(SENDING_FILE+":"+test));
			
			File file = new File(test);
			content = Files.readAllBytes(file.toPath());
			out.writeObject(content);
			System.out.println("sent file contents over network.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void receiveFile() {
		try {
			byte[] fileContent = (byte[]) in.readObject();
			File savedFile = new File("bitcoin2.pdf");
			Files.write(savedFile.toPath(), fileContent);
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void main(String args[]) 
	{ 
		Servent client = new Servent("127.0.0.1", PORT);	

		try {
		client.handleUserInput();
		}
		finally {
			client.closeConnectionWithRegistry();;
			
		}
//		client.searchForFileInRegistry("bitcoin2.pdf");
	
		 
	} 
} 
