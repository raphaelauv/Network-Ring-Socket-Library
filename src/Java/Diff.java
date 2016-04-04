import java.io.IOException;
import java.net.BindException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Scanner;
public class Diff {
	
	private Thread ThRecev;
	private Thread ThSend;
	
	private Runnable runRecev;
	private Runnable runSend;
	
	private Scanner scan;
	private String input;
	
	private byte[] output;
	private RingoSocket diffSocket;
	public Diff(Integer udp, Integer tcp) throws InterruptedException{
		
		this.scan = new Scanner(System.in);
		
		try {
			this.diffSocket= new RingoSocket("DIFF####",udp,tcp ,true);
			
			this.runRecev = new Runnable() {
				public void run() {
					boolean noError=true;
					while (noError) {
						try {
							output=null;
							diffSocket.receive(output);
							System.out.println(LocalDateTime.now() +"|"+"RECEVE :"+new String(output));
						} catch (DOWNmessageException e) {
							System.out.println("Thread APP RECEVE | DOWNmessageException , the socket is CLOSE");
							noError=false;
						}
					}
				}
			};
			
			this.runSend = new Runnable() {
				public void run() {
					boolean noError=true;
					while (noError) {
						input = scan.nextLine();
						
						try {
							if(input.equals("disconnect")){
								diffSocket.close();
								noError=true;
							}
							if(input.startsWith("connecTo ")){
								
								System.out.println("ASK FOR CONNECTION");
								Message a=new Message(input.getBytes(),"Noparse");
								a.parse_IP_SPACE_Port(9, Message.FLAG_IP_NORMAL);
								System.out.println("TRY TO CONNECT");
								diffSocket.connectTo(a.getIp(), a.getPort());
								
							}else{
								input = input.length() + " " + input;
								diffSocket.send(input.getBytes());
							}
						} catch (SizeMessageException e) {
							System.out.println("SizeMessageException !! the limit is : "+Ringo.maxSizeMsg);
						} catch (DOWNmessageException e) {
							System.out.println("Thread APP SEND | DOWNmessageException , the socket is CLOSE");
							noError=false;
						} catch (parseMessageException e) {
							System.out.println("respect :connecTo ipAdresse port");
						} catch (UnknownHostException e) {
							System.out.println("connecTo : UnknownHost ");
						} catch (AlreadyAllUdpPortSet e) {
							System.out.println("connecTo : Already connect");
						} catch (IOException e) {
							//e.printStackTrace();
						} catch (InterruptedException e) {
							//e.printStackTrace();
						}
					}
					ThRecev.interrupt();
				}
			};
			
			this.ThRecev = new Thread(runRecev);
			this.ThSend = new Thread(runSend);
			
			this.ThRecev.start();
			this.ThSend.start();
			
			
		} catch (BindException e) {
			System.out.println("The ports are already in use");
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {

		if (args==null || args.length == 0 || args[0] == null || args[1] == null) {
			System.out.println("ATTENTION IL MANQUE ARGUMENT !!");
			return;
		}
		
		System.out.println("arg0 UDP : " + args[0]); // 4242
		System.out.println("arg1 TCP : " + args[1]); // 5555
		
		System.out.println("To ask connection type :connecTo IpADRESSE(15) Port");
		
		try {
			new Diff(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
			
		} catch (InterruptedException e) {
			//e.printStackTrace();
		}
	}
}