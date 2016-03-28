import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.Runnable;

/*
 * Le message est trop grand
 */
class SizeMessageException extends Exception {
}

/*
 * L'entite est deja connecte
 */
class AlreadyAllUdpPortSet extends Exception {
}

/**
 * Exception l'entite reseaux a recu un DOWN et donc est deconnecter
 */
class DOWNmessageException extends Exception {

}

public class Serv implements Ringo {

	public boolean verboseMode;

	private String ip;

	private int idEntite;

	private Integer numberPortTcp;
	private ServerSocket sockServerTCP;

	private DatagramSocket sockSender;
	
	private Integer portUDP1;
	private String ipPortUDP1;

	private Integer portUDP2;
	private String ipPortUDP2;

	private DatagramSocket sockRecever;
	private Integer listenPortUDP;

	private String ip_diff;
	private Integer port_diff;
	private MulticastSocket sockMultiRECEP;

	private ConcurrentHashMap<UnsignedLong, Boolean> IdAlreadyReceveUDP1;// hashmap contenant
															// les
															// id deja croisé
	private ConcurrentHashMap<UnsignedLong, Boolean> IdAlreadyReceveUDP2;

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

	private Boolean boolClose;
	
	private void closeServ(boolean modeDOWN) {
		
		this.boolClose=true;
		this.sockRecever.close();
		this.sockMultiRECEP.close();
		
		try {
			this.sockServerTCP.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		this.ThRecev.interrupt();
		this.ThMULTIrecev.interrupt();
		this.ThServTCP.interrupt();;
		
		if(modeDOWN){
			this.sockSender.close();
			this.ThSend1.interrupt();
			//this.ThSend2.interrupt();
		}
		else{
		
			synchronized (listToSend) {
				while(!listToSend.isEmpty()){
					System.out.println(threadToString()+"FILE NON VIDE : taille ->"+listToSend.size());
					listToSend.notify();
					try {
						listToSend.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				this.sockSender.close();
				this.ThSend1.interrupt();
				//this.ThSend2.interrupt();
			}
			
		}
		
		
	}
	public void close() throws InterruptedException ,DOWNmessageException{
		isclose();
		Integer idMessage = 10000000;

		String quit = "GBYE" + " " + idMessage + " " + this.ip + " " + this.listenPortUDP + " " + this.ipPortUDP1
				+ " " + this.portUDP1;

		Message q = new Message(quit.getBytes());
		
		synchronized (listToSend) {
			listToSend.add(q);
		}

		if(verboseMode){System.out.println(threadToString()+"WAITING for EYBG message");}
		
		synchronized (EYBGisArrive) {
			EYBGisArrive = false;
			EYBGisArrive.wait(4000);
			if(EYBGisArrive){
				
			}
		}
		
		test(false);
		
		closeServ(false);
	}
	private void isclose() throws DOWNmessageException {
		synchronized(boolClose){
			if (boolClose) {
				throw new DOWNmessageException();
			}
		}
	}

	private void receveMULTI() throws IOException {
		this.sockMultiRECEP = new MulticastSocket(this.port_diff);
		this.sockMultiRECEP.joinGroup(InetAddress.getByName(ip_diff.toString()));

		byte[] data = new byte[100];
		DatagramPacket paquet = new DatagramPacket(data, data.length);

		this.sockMultiRECEP.receive(paquet);
		String st = new String(paquet.getData(), 0, paquet.getLength());
		if (verboseMode) {
			System.out.println(threadToString()+"message MULTI RECEVE : " + st);
		}

		if (st.equals("DOWN")) {
			closeServ(true);
		}

	}

	private String threadToString(){
		return "THREAD: "+Thread.currentThread().getName()+" | ";
	}
	public boolean test(boolean sendDownIfBreak) throws InterruptedException, DOWNmessageException {
		isclose();
		int idMessage = 20;
		String test = "TEST" + " " + idMessage + " " + this.ip_diff + " " + this.port_diff;

		Message q = new Message(test.getBytes());
		this.ValTEST = idMessage;
		this.TESTisComeBack = false;
		
		synchronized (listToSend) {
			listToSend.add(q);
		}
		Thread.sleep(2000);

		if (!TESTisComeBack) {
			if (verboseMode) {
				System.out.println(threadToString()+"message TEST is NOT comeback");
			}
			
			if(sendDownIfBreak){
				Message tmp=new Message("DOWN".getBytes());
				tmp.setMulti(true);
				addToListToSend(tmp);
				
			}
			
			return false;
		}
		if (verboseMode) {
			System.out.println(threadToString()+"message TEST is comeback");
		}
		return true;

	}

	public void connectTo(String adresse, int idTCP)
			throws AlreadyAllUdpPortSet, UnknownHostException, IOException, DOWNmessageException {
		isclose();
		if (portUDP1 != 0) {
			throw new AlreadyAllUdpPortSet();
		}

		synchronized (sockServerTCP) {// pas d'acceptation de connection pendant
										// une connection

			Socket socket = new Socket(adresse, idTCP);
			if (verboseMode) {
				System.out.println(threadToString()+"conecter en TCP a :" + adresse + " sur port : " + idTCP);
			}
			BufferedOutputStream buffOut = new BufferedOutputStream(socket.getOutputStream());
	        BufferedInputStream buffIn = new BufferedInputStream(socket.getInputStream());
			
			
			byte[] tmp=new byte[Ringo.maxSizeMsg];
			buffIn.read(tmp);
			Message msg1 =new Message(tmp);
			
			if (verboseMode) {
				System.out.println(threadToString()+"message RECEVE " + m1);
			}

			buffOut.write(Message.NEWC(this.ip, this.portUDP1).getData());
			buffOut.flush();
			
			if (verboseMode) {
				String m2 = "NEWC" + " " + this.ip + " " + this.portUDP1 + "\n";
				System.out.println(threadToString()+"message SEND " + m2);
			}

			buffIn.read(tmp);
			if (verboseMode) {
				System.out.println(threadToString()+"message RECEVE " + m3);
			}
			buffOut.close();
			buffIn.close();
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
				System.out.println(threadToString()+"TCP connect");

			}
			
	        BufferedOutputStream buffOut = new BufferedOutputStream(socket.getOutputStream());
	        BufferedInputStream buffIn = new BufferedInputStream(socket.getInputStream());
			

			buffOut.write(Message.WELC(this.ip, this.listenPortUDP, this.ip_diff, this.port_diff).getData());
			buffOut.flush();

			if (verboseMode) {
				String m1 = "WELC" + " " + this.ip + " " + this.listenPortUDP + " " + this.ip_diff + " "
						+ this.port_diff + "\n";
				System.out.println(threadToString()+"TCP : message SEND: " + m1);
			}
			
			Message msg2 =new Message(null);
			
			byte[] tmp=new byte[Ringo.maxSizeMsg];
			buffIn.read(tmp);
			msg2.setData(tmp);

			if (verboseMode) {
				System.out.println(threadToString()+"TCP : message RECEVE : " + m2);
			}

			buffOut.write(Message.ACKC().getData());
			buffOut.flush();

			if (verboseMode) {
				System.out.println(threadToString()+"TCP : message SEND: ACKC\\n");
			}

			buffOut.close();
			buffIn.close();
			socket.close();

			synchronized (portUDP1) {

			}
		}
	}



	public void send(Message msg) throws DOWNmessageException, SizeMessageException{
		isclose();
		
		if (msg.getData().length > Ringo.maxSizeMsg) {
			throw new SizeMessageException();
		}
		//TODO ID of the new message
		addToListToSend(msg);
	}
	
	private void addToListToSend(Message msg){
		synchronized (listToSend) {
			// TODO mettre en forme le message avant d'ajouter dans liste
			this.listToSend.add(msg);
			this.listToSend.notifyAll();
		}
	}

	public void receive(Message msg) throws DOWNmessageException {

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
			msg.setData(listForApply.pop().getDataForApply());
		}
	}
	
	private void receveMessage() throws IOException {
		
		if (verboseMode) {
			//System.out.println(threadToString()+"dans thread receve");
		}

		byte[] dataToReceve = new byte[100];
		DatagramPacket paquet = new DatagramPacket(dataToReceve, dataToReceve.length);
		if (verboseMode) {
			System.out.println(threadToString()+"j'attends de recevoir un message dans RECEVE");
		}

		this.sockRecever.receive(paquet);// attente passive

		
		String st = new String(paquet.getData(), 0, paquet.getLength());

		Message tmp = new Message(paquet.getData());
		if (verboseMode) {
			System.out.println(threadToString()+"Message Recu : " + st);
		}

		Integer idm = 100;

		if (st.startsWith("GBYE")) {
			String m = "EYBG" + " " + idm;
			//TODO

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

		Message msg;
		if (verboseMode) {
			//System.out.println(threadToString()+"dans thread send");
		}
		synchronized (listToSend) {

			while (listToSend.isEmpty()) {
				
				listToSend.notifyAll(); // pour le wait de closeServ
				
				if (verboseMode) {
					System.out.println(threadToString()+"j'attends d'avoir un message a envoyer dans SEND");
				}
				System.out.println(threadToString()+"FILE VIDE WAIT SEND methode");
				this.listToSend.wait();
			}
			msg=this.listToSend.pop();
			
		}
		
		byte[] dataTosend = msg.getData();
		
		if(msg.isMulti()){
			DatagramPacket paquetMulti = new DatagramPacket(dataTosend, dataTosend.length,
					InetAddress.getByName(this.ip_diff.toString()), port_diff);
			this.sockSender.send(paquetMulti);
		}
		else{
			
			if (portUDP1 != 0) {
				
				DatagramPacket paquet1 = new DatagramPacket(dataTosend, dataTosend.length,
						InetAddress.getByName(this.ipPortUDP1.toString()), portUDP1);
				
					this.sockSender.send(paquet1);
			
			}
			if (portUDP2 != 0) {
				DatagramPacket paquet2 = new DatagramPacket(dataTosend, dataTosend.length,
						InetAddress.getByName(this.ipPortUDP2.toString()), portUDP2);
				
					this.sockSender.send(paquet2);
				
			}
			
		}
		if (verboseMode) {
			if (verboseMode) {System.out.println(threadToString()+"Message envoyer : "+new String(msg.getData()));}
		}

	}

	public Serv(Integer numberLICENPortUDP,Integer numberPortTcp) throws IOException {

		super();
		this.ip_diff = "225.1.2.4";
		this.port_diff = 9999;
		this.numberPortTcp=numberPortTcp;
		this.sockServerTCP=new ServerSocket(numberPortTcp);
		this.sockSender = new DatagramSocket();
		this.listenPortUDP = numberLICENPortUDP;
		this.sockRecever = new DatagramSocket(numberLICENPortUDP);
		this.listToSend = new LinkedList<Message>();
		this.listForApply = new LinkedList<Message>();
		this.verboseMode=false;
		
		this.ipPortUDP1="localhost";
		this.ipPortUDP2="localhost";
		this.portUDP1=10;
		this.portUDP2=11;
		this.EYBGisArrive=false;
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
				if(verboseMode){System.out.println(threadToString()+"END thread RECEV");}
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
					}
				}
				if(verboseMode){System.out.println(threadToString()+"END thread MULTI");}
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
				if(verboseMode){System.out.println(threadToString()+"END thread SEND");}
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
				if(verboseMode){System.out.println(threadToString()+"END thread TCP");}
			}

		};

		this.ThRecev = new Thread(runRecev);
		this.ThSend1 = new Thread(runSend1);
		this.ThServTCP = new Thread(runServTCP);
		this.ThMULTIrecev = new Thread(runMULTIRecev);

		this.ThRecev.setName("Receve UDP");
		this.ThSend1.setName("Send UDP 1");
		this.ThServTCP.setName("server TCP");
		this.ThMULTIrecev.setName("MULTI-DIFF");
		
		//this.ThSend2.setName("Send UDP 2");
		
		this.ThRecev.start();
		this.ThSend1.start();
		this.ThServTCP.start();
		this.ThMULTIrecev.start();

	}

	public void dedoubler(int udpNew) throws AlreadyAllUdpPortSet, InterruptedException {

		if (this.portUDP1 != 0 && this.portUDP2 != 0) {
			throw new AlreadyAllUdpPortSet();

		} else if (this.portUDP2 == 0) {
			this.portUDP2 = udpNew;

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


		} else if (this.portUDP1 != 0) {
			this.portUDP1 = udpNew;

			// todo same que le else if, ameliorer synthaxe du code
		}
	}

	public int getIdEntite() {
		return idEntite;
	}

}
