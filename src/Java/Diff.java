import java.io.IOException;
import java.net.BindException;
import java.time.LocalDateTime;
import Protocol.Message;
import Protocol.Exceptions.DOWNmessageException;
import Protocol.Exceptions.numberOfBytesException;
import Application.*;
public class Diff extends Appl implements ReceveSend {

	public final static int byteSizeMess = 3;

	public Diff(Integer udpPort, Integer tcpPort, boolean verbose) throws BindException, IOException {
		super("DIFF####", udpPort, tcpPort, false, verbose);
		Appl appl = this.getAppl();
		Thread ThRecev = new Thread(new MyRunnableReceve(appl, this));
		Thread ThSend = new Thread(new MyRunnableSend(appl, this));
		initThread(ThRecev, ThSend, "DIFF");
	}

	public void doReceve(byte[] msgInByte) throws DOWNmessageException {
		int taille = Integer.parseInt(new String(msgInByte, 0, byteSizeMess));
		String message = new String(msgInByte, 4, taille);
		System.out.println(style + "\n" + LocalDateTime.now() + " -> " + "RECEVE :" + message + "\n" + style);
		ringoSocket.send(msgIN);// renvoi sur l'anneau du message

	}

	public void doSend() throws numberOfBytesException, DOWNmessageException, InterruptedException {
		String contenu = Message.longToStringRepresentation(input.length(), 3) + " " + input;
		ringoSocket.send(Message.APPL(ringoSocket.getUniqueIdm(), "DIFF####", contenu.getBytes()));
	}

	public static void main(String[] args) {

		boolean verbose = Appl.testArgs(args);
		try {
			new Diff(Integer.parseInt(args[0]), Integer.parseInt(args[1]), verbose);

		} catch (BindException e) {
			System.out.println("The ports are already in use");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}