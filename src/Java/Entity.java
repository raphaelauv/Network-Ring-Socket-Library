import java.io.IOException;
import java.net.BindException;
import application.*;
import protocol.exceptions.IpException;

public class Entity{
	/**
	 * pour lancer une simple entite ringoSocket , denouer de toute interaction utilisateur 
	 * @param args
	 */
	public static void main(String[] args) {
		boolean verbose=Appl.testArgs(args);
		try {
			new Appl(null,Integer.parseInt(args[0]), Integer.parseInt(args[1]),true,verbose);
			
		} catch (BindException e) {
			System.out.println("The ports are already in use");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}