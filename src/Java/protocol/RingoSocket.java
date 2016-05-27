package protocol;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;


import protocol.exceptions.AlreadyConnectException;
import protocol.exceptions.ImpossibleDUPLConnection;

import protocol.exceptions.ParseException;
import protocol.exceptions.ProtocolException;
import protocol.exceptions.RingoSocketCloseException;
import protocol.exceptions.UnknownTypeMesssage;
import uidm.IpPort_IDM;
import uidm.TimeIDM;
import uidm.UniqueIDM;

public class RingoSocket implements Ringo {
	
	
	class EntityInfo{
		String ipUdp;
		Integer portUdp;
		String ip_diff;
		Integer port_diff;
		public EntityInfo(String ipUdp, Integer portUdp, String ip_diff, Integer port_diff) {
			this.ipUdp = ipUdp;
			this.portUdp = portUdp;
			this.ip_diff = ip_diff;
			this.port_diff = port_diff;
		}
	}
	
	/********************************************************************
	 * NETWORK
	 */
	final String idApp;
	final String ip;
	final Integer portTcp;
	final Integer listenPortUDP;
	
	EntityInfo principal ;
	EntityInfo secondaire ;
	/*
	Integer portUDP1;
	String ipPortUDP1;
	Integer portUDPdupl;
	String ipPortUDPdupl;
	*/
	
	DatagramSocket sockRecever;
	DatagramSocket sockSender;
	ServerSocket sockServerTCP;

	/********************************************************************
	 * THREADS
	 */
	ServMULTI servMulti;
	private Thread ThRecev;
	private Thread ThMULTIrecev;
	private Thread ThServTCP;
	private Thread ThSend;
	
	/********************************************************************
	 * Verroux , set , boolean
	 */
	Semaphore tcpAcces;
	Semaphore EYBG_Acces;
	Semaphore UDP_MULTI_ipPort_Acces;
	Semaphore idmAcces;
	Object EYBGisArrive=new Object();//mutex
	AtomicBoolean EYBGisArriveBool=new AtomicBoolean();
	Object TESTisComeBack=new Object();//mutex
	AtomicBoolean TESTisComeBackBool=new AtomicBoolean();
	Long ValTest;
	AtomicBoolean isDUPL;
	
	Set<Long> IdAlreadyReceveUDP;// hashSet contenant les id deja croise
	LinkedList<Message> listForApply; // liste des message recu qui sont pour cette ID
	LinkedList<Message> listToSend;// liste des message a envoyer
	ConcurrentHashMap<InetSocketAddress,String> members;
	
	private AtomicBoolean verboseMode;
	AtomicBoolean boolClose;
	AtomicBoolean boolDisconnect;
	
	private UniqueIDM uidm;
	
	/*********************************************************************/

	
	/**
	 * Creer une entite RINGO
	 * @param idApp le nom de l'application
	 * @param listenUDPport port d'ecoute UDP
	 * @param portTcp port TCP port d'ecoute TCP
	 */
	public RingoSocket(String ip,String idApp, Integer listenUDPport, Integer portTcp, Integer multiPort,boolean modeService) throws IOException, ParseException
			 {
		
		if(idApp==null){
			this.idApp ="########";
		}else{
			if(idApp.length()!=8){
				throw new IOException();
			}
			this.idApp = idApp;
		}
		/********************************************************************
		 * NETWORK
		 */
		
		this.ip = Message.convertIP(ip);
		this.portTcp = portTcp;
		this.listenPortUDP = listenUDPport;
		
		this.principal=new EntityInfo(this.ip, this.listenPortUDP,Message.convertIP("225.1.2.4"),multiPort);

		this.sockServerTCP = new ServerSocket(portTcp);
		this.sockSender = new DatagramSocket();
		this.sockRecever = new DatagramSocket(listenUDPport);

		
		/********************************************************************
		 * boolean , semaphores , set , listes
		 */
		this.listToSend = new LinkedList<Message>();
		this.listForApply = new LinkedList<Message>();
		this.IdAlreadyReceveUDP = Collections.newSetFromMap(new ConcurrentHashMap<Long, Boolean>());
		this.EYBGisArriveBool= new AtomicBoolean(false);
		this.isDUPL=new AtomicBoolean(false);
		this.verboseMode = new AtomicBoolean(false);
		this.boolClose = new AtomicBoolean(false);
		this.boolDisconnect =new AtomicBoolean(true);
		this.tcpAcces=new Semaphore(1);
		this.idmAcces=new Semaphore(1);
		this.UDP_MULTI_ipPort_Acces = new Semaphore(1);
		this.EYBG_Acces= new Semaphore(0);
		
		//this.uidm=new IpPort_IDM(this.ip, this.portTcp);
		this.uidm= new TimeIDM();
		
		/********************************************************************
		 * THREADS
		 */
		this.servMulti =new ServMULTI(this,this.principal);
		this.ThRecev = new Thread(new ServUDPlisten(this),"Receve UDP");
		this.ThSend = new Thread(new ServUDPsend(this),"Send UDP  ");
		this.ThServTCP = new Thread(new ServTCP(this),"Server TCP");
		this.ThMULTIrecev = new Thread(servMulti,"MULTI-DIFF");


		if(modeService){
			this.ThRecev.setDaemon(true);
			this.ThSend.setDaemon(true);
			this.ThServTCP.setDaemon(true);
			this.ThMULTIrecev.setDaemon(true);
		}
		this.ThRecev.start();
		this.ThSend.start();
		this.ThServTCP.start();
		this.ThMULTIrecev.start();
	}

	public void editUIDM(UniqueIDM uidm){
		this.uidm=uidm;
	}
	
	/**
	 * Ferme tout les Thread de la RingoSocket
	 * @param modeDOWN if true envoi un DOWN en multi avant de fermer
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	void closeRingoSocket(boolean modeDOWN) throws IOException {

		this.boolClose.set(true);;
		this.sockRecever.close();
		this.sockServerTCP.close();
		
		this.ThRecev.interrupt();
		this.ThServTCP.interrupt();

		if (modeDOWN) {
			this.sockSender.close();
			this.ThSend.interrupt();
		} else {
			finishToSend();
			this.sockSender.close();
			this.ThSend.interrupt();
		}
		
		synchronized (listForApply) {
			this.listForApply.notify();
		}
		
		this.servMulti.sel.close();
		this.ThMULTIrecev.interrupt();
		
	}
	
	/**
	 * finish to send all message inside the RingoSocket
	 */
	private void finishToSend(){
		synchronized (listToSend) {
			while (!listToSend.isEmpty()) {
				listToSend.notify();
				try {
					listToSend.wait();
				} catch (InterruptedException e) {}
			}
		}
	}
	
	void duplClose(EntityInfo entityinfo) throws InterruptedException{
		
		UDP_MULTI_ipPort_Acces.acquire();
		if(this.principal==entityinfo){
			this.principal=this.secondaire;
		}
		this.secondaire=null;
		this.isDUPL.set(false);;
		
		UDP_MULTI_ipPort_Acces.release();
		
	}

	public void disconnect() throws InterruptedException, RingoSocketCloseException, ParseException, IOException{
		testClose();
		if(this.isDUPL.get()){
			return;
		}
		if(this.boolDisconnect.get()){
			return;
		}
		Message msg = Message.GBYE(getUniqueIdm(), this.ip, this.listenPortUDP, this.principal.ipUdp, this.principal.portUdp);
		send(msg);
		synchronized (this.EYBGisArrive) {
			this.EYBGisArriveBool.set(false);
			printVerbose("WAITING for EYBG message");
			this.EYBGisArrive.wait(maximumWaitTimeMessage); // attend que EYBG soit arriver a l'entite
			printVerbose("EYBG comeback ? :"+EYBGisArriveBool);
			
			
			UDP_MULTI_ipPort_Acces.acquire();
			this.principal.ipUdp=this.ip;
			this.principal.portUdp=this.listenPortUDP;
			//this.finishToSend();
			this.boolDisconnect.set(true);;
			UDP_MULTI_ipPort_Acces.release();
			
			
			printVerbose("DISCONNECT DONE");
			return;
		}
	}
	
	public void close() throws IOException {
		if(isClose()){
			return;
		}
		if(boolDisconnect.get()){
			//already disconnect
			closeRingoSocket(false);
		}
		else{
			//not yet disconnect
			try {
				disconnect();
			} catch (InterruptedException | RingoSocketCloseException | ParseException e) {
				//erreur during disconnect
			}
			closeRingoSocket(false);
		}
	}

	/**
	 * Tester si la ringoSocket est fermer
	 * @throws RingoSocketCloseException si la ringoSocket est fermer
	 */
	void testClose() throws RingoSocketCloseException {
		if (boolClose.get()) {
			throw new RingoSocketCloseException();
		}
	}

	public boolean isClose(){
		try{
			testClose();
		}catch(RingoSocketCloseException e){
			return true;
		}
		return false;
	}
	
	public HashMap<InetSocketAddress,String> whos() throws RingoSocketCloseException, InterruptedException, ParseException, IOException{
		testClose();
		long idm=getUniqueIdm();
		Message whos=Message.WHOS(idm);
		
		synchronized (this.TESTisComeBack) {
			this.ValTest=idm;
			this.TESTisComeBackBool.set(false);;
			send(whos);
			printVerbose("WAITING for WHOS message");
			this.TESTisComeBack.wait(maximumWaitTimeMessage); // attend que EYBG soit arriver a l'entite
			if (!TESTisComeBackBool.get()){
				printVerbose("message WHOS is NOT comeback");
				return null;
			}else{
				printVerbose("message WHOS is comeback");
				this.TESTisComeBack.wait(maximumWaitTimeMessage);// re attente pour attendre le maximum de MEMB
				this.ValTest=null;
			}
		}

		HashMap<InetSocketAddress, String> tmp=new HashMap<InetSocketAddress,String>();
		tmp.putAll(this.members);
		printVerbose("nombre de MEMB : "+tmp.size());
		return  tmp;
	}

	
	
	public boolean test(boolean sendDownIfBreak) throws InterruptedException, RingoSocketCloseException, ParseException, IOException {
		testClose();
		long idm=getUniqueIdm();
		Message test;
		test = Message.TEST(idm, this.principal.ip_diff, this.principal.port_diff);
		
		synchronized (this.TESTisComeBack) {
			this.ValTest=idm;
			this.TESTisComeBackBool.set(false);;
			send(test);
			printVerbose("WAITING for TEST message");
			this.TESTisComeBack.wait(maximumWaitTimeMessage); // attend que EYBG soit arriver a l'entite
			if (!TESTisComeBackBool.get()) {
				printVerbose("message TEST is NOT comeback");
				if (sendDownIfBreak) {
					down();
				}
				return false;
			}
			printVerbose("message TEST is comeback");
			return true;
		}
	}

	public void down() throws RingoSocketCloseException{
		send(Message.DOWN());
	}
	
	
	public void connect(RingoSocket ringo,boolean modeDUPL) 
			throws ParseException, RingoSocketCloseException, ProtocolException, 
			InterruptedException, AlreadyConnectException, ImpossibleDUPLConnection, IOException, UnknownTypeMesssage{
		this.connect(ringo.ip,ringo.portTcp,modeDUPL);
	}
	
	public void connect(String adresse, int TCP,boolean modeDUPL)
			throws ParseException, RingoSocketCloseException, ProtocolException, 
			InterruptedException, AlreadyConnectException, ImpossibleDUPLConnection, IOException ,UnknownTypeMesssage {
		
		testClose();
		adresse=Message.convertIP(adresse);
		
		
		if(( TCP==this.portTcp && adresse.equals(this.ip))){
			System.out.println("moi meme");
			throw new AlreadyConnectException();
			
		}
		if(!boolDisconnect.get()){
			throw new AlreadyConnectException();
		}
		tcpAcces.acquire();
		
		Socket socket=new Socket();
		try {
			socket.connect(new InetSocketAddress(adresse, TCP),5000); 
			
		} catch (IOException e) {
			tcpAcces.release();
			socket.close();
			throw e;
		}
		printVerbose("Conecter en TCP a :" + adresse + " sur port : " + TCP);

		BufferedOutputStream buffOut = new BufferedOutputStream(socket.getOutputStream());
		BufferedInputStream buffIn = new BufferedInputStream(socket.getInputStream());
		byte[] tmp = new byte[Ringo.maxSizeMsg];
		Message msg1 = null;
		int sizeReturn;
		try {
			sizeReturn = buffIn.read(tmp);
			/*
			 * //TODO if (sizeReturn != 47) { socket.close(); throw new
			 * ProtocolException(); } tmp = Arrays.copyOfRange(tmp, 0, 46);
			 */
			try {
				msg1 = Message.parseMessage(tmp);
			} catch (ParseException | UnknownTypeMesssage e1) {
				printVerbose("TCP : erreur de protocol\n    message recu : "+new String(tmp));
				throw new ProtocolException();
			}
			if (msg1.getType() == TypeMessage.NOTC) {
				throw new ImpossibleDUPLConnection();
			}
			else if (msg1.getType() != TypeMessage.WELC) {
				throw new ProtocolException();
			}

			printVerbose("TCP : message RECEVE : " + msg1.toString());
			Message msg2;
			if (modeDUPL) {
				msg2 = Message.DUPL(this.ip, this.listenPortUDP, this.principal.ip_diff, this.principal.port_diff);
			} else {
				msg2 = Message.NEWC(this.ip, this.listenPortUDP);
			}
			buffOut.write(msg2.getData());
			buffOut.flush();

			printVerbose("TCP : message SEND   : " + msg2.toString());

			/*
			 * //TODO if (sizeReturn != 6) { socket.close(); throw new
			 * ProtocolException(); } tmp = Arrays.copyOfRange(tmp, 0, 5);
			 */

		} catch (Exception e) {
			socket.close();
			buffIn.close();
			socket.close();
			this.tcpAcces.release();
			throw e;
		}
		
		this.UDP_MULTI_ipPort_Acces.acquire();
		
		
		Message msg3 = null;
		try {
			sizeReturn = buffIn.read(tmp);
			msg3 = Message.parseMessage(tmp);
		} catch (UnknownTypeMesssage | ParseException | IOException e) {
			this.tcpAcces.release();
			this.UDP_MULTI_ipPort_Acces.release();
			buffOut.close();
			buffIn.close();
			socket.close();
			throw e;
		}
		
		printVerbose("TCP : message RECEVE : " + msg3.toString());
		
		boolean erreur=false;
		if(modeDUPL){
			if(msg3.getType() != TypeMessage.ACKD){
				erreur=true;
			}
		}
		else if(msg3.getType() != TypeMessage.ACKC){
				erreur=true;
		}
		if(erreur){
			buffOut.close();
			buffIn.close();
			socket.close();
			this.UDP_MULTI_ipPort_Acces.release();
			this.tcpAcces.release();
			throw new ProtocolException();
		}

		if(modeDUPL){
			this.principal.ipUdp=adresse;
			this.principal.portUdp=msg3.getPort();
		}else{
			this.principal.ipUdp=msg1.getIp();
			this.principal.portUdp=msg1.getPort();
			this.principal.ip_diff=msg1.getIp_diff();
			this.principal.port_diff=msg1.getPort_diff();
			
			this.servMulti.updateMulti(this.principal);
		}
		this.boolDisconnect.set(false);
		buffOut.close();
		buffIn.close();
		socket.close();
		this.UDP_MULTI_ipPort_Acces.release();
		this.tcpAcces.release();
	}

	
	public void send(Message msg) throws RingoSocketCloseException {
		testClose();
		if(msg==null){
			return;
		}
		IdAlreadyReceveUDP.add(msg.getIdm());
		synchronized (listToSend) {
			this.listToSend.add(msg);
			this.listToSend.notify();
		}
	}

	public Message receive() throws RingoSocketCloseException, InterruptedException {
		testClose();
		synchronized (listForApply) {
			while (listForApply.isEmpty()) {
				testClose();// en cas de down durant l'attente
				listForApply.wait();
			}
			return listForApply.pop();
		}
	}

		
	public void setVerbose(boolean verbose){
		this.verboseMode.set(verbose);
	}


	/**
	 * Affiche l'argument si en mode VERBOSE
	 * @param toPrint text a afficher
	 */
	void printVerbose(String toPrint) {
		if (verboseMode.get()) {
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
	
	public long getUniqueIdm() throws RingoSocketCloseException, InterruptedException, IOException{
		testClose();
		return this.uidm.getIDM();
	}	
}