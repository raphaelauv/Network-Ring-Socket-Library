import java.io.IOException;
import java.net.BindException;
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
					while (true) {
						try {
							output=null;
							diffSocket.receive(output);
							
							System.out.println(LocalDateTime.now() +"|"+"RECEVE :"+new String(output));
						} catch (DOWNmessageException e) {
							System.out.println("DOWNmessageException , the socket is CLOSE");
						}
					}
				}
			};
			
			this.runSend = new Runnable() {
				public void run() {
					while (true) {
						input = scan.nextLine();
						input = input.length() + " " + input;
						try {
							diffSocket.send(input.getBytes());
						} catch (SizeMessageException e) {
							System.out.println("SizeMessageException !! the limit is : "+Ringo.maxSizeMsg);
						} catch (DOWNmessageException e) {
							System.out.println("DOWNmessageException , the socket is CLOSE");
						}
					}
				}
			};
			
			
			
			this.ThRecev = new Thread(runRecev);
			this.ThSend = new Thread(runSend);
			
			this.ThRecev.start();
			this.ThSend.start();
			
			this.ThRecev.join();
			this.ThSend.join();
			
		} catch (BindException e) {
			System.out.println("DOWNmessageException , the port are already in use");
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {

		if (args.length == 0 || args[0] == null || args[1] == null) {
			System.out.println("ATTENTION IL MANQUE ARGUMENT !!");
			return;
		}
		System.out.println("arg0 UDP : " + args[0]); // 4242
		System.out.println("arg1 TCP : " + args[1]); // 5s555

		Thread.currentThread().setName("DIFF -APPL");
		
		try {
			new Diff(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}