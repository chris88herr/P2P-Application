import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;



//class to handle communication with a specific Servent 

public class RegistryWorker extends Thread{
	
	public Socket serventSoccket ;
	public Date lastContact; 
	ArrayList<Socket> sockets;	
	private volatile boolean exit = false;	
	
	public RegistryWorker(Socket socket, ArrayList<Socket> sockets) {
		serventSoccket = socket;
		lastContact = new Date();
		this.sockets = sockets;
	}
	 
	@Override
	public void run() {
		//System.out.println("thread started... ");
	while(!exit) {

			try {
				handleServent();
				Thread.sleep(500);
			}
			catch(IOException e) {
				e.printStackTrace();
			
			} catch (InterruptedException e) {
				return;
			}
		}
	
	
	}
	
	public void interuptThread() {
		
		exit = true;
	}

	private void handleServent() throws IOException, InterruptedException {
			
		Thread.sleep(1000);
	}

}
