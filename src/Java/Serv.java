import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.lang.Runnable;

class SizeException extends Exception {
}

class AlreadyAllUdpPortSet extends Exception {

}

public class Serv implements Communication {
	public boolean verboseMode;

	private int id;
	private int idTCP;
	private int numberPortUDP1;
	private int numberPortUDP2;
	private int portTcp;

	private DatagramSocket sockSender;
	private DatagramSocket sockRecever;

	private ServerSocket sockServerTCP;

	private byte[] dataTosend;
	private byte[] dataToReceve;

	private HashMap<Integer, Boolean> IdAlreadyReceveUDP1;// hashmap contenant
															// les
															// id deja croisé
	private HashMap<Integer, Boolean> IdAlreadyReceveUDP2;

	private LinkedList<Message> listForApply; // liste des message recu qui sont
												// pour cette ID
	private LinkedList<Message> listToSend;// liste des message a envoyé

	private Runnable runRecev;
	private Runnable runServTCP;
	private Runnable runSend1;
	private Runnable runSend2;

	private Thread ThRecev;
	private Thread ThServTCP;
	private Thread ThSend1;
	private Thread ThSend2;

	
	
	public void connectTo(String adresse, int idTCP) throws AlreadyAllUdpPortSet {
		
		if(numberPortUDP1 != 0){
			throw new AlreadyAllUdpPortSet();
		}
		
	}
	
	/**
	 * Serv in TCP to accept an entrance TCP connection
	 * @param idTCP port TCP of serv
	 * @throws IOException
	 */
	private void servTCP(int idTCP) throws IOException {
		
		this.sockServerTCP=new ServerSocket(idTCP);
		
		Socket socket=sockServerTCP.accept();
		
		if(verboseMode){System.out.println("TCP connect");
		
		}
		BufferedReader br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintWriter pw=new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		
		
		String m1="WELC"+" "+ip+" "port+" "+ip-diff+" "+port-diff+"\n";
		
		pw.print(m1);
		if(verboseMode){System.out.println("TCP : message WELC envoyé: "+m1);}
		String m2=br.readLine();
		
		if(verboseMode){System.out.println("TCP : message recu : "+m2);}
		
		pw.print("ACKC\n");
		
		if(verboseMode){System.out.println("TCP : message ACKC envoyé: ");}
		
		pw.close();
		br.close();
		socket.close();
	}
	
	public String lire() {
		synchronized (listForApply) {

			while (listForApply.isEmpty()) {
				try {
					listForApply.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return listForApply.pop().getContenu();
		}
	}

	public void envoyer(String message, int id) throws SizeException {

		if (message.length() > 250) {
			throw new SizeException();
		}

		synchronized (listToSend) {
			
			System.out.println("je suis dans methode envoyer");
			
			// TODO mettre en forme le message avant d'ajouter dans liste
			this.listToSend.add(new Message(id, message));
			this.listToSend.notifyAll();
		}

	}

	private void receveMessage() throws IOException {

		if (verboseMode) {
			System.out.println("dans thread receve");
		}

		this.dataToReceve = new byte[100];
		DatagramPacket paquet = new DatagramPacket(dataToReceve, dataToReceve.length);
		if (verboseMode) {
			System.out.println("j'attends de recevoir un message dans RECEVE");
		}

		this.sockRecever.receive(paquet);

		String st = new String(paquet.getData(), 0, paquet.getLength());

		Message tmp = new Message(10, st);
		if (verboseMode) {
			System.out.println("Message Recu : " + st);
		}

		synchronized (this.listToSend) {
			this.listToSend.add(tmp);
			this.listToSend.notify();
		}

	}

	private void sendMessage() throws UnknownHostException, InterruptedException {
		String tmp;
		synchronized (listToSend) {

			if (verboseMode) {
				System.out.println("dans thread send");
			}

			while (listToSend.isEmpty()) {
				if (verboseMode) {
					System.out.println("j'attends d'avoir un message a envoyer dans SEND");
				}
				this.listToSend.wait();
			}
			tmp = this.listToSend.pop().getContenu();
			this.dataTosend = tmp.getBytes();
		}

		if (numberPortUDP1 != 0) {
			DatagramPacket paquet1 = new DatagramPacket(dataTosend, dataTosend.length,
					InetAddress.getByName("localhost"), numberPortUDP1);
			try {
				this.sockSender.send(paquet1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (numberPortUDP2 != 0) {
			DatagramPacket paquet2 = new DatagramPacket(dataTosend, dataTosend.length,
					InetAddress.getByName("localhost"), numberPortUDP2);
			try {
				this.sockSender.send(paquet2);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (verboseMode) {
			System.out.println("message envoyer : " + tmp);
		}

	}

	public Serv(boolean verboseMode) throws InterruptedException, IOException {

		super();
		this.sockSender = new DatagramSocket();
		this.sockRecever = new DatagramSocket(5555);
		this.listToSend = new LinkedList<Message>();
		this.listForApply = new LinkedList<Message>();
		this.verboseMode = verboseMode;
		
		// this.udp1 = udp1;
		// this.id = id;

		/*******************************************************************
		 * Creation des 2 class anonyme : thread d'envoi et de reception
		 * 
		 */
		this.runRecev = new Runnable() {
			public void run() {
				while (true) {
					try {
						receveMessage();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};

		this.runSend1 = new Runnable() {
			public void run() {
				while (true) {
					try {
						sendMessage();
					} catch (UnknownHostException | InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};

		this.runServTCP = new Runnable() {
			public void run() {
				while (true) {
					try {
						servTCP(idTCP);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		};
		
		this.ThRecev = new Thread(runRecev);
		this.ThSend1 = new Thread(runSend1);
		this.ThServTCP =new Thread(runServTCP);
		
		this.ThRecev.start();
		this.ThSend1.start();
		this.ThServTCP.start();

		//this.ThServTCP.join();
		//this.ThRecev.join();
		//this.ThSend1.join();

	}


	public void dedoubler(int udpNew) throws AlreadyAllUdpPortSet, InterruptedException {

		if (this.numberPortUDP1 != 0 && this.numberPortUDP2 != 0) {
			throw new AlreadyAllUdpPortSet();

		} else if (this.numberPortUDP2 == 0) {
			this.numberPortUDP2 = udpNew;

			this.runSend2 = new Runnable() {
				public void run() {
					while (true) {
						try {
							receveMessage();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			};
			this.ThSend2 = new Thread(runSend2);
			this.ThSend2.start();
			this.ThSend2.join();

		} else if (this.numberPortUDP1 != 0) {
			this.numberPortUDP1 = udpNew;

			// todo same que le else if, ameliorer synthaxe du code
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
