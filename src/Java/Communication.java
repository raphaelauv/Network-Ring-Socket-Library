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
	 * @throws DOWNmessageException 
	 */
	public void test() throws InterruptedException, DOWNmessageException;
	
	/**
	 * Permet de quitter un anneau
	 * @throws DOWNmessageException 
	 */
	public void quitter() throws InterruptedException, DOWNmessageException;
	
	/**
	 * Pour s'inserer dans un anneau
	 * @param adresse
	 * @param udp
	 * @throws AlreadyAllUdpPortSet
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws DOWNmessageException 
	 */
	public void connectTo(String adresse ,int udp) throws AlreadyAllUdpPortSet, UnknownHostException, IOException, DOWNmessageException;

	/**
	 * Pour demander l'envoi d'un message a l'entité id
	 * @param message
	 * @param id
	 * @throws SizeException
	 */
	public void envoyer(String message , int id) throws DOWNmessageException, SizeException;

	/**
	 * Demande un message en attente de lecture par l'apply
	 * @return Contenu du message
	 */
	public String lire() throws DOWNmessageException;
		
	
}
