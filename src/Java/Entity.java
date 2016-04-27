import java.io.IOException;
import java.net.BindException;
import application.*;
import protocol.RingoSocket;
import protocol.exceptions.IpException;

public class Entity extends Appl{
	
	/**
	 * Application
	 */
	public Entity(String ip,Integer udpPort, Integer tcpPort, boolean verbose) throws BindException, IOException, IpException{
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
			
		} catch (BindException e) {
			System.out.println("The ports are already in use");
		} catch (IOException |NumberFormatException | IpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}