import java.io.IOException;
import java.net.BindException;
import application.core.Appl;
import protocol.RingoSocket;
import protocol.exceptions.ParseException;

public class Entity extends Appl {

	/**
	 * Application
	 */
	public Entity(String ip, Integer udpPort, Integer tcpPort,Integer multiPort, boolean verbose)throws BindException, IOException, ParseException {
		super(ip, null, udpPort, tcpPort, multiPort, verbose);
		while (super.runContinue) {
			testEntry();
		}
	}

	/**
	 * Service
	 */
	public Entity(RingoSocket ringosocket) {
		super(null, ringosocket);
	}

	/**
	 * pour lancer une entite ringoSocket
	 */
	public static void main(String[] args) {
		boolean verbose = Appl.testArgs(args);
		try {

			new Entity(Appl.selectIp(), Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]), verbose);

		} catch (BindException | ParseException e) {
			System.out.println("The ports are already in use or are bigger than 4digit");
		} catch (IOException e) {
			// e.printStackTrace();
		}
	}
}