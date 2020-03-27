import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;


//class to handle communication with a specific Servent 
public class RegistryWorker extends Thread{
	
	public Socket serventSoccket ;
	
	public RegistryWorker(Socket socket) {
		this.serventSoccket = socket;
	}
	
	@Override
	public void run() {
		try {
			handleServent();
		}
		catch(IOException e) {
			e.printStackTrace();
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void handleServent() throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		OutputStream outputStream = this.serventSoccket.getOutputStream();
	}

}
