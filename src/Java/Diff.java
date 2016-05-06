import java.io.IOException;
import java.net.BindException;
import java.time.LocalDateTime;
import protocol.*;
import protocol.exceptions.*;
import application.*;

public class Diff extends Appl implements ReceveSend {

	public final static int byteSizeMess = 3;

	/**
	 * Application 
	 */
	public Diff(String ip,Integer udpPort, Integer tcpPort,Integer multiPort, boolean verbose) throws BindException, IOException, ParseException {
		super(ip,"DIFF####", udpPort, tcpPort, multiPort,verbose);	
		super.initThread(new MyRunnableReceve(this), new MyRunnableSend(this));
	}
	
	/**
	 * Service
	 */
	public Diff(RingoSocket ringosocket){
		super("DIFF####",ringosocket);
		super.initThread(new MyRunnableReceve(this), new MyRunnableSend(this));
	}

	public void doReceve(Message msg) throws RingoSocketCloseException {
		byte[] msgInByte =msg.getData_app();
		int taille = Integer.parseInt(new String(msgInByte, 0, byteSizeMess));
		String message = new String(msgInByte, 4, taille);
		if(super.listOutput!=null){
			synchronized (listOutput) {
				listOutput.add(message.getBytes());
				listOutput.notify();
			}
		}else{
			printModeApplication(style + "\n" + LocalDateTime.now() + " -> " + "RECEVE :" + message + "\n" + style);
		}
		super.ringoSocket.send(msg);// renvoi sur l'anneau du message
	}

	public void doSend() throws NumberOfBytesException, RingoSocketCloseException, InterruptedException, ParseException {
		String contenu = Message.longToStringRepresentation(super.input.length(), 3) + " " + input;
		Message msg=Message.APPL(ringoSocket.getUniqueIdm(), "DIFF####", contenu.getBytes());
		super.ringoSocket.send(msg);
	}

	public static void main(String[] args) {
		boolean verbose = Appl.testArgs(args);
		try {
			String ip = Appl.selectIp();
			new Diff(ip,Integer.parseInt(args[0]), Integer.parseInt(args[1]),Integer.parseInt(args[2]) , verbose);
		} catch (BindException | ParseException e) {
			System.out.println("The ports are already in use or are bigger than 4digit");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}