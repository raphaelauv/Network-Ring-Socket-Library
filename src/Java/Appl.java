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
	
	Runnable runRecev;
	Runnable runSend;
	Scanner scan;
	
	RingoSocket diffSocket;
	
	public Appl(String APPLID,Integer udpPort, Integer tcpPort, boolean verboseMode) throws BindException,IOException{
		this.diffSocket= new RingoSocket(APPLID,udpPort,tcpPort ,verboseMode);
		this.scan = new Scanner(System.in);
		this.runContinue=true;
	}
	
	public static boolean testArgs(String[] args){
		if (args==null || args.length == 0 || args[0] == null || args[1] == null) {
			System.out.println("ATTENTION IL MANQUE ARGUMENT !!");
			return false;
		}
		return true;
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
				diffSocket.close();
				runContinue = false;
				return true;
			}
			if (input.startsWith("connecTo ")) {
				System.out.println("##### ASK FOR CONNECTION #####");
				Message a = new Message(input.getBytes(), "Noparse");
				a.parse_IP_SPACE_Port(9, Message.FLAG_IP_NORMAL);

				System.out.println(" | TRY TO CONNECT " + a.getIp() + " " + a.getPort());
				diffSocket.connectTo(a.getIp(), a.getPort());
				
				return true;
			}
		} catch (parseMessageException e) {
			System.out.println("\nERREUR respect :connecTo ipAdresse port");
		} catch (UnknownHostException e) {
			System.out.println("\nERREUR connecTo : UnknownHost ");
		} catch (AlreadyAllUdpPortSet e) {
			e.printStackTrace();
			System.out.println("\nERREUR connecTo : Already connect");
		} catch (IOException e) {
			System.out.println("\nERREUR connecTo : IO");
		} catch (InterruptedException e) {
			System.out.println("\nERREUR connecTo : Interrupted");
		} catch (NoSuchElementException e) {
			System.out.println("\nERREUR connecTo : NoSuchElement");
		} catch (ProtocolException e) {
			System.out.println("\nERREUR connecTo : Erreur de protocol");
		} catch (DOWNmessageException e) {
			System.out.println("\nTHREAD: APP SEND   | DOWNmessageException , the socket is CLOSE");
			runContinue = false;
		}
		
		return false;
	}
	
	public static void printInfo(String[] args){
		System.out.println("arg0 UDP : " + args[0]); // 4242
		System.out.println("arg1 TCP : " + args[1]); // 5555
		System.out.println("#########################################################");
		System.out.println("## To ask disconnect,type : disconnecT                 ##");
		System.out.println("## To ask connection,type :connecTo IpADRESSE(15) Port ##");
		System.out.println("#########################################################");
	}
}
