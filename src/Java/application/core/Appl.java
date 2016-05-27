package application.core;

import protocol.*;
import protocol.exceptions.*;
import java.io.Closeable;
import java.io.IOException;
import java.net.BindException;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Scanner;

public abstract class Appl implements Closeable{

	protected boolean modeService;
	private boolean verboseAppl=false;
	protected boolean runContinue;
	protected boolean verboseMode;
	
	protected String APPLID;
	private String input;
	
	protected Thread ThRecev;
	protected Thread ThSend;
	protected Scanner scan;
	
	protected RingoSocket ringoSocket;
	protected final static String style="##############################################################";
	
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
	public Appl(String ip,String APPLID,Integer udpPort, Integer tcpPort,Integer multiPort,boolean verbose) throws BindException,IOException, ParseException{
		this.APPLID=APPLID;
		this.verboseMode=verbose;
		this.ringoSocket= new RingoSocket(ip,APPLID,udpPort,tcpPort,multiPort,false);
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
		this.listOutput=new LinkedList<byte []>();
		this.runContinue=true;
		this.modeService=true;
	}
	
	public void close() throws IOException{
		runContinue = false;
		ringoSocket.close();
		
	}

	/**
	 * Test les arguments et affiche les informations de base des APPL
	 * pour le  mode Application
	 * @param args les args du main
	 * @return 
	 */
	public static boolean testArgs(String[] args){
		if (args==null || args.length < 3 || args[0] == null || args[1] == null || args[2] == null) {
			System.out.println("ATTENTION IL MANQUE ARGUMENT , entrer les deux ports  !!");
			System.exit(1);
		}
		System.out.println("arg0 UDP  : " + args[0]+"\narg1 TCP  : " + args[1]+"\narg2 MULTI : " + args[2]);
		
		if(args.length>3 && args[3].equals("-v")){
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
		
		System.out.println(style+"\n"
				+ "## add -v after the port argument for VERBOSE Mode          ##\n"
				+ "## To ask connection      type : connecTo Ip Port           ##\n"
				+ "## To ask duplication     type : dupL Ip Port               ##\n"
				+ "## To ask test            type : tesT                       ##\n"
				+ "## To ask whos            type : whoS                       ##\n"
				+ "## To ask disconnect      type : disconnecT                 ##\n"
				+ "## To ask down            type : dowN                       ##\n"
				+ "## For closing Appl       type : closeAppl                  ##\n"
				+ style );
		
		return ipSelect;
	}
	
	/**
	 * Test if the user ask for connecTo or disconnecT or other action 
	 * 
	 * @return null if its a command or a string of the input of user
	 */
	protected String testEntry(){
		try {
			if(!modeService){//mode application
				input = scan.nextLine();
			}else{
				return null;
			}
			
			
			if(ringoSocket.isClose()){
				return null;//si l'entity a fermer pendant le nextline()
			}
			if (input.equals("tesT")) {
				printModeApplication("##### ASK FOR TEST #####");
				ringoSocket.test(false);
				return null;
			}
			else if (input.equals("whoS")) {
				printModeApplication("##### ASK FOR WHOS #####");
				ringoSocket.whos();
				return null;
			}
			else if (input.equals("dowN")) {
				printModeApplication("##### ASK FOR DOWN #####");
				runContinue = false;
				ringoSocket.down();
				return null;
			}
			else if (input.equals("disconnecT")) {
				printModeApplication("##### ASK FOR DISCONNECT #####");
				ringoSocket.disconnect();
				return null;
			}
			else if(input.equals("closeAppl")){
				printModeApplication("##### ASK FOR CLOSING #####");
				ringoSocket.close();
				runContinue = false;
				return null;
			}
			if (input.startsWith("connecTo ") || input.startsWith("dupL ")) {
				
				boolean dupl=false;
				int curseur=0;
				if(input.startsWith("dupL")){
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
					ringoSocket.connect(ip, port, true);
				}else{
					ringoSocket.connect(ip, port,false);
				}
				printModeApplication(" ---> SUCCES");
				return null;
			}
			
			
			return input;
		} catch (IOException e) {
			printModeApplication("\nERREUR connecTo : IO - "+e.getMessage()+" , retry");
			
		} catch (InterruptedException e) {
			printModeApplication("\nERREUR connecTo : Interrupted ");
			
		} catch (NoSuchElementException e) {
			printModeApplication("\nERREUR connecTo : NoSuchElement");
			
		} catch (ProtocolException  |ParseException | UnknownTypeMesssage e ) {
			printModeApplication("\nERREUR connecTo : Erreur de protocol");
			
		} catch (RingoSocketCloseException e) {
			printModeApplication("\nTHREAD: APP SEND   | DOWNmessageException , the socket is CLOSE");
			runContinue = false;
			
		} catch(StringIndexOutOfBoundsException |NumberFormatException e){
			printModeApplication("\nERREUR connecTo : Erreur format IP ou port invalide");
			
		} catch (AlreadyConnectException e) {
			printModeApplication("\nERREUR connecTo : deja connecter ou connection a soi meme ( utiliser disconnecT ou Dupl)");
			
		} catch (ImpossibleDUPLConnection e) {
			printModeApplication("\nERREUR connecTo : impossible to connect To Dupl entity");
			
		}catch(Exception e){
			runContinue = false;
			printModeApplication(e.getMessage());
		}
		return null;

	}
	
	/**
	 * Print uniquement AFFICHER sur la STDIN en mode Application
	 * @param toPrint contenu a afficher
	 */
	protected void printModeApplication(String toPrint) {
		if (!modeService || verboseAppl) {
			System.out.println(toPrint);
		}
	}
	
	public void setVerbose(boolean verboseAppl){
		this.verboseAppl=verboseAppl;
	}
}