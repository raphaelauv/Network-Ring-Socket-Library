import java.io.IOException;
import java.net.BindException;
import java.time.LocalDateTime;

public class Diff extends Appl {

	public Diff(Integer udpPort, Integer tcpPort) throws BindException, IOException {
		
		super("DIFF####", udpPort, tcpPort, true);

		this.runRecev = new Runnable() {
			public void run() {
				while (runContinue) {
					try {
						output = diffSocket.receive();
						byte[] content = output.getData_app();
						int taille = Integer.parseInt(new String(content, 0, 3));
						String message = new String(content, 4, taille);
						System.out.println(
								style + "\n" + LocalDateTime.now() + " -> " + "RECEVE :" + message + "\n" + style);

					} catch (DOWNmessageException e) {
						System.out.println("THREAD: APP RECEVE | DOWNmessageException , the socket is CLOSE");
						runContinue = false;
					}
				}
				ThSend.interrupt();
			}
		};

		this.runSend = new Runnable() {
			public void run() {
				int val = 0;
				boolean entrytested;
				while (runContinue) {
					entrytested = testEntry();

					if (!entrytested) {
						try {
							val++;
							String contenu = Message.longToStringRepresentation(input.length(), 3) + " " + input;
							diffSocket.send(Message.APPL(val, "DIFF####", contenu.getBytes()));
						} catch (numberOfBytesException | SizeMessageException e) {
							System.out.println("\nERREUR SizeMessageException !! the limit is : " + Ringo.maxSizeMsg);
						} catch (DOWNmessageException e) {
							System.out.println("\nTHREAD: APP SEND   | DOWNmessageException , the socket is CLOSE");
							runContinue = false;
						}

					}
					else{
						if(!runContinue){
							System.out.println("\nTHREAD: APP SEND   | END");
						}
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

	}

	public static void main(String[] args) {

		if (!Appl.testArgs(args)) {
			return;
		}
		Appl.printInfo(args);

		try {
			new Diff(Integer.parseInt(args[0]), Integer.parseInt(args[1]));

		} catch (BindException e) {
			System.out.println("The ports are already in use");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}