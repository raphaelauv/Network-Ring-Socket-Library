import java.io.IOException;
import java.net.BindException;
import application.*;
import protocol.RingoSocket;
import protocol.exceptions.IpException;
import protocol.exceptions.ParseMessageException;

public class Entity extends Appl{
	
	/**
	 * Application
	 */
	public Entity(String ip,Integer udpPort, Integer tcpPort, boolean verbose) throws BindException, IOException, ParseMessageException{
		super(ip,null,udpPort,tcpPort,verbose);
		while(super.runContinue){
			testEntry();
		}
	}

	/**
	 * Service
	 */
	public Entity(RingoSocket ringosocket){
		super(null, ringosocket);
	}
	
	
	/**
	 * pour lancer une entite ringoSocket
	 * @param args
	 */
	public static void main(String[] args) {
		boolean verbose=Appl.testArgs(args);
		try {
			String ip = Appl.selectIp();
			new Entity(ip,Integer.parseInt(args[0]), Integer.parseInt(args[1]),verbose);
			
		} catch (BindException | ParseMessageException e) {
			System.out.println("The ports are already in use or are bigger than 4digit");
		} catch (IOException |NumberFormatException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}