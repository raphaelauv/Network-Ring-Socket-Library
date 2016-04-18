package Protocol;
import Protocol.Exceptions.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.lang.Runnable;

public class RingoSocket implements Ringo {

	private boolean verboseMode;
	String idApp;
	String ip;

	private int idEntite;

	ServerSocket sockServerTCP;
	private Integer portTcp;
	
	DatagramSocket sockSender;
	Integer portUDP1;
	String ipPortUDP1;

	Integer portUDP2;
	String ipPortUDP2;

	DatagramSocket sockRecever;
	Integer listenPortUDP;

	MulticastSocket sockMultiRECEP;
	String ip_diff;
	Integer port_diff;
	

	Set<Long> IdAlreadyReceveUDP1;// hashSet contenant les id deja croise
	Set<Long> IdAlreadyReceveUDP2;

	LinkedList<Message> listForApply; // liste des message recu qui sont pour cette ID
	LinkedList<Message> listToSend;// liste des message a envoyer
	
	private Thread ThRecev;
	private Thread ThMULTIrecev;
	private Thread ThServTCP;
	private Thread ThSend1;
	private Thread ThSend2;
	
	/*
	 * Verroux
	 */
	Semaphore tcpAcces;
	Semaphore EYBG_Acces;
	Semaphore UDP_ipPort_Acces;
	Semaphore idmAcces;
	
	private Long idmActuel;

	Object EYBGisArrive=new Object();
	boolean EYBGisArriveBool;

	Object TESTisComeBack=new Object();
	private Boolean TESTisComeBackBool;
	long ValTEST;

	private Boolean boolClose;

	/**
	 * Ferme tout les Thread de la RingoSocket
	 * @param modeDOWN if true envoi un DOWN en multi avant de fermer
	 */
	void closeServ(boolean modeDOWN) {

		this.boolClose = true;
		this.sockRecever.close();
		this.sockMultiRECEP.close();

		try {
			this.sockServerTCP.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		this.ThRecev.interrupt();

		this.ThServTCP.interrupt();
		;

		if (modeDOWN) {
			this.sockSender.close();
			this.ThSend1.interrupt();
			synchronized (listForApply) {
				this.listForApply.notifyAll();
			}
			// this.ThSend2.interrupt();
		} else {

			synchronized (listToSend) {
				while (!listToSend.isEmpty()) {
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
				// this.ThSend2.interrupt();
			}
			this.ThMULTIrecev.interrupt();
		}
	}

	public void close() throws InterruptedException, DOWNmessageException {
		isClose();
		int idm = 10000;//TODO
		Message msg = Message.GBYE(idm, this.ip, this.listenPortUDP, this.ipPortUDP1, this.portUDP1);

		synchronized (listToSend) {
			listToSend.add(msg);
			listToSend.notifyAll();
		}
		synchronized (this.EYBGisArrive) {
			this.EYBGisArriveBool = false;
			printVerbose("WAITING for EYBG message");
			this.EYBGisArrive.wait(5000); // attend que EYBG soit arriver a l'entite
			printVerbose("EYBG comeback ? :"+EYBGisArriveBool);
			if (EYBGisArriveBool) {
				closeServ(false);
				return;
			}
		}
		
		test(false);

		closeServ(false);
	}

	/**
	 * Tester si la ringoSocket est fermer
	 * @throws DOWNmessageException si la ringoSocket est fermer
	 */
	void isClose() throws DOWNmessageException {
		synchronized (boolClose) {
			if (boolClose) {
				throw new DOWNmessageException();
			}
		}
	}

	public boolean test(boolean sendDownIfBreak) throws InterruptedException, DOWNmessageException {
		isClose();
		int idm = 20;//TODO

		Message test = Message.TEST(idm, this.ip_diff, this.port_diff);
		
		send(test);
		
		synchronized (this.TESTisComeBack) {
			this.ValTEST = idm;
			this.TESTisComeBackBool= false;
			printVerbose("WAITING for TEST message");
			this.TESTisComeBack.wait(5000); // attend que EYBG soit arriver a l'entite
			
			if (!TESTisComeBackBool) {
				printVerbose("message TEST is NOT comeback");
				if (sendDownIfBreak) {
					send(Message.DOWN());
				}
				return false;
			}
			printVerbose("message TEST is comeback");
			return true;
		}
	}

	public void connectTo(String adresse, int idTCP)
			throws AlreadyAllUdpPortSet, UnknownHostException, IOException, DOWNmessageException, ProtocolException, InterruptedException {
		isClose();
		
		tcpAcces.acquire();	
		
		Socket socket = new Socket(adresse, idTCP);
		printVerbose("Conecter en TCP a :" + adresse + " sur port : " + idTCP);

		BufferedOutputStream buffOut = new BufferedOutputStream(socket.getOutputStream());
		BufferedInputStream buffIn = new BufferedInputStream(socket.getInputStream());

		byte[] tmp = new byte[Ringo.maxSizeMsg];
		int sizeReturn = buffIn.read(tmp);
		if (sizeReturn != 46) {
			socket.close();
			return;
		}
		//tmp = Arrays.copyOfRange(tmp, 0, 46);
		Message msg1 = null;
		try {
			msg1 = new Message(tmp);
		} catch (parseMessageException |unknownTypeMesssage e1) {
			e1.printStackTrace();//TODO a retirer apres tests
			socket.close();
			throw new ProtocolException();
		}
		if (msg1.getType() != TypeMessage.WELC) {
			socket.close();
			throw new ProtocolException();
		}

		printVerbose("TCP : message RECEVE : " + msg1.toString());

		Message msg2 = Message.NEWC(this.ip, this.portUDP1);
		buffOut.write(msg2.getData());
		buffOut.flush();

		printVerbose("TCP : message SEND   : " + msg2.toString());

		try {
			this.UDP_ipPort_Acces.acquire();
			System.out.println("verroux acquis");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sizeReturn = buffIn.read(tmp);

		if (sizeReturn != 5) {
			socket.close();
			return;
		}
		//tmp = Arrays.copyOfRange(tmp, 0, 5);

		Message msg3 = null;
		try {
			msg3 = new Message(tmp);
		} catch (parseMessageException | unknownTypeMesssage e) {
			socket.close();
			this.UDP_ipPort_Acces.release();
			throw new ProtocolException();
		}
		if (msg3.getType() != TypeMessage.ACKC) {
			socket.close();
			this.UDP_ipPort_Acces.release();
			throw new ProtocolException();
		}		
		printVerbose("TCP : message RECEVE : " + msg3.toString());

		
		this.ipPortUDP1 = msg1.getIp();
		this.portUDP1 = msg1.getPort();
		
		this.ip_diff = msg1.getIp_diff();
		this.port_diff = msg1.getPort_diff();
		this.UDP_ipPort_Acces.release();
		System.out.println("verroux liberer");
		
		buffOut.close();
		buffIn.close();
		socket.close();
		
		tcpAcces.release();
	}

	
	public void send(Message msg) throws DOWNmessageException {
		isClose();
		if (IdAlreadyReceveUDP1.contains(msg.getIdm())) {
			printVerbose(threadToString() + "Message DEJA ENVOYER OU RECU : " + msg.toString());
			return;
		} else {
			IdAlreadyReceveUDP1.add(msg.getIdm());
		}
		synchronized (listToSend) {
			this.listToSend.add(msg);
			this.listToSend.notifyAll();
		}
	}

	public Message receive() throws DOWNmessageException {
		isClose();
		synchronized (listForApply) {
			while (listForApply.isEmpty()) {
				isClose();// en cas de down durant l'attente
				try {
					listForApply.wait();
				} catch (InterruptedException e) {
				}
			}
			return listForApply.pop();
		}
	}

	public RingoSocket(String idApp, Integer LICENPortUDP, Integer numberPortTcp, boolean verboseMode)
			throws IOException {

		super();

		this.IdAlreadyReceveUDP1 = Collections.newSetFromMap(new ConcurrentHashMap<Long, Boolean>());

		this.idApp = idApp;
		this.ip = "127.000.000.001";
		this.ip_diff = "225.1.2.4";
		this.port_diff = 9999;
		this.portTcp = numberPortTcp;
		this.sockServerTCP = new ServerSocket(numberPortTcp);
		this.sockSender = new DatagramSocket();
		this.listenPortUDP = LICENPortUDP;
		this.sockRecever = new DatagramSocket(LICENPortUDP);

		this.listToSend = new LinkedList<Message>();
		this.listForApply = new LinkedList<Message>();
		this.verboseMode = false;
		this.verboseMode = verboseMode;

		this.ipPortUDP1 = "127.000.000.001";
		this.ipPortUDP2 = "127.000.000.001";
		this.portUDP1 = LICENPortUDP;
		this.portUDP2 = null;
		this.EYBGisArriveBool= false;
		this.boolClose = false;
		this.tcpAcces=new Semaphore(1);
		this.idmAcces=new Semaphore(1);
		this.UDP_ipPort_Acces = new Semaphore(1);
		this.EYBG_Acces= new Semaphore(0);
		this.idmActuel=0L;
		
		this.ThRecev = new Thread(new servUDPlisten(this).runServUDPlisten);
		this.ThSend1 = new Thread(new servUDPsend(this).runServUDPsend);
		this.ThServTCP = new Thread(new servTCP(this).runServTcp);
		this.ThMULTIrecev = new Thread(new servMULTI(this).runServMULTI);

		this.ThRecev.setName("Receve UDP");
		this.ThSend1.setName("Send UDP 1");
		this.ThServTCP.setName("server TCP");
		this.ThMULTIrecev.setName("MULTI-DIFF");

		this.ThRecev.start();
		this.ThSend1.start();
		this.ThServTCP.start();
		this.ThMULTIrecev.start();

	}
	
/*
	public void dedoubler(int udpNew) throws AlreadyAllUdpPortSet, InterruptedException {

		if (this.portUDP1 != 0 && this.portUDP2 != 0) {
			throw new AlreadyAllUdpPortSet();

		} else if (this.portUDP2 == 0) {
			this.portUDP2 = udpNew;

			this.runSend2 = new Runnable() {
				public void run() {
					boolean erreur = false;
					while (!erreur) {
						try {
							receveMessage();
						} catch (IOException | InterruptedException | DOWNmessageException e) {
							erreur = true;
						}
					}
					printVerbose("END thread UDP2");
				}
			};
			this.ThSend2 = new Thread(runSend2);
			this.ThSend2.start();

		} else if (this.portUDP1 != 0) {
			this.portUDP1 = udpNew;

			// todo same que le else if, ameliorer synthaxe du code
		}
	}
	
	*/
	public int getIdEntite() {
		return idEntite;
	}
	
	/**
	 * Affiche l'argument si en mode VERBOSE
	 * @param toPrint text a afficher
	 */
	void printVerbose(String toPrint) {
		if (verboseMode) {
			System.out.println(threadToString() + toPrint);
		}
	}
	/**
	 * Recuprer le nom du Thread actuel
	 * @return le String du nom du Thread actuel
	 */
	private String threadToString() {
		return "THREAD: " + Thread.currentThread().getName() + " | ";
	}
	
	public long getUniqueIdm(){
		try {
			idmAcces.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		idmAcces.release();
		return idmActuel++;
	}
}