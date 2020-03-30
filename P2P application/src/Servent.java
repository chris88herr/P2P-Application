// A Java program for a Client 
import java.net.*;
import java.util.Date;
import java.io.*; 



public class Servent 
{ 
	static int PORT = 1234;
	static int UDP_PORT = 1233;
	
	// TCP socket 
	private Socket socket		 = null; 
	
	//UDP communication variables
	DatagramSocket datagramSocket;
	byte[] buffer;
	DatagramPacket datagramPacket;

	// constructor to put ip address and port 
	public Servent(String address, int port) 
	{ 
		// establish a connection 
		try
		{ 
			socket = new Socket(address, port); 
	        datagramSocket = new DatagramSocket(); 
			System.out.println("Connected to the registry: "+ socket.getPort()+"---"+socket.getLocalPort()); 
			
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
	
	// close the connection 
	public void closeConnection() {
		
		try
			{  
				socket.close(); 
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
	public void sendHellothroughUDP() {
		InetAddress ip;
		try {
			ip = InetAddress.getLocalHost();
			String hello = "Hello from "+this.toString()+"\n";
			buffer = new byte[1000];
			buffer =  hello.getBytes();
			Thread t = new Thread() {
				@Override
				public void run() {
					int flag = 0;
					while(flag <3) {
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

						
						Thread.sleep(2000);
						flag++;
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
			
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace(); 
		}
		



	}

	public static void main(String args[]) 
	{ 
		Servent client = new Servent("127.0.0.1", PORT);
		client.sendHellothroughUDP();
		System.out.println("out");
	} 
} 
