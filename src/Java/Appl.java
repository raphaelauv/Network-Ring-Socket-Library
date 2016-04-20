import java.io.IOException;
import java.net.BindException;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import Protocol.Message;
import Protocol.RingoSocket;
import Protocol.Exceptions.*;
public class Appl {

	final String style="#########################################################";
	String input;
	Message msgIN;
	boolean runContinue;
	
	Thread ThRecev;
	Thread ThSend;
	
	Scanner scan;
	RingoSocket ringoSocket;
	public Appl getAppl(){
		return this;
	}
	
	public Appl(String APPLID,Integer udpPort, Integer tcpPort,boolean relayMSGAuto ,boolean verboseMode) throws BindException,IOException{
		this.ringoSocket= new RingoSocket(APPLID,udpPort,tcpPort,relayMSGAuto ,verboseMode);
		this.scan = new Scanner(System.in);
		this.runContinue=true;
	}
	
	/**
	 * Pour initialiser les threads , les nommer puis les lancer
	 * @param receve
	 * @param send
	 * @param name nom de l'APPL
	 */
	public void initThread(Thread ThRecev,Thread ThSend,String name){
	
		this.ThRecev=ThRecev;
		this.ThSend=ThSend;
		this.ThRecev.setName(name+" RECE");
		this.ThSend.setName(name+" SEND ");

		this.ThRecev.start();
		this.ThSend.start();
	}
	
	
	/**
	 * Test les arguments et affiche les informations de base des APPL
	 * @param args les args du main
	 * @return 
	 */
	public static boolean testArgs(String[] args){
		if (args==null || args.length == 0 || args[0] == null || args[1] == null) {
			System.out.println("ATTENTION IL MANQUE ARGUMENT !!");
			System.exit(1);
		}
		System.out.println("arg0 UDP : " + args[0]); // 4242
		System.out.println("arg1 TCP : " + args[1]); // 5555
		System.out.println("#########################################################");
		System.out.println("## add -v after the port argument for VERBOSE Mode     ##");
		System.out.println("## To ask disconnect,type : disconnecT                 ##");
		System.out.println("## To ask connection,type : connecTo Ip Port           ##");
		System.out.println("#########################################################");
		if(args.length>2 && args[2].equals("-v")){
			return true;
		}
		return false;
		
	}
	
	
	/**
	 * Test if the user ask for connecTo or disconnecT 
	 * 
	 * @return true if the user asked for 1 or this action else false
	 */
	public boolean testEntry(){
		try {
			input = scan.nextLine();
			if (input.equals("disconnecT")) {
				System.out.println("##### ASK FOR DISCONNECT #####");
				ringoSocket.disconnect();
				return true;
			}
			else if(input.equals("closeAppl")){
				System.out.println("##### ASK FOR CLOSING #####");
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
					System.out.println("##### ASK FOR DUPLICATION #####");
					curseur+=5;
				}else{
					System.out.println("##### ASK FOR CONNECTION #####");
					curseur+=9;
				}
				
				String info=input.substring(curseur,input.length());
				
				int positionEspace=info.indexOf(" ");
				String ip=info.substring(0,positionEspace);
				ip=Message.convertIP(ip);
				int port=Integer.parseInt(info.substring(positionEspace+1,info.length()));
				System.out.println(" | TRY TO CONNECT " + ip + " " + port);
				if(dupl){
					ringoSocket.connectTo(ip, port, true);
				}else{
					ringoSocket.connectTo(ip, port,false);
				}
				
				System.out.println(" ---> SUCCES");
				return true;
			}
		} catch (UnknownHostException e) {
			System.out.println("\nERREUR connecTo : UnknownHost ");
			return true;
		} catch (AlreadyAllUdpPortSet e) {
			e.printStackTrace();
			System.out.println("\nERREUR connecTo : Already connect");
			return true;
		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println("\nERREUR connecTo : IO - ConnectException");
			return true;
		} catch (InterruptedException e) {
			System.out.println("\nERREUR connecTo : Interrupted");
		} catch (NoSuchElementException e) {
			System.out.println("\nERREUR connecTo : NoSuchElement");
			return true;
		} catch (ProtocolException e) {
			System.out.println("\nERREUR connecTo : Erreur de protocol");
			return true;
		} catch (DOWNmessageException e) {
			System.out.println("\nTHREAD: APP SEND   | DOWNmessageException , the socket is CLOSE");
			runContinue = false;
		} catch(IpException |StringIndexOutOfBoundsException |NumberFormatException e){
			System.out.println("\nTHREAD connecTo : Erreur format IP ou port invalide");
			return true;
		} catch (AlreadyConnectException e) {
			System.out.println("\nTHREAD connecTo : deja connecter , utiliser disconnecT ou Dupl");
		} catch (ImpossibleDUPLConnection e) {
			System.out.println("\nTHREAD connecTo : impossible to connect To Dupl entity");
		}
		return false;
	}
	
	/**
	 * pour lancer une simple entite ringoSocket , denouer de toute interaction utilisateur 
	 * @param args
	 */
	public static void main(String[] args) {
		boolean verboseMode=testArgs(args);
		try {
			new Appl(null,Integer.parseInt(args[0]), Integer.parseInt(args[1]),true,verboseMode);
			
		} catch (BindException e) {
			System.out.println("The ports are already in use");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
