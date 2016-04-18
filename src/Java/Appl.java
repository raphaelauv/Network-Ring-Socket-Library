import java.io.IOException;
import java.net.BindException;
import java.net.UnknownHostException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import Protocol.Message;
import Protocol.RingoSocket;
import Protocol.Exceptions.*;
public abstract class Appl {

	final String style="#########################################################";
	String input;
	Message output;
	boolean runContinue;
	
	Thread ThRecev;
	Thread ThSend;
	
	Scanner scan;
	RingoSocket ringoSocket;
	
	public Appl(String APPLID,Integer udpPort, Integer tcpPort, boolean verboseMode) throws BindException,IOException{
		this.ringoSocket= new RingoSocket(APPLID,udpPort,tcpPort ,verboseMode);
		this.scan = new Scanner(System.in);
		this.runContinue=true;
	}
	
	/**
	 * Pour initialiser les threads , les nommer puis les lancer
	 * @param receve
	 * @param send
	 * @param name nom de l'APPL
	 */
	public void initThread(Runnable receve,Runnable send,String name){
		this.ThRecev = new Thread(receve);
		this.ThSend = new Thread(send);

		this.ThRecev.setName(name+" RECE");
		this.ThSend.setName(name+" SEND ");

		this.ThRecev.start();
		this.ThSend.start();
	}
	
	
	/**
	 * Test les arguments et affiche les informations de base des APPL
	 * @param args les args du main
	 */
	public static void start(String[] args){
		if (args==null || args.length == 0 || args[0] == null || args[1] == null) {
			System.out.println("ATTENTION IL MANQUE ARGUMENT !!");
			System.exit(1);
		}

		System.out.println("arg0 UDP : " + args[0]); // 4242
		System.out.println("arg1 TCP : " + args[1]); // 5555
		System.out.println("#########################################################");
		System.out.println("## To ask disconnect,type : disconnecT                 ##");
		System.out.println("## To ask connection,type :connecTo Ip Port ##");
		System.out.println("#########################################################");
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
				ringoSocket.close();
				runContinue = false;
				return true;
			}
			if (input.startsWith("connecTo ")) {
				
				System.out.println("##### ASK FOR CONNECTION #####");
				String info=input.substring(9,input.length());
				int positionEspace=info.indexOf(" ");
				String ip=info.substring(0,positionEspace);
				ip=Message.convertIP(ip);
				int port=Integer.parseInt(info.substring(positionEspace+1,info.length()));
				System.out.println(" | TRY TO CONNECT " + ip + " " + port);
				
				ringoSocket.connectTo(ip, port);
				
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
			e.printStackTrace();
			System.out.println("\nERREUR connecTo : IO");
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
		} catch (IpException e) {
			System.out.println("\nTHREAD connecTo : Erreur format IP invalide");
			return true;
		}
		
		return false;
	}
	
}
