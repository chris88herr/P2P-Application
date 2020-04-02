import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



//class to handle communication with a specific Servent 

public class RegistryWorker extends Thread{
	
	public static final String STATUS_CONNECTION_CLOSED = "CLOSED_CONNECTION";
	public static final String CLOSE_REGISTRY_CONNECTION = "Close_Registry_Connection";
	public static final String SENDING_FILE = "SENDING_FILE";

	
	public Socket serventSocket ;
	public Date lastContact; 
	ArrayList<Socket> sockets;	
	private volatile boolean exit = false;	
	public ObjectOutputStream out;
	public ObjectInputStream in;

	private Registry registry;
	private int registryWorkerId;
	private ServentEntry serventEntry;
	
	public RegistryWorker(int id,Registry registry, Socket socket, ArrayList<Socket> sockets,
			 OutputStream out, InputStream in) {
		
		serventSocket = socket;
		lastContact = new Date();
		try {
			this.out = new ObjectOutputStream(out);
			this.in = new ObjectInputStream(in);
			System.out.println("streams opened");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.sockets = sockets;
		this.registry = registry;
		registryWorkerId = id;
		serventEntry = new ServentEntry(new Date(), serventSocket, getFilesFromServent());
		registry.addServentEntry(serventEntry);
	}
	
	public ServentEntry getServentEntry() {
		return this.serventEntry;
	}
	 
	@Override
	public void run() {
		//System.out.println("thread started... ");
	String commandParam = "";
	while(!exit) {

			try {
				handleServent(commandParam);
				Thread.sleep(500);
			}
			catch (InterruptedException e) {
				return;
			}
		}
	
	
	}
	
	private int getRegistryWorkerId() {
		return registryWorkerId;
	}
	
	public void interuptThread() {
		
		exit = true;
	}
	
	public void sendClosingNotification() {
		try {
			out.writeObject(new String("CLOSE CONNECTION"));
		} catch (IOException e) {
			System.out.println("failed to send closing notification");
			e.printStackTrace();
		}
	}
	
	
	public void sendFileAndServantInformation(String file) {
		String stringToSend = registry.searchTableForFile(file);
		try {
			out.writeObject(stringToSend);
			System.out.println("sent information about "+file+"--> "+stringToSend);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("couldnt send the file back to servent");
			e.printStackTrace();
		}
	}

	//Stops the communication to servent and cleans up resources from
	// registry to maintain such connection
	public void closeConnectionWithServent() {
		System.out.println("Closing connection with: "+serventSocket);
		sockets.remove(serventSocket);
		registry.deleteServentEntryFromList(serventEntry);
		registry.removeRegistryWorkerFromList(this);
	
		try {
			sendClosingNotification();
			out.close();
			in.close();
			interuptThread();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	public void printSockets() {
		if(sockets.size()==0) {
			System.out.println("no sockets");
			return;
		}
		for(int i =0; i< sockets.size(); i++) {
			System.out.println(sockets.get(i));
		}
	}
	
	private void handleServent(String commandParam) throws InterruptedException {
//		System.out.println("handling stuff....");

		try {
			commandParam = (String) in.readObject();
			System.out.println(commandParam);
			String[] params = commandParam.split(":");
			System.out.println("params length"+params.length);
			
			if(params!=null&&params.length>0) {
				switch(params[0]) {
				case "search":
					System.out.println(params[1]);
					sendFileAndServantInformation(params[1]); 
					break;
				case CLOSE_REGISTRY_CONNECTION:
					System.out.println("getting to close..");
					closeConnectionWithServent();
					break;
				case SENDING_FILE:
					receiveFile(params[1]);
				default:
					System.out.println("not a valid command from servent");
					break;
				
				}
			}
			
			
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			System.out.println("something went wrong here");
			e.printStackTrace();
		}
		
	}
	
	public void receiveFile(String fileName) {
		try {
			byte[] fileContent = (byte[]) in.readObject();
			File savedFile = new File("bitcoin2.pdf");
			Files.write(savedFile.toPath(), fileContent);
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public ArrayList<String> getFilesFromServent(){
		
		ArrayList<String> files = new ArrayList<String>();
		try {
			files = (ArrayList<String>) in.readObject();
			System.out.println("files--->"+files.size());

			if(files !=null) {
				System.out.println("files received:");

				for(String s: files) {
					System.out.print("| "+s+" |");
				}
				System.out.println();
				return files;
			}
			
		}
		catch(Exception e){
			System.out.println("Something went wrong trying to get the files");
		}
		
		System.out.println("Files Not Found 2");
		return null;
	}
}
