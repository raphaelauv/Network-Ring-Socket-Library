import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;

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
	
	public Serv(int a, int id) throws SocketException {

		super();
		this.sender=new DatagramSocket();
		this.listToSend=new LinkedList<Message>();
		this.listToSend=new LinkedList<Message>();
		this.udp1 = a;
		this.id = id;
	}

	public void dedoubler(int udpNew) throws AlreadyAllUdpPortSet{
		if (this.udp2 !=0){
			this.udp2=udpNew;
		}
		else if (this.udp1 != 0){
			this.udp1=udpNew;
		}
		else{
			throw new AlreadyAllUdpPortSet();
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
