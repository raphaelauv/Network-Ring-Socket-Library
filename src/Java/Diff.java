import java.io.IOException;	
import java.net.BindException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Scanner;
public class Diff {
	
	private Thread ThRecev;
	private Thread ThSend;
	
	private Runnable runRecev;
	private Runnable runSend;
	
	private Scanner scan;
	private String input;
	
	private Message output;
	private RingoSocket diffSocket;
	public Diff(Integer udpPort, Integer tcpPort) throws InterruptedException{
		
		this.scan = new Scanner(System.in);
		
		try {
			this.diffSocket= new RingoSocket("DIFF####",udpPort,tcpPort ,true);
			
			this.runRecev = new Runnable() {
				public void run() {
					boolean noError=true;
					while (noError) {
						try {
							output=null;
							diffSocket.receive(output);
							System.out.println(LocalDateTime.now() +"|"+"RECEVE :"+new String(output.toString()));
						} catch (DOWNmessageException e) {
							System.out.println("THREAD: APP RECEVE | DOWNmessageException , the socket is CLOSE");
							noError=false;
						}
					}
				}
			};
			
			this.runSend = new Runnable() {
				public void run() {
					boolean noError=true;
					int val=0;
					while (noError) {
						
						try {
							input = scan.nextLine();
							if(input.equals("disconnect")){
								diffSocket.close();
								noError=true;
							}
							if(input.startsWith("connecTo ")){
								System.out.print("##### ASK FOR CONNECTION #####");
								Message a=new Message(input.getBytes(),"Noparse");
								a.parse_IP_SPACE_Port(9, Message.FLAG_IP_NORMAL);
							
								System.out.println(" | TRY TO CONNECT "+a.getIp()+" "+a.getPort());
								diffSocket.connectTo(a.getIp(), a.getPort());
							}else{
								val++;
								diffSocket.send(Message.APPL(val,"DIFF####",input.length(),input.getBytes()));
							}
						} catch (SizeMessageException e) {
							System.out.println("\nERREUR SizeMessageException !! the limit is : "+Ringo.maxSizeMsg);
						} catch (DOWNmessageException e) {
							System.out.println("\nTHREAD: APP SEND   | DOWNmessageException , the socket is CLOSE");
							noError=false;
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
							//e.printStackTrace();
						} 
					}
					ThRecev.interrupt();
				}
			};
			
			this.ThRecev = new Thread(runRecev);
			this.ThSend = new Thread(runSend);
			
			this.ThRecev.setName("DIFF RECE");
			this.ThSend.setName("DIFF SEND ");
			
			this.ThRecev.start();
			this.ThSend.start();
			
			
		} catch (BindException e) {
			System.out.println("The ports are already in use");
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws UnknownHostException, unknownTypeMesssage, parseMessageException {

		if (args==null || args.length == 0 || args[0] == null || args[1] == null) {
			System.out.println("ATTENTION IL MANQUE ARGUMENT !!");
			return;
		}
		System.out.println("arg0 UDP : " + args[0]); // 4242
		System.out.println("arg1 TCP : " + args[1]); // 5555
		System.out.println("#########################################################");
		System.out.println("## To ask connection,type :connecTo IpADRESSE(15) Port ##");
		System.out.println("#########################################################");
		try {
			new Diff(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
			
		} catch (InterruptedException e) {
			//e.printStackTrace();
		}
	}
}