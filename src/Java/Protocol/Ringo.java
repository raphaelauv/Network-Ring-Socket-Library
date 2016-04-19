package Protocol;
import Protocol.Exceptions.*;
import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Cette interface defini les actions realisable par une applicationa avec cette implementation reseaux
 * 
 */
public interface Ringo {

	public final static int maxSizeMsg = 512;
	public final static int byteSizeType = 4;
	public final static int byteSizeId = 8;
	public final static int byteSizeIdm = 8;
	public final static int byteSizeIdApp = 8;
	public final static int byteSizeIP = 15;
	public final static int byteSizePort = 4;
	public final static int byteSizeTypeMSG = 4;
	public final static int byteSizeSpace = 1;
	
	/**
	 * Tester si l'entiter RINGO est fermer
	 * @return
	 */
	public boolean isClose();
	
	
	/**
	 * 	Demande un test de l'anneau
	 * @param sendDownIfBreak true -> si l'anneau est cassee alors averti sur multi diffusion | else -> pas d'alert
	 * @return true -> anneau pas casse
	 * @throws InterruptedException
	 * @throws DOWNmessageException
	 */
	public boolean test(boolean sendDownIfBreak) throws InterruptedException, DOWNmessageException;
	
	/**
	 * demande la deconnection de l'entiter ,elle boucle sur elle meme
	 * @throws InterruptedException
	 * @throws DOWNmessageException
	 */
	public void disconnect() throws InterruptedException, DOWNmessageException;
	
	
	/**
	 * Permet de quitter un anneau
	 * @throws DOWNmessageException 
	 * @throws IOException 
	 */
	public void close() throws InterruptedException, DOWNmessageException, IOException;
	
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
	public void connectTo(String adresse ,int idTCP) throws AlreadyAllUdpPortSet, UnknownHostException, IOException, DOWNmessageException, ProtocolException ,InterruptedException;

	/**
	 * Pour demander l'envoi d'un message a l'entite id
	 * @param message
	 * @param id
	 * @throws SizeException
	 */
	public void send(Message msg) throws DOWNmessageException, SizeMessageException;

	/**
	 * Demande un message en attente de lecture par l'apply
	 * attente passive
	 * @return Contenu du message
	 * @throws InterruptedException 
	 */
	public Message receive() throws DOWNmessageException, InterruptedException;
	
	/**
	 * Get an unique IDM message of the RINGO entiter
	 * @return
	 * @throws DOWNmessageException 
	 * @throws InterruptedException 
	 */
	public long getUniqueIdm() throws DOWNmessageException, InterruptedException;
}
