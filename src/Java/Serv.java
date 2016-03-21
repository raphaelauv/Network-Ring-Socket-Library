import java.util.HashMap;
import java.util.LinkedList;

class SizeException extends Exception{
}

public class Serv implements Communication{
	
	
	private int id;
	private int udp1;
	private int udp2;
	private int portTcp;
	
	private HashMap<Integer, Boolean> IdAlreadyReceve;// hashmap contenant les id deja croisé
	
	
	private LinkedList<Message> listForApply; // liste des message recu qui sont pour cette ID
	private LinkedList<Message> listToSend;// liste des message a envoyé

	public String lire(){
		//prend le verrou

		while(listForApply.isEmpty()){
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}//se met en attente
		}
		//libere le verrou
		return listForApply.pop().getContenu();
	
	}

	public void envoyer(String message , int id) throws SizeException{

		//envoyer un message
		
		if(message.length()>250){
			throw new SizeException();
		}
		
		Message tmp=new Message(id,message);
		
		//prend le verrou
		
		
	}

	
	public Serv(int a, int id) {

		super();
		this.listToSend=new LinkedList<Message>();
		this.listToSend=new LinkedList<Message>();
		this.udp1 = a;
		this.id = id;
	}

	public void dedoubler(int b){
		if (this.udp2 !=0){
			this.udp2=b;
		}
		else{
			// TODO
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
