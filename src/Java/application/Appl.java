package application;

import protocol.*;
import protocol.exceptions.*;

import java.io.Closeable;
import java.io.IOException;
import java.net.BindException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Appl implements Closeable{

	private boolean modeService;
	protected boolean runContinue;
	protected boolean verboseMode;
	
	protected String APPLID;
	protected String input;
	
	protected Thread ThRecev;
	protected Thread ThSend;
	protected Scanner scan;
	
	protected RingoSocket ringoSocket;
	protected final static String style="##############################################################";
	
	protected LinkedList<byte []> listInput;	// mode service
	protected LinkedList<byte []> listOutput; // mode service
	
	/**
	 * Constructeur pour application independante , ecoute sur STDIN et ecris sur STDOUT
	 * @param APPLID
	 * @param udpPort
	 * @param tcpPort
	 * @param relayMSGAuto
	 * @param verboseMode
	 * @throws BindException
	 * @throws IOException
	 * @throws IpException
	 */
	public Appl(String APPLID,Integer udpPort, Integer tcpPort,boolean verboseMode) throws BindException,IOException, IpException{
		this.APPLID=APPLID;
		this.verboseMode=verboseMode;
		this.ringoSocket= new RingoSocket(APPLID,udpPort,tcpPort ,verboseMode,false);
		this.scan = new Scanner(System.in);
		this.runContinue=true;
		this.modeService=false;
	}
	
	/**
	 * Constructeur pour service
	 * @param APPLID
	 * @param relayMSGAuto
	 * @param ringoSocket
	 */
	public Appl(String APPLID,RingoSocket ringoSocket){
		this.APPLID=APPLID;
		this.verboseMode=false;
		this.ringoSocket=ringoSocket;
		this.listInput = new LinkedList<byte []>();
		this.listOutput=new LinkedList<byte []>();
		this.runContinue=true;
		this.modeService=true;
	}
	
	public void input(byte [] content) throws Exception{
		if(!modeService){
			throw new Exception();
		}
		synchronized (this.listInput) {
			this.listInput.add(content);
			this.listInput.notify();
		}
	}
	
	public byte[] output() throws Exception, InterruptedException{
		if(!modeService){
			throw new Exception();
		}
		synchronized (listOutput) {
			while (listOutput.isEmpty()) {
				listOutput.wait();
			}
			return listOutput.pop();
		}
	}
	
	public void close() throws IOException{
		runContinue = false;
		ringoSocket.close();
		
	}
	
	/**
	 * Pour initialiser les threads , les nommer puis les lancer
	 * @param receve
	 * @param send
	 * @param name nom de l'APPL
	 */
	protected void initThread(MyRunnableReceve runnableReceve,MyRunnableSend runnableSend){
	
		this.ThRecev=new Thread(runnableReceve);
		this.ThSend=new Thread(runnableSend);
		this.ThRecev.setName(APPLID+" RECE");
		this.ThSend.setName(APPLID+" SEND ");
		if(modeService){
			this.ThRecev.setDaemon(true);
			this.ThSend.setDaemon(true);
		}
		this.ThRecev.start();
		this.ThSend.start();
		
		
	}
	
	/**
	 * Test les arguments et affiche les informations de base des APPL
	 * pour le  mode Application
	 * @param args les args du main
	 * @return 
	 */
	public static boolean testArgs(String[] args){
		if (args==null || args.length == 0 || args[0] == null || args[1] == null) {
			System.out.println("ATTENTION IL MANQUE ARGUMENT , entrer les deux ports  !!");
			System.exit(1);
		}
		System.out.println("arg0 UDP : " + args[0]); // 4242
		System.out.println("arg1 TCP : " + args[1]); // 5555
		System.out.println(style);
		System.out.println("## add -v after the port argument for VERBOSE Mode          ##");
		System.out.println("## To ask connection      type : connecTo Ip Port           ##");
		System.out.println("## To ask duplication     type : dupl Ip Port               ##");
		System.out.println("## To ask test            type : testT                      ##");
		System.out.println("## To ask disconnect      type : disconnecT                 ##");
		System.out.println("## To ask down            type : dowN                       ##");
		System.out.println("## For closing Appl       type : closeAppl                  ##");
		
		System.out.println(style);
		if(args.length>2 && args[2].equals("-v")){
			return true;
		}
		return false;
		
	}
	
	
	/**
	 * Test if the user ask for connecTo or disconnecT or other action 
	 * 
	 * @return true if the user asked for an action, else false
	 * @throws UnknownTypeMesssage 
	 * @throws ParseMessageException 
	 */
	public boolean testEntry(){
		try {
			if(!modeService){//mode application
				input = scan.nextLine();
			}else{
				synchronized (this.listInput) {
					while (this.listInput.isEmpty()) {
						this.listInput.wait();
					}
					input = new String(this.listInput.pop());
					return false;// mode service
				}
			}
			
			
			if(ringoSocket.isClose()){
				return true;//si l'entity a fermer pendant le nextline()
			}
			if (input.equals("tesT")) {
				printModeApplication("##### ASK FOR TEST #####");
				ringoSocket.test(false);
				return true;
			}
			else if (input.equals("dowN")) {
				printModeApplication("##### ASK FOR DOWN #####");
				runContinue = false;
				ringoSocket.down();
				return true;
			}
			else if (input.equals("disconnecT")) {
				printModeApplication("##### ASK FOR DISCONNECT #####");
				ringoSocket.disconnect();
				return true;
			}
			else if(input.equals("closeAppl")){
				printModeApplication("##### ASK FOR CLOSING #####");
				ringoSocket.close();
				runContinue = false;
				return true;
			}
			if (input.startsWith("connecTo ") || input.startsWith("dupl ")) {
				
				boolean dupl=false;
				int curseur=0;
				if(input.startsWith("dupl")){
					dupl=true;
				}
				if(dupl){
					printModeApplication("##### ASK FOR DUPLICATION #####");
					curseur+=5;
				}else{
					printModeApplication("##### ASK FOR CONNECTION #####");
					curseur+=9;
				}
				
				String info=input.substring(curseur,input.length());
				
				int positionEspace=info.indexOf(" ");
				String ip=info.substring(0,positionEspace);
				ip=Message.convertIP(ip);
				int port=Integer.parseInt(info.substring(positionEspace+1,info.length()));
				printModeApplication(" | TRY TO CONNECT " + ip + " " + port);
				if(dupl){
					ringoSocket.connectTo(ip, port, true);
				}else{
					ringoSocket.connectTo(ip, port,false);
				}
				
				printModeApplication(" ---> SUCCES");
				return true;
			}
		} catch (UnknownHostException e) {
			printModeApplication("\nERREUR connecTo : UnknownHost ");
			return true;
		} catch (AlreadyAllUdpPortSet e) {
			printModeApplication("\nERREUR connecTo : Already connect");
			return true;
		} catch (IOException e) {
			printModeApplication("\nERREUR connecTo : IO - ConnectException");
			return true;
		} catch (InterruptedException e) {
			printModeApplication("\nERREUR connecTo : Interrupted");
			return true;
		} catch (NoSuchElementException e) {
			printModeApplication("\nERREUR connecTo : NoSuchElement");
			return true;
		} catch (ProtocolException  |ParseMessageException | UnknownTypeMesssage e ) {
			printModeApplication("\nERREUR connecTo : Erreur de protocol");
			return true;
		} catch (DOWNmessageException e) {
			printModeApplication("\nTHREAD: APP SEND   | DOWNmessageException , the socket is CLOSE");
			runContinue = false;
			return true;
		} catch(IpException |StringIndexOutOfBoundsException |NumberFormatException e){
			printModeApplication("\nERREUR connecTo : Erreur format IP ou port invalide");
			return true;
		} catch (AlreadyConnectException e) {
			printModeApplication("\nERREUR connecTo : deja connecter , utiliser disconnecT ou Dupl");
			return true;
		} catch (ImpossibleDUPLConnection e) {
			printModeApplication("\nERREUR connecTo : impossible to connect To Dupl entity");
			return true;
		}
		return false;
	}
	
	/**
	 * Print uniquement AFFICHER sur la STDIN en mode Application
	 * @param toPrint contenu a afficher
	 */
	protected void printModeApplication(String toPrint) {
		if (!modeService) {
			System.out.println(toPrint);
		}
	}
	
}