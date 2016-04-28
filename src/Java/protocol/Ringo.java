package protocol;
import protocol.exceptions.*;

import java.io.Closeable;
import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Cette interface defini les actions realisable par une applicationa avec cette implementation reseaux
 * 
 */
public interface Ringo extends Closeable{

	public final static int maxSizeMsg = 512;
	public final static int byteSizeType = 4;
	public final static int byteSizeId = 8;
	public final static int byteSizeIdm = 8;
	public final static int byteSizeIdApp = 8;
	public final static int byteSizeIP = 15;
	public final static int byteSizePort = 4;
	public final static int byteSizeTypeMSG = 4;
	public final static int byteSizeSpace = 1;
	
	public final static int maximumWaitTimeMessage=5000;
	
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
	 * @throws RingoSocketCloseException
	 * @throws ParseException 
	 */
	public boolean test(boolean sendDownIfBreak) throws InterruptedException, RingoSocketCloseException, ParseException;
	
	/**
	 * demande la deconnection de l'entiter ,elle boucle sur elle meme
	 * @throws InterruptedException
	 * @throws RingoSocketCloseException
	 * @throws ParseException 
	 */
	public void disconnect() throws InterruptedException, RingoSocketCloseException, ParseException;
	
	
	/**
	 * Permet de quitter un anneau
	 * @throws IOException 
	 */
	public void close() throws IOException;
	
	/**
	 * Pour s'inserer dans un anneau
	 * @param adresse
	 * @param idTCP
	 * @param modeDUPL
	 * @throws AlreadyAllUdpPortSet
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws RingoSocketCloseException
	 * @throws ProtocolException
	 * @throws InterruptedException
	 * @throws AlreadyConnectException
	 * @throws ImpossibleDUPLConnection
	 * @throws UnknownTypeMesssage
	 * @throws ParseException
	 */
	public void connectTo(String adresse, int idTCP,boolean modeDUPL)
			throws AlreadyAllUdpPortSet, UnknownHostException, IOException, RingoSocketCloseException, 
			ProtocolException, InterruptedException, AlreadyConnectException ,ImpossibleDUPLConnection, 
			UnknownTypeMesssage, ParseException;

	/**
	 * Pour demander l'envoi d'un message a l'entite id
	 * @param message
	 * @param id
	 * @throws SizeException
	 */
	public void send(Message msg) throws RingoSocketCloseException, SizeMessageException;

	/**
	 * Demande un message en attente de lecture par l'apply
	 * attente passive
	 * @return Contenu du message
	 * @throws InterruptedException 
	 */
	public Message receive() throws RingoSocketCloseException, InterruptedException;
	
	/**
	 * Envoyer un down sur multidiffusion
	 * @throws RingoSocketCloseException
	 */
	public void down() throws RingoSocketCloseException;
	
	/**
	 * Get an unique IDM message of the RINGO entiter
	 * @return
	 * @throws RingoSocketCloseException 
	 * @throws InterruptedException 
	 */
	public long getUniqueIdm() throws RingoSocketCloseException, InterruptedException;
}
