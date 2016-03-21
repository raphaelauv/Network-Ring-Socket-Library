import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.lang.Runnable;

class SizeException extends Exception{
}

class AlreadyAllUdpPortSet extends Exception{
	
}

public class Serv implements Communication{
	
	
	private int id;
	private int udp1;
	private int udp2;
	private int portTcp;
	
	
	DatagramSocket sender;
	byte[]dataTosend;
	
	private HashMap<Integer, Boolean> IdAlreadyReceve;// hashmap contenant les id deja croisé
	private LinkedList<Message> listForApply; // liste des message recu qui sont pour cette ID
	private LinkedList<Message> listToSend;// liste des message a envoyé
	
	private Runnable runRecev;
	private Runnable runSend1;
	private Runnable runSend2;
	
	private Thread ThRecev;
	private Thread ThSend1;
	private Thread ThSend2;
	
	public String lire() {
		synchronized (listForApply) {

			while (listForApply.isEmpty()) {
				try {
					wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
			return listForApply.pop().getContenu();
		}
	}

	public void envoyer(String message , int id) throws SizeException{
	
		if(message.length()>250){
			throw new SizeException();
		}
		
		synchronized(listToSend){
			
			//TODO mettre en forme le message avant d'ajouter dans liste
			listToSend.add(new Message(id,message));
			notifyAll();
		}
		
	}

	private void receveMessage(){
		
		//TODO
	}
	
	private void sendMessage() throws UnknownHostException, InterruptedException{
		synchronized (listToSend){
			while(listToSend.isEmpty()){
				wait();
			}
			dataTosend=listToSend.pop().getContenu().getBytes();
		}
		
		if(udp1 !=0){
			DatagramPacket paquet1=new DatagramPacket(dataTosend,dataTosend.length,InetAddress.getByName("localhost"),udp1);
			try {
				sender.send(paquet1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(udp2 != 0){
			DatagramPacket paquet2=new DatagramPacket(dataTosend,dataTosend.length,InetAddress.getByName("localhost"),udp2);
			try {
				sender.send(paquet2);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		;
		
	}
	
	public Serv(int a, int id) throws SocketException, InterruptedException {

		super();
		this.sender = new DatagramSocket();
		this.listToSend = new LinkedList<Message>();
		this.listToSend = new LinkedList<Message>();
		this.udp1 = a;
		this.id = id;

		/*******************************************************************
		 * Creation des class anonyme propre au thread d'envoi et celui de reception
		 * 
		 */
		this.runRecev = new Runnable() {
			public void run() {
				while(true){
					try {
						sendMessage();
					} catch (UnknownHostException | InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		
		this.runSend1 = new Runnable() {
			public void run() {
				while(true){
					receveMessage();
				}
			}
		};

		this.ThRecev=new Thread(runRecev);
		this.ThSend1=new Thread(runSend1);
		
		this.ThRecev.join();
		this.ThSend1.join();
		
	}

	public void dedoubler(int udpNew) throws AlreadyAllUdpPortSet, InterruptedException{
		
		if(this.udp1 != 0 && this.udp2 != 0 ){
			throw new AlreadyAllUdpPortSet();
		}
		else if (this.udp2 ==0){
			this.udp2=udpNew;
			
			this.runSend2 = new Runnable() {
				public void run() {
					while(true){
						receveMessage();
					}
				}
			};
			this.ThSend2=new Thread(runSend2);
			this.ThSend2.join();
		}
		else if (this.udp1 != 0){
			this.udp1=udpNew;
			
			//todo same que le else if, ameliorer synthaxe du code
		}
	}
	public int getB() {
		return udp2;
	}

	public void setB(int b) {
		this.udp2 = b;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}


}
