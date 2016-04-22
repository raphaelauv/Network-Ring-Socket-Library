import java.io.IOException;
import java.net.BindException;
import java.time.LocalDateTime;
import protocol.*;
import protocol.exceptions.*;
import application.*;

public class Diff extends Appl implements ReceveSend {

	public final static int byteSizeMess = 3;

	public Diff(Integer udpPort, Integer tcpPort, boolean verbose) throws BindException, IOException, IpException {
		super("DIFF####", udpPort, tcpPort, false, verbose);	
		super.initThread(new MyRunnableReceve(this), new MyRunnableSend(this));
	}
	
	public Diff(RingoSocket ringosocket){
		super("DIFF####",false,ringosocket);
		super.initThread(new MyRunnableReceve(this), new MyRunnableSend(this));
	}

	public void doReceve(Message msg) throws DOWNmessageException {
		byte[] msgInByte =msg.getData_app();
		int taille = Integer.parseInt(new String(msgInByte, 0, byteSizeMess));
		String message = new String(msgInByte, 4, taille);
		if(super.listOutput!=null){
			synchronized (listOutput) {
				listOutput.add(msgInByte);
				listOutput.notify();
			}
		}else{
			System.out.println(style + "\n" + LocalDateTime.now() + " -> " + "RECEVE :" + message + "\n" + style);
		}
		super.ringoSocket.send(msg);// renvoi sur l'anneau du message
	}

	public void doSend() throws NumberOfBytesException, DOWNmessageException, InterruptedException {
		String contenu = Message.longToStringRepresentation(super.input.length(), 3) + " " + input;
		Message msg=Message.APPL(ringoSocket.getUniqueIdm(), "DIFF####", contenu.getBytes());
		super.ringoSocket.send(msg);
	}

	public static void main(String[] args) {
		boolean verbose = Appl.testArgs(args);
		try {
			new Diff(Integer.parseInt(args[0]), Integer.parseInt(args[1]), verbose);
		} catch (BindException e) {
			System.out.println("The ports are already in use");
		} catch (IOException | IpException e) {
			e.printStackTrace();//TODO
		}
	}
}