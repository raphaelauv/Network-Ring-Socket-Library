import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Cette interface defini les actions realisable par une applicationa avec cette implementation reseaux
 * 
 */
public interface Ringo {

	public final static int maxSizeMsg = 512;
	public final static int octalSizeIdm = 8;
	public final static int octalSizeIdApp = 8;
	public final static int octalSizeIP = 15;
	public final static int octalSizePort = 4;
	public final static int octalSizeTypeMSG = 4;
	public final static int octalSizeMessSize = 3;
	public final static int octalSizeNom = 2;
	/**
	 * 	Demande un test de l'anneau
	 * @param sendDownIfBreak true -> si l'anneau est cassé alors averti sur multi diffusion | else -> pas d'alert
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
	 * @throws ProtocolException 
	 */
	public void connectTo(String adresse ,int idTCP) throws AlreadyAllUdpPortSet, UnknownHostException, IOException, DOWNmessageException, ProtocolException;

	/**
	 * Pour demander l'envoi d'un message a l'entité id
	 * @param message
	 * @param id
	 * @throws SizeException
	 */
	public void send(Message msg) throws DOWNmessageException, SizeMessageException;

	/**
	 * Demande un message en attente de lecture par l'apply
	 * attente passive
	 * @return Contenu du message
	 */
	public void receive(Message msg) throws DOWNmessageException;
	
}
