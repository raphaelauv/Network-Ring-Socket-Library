import java.io.IOException;
import java.net.BindException;
import java.time.LocalDateTime;
import Protocol.Message;
import Protocol.Ringo;
import Protocol.Exceptions.*;

public class Diff extends Appl {
	
	public final static int byteSizeMess = 3;
	
	public Diff(Integer udpPort, Integer tcpPort) throws BindException, IOException {
		
		super("DIFF####", udpPort, tcpPort, true);

		Runnable runRecev = new Runnable() {
			public void run() {
				while (runContinue) {
					try {
						msgIN = ringoSocket.receive();
						byte[] content = msgIN.getData_app();
						int taille = Integer.parseInt(new String(content, 0, byteSizeMess));
						String message = new String(content, 4, taille);
						System.out.println(
								style + "\n" + LocalDateTime.now() + " -> " + "RECEVE :" + message + "\n" + style);

						ringoSocket.send(msgIN);//renvoi sur l'anneau du message
					} catch (DOWNmessageException e) {
						System.out.println("THREAD: APP RECEVE | DOWNmessageException , the socket is CLOSE");
						runContinue = false;
					}
				}
				ThSend.interrupt();
			}
		};

		Runnable runSend = new Runnable() {
			public void run() {
				int val = 0;
				boolean entrytested;
				while (runContinue) {
					entrytested = testEntry();

					if (!entrytested) {
						try {
							val++;
							String contenu = Message.longToStringRepresentation(input.length(), 3) + " " + input;
							ringoSocket.send(Message.APPL(val, "DIFF####", contenu.getBytes()));
						} catch (numberOfBytesException e) {
							//TODO
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

		super.initThread(runRecev,runSend,"DIFF");
	}

	public static void main(String[] args) {

		Appl.start(args);

		try {
			new Diff(Integer.parseInt(args[0]), Integer.parseInt(args[1]));

		} catch (BindException e) {
			System.out.println("The ports are already in use");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}