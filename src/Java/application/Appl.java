package application;

import protocol.*;
import protocol.exceptions.*;
import java.io.Closeable;
import java.io.IOException;
import java.net.BindException;
import java.net.SocketException;
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
	 * @param verbose
	 * @throws BindException
	 * @throws IOException
	 * @throws IpException
	 * @throws ParseException 
	 */
	public Appl(String ip,String APPLID,Integer udpPort, Integer tcpPort,boolean verbose) throws BindException,IOException, ParseException{
		this.APPLID=APPLID;
		this.verboseMode=verbose;
		this.ringoSocket= new RingoSocket(ip,APPLID,udpPort,tcpPort,false);
		this.ringoSocket.setVerbose(verbose);
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
		System.out.println("arg0 UDP : " + args[0]+"\narg1 TCP : " + args[1]);
		
		System.out.println(style+"\n"
				+ "## add -v after the port argument for VERBOSE Mode          ##\n"
				+ "## To ask connection      type : connecTo Ip Port           ##\n"
				+ "## To ask duplication     type : dupl Ip Port               ##\n"
				+ "## To ask test            type : testT                      ##\n"
				+ "## To ask whos            type : whoS                       ##\n"
				+ "## To ask disconnect      type : disconnecT                 ##\n"
				+ "## To ask down            type : dowN                       ##\n"
				+ "## For closing Appl       type : closeAppl                  ##\n"
				+ style );
		
		if(args.length>2 && args[2].equals("-v")){
			return true;
		}
		return false;
		
	}
	
	public static String selectIp () throws SocketException{
		LinkedList<String> allIp=OwnIp.getAllIp();
		int i=0;
		System.out.println("select an IP between 0 and "+(allIp.size()-1));
		for(String tmp : allIp){
			System.out.println(i+") "+tmp);
			i++;
		}
		String ipSelect = null;
		@SuppressWarnings("resource")
		Scanner scanner= new Scanner(System.in);
		boolean notGoodSelect=true;
		int input=-1;
		while(notGoodSelect){
			try{input = Integer.parseInt(scanner.nextLine());}
			catch(NumberFormatException e){
				System.out.println("input incorrect , just tape a number");
			}
			if(input>=0 && input<i){
				notGoodSelect=false;
				ipSelect=allIp.get(input);;
				System.out.println("IP SELECT : "+ipSelect);
			}
			
		}
		return ipSelect;
	}
	
	/**
	 * Test if the user ask for connecTo or disconnecT or other action 
	 * 
	 * @return true if the user asked for an action, else false
	 * @throws UnknownTypeMesssage 
	 * @throws ParseException 
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
			else if (input.equals("whoS")) {
				printModeApplication("##### ASK FOR WHOS #####");
				ringoSocket.whos();
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
		} catch (IOException e) {
			printModeApplication("\nERREUR connecTo : IO - ConnectException");
			return true;
		} catch (InterruptedException e) {
			printModeApplication("\nERREUR connecTo : Interrupted");
			return true;
		} catch (NoSuchElementException e) {
			printModeApplication("\nERREUR connecTo : NoSuchElement");
			return true;
		} catch (ProtocolException  |ParseException | UnknownTypeMesssage e ) {
			printModeApplication("\nERREUR connecTo : Erreur de protocol");
			return true;
		} catch (RingoSocketCloseException e) {
			printModeApplication("\nTHREAD: APP SEND   | DOWNmessageException , the socket is CLOSE");
			runContinue = false;
			return true;
		} catch(StringIndexOutOfBoundsException |NumberFormatException e){
			printModeApplication("\nERREUR connecTo : Erreur format IP ou port invalide");
			return true;
		} catch (AlreadyConnectException e) {
			printModeApplication("\nERREUR connecTo : deja connecter , utiliser disconnecT ou Dupl");
			return true;
		} catch (ImpossibleDUPLConnection e) {
			printModeApplication("\nERREUR connecTo : impossible to connect To Dupl entity");
			return true;
		}catch(Exception e){
			e.printStackTrace();
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