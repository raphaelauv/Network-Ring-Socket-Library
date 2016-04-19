package Protocol;
import Protocol.Exceptions.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class RingoSocket implements Ringo {

	private boolean verboseMode;
	String idApp;
	String ip;


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
	
	private byte [] idmStart;

	Object EYBGisArrive=new Object();
	boolean EYBGisArriveBool;

	Object TESTisComeBack=new Object();
	private Boolean TESTisComeBackBool;
	long ValTEST;

	private Boolean boolClose;
	private Boolean boolDisconnect;

	/**
	 * Ferme tout les Thread de la RingoSocket
	 * @param modeDOWN if true envoi un DOWN en multi avant de fermer
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	void closeServ(boolean modeDOWN) throws InterruptedException, IOException {

		this.boolClose = true;
		this.sockRecever.close();
		this.sockMultiRECEP.close();
		this.sockServerTCP.close();
		
		this.ThRecev.interrupt();

		this.ThServTCP.interrupt();
		;

		if (modeDOWN) {
			this.sockSender.close();
			this.ThSend1.interrupt();
			synchronized (listForApply) {
				this.listForApply.notify();
			}
			// this.ThSend2.interrupt();
		} else {

			synchronized (listToSend) {
				while (!listToSend.isEmpty()) {
					listToSend.notify();
					listToSend.wait();
					
				}
				this.sockSender.close();
				this.ThSend1.interrupt();
				// this.ThSend2.interrupt();
			}
			this.ThMULTIrecev.interrupt();
		}
	}

	public void disconnect() throws InterruptedException, DOWNmessageException{
		testClose();
		Message msg = Message.GBYE(getUniqueIdm(), this.ip, this.listenPortUDP, this.ipPortUDP1, this.portUDP1);

		synchronized (listToSend) {
			listToSend.add(msg);
			listToSend.notify();
		}
		synchronized (this.EYBGisArrive) {
			this.EYBGisArriveBool = false;
			printVerbose("WAITING for EYBG message");
			this.EYBGisArrive.wait(5000); // attend que EYBG soit arriver a l'entite
			printVerbose("EYBG comeback ? :"+EYBGisArriveBool);
			boolDisconnect=true;
			
			UDP_ipPort_Acces.acquire();
			this.ipPortUDP1=this.ip;
			this.portUDP1=this.listenPortUDP;
			UDP_ipPort_Acces.release();
			
			printVerbose("DISCONNECT DONE");
			return;
		}
	}
	
	public void close() throws InterruptedException, DOWNmessageException, IOException {
		testClose();
		if(boolDisconnect){
			closeServ(false);
		}
		else{
			disconnect();
			closeServ(false);
		}
	}

	/**
	 * Tester si la ringoSocket est fermer
	 * @throws DOWNmessageException si la ringoSocket est fermer
	 */
	void testClose() throws DOWNmessageException {
		synchronized (boolClose) {
			if (boolClose) {
				throw new DOWNmessageException();
			}
		}
	}
		
	public boolean isClose(){
		try{
			testClose();
		}catch(DOWNmessageException e){
			return true;
		}
		return false;
	}

	public boolean test(boolean sendDownIfBreak) throws InterruptedException, DOWNmessageException {
		testClose();
		
		long idm=getUniqueIdm();
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
		testClose();
		
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
			msg1 = Message.parseMessage(tmp);
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
			msg3 = Message.parseMessage(tmp);
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
		
		buffOut.close();
		buffIn.close();
		socket.close();
		boolDisconnect=false;
		tcpAcces.release();
	}

	
	public void send(Message msg) throws DOWNmessageException {
		
		testClose();
		if(msg==null){
			return;
		}
		/*if (IdAlreadyReceveUDP1.contains(msg.getIdm())) {
			printVerbose(threadToString() + "Message DEJA ENVOYER OU RECU : " + msg.toString());
			return;
		} 
			*/
		IdAlreadyReceveUDP1.add(msg.getIdm());
		synchronized (listToSend) {
			this.listToSend.add(msg);
			this.listToSend.notify();
		}
	}

	public Message receive() throws DOWNmessageException, InterruptedException {
		testClose();
		synchronized (listForApply) {
			while (listForApply.isEmpty()) {
				testClose();// en cas de down durant l'attente
				listForApply.wait();

			}
			return listForApply.pop();
		}
	}

	public RingoSocket(String idApp, Integer LICENPortUDP, Integer numberPortTcp, boolean verboseMode)
			throws IOException {

		super();

		this.IdAlreadyReceveUDP1 = Collections.newSetFromMap(new ConcurrentHashMap<Long, Boolean>());

		if(idApp==null){
			this.idApp ="";
		}else{
			this.idApp = idApp;
		}
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
		this.boolDisconnect =true;
		this.tcpAcces=new Semaphore(1);
		this.idmAcces=new Semaphore(1);
		this.UDP_ipPort_Acces = new Semaphore(1);
		this.EYBG_Acces= new Semaphore(0);
		this.idmActuel=0L;
		
		this.build_IDM_array();
		
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
	
	/**
	 * Build the start of the IDM array
	 * 
	 * @throws UnknownHostException
	 */
	private void build_IDM_array() throws UnknownHostException{

		InetAddress ip = InetAddress.getByName(this.ip);
		byte[] ipBytes = ip.getAddress();
		
		
		byte[] portBytes = new byte[2];
		portBytes[0] = (byte)(this.portTcp & 0xFF);
		portBytes[1] = (byte)((this.portTcp >> 8) & 0xFF);
		
		this.idmStart= new byte[Ringo.byteSizeIdm];
		
		int cmp=0;
		for(int j=0;j<ipBytes.length;j++){
			this.idmStart[cmp]=ipBytes[j];
			cmp++;
		}
		for(int j=0; j<portBytes.length;j++){
			this.idmStart[cmp]=portBytes[j];
			cmp++;
		}
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
	
	public long getUniqueIdm() throws DOWNmessageException, InterruptedException{
		testClose();
		
		idmAcces.acquire();
		
		byte [] end_of_IDM= new byte[2];
		this.idmActuel=this.idmActuel%65000;//~limite de 2^255;
		end_of_IDM[0] = (byte)(this.idmActuel & 0xFF);
		end_of_IDM[1] = (byte)((this.idmActuel >> 8) & 0xFF);
		idmActuel++;
		idmAcces.release();
		byte[] val=Arrays.copyOf(this.idmStart, Ringo.byteSizeIdm);
		
		
		
		
		val[6]=end_of_IDM[0];
		val[7]=end_of_IDM[1];
		
		/*
		System.out.print("IDM :");
		for (byte b : val) {
		    System.out.print(b & 0xFF);
		    System.out.print(" ");
		}
		*/
		
		long valLong=Message.byteArrayToLong(val,Ringo.byteSizeIdm, ByteOrder.nativeOrder());
		
		return valLong;
	}
}