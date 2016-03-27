import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Cette interface defini les actions realisable par une applicationa avec son implementation reseaux
 * 
 */
public interface Ringo {

	public static int maxSizeMsg=512;
	
	
	/**
	 * 	Demande un test de l'anneau
	 * @param sendDownIfBreak true -> si anneau casse alors averti sur multi diffusion | else -> pas d'alert
	 * @return true -> anneau pas casse
	 * @throws InterruptedException
	 * @throws DOWNmessageException
	 */
	public boolean test(boolean sendDownIfBreak) throws InterruptedException, DOWNmessageException;
	
	/**
	 * Permet de quitter un anneau
	 * @throws DOWNmessageException 
	 */
	public void close() throws InterruptedException, DOWNmessageException;
	
	/**
	 * Pour s'inserer dans un anneau
	 * @param adresse
	 * @param udp
	 * @throws AlreadyAllUdpPortSet
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws DOWNmessageException 
	 */
	public void connectTo(String adresse ,int idTCP) throws AlreadyAllUdpPortSet, UnknownHostException, IOException, DOWNmessageException;

	/**
	 * Pour demander l'envoi d'un message a l'entité id
	 * @param message
	 * @param id
	 * @throws SizeException
	 */
	public void send(byte[] paquet) throws DOWNmessageException, SizeMessageException;

	/**
	 * Demande un message en attente de lecture par l'apply
	 * @return Contenu du message
	 */
	public void receive(byte[] paquet) throws DOWNmessageException;
		
	
}
