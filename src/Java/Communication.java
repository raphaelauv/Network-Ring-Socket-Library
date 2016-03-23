import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Cette interface defini les actions realisable par une applicationa avec son implementation reseaux
 * 
 *
 */
public interface Communication {

	/**
	 * Demande une test de l'anneau
	 * @throws InterruptedException
	 */
	public void test() throws InterruptedException;
	
	/**
	 * Permet de quitter un anneau
	 */
	public void quitter();
	
	/**
	 * Pour s'inserer dans un anneau
	 * @param adresse
	 * @param udp
	 * @throws AlreadyAllUdpPortSet
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void connectTo(String adresse ,int udp) throws AlreadyAllUdpPortSet, UnknownHostException, IOException;

	/**
	 * Pour demander l'envoi d'un message a l'entité id
	 * @param message
	 * @param id
	 * @throws SizeException
	 */
	public void envoyer(String message , int id) throws SizeException;

	/**
	 * Demande un message en attente de lecture par l'apply
	 * @return Contenu du message
	 */
	public String lire();
		
	
}
