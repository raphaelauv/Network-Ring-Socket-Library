const int maxSizeMsg = 512;
const int byteSizeType = 4;
const int byteSizeId = 8;
const int byteSizeIdm = 8;
const int byteSizeIdApp = 8;
const int byteSizeIP = 15;
const int byteSizePort = 4;
const int byteSizeTypeMSG = 4;
const int byteSizeSpace = 1;
const int maximumWaitTimeMessage=5000;

/**
 * Tester si l'entiter RINGO est fermer
 * @return
 */
boolean isClose();


/**
 * 	Demande un test de l'anneau
 * @param sendDownIfBreak true -> si l'anneau est cassee alors averti sur multi diffusion | else -> pas d'alert
 * @return true -> anneau pas casse
 * @throws InterruptedException
 * @throws DOWNmessageException
 */
boolean test(boolean sendDownIfBreak);

/**
 * demande la deconnection de l'entiter ,elle boucle sur elle meme
 * @throws InterruptedException
 * @throws DOWNmessageException
 */
void disconnect();


/**
 * Permet de quitter un anneau
 * @throws DOWNmessageException 
 * @throws IOException 
 */
void close();

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
void connectTo(String adresse ,int idTCP);

/**
 * Pour demander l'envoi d'un message a l'entite id
 * @param message
 * @param id
 * @throws SizeException
 */
void send(Message msg);

/**
 * Demande un message en attente de lecture par l'apply
 * attente passive
 * @return Contenu du message
 * @throws InterruptedException 
 */
Message receive();

/**
 * Get an unique IDM message of the RINGO entiter
 * @return
 * @throws DOWNmessageException 
 * @throws InterruptedException 
 */
long getUniqueIdm();