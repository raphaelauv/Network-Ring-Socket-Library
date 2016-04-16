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
import java.lang.Runnable;

public class RingoSocket implements Ringo {

	private boolean verboseMode;

	private String idApp;

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

	private Set<Long> IdAlreadyReceveUDP1;// hashSet contenant les id deja
											// croise
	private Set<Long> IdAlreadyReceveUDP2;

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

	private Object EYBGisArrive=new Object(); //TODO replace par un semaphore
	private boolean EYBGisArriveBool;

	private Object TESTisComeBack=new Object();
	private Boolean TESTisComeBackBool;
	private long ValTEST;

	private Boolean boolClose;

	private void closeServ(boolean modeDOWN) {

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

	private void isClose() throws DOWNmessageException {
		synchronized (boolClose) {
			if (boolClose) {
				throw new DOWNmessageException();
			}
		}
	}

	private void receveMULTI() throws IOException, DOWNmessageException {
		this.sockMultiRECEP = new MulticastSocket(this.port_diff);
		this.sockMultiRECEP.joinGroup(InetAddress.getByName(ip_diff.toString()));

		byte[] data = new byte[100];
		DatagramPacket paquet = new DatagramPacket(data, data.length);

		this.sockMultiRECEP.receive(paquet);
		String st = new String(paquet.getData(), 0, paquet.getLength());
		printVerbose("message MULTI RECEVE : " + st);

		if (st.equals("DOWN\n")) {
			closeServ(true);
			throw new DOWNmessageException(); // to the thread MULTI
		}

	}

	private String threadToString() {
		return "THREAD: " + Thread.currentThread().getName() + " | ";
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
			throws AlreadyAllUdpPortSet, UnknownHostException, IOException, DOWNmessageException, ProtocolException {
		isClose();
		/*
		 * if (portUDP1 != 0) { throw new AlreadyAllUdpPortSet(); }
		 */
		// sockServerTCP.close();
		// synchronized (sockServerTCP) {// pas d'acceptation de connection
		// pendant
		// une connection
		Socket socket = new Socket(adresse, idTCP);
		printVerbose("conecter en TCP a :" + adresse + " sur port : " + idTCP);

		BufferedOutputStream buffOut = new BufferedOutputStream(socket.getOutputStream());
		BufferedInputStream buffIn = new BufferedInputStream(socket.getInputStream());

		byte[] tmp = new byte[Ringo.maxSizeMsg];
		int sizeReturn = buffIn.read(tmp);
		if (sizeReturn != 46) {
			socket.close();
			return;
		}
		tmp = Arrays.copyOfRange(tmp, 0, 46);
		Message msg1 = null;
		try {
			msg1 = new Message(tmp);
		} catch (unknownTypeMesssage e1) {
			e1.printStackTrace();
			socket.close();
			return;
		} catch (parseMessageException e) {
			e.printStackTrace();
			socket.close();
			return;
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

		sizeReturn = buffIn.read(tmp);

		if (sizeReturn != 5) {
			socket.close();
			return;
		}
		tmp = Arrays.copyOfRange(tmp, 0, 5);

		Message msg3 = null;
		try {
			msg3 = new Message(tmp);
		} catch (parseMessageException | unknownTypeMesssage e) {
			socket.close();
			throw new ProtocolException();
		}
		if (msg3.getType() != TypeMessage.ACKC) {
			socket.close();
			throw new ProtocolException();
		}
		printVerbose("TCP : message RECEVE : " + msg3.toString());

		synchronized (ipPortUDP1) {
			synchronized (portUDP1) {
				this.ipPortUDP1 = msg1.getIp();
				this.portUDP1 = msg1.getPort();
			}
		}
		synchronized (ip_diff) {
			synchronized (port_diff) {
				this.ip_diff = msg1.getIp_diff();
				this.port_diff = msg1.getPort_diff();
			}
		}
		buffOut.close();
		buffIn.close();
		socket.close();
	}

	/**
	 * Serv in TCP to accept an entrance TCP connection
	 * 
	 * @param idTCP
	 *            port TCP of serv
	 * @throws IOException
	 * @throws ProtocolException
	 * @throws DOWNmessageException
	 */
	private void servTCP() throws IOException, ProtocolException {
		synchronized (sockServerTCP) {

			Socket socket = sockServerTCP.accept();

			printVerbose("TCP connect");

			BufferedOutputStream buffOut = new BufferedOutputStream(socket.getOutputStream());
			BufferedInputStream buffIn = new BufferedInputStream(socket.getInputStream());

			Message msg1 = Message.WELC(this.ip, this.portUDP1, this.ip_diff, this.port_diff);
			buffOut.write(msg1.getData());
			buffOut.flush();

			printVerbose("TCP : message SEND   : " + msg1.toString());

			byte[] tmp = new byte[Ringo.maxSizeMsg];
			int sizeReturn = buffIn.read(tmp);
			if (sizeReturn != 25) {
				throw new ProtocolException();
			}
			tmp = Arrays.copyOfRange(tmp, 0, 25);

			Message msg2 = null;
			try {
				msg2 = new Message(tmp);
				if (msg2.getType() != TypeMessage.NEWC) {
					throw new ProtocolException();
				}
			} catch (parseMessageException | unknownTypeMesssage e) {
				printVerbose("TCP : erreur protocol");
				return;
			}

			printVerbose("TCP : message RECEVE : " + msg2.toString());

			synchronized (this.portUDP1) {
				this.portUDP1 = msg2.getPort();
			}

			Message msg3 = Message.ACKC();
			buffOut.write(msg3.getData());
			buffOut.flush();

			printVerbose("TCP : message SEND   : " + msg3.toString());

			buffOut.close();
			buffIn.close();
		}
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

	private void receveMessage() throws IOException, InterruptedException {

		byte[] dataToReceve = new byte[Ringo.maxSizeMsg];
		DatagramPacket paquet = new DatagramPacket(dataToReceve, dataToReceve.length);

		this.sockRecever.receive(paquet);// attente passive
		Message msgR = null;
		try {
			msgR = new Message(paquet.getData());
		} catch (parseMessageException | unknownTypeMesssage e) {
			e.printStackTrace();
			return;
		}

		if (IdAlreadyReceveUDP1.contains(msgR.getIdm())) {
			printVerbose("Message DEJA ENVOYER OU RECU : " + msgR.toString());
			return;
		} else {
			IdAlreadyReceveUDP1.add(msgR.getIdm());
		}
		printVerbose("Message Recu    : " + msgR.toString());

		if (msgR.getType() == TypeMessage.GBYE) {
			if(msgR.getIp().equals(this.ipPortUDP1) && msgR.getPort().equals(this.portUDP1)){
				printVerbose("My next leave the RING");
				synchronized (this.listToSend) {
					this.listToSend.add(Message.EYBG(300));
					this.listToSend.notifyAll();
				}
				synchronized(this.ipPortUDP1){
					this.ipPortUDP1.wait();
					synchronized (this.portUDP1) {
						this.ipPortUDP1=msgR.getIp_succ();
						this.portUDP1=msgR.getPort_succ();
					}
				}
				return;
			}
			
		} else if (msgR.getType() == TypeMessage.TEST) {
			if (msgR.getIdm() == ValTEST) {
				this.TESTisComeBack = true;
				return;
			}

		} else if (msgR.getType() == TypeMessage.EYBG) {
			synchronized (this.EYBGisArrive) {
				this.EYBGisArriveBool=true;
				this.EYBGisArrive.notify();
			}
			return;

		} else if (msgR.getType() == TypeMessage.APPL) {

			if (msgR.getId_app().equals(idApp)) {
				synchronized (this.listForApply) {
					this.listForApply.add(msgR);
					this.listForApply.notifyAll();
				}
				// TODO renvoyer automatique ou pas
			}
		}
		synchronized (this.listToSend) {
			this.listToSend.add(msgR);
			this.listToSend.notifyAll();
		}
	}

	private void sendMessage() throws IOException, InterruptedException {

		Message msg;
		synchronized (listToSend) {
			while (listToSend.isEmpty()) {
				listToSend.notifyAll(); // pour le wait de closeServ
				this.listToSend.wait();
			}
			msg = this.listToSend.pop();
		}

		byte[] dataTosend = msg.getData();

		if (msg.isMulti()) {
			DatagramPacket paquetMulti = new DatagramPacket(dataTosend, dataTosend.length,
					InetAddress.getByName(this.ip_diff.toString()), port_diff);
			this.sockSender.send(paquetMulti);
		} else {

			if (portUDP1 != null) {

				String ipTemp;
				synchronized(this.ipPortUDP1){
					ipTemp=new String(this.ipPortUDP1);
				}
				
				
				DatagramPacket paquet1 = new DatagramPacket(dataTosend, dataTosend.length,
						InetAddress.getByName(ipTemp), portUDP1);

				this.sockSender.send(paquet1);

			}
			if (portUDP2 != null) {
				
				String ipTemp;
				synchronized(this.ipPortUDP2){
					ipTemp=new String(this.ipPortUDP2);
				}
				DatagramPacket paquet2 = new DatagramPacket(dataTosend, dataTosend.length,
						InetAddress.getByName(ipTemp), portUDP2);

				this.sockSender.send(paquet2);
			}
		}
		
		//Pour debloquer l'attente de changement de port
		if(msg.getType()==TypeMessage.EYBG){
			synchronized(this.ipPortUDP1){
				ipPortUDP1.notifyAll();
			}
		}
		
		printVerbose("Message Envoyer : " + msg.toString());

	}

	public RingoSocket(String idApp, Integer numberLICENPortUDP, Integer numberPortTcp, boolean verboseMode)
			throws IOException {

		super();

		this.IdAlreadyReceveUDP1 = Collections.newSetFromMap(new ConcurrentHashMap<Long, Boolean>());

		this.idApp = idApp;
		this.ip = "127.000.000.001";
		this.ip_diff = "225.1.2.4";
		this.port_diff = 9999;
		this.numberPortTcp = numberPortTcp;
		this.sockServerTCP = new ServerSocket(numberPortTcp);
		this.sockSender = new DatagramSocket();
		this.listenPortUDP = numberLICENPortUDP;
		this.sockRecever = new DatagramSocket(numberLICENPortUDP);

		this.listToSend = new LinkedList<Message>();
		this.listForApply = new LinkedList<Message>();
		this.verboseMode = false;
		this.verboseMode = verboseMode;

		this.ipPortUDP1 = "127.000.000.001";
		this.ipPortUDP2 = "127.000.000.001";
		this.portUDP1 = numberLICENPortUDP;
		this.portUDP2 = null;
		this.EYBGisArriveBool= false;
		this.boolClose = false;

		/*******************************************************************
		 * Creation des 4 thread anonyme : d'envoi UDP | reception UDP | serv
		 * tcp | reception multi
		 * 
		 */
		this.runRecev = new Runnable() {
			public void run() {
				boolean erreur = false;
				while (!erreur) {
					try {
						receveMessage();
					} catch (IOException | InterruptedException e) {
						erreur = true;
					}
				}
				printVerbose("END thread RECEV");
			}
		};

		this.runMULTIRecev = new Runnable() {
			public void run() {
				boolean erreur = false;
				while (!erreur) {
					try {
						receveMULTI();
					} catch (DOWNmessageException | IOException e) {
						erreur = true;
					}
				}
				printVerbose("END thread MULTI");
			}
		};

		this.runSend1 = new Runnable() {
			public void run() {
				boolean erreur = false;
				while (!erreur) {
					try {
						sendMessage();
					} catch (InterruptedException | IOException e) {
						erreur = true;
					}
				}
				printVerbose("END thread SEND");
			}
		};

		this.runServTCP = new Runnable() {
			public void run() {
				boolean erreur = false;
				while (!erreur) {
					try {
						servTCP();
					} catch (ProtocolException | IOException e) {
						try {
							isClose();
						} catch (DOWNmessageException e1) {
							// TODO Auto-generated catch block
							// e1.printStackTrace();
							erreur = true;
						}
					}
				}
				printVerbose("END thread TCP");
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

		// this.ThSend2.setName("Send UDP 2");

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
					boolean erreur = false;
					while (!erreur) {
						try {
							receveMessage();
						} catch (IOException | InterruptedException e) {
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

	private void printVerbose(String toPrint) {
		if (verboseMode) {
			System.out.println(threadToString() + toPrint);
		}
	}
}