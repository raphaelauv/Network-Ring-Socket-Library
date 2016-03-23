import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
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

class DOWNmessageException extends Exception {

}

public class Serv implements Communication {
	public boolean verboseMode;
	private String ip;

	private int id;

	private Integer numberPortTcp;
	private ServerSocket sockServerTCP;

	private DatagramSocket sockSender;
	private Integer numberPortUDP1;
	private String ipPortUDP1;

	private Integer numberPortUDP2;
	private String ipPortUDP2;

	private DatagramSocket sockRecever;
	private Integer numberLICENPortUDP;

	private String ipMULTI;
	private Integer numberPortMULTI;
	private MulticastSocket sockMultiRECEP;

	private HashMap<Integer, Boolean> IdAlreadyReceveUDP1;// hashmap contenant
															// les
															// id deja croisé
	private HashMap<Integer, Boolean> IdAlreadyReceveUDP2;

	private LinkedList<Message> listForApply; // liste des message recu qui sont
												// pour cette ID
	private LinkedList<Message> listToSend;// liste des message a envoyé

	private Runnable runRecev;
	private Runnable runMULTIRecev;
	private Runnable runServTCP;
	private Runnable runSend1;
	private Runnable runSend2;

	private Thread ThRecev;
	private Thread ThMULTIrecev;
	private Thread ThServTCP;
	private Thread ThSend1;
	private Thread ThSend2;

	private Boolean EYBGisArrive;

	private Boolean TESTisComeBack;
	private Integer ValTEST;

	private boolean boolClose;
	
	public void close() {
		this.sockRecever.close();
		this.sockMultiRECEP.close();
		this.sockSender.close();
		try {
			this.sockServerTCP.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		this.ThRecev.interrupt();
		this.ThMULTIrecev.interrupt();
		this.ThServTCP.interrupt();;
		this.ThSend1.interrupt();
		//this.ThSend2.interrupt();
		
	}

	private void isclose() throws DOWNmessageException {
		if (boolClose) {
			throw new DOWNmessageException();
		}
	}

	private void receveMULTI() throws IOException, DOWNmessageException {
		this.sockMultiRECEP = new MulticastSocket(this.numberPortMULTI);
		this.sockMultiRECEP.joinGroup(InetAddress.getByName(ipMULTI.toString()));

		byte[] data = new byte[100];
		DatagramPacket paquet = new DatagramPacket(data, data.length);

		this.sockMultiRECEP.receive(paquet);
		String st = new String(paquet.getData(), 0, paquet.getLength());
		if (verboseMode) {
			System.out.println("message MULTI RECEVE : " + st);
		}

		if (st.equals("DOWN\n")) {
			close();
			throw new DOWNmessageException();
		}

	}

	public void test() throws InterruptedException, DOWNmessageException {
		isclose();
		Integer idMessage = 100;
		String test = "TEST" + " " + idMessage + " " + this.ipMULTI + " " + this.numberPortMULTI;

		Message q = new Message(8, test);
		this.ValTEST = idMessage;
		this.TESTisComeBack = false;
		
		synchronized (listToSend) {
			listToSend.add(q);
		}

		wait(2000);

		if (!TESTisComeBack) {
			if (verboseMode) {
				System.out.println("message TEST is NOT comeback");
			}
			Message tmp=new Message(10,"Down");
			tmp.setMulti(true);
			try {
				envoyer(tmp);
			} catch (SizeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (verboseMode) {
			System.out.println("message TEST is comeback");
		}

	}

	public void quitter() throws InterruptedException, DOWNmessageException {
		isclose();
		Integer idMessage = 100;

		String quit = "GBYE" + " " + idMessage + " " + this.ip + " " + this.numberLICENPortUDP + " " + this.ipPortUDP1
				+ " " + this.numberPortUDP1;

		Message q = new Message(8, quit);
		EYBGisArrive = false;
		synchronized (listToSend) {
			listToSend.add(q);
		}

		synchronized (EYBGisArrive) {
			while (!EYBGisArrive) {
				EYBGisArrive.wait();
			}
		}
		// TODO deconnect;

	}

	public void connectTo(String adresse, int idTCP)
			throws AlreadyAllUdpPortSet, UnknownHostException, IOException, DOWNmessageException {
		isclose();
		if (numberPortUDP1 != 0) {
			throw new AlreadyAllUdpPortSet();
		}

		synchronized (sockServerTCP) {// pas d'acceptation de connection pendant
										// une connection

			Socket socket = new Socket(adresse, idTCP);
			if (verboseMode) {
				System.out.println("conecter en TCP a :" + adresse + " sur port : " + idTCP);
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
			String m1 = br.readLine();

			if (verboseMode) {
				System.out.println("message RECEVE " + m1);
			}

			String m2 = "NEWC" + " " + this.ip + " " + this.numberPortUDP1 + "\n";

			pw.print(m2);
			pw.flush();
			if (verboseMode) {
				System.out.println("message SEND " + m2);
			}

			String m3 = br.readLine();
			if (verboseMode) {
				System.out.println("message RECEVE " + m3);
			}
			pw.close();
			br.close();
			socket.close();

		}
	}

	/**
	 * Serv in TCP to accept an entrance TCP connection
	 * 
	 * @param idTCP
	 *            port TCP of serv
	 * @throws IOException
	 * @throws DOWNmessageException 
	 */
	private void servTCP() throws IOException {
		synchronized (sockServerTCP) {

			Socket socket = sockServerTCP.accept();

			if (verboseMode) {
				System.out.println("TCP connect");

			}
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

			String m1 = "WELC" + " " + this.ip + " " + this.numberLICENPortUDP + " " + this.ipMULTI + " "
					+ this.numberPortMULTI + "\n";

			pw.print(m1);
			pw.flush();

			if (verboseMode) {
				System.out.println("TCP : message SEND: " + m1);
			}
			String m2 = br.readLine();

			if (verboseMode) {
				System.out.println("TCP : message RECEVE : " + m2);
			}

			String m3="ACKC\n";
			pw.print(m3);
			pw.flush();

			if (verboseMode) {
				System.out.println("TCP : message SEND: "+m3);
			}

			pw.close();
			br.close();
			socket.close();

			synchronized (numberPortUDP1) {

			}
		}
	}

	public String lire() throws DOWNmessageException {

		isclose();
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

	public void envoyer(String message, int id) throws DOWNmessageException, SizeException{
		envoyer(new Message(id, message));
	}
	
	private void envoyer(Message msg) throws DOWNmessageException, SizeException{
		
		if (msg.getContenu().length() > 250) {
			throw new SizeException();
		}

		synchronized (listToSend) {
			// TODO mettre en forme le message avant d'ajouter dans liste
			this.listToSend.add(msg);
			this.listToSend.notifyAll();
		}
	}

	private void receveMessage() throws IOException {
		
		if (verboseMode) {
			System.out.println("dans thread receve");
		}

		byte[] dataToReceve = new byte[100];
		DatagramPacket paquet = new DatagramPacket(dataToReceve, dataToReceve.length);
		if (verboseMode) {
			System.out.println("j'attends de recevoir un message dans RECEVE");
		}

		this.sockRecever.receive(paquet);// attente passive

		String st = new String(paquet.getData(), 0, paquet.getLength());

		Message tmp = new Message(10, st);
		if (verboseMode) {
			System.out.println("Message Recu : " + st);
		}

		Integer idm = 100;

		if (st.startsWith("GBYE")) {
			String m = "EYBG" + " " + idm;

		}

		else if (st.startsWith("TEST")) {
			if (st.substring(4).startsWith(ValTEST.toString())) {
				this.TESTisComeBack = true;
			}
		} else if (st.startsWith("EYBG")) {

			synchronized (EYBGisArrive) {
				EYBGisArrive = true;
				EYBGisArrive.notifyAll();
			}

		} else {
			synchronized (this.listToSend) {
				this.listToSend.add(tmp);
				this.listToSend.notifyAll();
			}
		}
	}

	private void sendMessage() throws IOException, InterruptedException {
		
		String tmp;
		Message msg;
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
			msg=this.listToSend.pop();
		}
		tmp = msg.getContenu();
		
		System.out.println("contenu du message a envoyer : "+tmp);
		byte[] dataTosend = tmp.getBytes();
		if(msg.isMulti()){
			DatagramPacket paquetMulti = new DatagramPacket(dataTosend, dataTosend.length,
					InetAddress.getByName(this.ipMULTI.toString()), numberPortMULTI);
			this.sockSender.send(paquetMulti);
		}
		else{
			
			if (numberPortUDP1 != 0) {
				
				DatagramPacket paquet1 = new DatagramPacket(dataTosend, dataTosend.length,
						InetAddress.getByName(this.ipPortUDP1.toString()), numberPortUDP1);
				
					this.sockSender.send(paquet1);
			
			}
			if (numberPortUDP2 != 0) {
				DatagramPacket paquet2 = new DatagramPacket(dataTosend, dataTosend.length,
						InetAddress.getByName(this.ipPortUDP2.toString()), numberPortUDP2);
				
					this.sockSender.send(paquet2);
				
			}
			
		}
		if (verboseMode) {
			System.out.println("message envoyer : " + tmp);
		}

	}

	public Serv( boolean verbose, Integer numberLICENPortUDP,Integer numberPortTcp) throws IOException {

		super();
		this.ipMULTI = "225.1.2.4";
		this.numberPortMULTI = 9999;
		this.numberPortTcp=numberPortTcp;
		this.sockServerTCP=new ServerSocket(numberPortTcp);
		this.sockSender = new DatagramSocket();
		this.numberLICENPortUDP = numberLICENPortUDP;
		this.sockRecever = new DatagramSocket(numberLICENPortUDP);
		this.listToSend = new LinkedList<Message>();
		this.listForApply = new LinkedList<Message>();
		this.verboseMode = verbose;
		
		this.ipPortUDP1="localhost";
		this.ipPortUDP2="localhost";
		this.numberPortUDP1=10;
		this.numberPortUDP2=11;
		
		this.boolClose=false;
		
		/*******************************************************************
		 * Creation des 4 thread anonyme : d'envoi UDP | reception UDP | serv
		 * tcp | reception multi
		 * 
		 */
		this.runRecev = new Runnable() {
			public void run() {
				boolean erreur=false;
				while (!erreur) {
					try {
						receveMessage();
					} catch (IOException e) {
						erreur=true;
					}
				}
				if(verboseMode){System.out.println("fin thread RECEV");}
			}
			
		};

		this.runMULTIRecev = new Runnable() {
			public void run() {
				boolean erreur = false;
				while (!erreur) {
					try {
						receveMULTI();
					} catch (IOException e) {
						erreur=true;			
					} catch (DOWNmessageException e) {
						erreur = true;
					}
				}
				if(verboseMode){System.out.println("fin thread MULTI");}
			}

		};

		this.runSend1 = new Runnable() {
			public void run() {
				boolean erreur=false;
				while (!erreur) {
					try {
						sendMessage();
					} catch ( InterruptedException | IOException e) {
						erreur=true;
					}
				}
				if(verboseMode){System.out.println("fin thread SEND");}
			}
		};

		this.runServTCP = new Runnable() {
			public void run() {
				boolean erreur=false;
				while (!erreur) {
					try {
						servTCP();
					} catch (IOException e) {
						erreur=true;
					}
				}
				if(verboseMode){System.out.println("fin thread TCP");}
			}

		};

		this.ThRecev = new Thread(runRecev);
		this.ThSend1 = new Thread(runSend1);
		this.ThServTCP = new Thread(runServTCP);
		this.ThMULTIrecev = new Thread(runMULTIRecev);

		this.ThRecev.start();
		this.ThSend1.start();
		this.ThServTCP.start();
		this.ThMULTIrecev.start();

	}

	public void dedoubler(int udpNew) throws AlreadyAllUdpPortSet, InterruptedException {

		if (this.numberPortUDP1 != 0 && this.numberPortUDP2 != 0) {
			throw new AlreadyAllUdpPortSet();

		} else if (this.numberPortUDP2 == 0) {
			this.numberPortUDP2 = udpNew;

			this.runSend2 = new Runnable() {
				public void run() {
					boolean erreur=false;
					while (!erreur) {
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
